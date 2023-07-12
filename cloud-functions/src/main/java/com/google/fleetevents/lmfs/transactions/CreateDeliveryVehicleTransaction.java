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

package com.google.fleetevents.lmfs.transactions;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.GeoPoint;
import com.google.cloud.firestore.Transaction;
import com.google.common.collect.ImmutableList;
import com.google.fleetevents.FleetEventCreator;
import com.google.fleetevents.FleetEventHandler;
import com.google.fleetevents.common.database.FirestoreDatabaseClient;
import com.google.fleetevents.common.util.ProtoParser;
import com.google.fleetevents.common.util.TimeUtil;
import com.google.fleetevents.lmfs.models.DeliveryVehicleData;
import com.google.fleetevents.lmfs.models.DeliveryVehicleFleetEvent;
import com.google.fleetevents.lmfs.models.outputs.OutputEvent;
import com.google.logging.v2.LogEntry;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.type.LatLng;
import google.maps.fleetengine.delivery.v1.CreateDeliveryVehicleRequest;
import google.maps.fleetengine.delivery.v1.DeliveryVehicle;
import java.util.List;

/**
 * Creates transaction to update Firestore about a new delivery vehicle. Generates a
 * DeliveryVehicleFleetEvent for the new vehicle.
 */
public class CreateDeliveryVehicleTransaction implements Transaction.Function<List<OutputEvent>> {

  private final List<FleetEventHandler> fleetEventHandlers;
  private final FirestoreDatabaseClient firestoreDatabaseClient;
  private final DeliveryVehicleFleetEvent deliveryVehicleFleetEvent;
  private final DocumentReference newDeliveryVehicleDocRef;
  private final DeliveryVehicleData newDeliveryVehicleData;

  public CreateDeliveryVehicleTransaction(
      LogEntry logEntry,
      List<FleetEventHandler> fleetEventHandlers,
      FirestoreDatabaseClient firestoreDatabaseClient)
      throws InvalidProtocolBufferException {
    this.fleetEventHandlers = fleetEventHandlers;
    this.firestoreDatabaseClient = firestoreDatabaseClient;
    CreateDeliveryVehicleRequest request =
        ProtoParser.parseLogEntryRequest(
            logEntry, CreateDeliveryVehicleRequest.getDefaultInstance());
    DeliveryVehicle response =
        ProtoParser.parseLogEntryResponse(logEntry, DeliveryVehicle.getDefaultInstance());

    String deliveryVehicleId = request.getDeliveryVehicleId();
    String name = response.getName();
    LatLng lastLocation = response.getLastLocation().getLocation();
    newDeliveryVehicleData =
        DeliveryVehicleData.builder()
            .setDeliveryVehicleId(deliveryVehicleId)
            .setName(name)
            .setLastLocation(new GeoPoint(lastLocation.getLatitude(), lastLocation.getLongitude()))
            .setExpireAt(TimeUtil.offsetFromNow(TimeUtil.ONE_HOUR_IN_SECONDS))
            .build();

    deliveryVehicleFleetEvent =
        DeliveryVehicleFleetEvent.builder()
            .setDeliveryVehicleId(deliveryVehicleId)
            .setNewDeliveryVehicle(newDeliveryVehicleData)
            .build();

    newDeliveryVehicleDocRef = firestoreDatabaseClient.getVehicleDocument(deliveryVehicleId);
  }

  @Override
  public List<OutputEvent> updateCallback(Transaction transaction) {
    List<OutputEvent> outputEvents =
        FleetEventCreator.callFleetEventHandlers(
            ImmutableList.of(deliveryVehicleFleetEvent),
            fleetEventHandlers,
            transaction,
            firestoreDatabaseClient);

    // Create the vehicle as a final step.
    transaction.set(newDeliveryVehicleDocRef, newDeliveryVehicleData);
    return outputEvents;
  }
}
