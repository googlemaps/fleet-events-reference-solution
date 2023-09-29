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
import com.google.fleetevents.common.util.GeoUtil;
import com.google.fleetevents.common.util.NameFormatter;
import com.google.fleetevents.common.util.ProtoParser;
import com.google.fleetevents.common.util.TimeUtil;
import com.google.fleetevents.odrd.models.TripData;
import com.google.fleetevents.odrd.models.TripFleetEvent;
import com.google.fleetevents.odrd.models.TripWaypointData;
import com.google.fleetevents.odrd.models.TripWaypointType;
import com.google.fleetevents.odrd.models.VehicleData;
import com.google.fleetevents.odrd.models.VehicleFleetEvent;
import com.google.logging.v2.LogEntry;
import com.google.protobuf.FieldMask;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.type.LatLng;
import google.maps.fleetengine.v1.TripStatus;
import google.maps.fleetengine.v1.UpdateVehicleRequest;
import google.maps.fleetengine.v1.Vehicle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

/**
 * Creates transaction to update Firestore about an existing vehicle. Generates a VehicleFleetEvent
 * for the new vehicle update as well as TripFleetEvents and TripWaypointFleetEvents for any trips
 * the vehicle is currently executing.
 */
public class UpdateVehicleTransaction implements Transaction.Function<List<OutputEvent>> {

  private static final Logger logger = Logger.getLogger(UpdateVehicleTransaction.class.getName());
  private final List<FleetEventHandler> fleetEventHandlers;
  private final FirestoreDatabaseClient firestoreDatabaseClient;
  private final String vehicleId;
  private final Vehicle newVehicle;
  private final UpdateVehicleRequest request;
  private final LogEntry logEntry;

  public UpdateVehicleTransaction(
      LogEntry logEntry,
      List<FleetEventHandler> fleetEventHandlers,
      FirestoreDatabaseClient firestoreDatabaseClient)
      throws InvalidProtocolBufferException {
    this.fleetEventHandlers = fleetEventHandlers;
    this.firestoreDatabaseClient = firestoreDatabaseClient;
    UpdateVehicleRequest request =
        ProtoParser.parseLogEntryRequest(logEntry, UpdateVehicleRequest.getDefaultInstance());
    Vehicle response = ProtoParser.parseLogEntryResponse(logEntry, Vehicle.getDefaultInstance());
    this.vehicleId = NameFormatter.getIdFromName(response.getName());
    this.logEntry = logEntry;
    this.request = request;
    this.newVehicle = response;
  }

