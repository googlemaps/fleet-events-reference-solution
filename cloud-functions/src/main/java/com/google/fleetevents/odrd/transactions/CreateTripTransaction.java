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

import static com.google.fleetevents.odrd.models.TripWaypointData.terminalLocationToWaypointData;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Transaction;
import com.google.fleetevents.FleetEventCreatorBase;
import com.google.fleetevents.FleetEventHandler;
import com.google.fleetevents.common.database.FirestoreDatabaseClient;
import com.google.fleetevents.common.models.Change;
import com.google.fleetevents.common.models.FleetEvent;
import com.google.fleetevents.common.models.OutputEvent;
import com.google.fleetevents.common.util.ProtoParser;
import com.google.fleetevents.common.util.TimeUtil;
import com.google.fleetevents.odrd.models.TripData;
import com.google.fleetevents.odrd.models.TripFleetEvent;
import com.google.fleetevents.odrd.models.TripWaypointData;
import com.google.fleetevents.odrd.models.TripWaypointType;
import com.google.logging.v2.LogEntry;
import com.google.protobuf.InvalidProtocolBufferException;
import google.maps.fleetengine.v1.CreateTripRequest;
import google.maps.fleetengine.v1.Trip;
import google.maps.fleetengine.v1.TripType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Transaction class to create a trip entity in Firestore. */
public class CreateTripTransaction implements Transaction.Function<List<OutputEvent>> {
  private final List<FleetEventHandler> fleetEventHandlers;
  private final DocumentReference newTripDocRef;
  private final List<FleetEvent> tripFleetEvents;
  private final FirestoreDatabaseClient firestoreDatabaseClient;
  private final TripData newTripData;

  public CreateTripTransaction(
      LogEntry logEntry,
      List<FleetEventHandler> fleetEventHandlers,
      FirestoreDatabaseClient firestoreDatabaseClient)
      throws InvalidProtocolBufferException {

    this.firestoreDatabaseClient = firestoreDatabaseClient;
    this.tripFleetEvents = new ArrayList<>();
    this.fleetEventHandlers = fleetEventHandlers;

    CreateTripRequest request =
        ProtoParser.parseLogEntryRequest(logEntry, CreateTripRequest.getDefaultInstance());
    Trip response = ProtoParser.parseLogEntryResponse(logEntry, Trip.getDefaultInstance());
    String vehicleId = response.getVehicleId();
    String tripId = request.getTripId();
    String tripStatus = response.getTripStatus().name();
    newTripDocRef = firestoreDatabaseClient.getTripDocument(tripId);

    List<TripWaypointData> waypoints = new ArrayList<>();
    var tripWaypointDifferences = new ArrayList<Map<String, Change>>();
    var pickupPoint = response.getPickupPoint();
    var dropoffPoint = response.getDropoffPoint();
    var intermediateDestinations = response.getIntermediateDestinationsList();
    var emptyWaypoint = TripWaypointData.builder().build();
    waypoints.add(
        terminalLocationToWaypointData(
            pickupPoint,
            0,
            TripWaypointType.PICKUP_WAYPOINT_TYPE,
            tripId,
            vehicleId,
            emptyWaypoint));
    tripWaypointDifferences.add(getWaypointDifferences(vehicleId, tripId, waypoints.get(0)));
    int n = 1;

    for (var terminalLocation : intermediateDestinations) {
      waypoints.add(
          terminalLocationToWaypointData(
              terminalLocation,
              n,
              TripWaypointType.INTERMEDIATE_WAYPOINT_TYPE,
              tripId,
              vehicleId,
              emptyWaypoint));
      tripWaypointDifferences.add(getWaypointDifferences(vehicleId, tripId, waypoints.get(n)));
      n += 1;
    }
    waypoints.add(
        terminalLocationToWaypointData(
            dropoffPoint,
            n,
            TripWaypointType.DROP_OFF_WAYPOINT_TYPE,
            tripId,
            vehicleId,
            emptyWaypoint));
    tripWaypointDifferences.add(
        getWaypointDifferences(vehicleId, tripId, waypoints.get(waypoints.size() - 1)));

    this.newTripData =
        TripData.builder()
            .setVehicleId(vehicleId)
            .setTripId(tripId)
            .setWaypoints(waypoints)
            .setIsSharedTrip(response.getTripType().equals(TripType.SHARED))
            .setTripStatus(tripStatus)
            .setEventTimestamp(
                Timestamp.ofTimeSecondsAndNanos(
                    logEntry.getTimestamp().getSeconds(), logEntry.getTimestamp().getNanos()))
            .setExpireAt(TimeUtil.offsetFromNow(TimeUtil.ONE_HOUR_IN_SECONDS))
            .build();
    HashMap<String, Change> tripDifferences = new HashMap<>();
    tripDifferences.put("tripId", new Change<>(null, tripId));
    tripDifferences.put("tripStatus", new Change<>(null, tripStatus));

    var tripFleetEvent =
        TripFleetEvent.builder()
            .setNewTrip(newTripData)
            .setVehicleId(vehicleId)
            .setTripId(tripId)
            .setTripDifferences(tripDifferences)
            .setNewTripWaypoints(waypoints)
            .build();
    tripFleetEvents.add(tripFleetEvent);
  }

  @Override
  public List<OutputEvent> updateCallback(Transaction transaction) throws Exception {
    var outputEvents =
        FleetEventCreatorBase.callFleetEventHandlers(
            tripFleetEvents, fleetEventHandlers, transaction, firestoreDatabaseClient);
    transaction.set(newTripDocRef, newTripData);
    return outputEvents;
  }

  HashMap<String, Change> getWaypointDifferences(
      String vehicleId, String tripId, TripWaypointData waypointData) {
    var diff = new HashMap<String, Change>();
    diff.put("tripId", new Change<>(null, tripId));
    diff.put("waypointId", new Change<>(null, waypointData.getWaypointId()));
    return diff;
  }
}
