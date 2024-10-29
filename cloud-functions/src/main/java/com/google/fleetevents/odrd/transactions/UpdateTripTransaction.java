/*
 * Copyright 2023 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.fleetevents.odrd.transactions;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.Transaction;
import com.google.common.collect.ImmutableList;
import com.google.fleetevents.FleetEventCreatorBase;
import com.google.fleetevents.FleetEventHandler;
import com.google.fleetevents.common.database.FirestoreDatabaseClient;
import com.google.fleetevents.common.models.Change;
import com.google.fleetevents.common.models.FleetEvent;
import com.google.fleetevents.common.models.OutputEvent;
import com.google.fleetevents.common.models.UpdateAndDifference;
import com.google.fleetevents.common.util.NameFormatter;
import com.google.fleetevents.common.util.ProtoParser;
import com.google.fleetevents.common.util.TimeUtil;
import com.google.fleetevents.odrd.models.TripData;
import com.google.fleetevents.odrd.models.TripFleetEvent;
import com.google.fleetevents.odrd.models.TripWaypointData;
import com.google.logging.v2.LogEntry;
import com.google.maps.fleetengine.v1.Trip;
import com.google.maps.fleetengine.v1.TripType;
import com.google.maps.fleetengine.v1.UpdateTripRequest;
import com.google.protobuf.FieldMask;
import com.google.protobuf.InvalidProtocolBufferException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;

/** Class for updating trips in firestore based on an update_trip log from the Fleet Engine API. */
public class UpdateTripTransaction implements Transaction.Function<List<OutputEvent>> {
  private static final Logger logger = Logger.getLogger(UpdateVehicleTransaction.class.getName());

  private final LogEntry logEntry;
  private final FirestoreDatabaseClient firestoreDatabaseClient;
  private final Trip trip;
  private final String tripId;
  private final FieldMask updateMask;
  private final List<FleetEventHandler> fleetEventHandlers;

  public UpdateTripTransaction(
      LogEntry logEntry,
      List<FleetEventHandler> fleetEventHandlers,
      FirestoreDatabaseClient firestoreDatabaseClient)
      throws InvalidProtocolBufferException {
    this.logEntry = logEntry;
    this.fleetEventHandlers = fleetEventHandlers;
    this.firestoreDatabaseClient = firestoreDatabaseClient;
    UpdateTripRequest request =
        ProtoParser.parseLogEntryRequest(logEntry, UpdateTripRequest.getDefaultInstance());
    Trip response = ProtoParser.parseLogEntryResponse(logEntry, Trip.getDefaultInstance());
    this.tripId = NameFormatter.getIdFromName(response.getName());
    this.updateMask = request.getUpdateMask();
    this.trip = response;
  }