  static UpdateAndDifference<VehicleData> getVehicleAndDiff(
      com.google.protobuf.Timestamp eventTimestamp,
      FieldMask updateMask,
      Vehicle newVehicle,
      VehicleData oldVehicleData) {
    var updatedFields = new HashSet<>(updateMask.getPathsList());
    Map<String, Change> vehicleDifferences = new HashMap<>();
    var newVehicleBuilder =
        oldVehicleData.toBuilder()
            .setName(newVehicle.getName())
            .setEventTimestamp(
                Timestamp.ofTimeSecondsAndNanos(
                    eventTimestamp.getSeconds(), eventTimestamp.getNanos()))
            .setExpireAt(TimeUtil.offsetFromNow(TimeUtil.ONE_HOUR_IN_SECONDS));

    if (updatedFields.contains("last_location")
        || !Objects.equals(
            GeoUtil.latLngToGeoPoint(newVehicle.getLastLocation().getLocation()),
            oldVehicleData.getLastLocation())) {
      LatLng lastLocation = newVehicle.getLastLocation().getLocation();
      newVehicleBuilder.setLastLocation(GeoUtil.latLngToGeoPoint(lastLocation));
      vehicleDifferences.put(
          "lastLocation", new Change<>(oldVehicleData.getLastLocation(), lastLocation));
      updatedFields.remove("last_location");
    }

    if (updatedFields.contains("navigation_status")
        || !Objects.equals(
            oldVehicleData.getNavigationStatus(), newVehicle.getNavigationStatus().name())) {
      String status = newVehicle.getNavigationStatus().name();
      newVehicleBuilder.setNavigationStatus(status);
      vehicleDifferences.put(
          "navigationStatus", new Change<>(oldVehicleData.getNavigationStatus(), status));
      updatedFields.remove("navigation_status");
    }

    if (updatedFields.contains("state")
        || !Objects.equals(oldVehicleData.getState(), newVehicle.getVehicleState().name())) {
      String vehicleState = newVehicle.getVehicleState().name();
      newVehicleBuilder.setState(vehicleState);
      vehicleDifferences.put("state", new Change<>(oldVehicleData.getState(), vehicleState));
      updatedFields.remove("state");
    }

    if (updatedFields.contains("current_trips")
        || !Objects.equals(oldVehicleData.getTripIds(), newVehicle.getCurrentTripsList())) {
      List<String> tripIds = newVehicle.getCurrentTripsList();
      newVehicleBuilder.setTripIds(tripIds);
      vehicleDifferences.put("tripIds", new Change<>(oldVehicleData.getTripIds(), tripIds));
      updatedFields.remove("current_trips");
    }

    if (updatedFields.contains("remaining_distance_meters")
        || !Objects.equals(
            oldVehicleData.getRemainingDistanceMeters(),
            (long) newVehicle.getRemainingDistanceMeters().getValue())) {
      Long remainingDistanceMeters = (long) newVehicle.getRemainingDistanceMeters().getValue();
      newVehicleBuilder.setRemainingDistanceMeters(remainingDistanceMeters);
      vehicleDifferences.put(
          "remainingDistanceMeters",
          new Change<>(oldVehicleData.getRemainingDistanceMeters(), remainingDistanceMeters));
      updatedFields.remove("remaining_distance_meters");
    }

    if (updatedFields.contains("eta_to_first_waypoint")
        || !Objects.equals(
            oldVehicleData.getEta(),
            Timestamp.ofTimeSecondsAndNanos(
                newVehicle.getEtaToFirstWaypoint().getSeconds(),
                newVehicle.getEtaToFirstWaypoint().getNanos()))) {
      Timestamp eta =
          Timestamp.ofTimeSecondsAndNanos(
              newVehicle.getEtaToFirstWaypoint().getSeconds(),
              newVehicle.getEtaToFirstWaypoint().getNanos());
      newVehicleBuilder.setEta(eta);
      // Since remaining_time_in_seconds isn't populated use the eta to get remaining duration
      var newRemainingDuration =
          TimeUtil.protobufToLong(
                  com.google.protobuf.Timestamp.newBuilder()
                      .setSeconds(eta.getSeconds())
                      .setNanos(eta.getNanos())
                      .build())
              - TimeUtil.protobufToLong(eventTimestamp);
      if (vehicleDifferences.containsKey("remainingDistanceMeters")
          && (long) vehicleDifferences.get("remainingDistanceMeters").newValue == 0) {
        newRemainingDuration = 0;
      }
      newVehicleBuilder.setRemainingDuration(newRemainingDuration);
      vehicleDifferences.put(
          "remainingDuration",
          new Change<>(oldVehicleData.getRemainingDuration(), newRemainingDuration));
      vehicleDifferences.put("eta", new Change<>(oldVehicleData.getEta(), eta));
      updatedFields.remove("eta_to_first_waypoint");
    }

    return new UpdateAndDifference<>(newVehicleBuilder.build(), vehicleDifferences);
  }

