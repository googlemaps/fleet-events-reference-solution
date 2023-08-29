package com.google.fleetevents.odrd.transactions;

import static com.google.fleetevents.odrd.models.TripWaypointData.terminalLocationToWaypointData;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Transaction;
import com.google.fleetevents.FleetEventCreatorBase;
import com.google.fleetevents.FleetEventHandler;
import com.google.fleetevents.common.database.FirestoreDatabaseClient;
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
import java.util.List;

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

    newTripDocRef = firestoreDatabaseClient.getTripDocument(tripId);

    List<TripWaypointData> waypoints = new ArrayList<>();
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

    this.newTripData =
        TripData.builder()
            .setVehicleId(vehicleId)
            .setTripId(tripId)
            .setWaypoints(waypoints)
            .setIsSharedTrip(response.getTripType().equals(TripType.SHARED))
            .setEventTimestamp(
                Timestamp.ofTimeSecondsAndNanos(
                    logEntry.getTimestamp().getSeconds(), logEntry.getTimestamp().getNanos()))
            .setExpireAt(TimeUtil.offsetFromNow(TimeUtil.ONE_HOUR_IN_SECONDS))
            .build();

    var tripFleetEvent =
        TripFleetEvent.builder()
            .setNewTrip(newTripData)
            .setVehicleId(vehicleId)
            .setTripId(tripId)
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
}
