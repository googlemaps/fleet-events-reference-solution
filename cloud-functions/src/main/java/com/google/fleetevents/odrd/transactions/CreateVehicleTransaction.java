package com.google.fleetevents.odrd.transactions;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Transaction;
import com.google.common.collect.ImmutableList;
import com.google.fleetevents.FleetEventCreatorBase;
import com.google.fleetevents.FleetEventHandler;
import com.google.fleetevents.common.database.FirestoreDatabaseClient;
import com.google.fleetevents.common.models.Change;
import com.google.fleetevents.common.models.OutputEvent;
import com.google.fleetevents.common.util.ProtoParser;
import com.google.fleetevents.odrd.models.VehicleData;
import com.google.fleetevents.odrd.models.VehicleFleetEvent;
import com.google.logging.v2.LogEntry;
import com.google.protobuf.InvalidProtocolBufferException;
import google.maps.fleetengine.v1.CreateVehicleRequest;
import google.maps.fleetengine.v1.Vehicle;
import java.util.HashMap;
import java.util.List;

/**
 * Class for creating vehicles in Firestore based on create vehicle logs from Fleet Engine API
 * calls.
 */
/** Transaction class to create a vehicle entity in Firestore. */
public class CreateVehicleTransaction implements Transaction.Function<List<OutputEvent>> {
  private final List<FleetEventHandler> fleetEventHandlers;
  private final FirestoreDatabaseClient firestoreDatabaseClient;
  private final VehicleFleetEvent vehicleFleetEvent;
  private final DocumentReference newVehicleDocRef;
  private final VehicleData newVehicleData;

  public CreateVehicleTransaction(
      LogEntry logEntry,
      List<FleetEventHandler> fleetEventHandlers,
      FirestoreDatabaseClient firestoreDatabaseClient)
      throws InvalidProtocolBufferException {
    this.fleetEventHandlers = fleetEventHandlers;
    this.firestoreDatabaseClient = firestoreDatabaseClient;
    CreateVehicleRequest createVehicleRequest =
        ProtoParser.parseLogEntryRequest(logEntry, CreateVehicleRequest.getDefaultInstance());
    Vehicle response = ProtoParser.parseLogEntryResponse(logEntry, Vehicle.getDefaultInstance());
    var newVehicleData =
        VehicleData.fromVehicle(
            response,
            Timestamp.ofTimeSecondsAndNanos(
                logEntry.getTimestamp().getSeconds(), logEntry.getTimestamp().getNanos()));
    var vehicleDifferences = new HashMap<String, Change>();
    vehicleDifferences.put("vehicleId", new Change(null, newVehicleData.getVehicleId()));
    this.vehicleFleetEvent =
        VehicleFleetEvent.builder()
            .setVehicleId(newVehicleData.getVehicleId())
            .setNewVehicle(newVehicleData)
            .setVehicleDifferences(vehicleDifferences)
            .build();
    this.newVehicleDocRef =
        firestoreDatabaseClient.getVehicleDocument(newVehicleData.getVehicleId());
    this.newVehicleData = newVehicleData;
  }

  @Override
  public List<OutputEvent> updateCallback(Transaction transaction) {
    List<OutputEvent> outputEvents =
        FleetEventCreatorBase.callFleetEventHandlers(
            ImmutableList.of(vehicleFleetEvent),
            fleetEventHandlers,
            transaction,
            firestoreDatabaseClient);

    // Create the vehicle as a final step.
    transaction.set(newVehicleDocRef, newVehicleData);
    return outputEvents;
  }
}