  UpdateAndDifference<TripData> getDiff(FieldMask updateMask, Trip newTrip, TripData oldTripData) {
    HashMap<String, Change> tripDifferences = new HashMap<>();
    TripData.Builder newTripDataBuilder =
        oldTripData.toBuilder()
            .setEventTimestamp(
                Timestamp.ofTimeSecondsAndNanos(
                    logEntry.getTimestamp().getSeconds(), logEntry.getTimestamp().getNanos()))
            .setExpireAt(TimeUtil.offsetFromNow(TimeUtil.ONE_HOUR_IN_SECONDS));

    var updatedFields = new HashSet<>(updateMask.getPathsList());
    if (updatedFields.contains("vehicle_id")
        || !Objects.equals(oldTripData.getVehicleId(), newTrip.getVehicleId())) {
      newTripDataBuilder.setVehicleId(newTrip.getVehicleId());
      tripDifferences.put(
          "vehicleId", new Change<>(oldTripData.getVehicleId(), newTrip.getVehicleId()));
      updatedFields.remove("vehicle_id");
    }

    if (updatedFields.contains("trip_status")
        || !Objects.equals(oldTripData.getTripStatus(), newTrip.getTripStatus().name())) {
      newTripDataBuilder.setTripStatus(newTrip.getTripStatus().name());
      tripDifferences.put(
          "tripStatus", new Change<>(oldTripData.getTripStatus(), newTrip.getTripStatus().name()));
      updatedFields.remove("trip_status");
    }

    var newIntermediateDestinationsVersion =
        newTrip.hasIntermediateDestinationsVersion()
            ? Timestamp.ofTimeSecondsAndNanos(
                newTrip.getIntermediateDestinationsVersion().getSeconds(),
                newTrip.getIntermediateDestinationsVersion().getNanos())
            : null;

    var waypoints = TripWaypointData.getWaypoints(trip, oldTripData.getWaypoints());
    if (oldTripData.getWaypoints().size() != waypoints.size()) {
      newTripDataBuilder.setWaypoints(waypoints);
      tripDifferences.put("waypoints", new Change<>(oldTripData.getWaypoints(), waypoints));
    }

    /* If intermediate destinations version has changed then the intermediate waypoints have
     * changed. */
    if (updatedFields.contains("intermediate_destinations_version")
        || !Objects.equals(
            oldTripData.getIntermediateDestinationsVersion(), newIntermediateDestinationsVersion)) {
      tripDifferences.put(
          "intermediate_destinations_version",
          new Change<>(
              oldTripData.getIntermediateDestinationsVersion(),
              newIntermediateDestinationsVersion));
      newTripDataBuilder.setIntermediateDestinationsVersion(newIntermediateDestinationsVersion);
      updatedFields.remove("intermediate_destinations_version");
    }

    var currentWaypointIndex = getWaypointIndex(newTrip, (long) waypoints.size());
    if (updatedFields.contains("intermediate_destinations_index")
        || !Objects.equals(oldTripData.getCurrentWaypointIndex(), currentWaypointIndex)) {
      newTripDataBuilder.setCurrentWaypointIndex(currentWaypointIndex);
      tripDifferences.put(
          "currentWaypointIndex",
          new Change<>(oldTripData.getCurrentWaypointIndex(), currentWaypointIndex));
      updatedFields.remove("intermediate_destinations_index");
    }

    if (newTrip.getTripType() == TripType.EXCLUSIVE && oldTripData.getIsSharedTrip()) {
      newTripDataBuilder.setIsSharedTrip(false);
      tripDifferences.put("isSharedTrip", new Change<>(false, true));
      updatedFields.remove("trip_type");
    } else if (newTrip.getTripType() == TripType.SHARED && !oldTripData.getIsSharedTrip()) {
      newTripDataBuilder.setIsSharedTrip(true);
      tripDifferences.put("isSharedTrip", new Change<>(true, false));
      updatedFields.remove("trip_type");
    }

    return new UpdateAndDifference<>(newTripDataBuilder.build(), tripDifferences);
  }

  private Long getWaypointIndex(Trip newTrip, Long numberOfWaypoints) {
    switch (newTrip.getTripStatus()) {
      case ARRIVED_AT_PICKUP, ENROUTE_TO_PICKUP -> {
        return 0L;
      }
      case ARRIVED_AT_INTERMEDIATE_DESTINATION, ENROUTE_TO_INTERMEDIATE_DESTINATION -> {
        return (long) newTrip.getIntermediateDestinationIndex();
      }
      case ENROUTE_TO_DROPOFF -> {
        return numberOfWaypoints - 1;
      }
      default -> {
        /* If the trip status is new, canceled, complete, unrecognized return -1 for the index. */
        return -1L;
      }
    }
  }

  @Override
  public List<OutputEvent> updateCallback(Transaction transaction) throws Exception {
    var oldTripData =
        transaction
            .get(firestoreDatabaseClient.getTripDocument(tripId))
            .get()
            .toObject(TripData.class);
    if (oldTripData == null) {
      oldTripData = TripData.builder().setTripId(tripId).build();
    }
    UpdateAndDifference<TripData> tripAndDifference = getDiff(updateMask, trip, oldTripData);
    var fleetEventsBuilder = ImmutableList.<FleetEvent>builder();
    var tripFleetEvent =
        TripFleetEvent.builder()
            .setVehicleId(trip.getVehicleId())
            .setTripId(tripId)
            .setOldTrip(oldTripData)
            .setNewTrip(tripAndDifference.updated)
            .setTripDifferences(tripAndDifference.differences)
            .setWaypointsChanged(tripAndDifference.differences.containsKey("waypoints"))
            .build();
    fleetEventsBuilder.add(tripFleetEvent);
    var outputEvents =
        FleetEventCreatorBase.callFleetEventHandlers(
            fleetEventsBuilder.build(), fleetEventHandlers, transaction, firestoreDatabaseClient);
    transaction.set(firestoreDatabaseClient.getTripDocument(tripId), tripAndDifference.updated);
    return outputEvents;
  }
}