  @Override
  public List<OutputEvent> updateCallback(Transaction transaction) throws Exception {
    VehicleData oldVehicleData =
        transaction
            .get(firestoreDatabaseClient.getVehicleDocument(vehicleId))
            .get()
            .toObject(VehicleData.class);
    if (oldVehicleData == null) {
      oldVehicleData =
          VehicleData.builder().setVehicleId(this.vehicleId).setName(newVehicle.getName()).build();
    }
    var vehicleAndDifference =
        getVehicleAndDiff(
            logEntry.getTimestamp(), request.getUpdateMask(), this.newVehicle, oldVehicleData);
    var updateVehicleFleetEvent =
        VehicleFleetEvent.builder()
            .setVehicleId(vehicleId)
            .setOldVehicle(oldVehicleData)
            .setNewVehicle(vehicleAndDifference.updated)
            .setVehicleDifferences(vehicleAndDifference.differences)
            .build();
    var vehicleDocRef = firestoreDatabaseClient.getVehicleDocument(vehicleId);
    var fleetEvents = ImmutableList.<FleetEvent>builder();
    var tripUpdates = new ArrayList<TripData>();
    var tripWaypointUpdates = new ArrayList<TripWaypointData>();
    fleetEvents.add(updateVehicleFleetEvent);
    if (vehicleAndDifference.differences.containsKey("eta")
        || vehicleAndDifference.differences.containsKey("remainingDistanceMeters")
        || vehicleAndDifference.differences.containsKey("remainingDuration")) {
      fleetEvents.addAll(
          updateTripAndWaypoints(
              transaction, tripUpdates, tripWaypointUpdates, vehicleAndDifference));
    }

    var outputEvents =
        FleetEventCreatorBase.callFleetEventHandlers(
            fleetEvents.build(), fleetEventHandlers, transaction, firestoreDatabaseClient);
    transaction.set(vehicleDocRef, vehicleAndDifference.updated);
    for (var tripUpdate : tripUpdates) {
      transaction.set(firestoreDatabaseClient.getTripDocument(tripUpdate.getTripId()), tripUpdate);
    }
    return outputEvents;
  }

  List<FleetEvent> updateTripAndWaypoints(
      Transaction transaction,
      List<TripData> tripUpdates,
      List<TripWaypointData> tripWaypointUpdates,
      UpdateAndDifference<VehicleData> vehicleAndDifference)
      throws ExecutionException, InterruptedException {
    HashMap<String, List<TripWaypointData>> tripIdToWaypoints = new HashMap<>();
    long cumulativeDistanceMeters = 0;
    for (var i = 0; i < newVehicle.getWaypointsList().size(); i++) {
      var tripWaypoint = newVehicle.getWaypointsList().get(i);
      cumulativeDistanceMeters +=
          i == 0
              ? vehicleAndDifference.updated.getRemainingDistanceMeters()
              : tripWaypoint.getDistanceMeters().getValue();
      // Duration does not appear to ever be populated in the trip waypoints only eta and distance
      // meters, so calculating duration based off of eta and log entry timestamp.
      var remainingDurationMilliSeconds =
          TimeUtil.protobufToLong(tripWaypoint.getEta())
              - TimeUtil.protobufToLong(logEntry.getTimestamp());

      var tripWaypointDataUpdate =
          TripWaypointData.builder()
              .setTripWaypointType(
                  TripWaypointType.values()[tripWaypoint.getWaypointType().getNumber()])
              .setRemainingDistanceMeters(cumulativeDistanceMeters)
              .setRemainingDuration(remainingDurationMilliSeconds)
              .setEta(
                  Timestamp.ofTimeSecondsAndNanos(
                      tripWaypoint.getEta().getSeconds(), tripWaypoint.getEta().getNanos()))
              .build();
      var waypoints = tripIdToWaypoints.getOrDefault(tripWaypoint.getTripId(), new ArrayList<>());
      waypoints.add(tripWaypointDataUpdate);
      tripIdToWaypoints.put(tripWaypoint.getTripId(), waypoints);
    }
    var fleetEvents = ImmutableList.<FleetEvent>builder();

    for (var tripId : tripIdToWaypoints.keySet()) {
      fleetEvents.addAll(
          generateFleetEventsForTripAndWaypoints(
              transaction,
              tripUpdates,
              vehicleId,
              tripId,
              tripIdToWaypoints.get(tripId),
              vehicleAndDifference.updated.getEventTimestamp()));
    }
    return fleetEvents.build();
  }

  List<FleetEvent> generateFleetEventsForTripAndWaypoints(
      Transaction transaction,
      List<TripData> tripUpdates,
      String vehicleId,
      String tripId,
      List<TripWaypointData> updates,
      Timestamp eventTimestamp)
      throws ExecutionException, InterruptedException {
    var fleetEvents = ImmutableList.<FleetEvent>builder();
    var oldTripData =
        transaction
            .get(firestoreDatabaseClient.getTripDocument(tripId))
            .get()
            .toObject(TripData.class);
    if (oldTripData == null) {
      oldTripData = TripData.builder().build();
    }
    var tripFleetEventBuilder =
        TripFleetEvent.builder()
            .setVehicleId(vehicleId)
            .setTripId(tripId)
            .setOldTripWaypoints(oldTripData.getWaypoints())
            .setOldTrip(oldTripData);
    int numberOfWaypoints = oldTripData.getWaypoints().size();
    /* If the number of waypoints is less than the update size just use the update size for the
     * number of waypoints and change the waypoints flag to recognize this. */
    if (numberOfWaypoints < updates.size()) {
      numberOfWaypoints = updates.size();
      tripFleetEventBuilder.setWaypointsChanged(true);
    }
    var newTripWaypoints = new ArrayList<TripWaypointData>();
    var tripWaypointDifferences = new ArrayList<Map<String, Change>>();
    for (var i = 0; i < numberOfWaypoints - updates.size(); i++) {
      /* Add in the waypoints that aren't updated so the trip maintains a record of its completed
       * waypoints. */
      newTripWaypoints.add(oldTripData.getWaypoints().get(i));
      tripWaypointDifferences.add(new HashMap<>());
    }
    for (int i = 0; i < updates.size(); i++) {
      long waypointIndex = numberOfWaypoints - updates.size() + i;
      var tripWaypoint = updates.get(i);
      var tripWaypointId =
          TripWaypointData.generateWaypointId(
              tripId, waypointIndex, tripWaypoint.getTripWaypointType());
      /* If the old waypoint for this index doesn't exist create a new one. */
      var oldTripWaypointData =
          waypointIndex < oldTripData.getWaypoints().size()
              ? oldTripData.getWaypoints().get((int) waypointIndex)
              : null;
      if (oldTripWaypointData == null) {
        oldTripWaypointData =
            TripWaypointData.builder()
                .setVehicleId(vehicleId)
                .setTripId(tripId)
                .setWaypointId(tripWaypointId)
                .build();
      }
      var differences = new HashMap<String, Change>();
      differences.put(
          "remainingDistanceMeters",
          new Change<>(
              oldTripWaypointData.getRemainingDistanceMeters(),
              tripWaypoint.getRemainingDistanceMeters()));
      differences.put(
          "remainingDuration",
          new Change<>(
              oldTripWaypointData.getRemainingDuration(), tripWaypoint.getRemainingDuration()));
      differences.put("eta", new Change<>(oldTripWaypointData.getEta(), tripWaypoint.getEta()));
      var newTripWaypointData =
          oldTripWaypointData.toBuilder()
              .setVehicleId(vehicleId)
              .setTripId(tripId)
              .setWaypointId(tripWaypointId)
              .setRemainingDistanceMeters(tripWaypoint.getRemainingDistanceMeters())
              .setRemainingDuration(tripWaypoint.getRemainingDuration())
              .setEta(tripWaypoint.getEta())
              .setTripWaypointType(tripWaypoint.getTripWaypointType())
              .setWaypointIndex(waypointIndex)
              .setIsTerminal(
                  tripWaypoint.getTripWaypointType() == TripWaypointType.DROP_OFF_WAYPOINT_TYPE)
              .build();
      newTripWaypoints.add(newTripWaypointData);
      tripWaypointDifferences.add(differences);
      // Updates for dropoff points are also updates for the entire trip itself.
      if (tripWaypoint.getTripWaypointType() == TripWaypointType.DROP_OFF_WAYPOINT_TYPE) {
        var currentWaypointIndex = numberOfWaypoints - updates.size();
        var potentiallyUpdatedTripStatus =
            tryUpdateTripStatus(
                newTripWaypoints.get(currentWaypointIndex).getTripWaypointType(), oldTripData);
        var newTripDataBuilder =
            oldTripData.toBuilder()
                .setVehicleId(vehicleId)
                .setTripId(tripId)
                .setRemainingDistanceMeters(tripWaypoint.getRemainingDistanceMeters())
                .setRemainingDuration(tripWaypoint.getRemainingDuration())
                .setEta(tripWaypoint.getEta())
                .setExpireAt(TimeUtil.offsetFromNow(TimeUtil.ONE_HOUR_IN_SECONDS))
                .setCurrentWaypointIndex((long) currentWaypointIndex)
                .setWaypoints(newTripWaypoints)
                .setEventTimestamp(eventTimestamp);
        var tripDifferences = new HashMap<>(differences);
        if (potentiallyUpdatedTripStatus.isPresent()) {
          newTripDataBuilder.setTripStatus(potentiallyUpdatedTripStatus.get().name());
          tripDifferences.put(
              "tripStatus",
              new Change<>(oldTripData.getTripStatus(), potentiallyUpdatedTripStatus.get().name()));
        }
        var newTripData = newTripDataBuilder.build();
        tripFleetEventBuilder
            .setNewTrip(newTripData)
            .setTripDifferences(tripDifferences)
            .setNewTripWaypoints(newTripWaypoints)
            .setTripWaypointDifferences(tripWaypointDifferences);
        tripUpdates.add(newTripData);
      }
    }
    fleetEvents.add(tripFleetEventBuilder.build());
    logger.info(tripFleetEventBuilder.build().toString());
    return fleetEvents.build();
  }

  public Optional<TripStatus> tryUpdateTripStatus(
      TripWaypointType currentTripWaypointType, TripData oldTripData) {
    if (oldTripData == null) {
      return Optional.empty();
    }
    var oldTripStatus =
        oldTripData.getTripStatus() != null
            ? TripStatus.valueOf(oldTripData.getTripStatus())
            : TripStatus.UNKNOWN_TRIP_STATUS;
    var minimumStatusFromCurrentType = oldTripStatus;
    switch (currentTripWaypointType) {
      case PICKUP_WAYPOINT_TYPE -> minimumStatusFromCurrentType = TripStatus.ENROUTE_TO_PICKUP;

      case DROP_OFF_WAYPOINT_TYPE -> minimumStatusFromCurrentType = TripStatus.ENROUTE_TO_DROPOFF;

      case INTERMEDIATE_WAYPOINT_TYPE -> minimumStatusFromCurrentType =
          TripStatus.ENROUTE_TO_INTERMEDIATE_DESTINATION;
    }
    if (((oldTripStatus == TripStatus.NEW || oldTripStatus == TripStatus.UNKNOWN_TRIP_STATUS)
            && currentTripWaypointType != TripWaypointType.UNKNOWN_WAYPOINT_TYPE)
        || (oldTripStatus == TripStatus.ARRIVED_AT_PICKUP
            && currentTripWaypointType != TripWaypointType.UNKNOWN_WAYPOINT_TYPE
            && currentTripWaypointType != TripWaypointType.PICKUP_WAYPOINT_TYPE)
        || (oldTripStatus == TripStatus.ARRIVED_AT_INTERMEDIATE_DESTINATION
            && currentTripWaypointType != TripWaypointType.INTERMEDIATE_WAYPOINT_TYPE)
        || (oldTripStatus != TripStatus.ENROUTE_TO_DROPOFF
            && currentTripWaypointType == TripWaypointType.DROP_OFF_WAYPOINT_TYPE)) {
      return Optional.of(minimumStatusFromCurrentType);
    }
    return Optional.empty();
  }
}
