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

package com.google.fleetevents.transactions;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Transaction;
import com.google.common.collect.ImmutableList;
import com.google.fleetevents.FleetEventCreator;
import com.google.fleetevents.FleetEventHandler;
import com.google.fleetevents.database.FirestoreDatabaseClient;
import com.google.fleetevents.models.DeliveryTaskData;
import com.google.fleetevents.models.DeliveryTaskFleetEvent;
import com.google.fleetevents.models.DeliveryVehicleData;
import com.google.fleetevents.models.outputs.OutputEvent;
import com.google.fleetevents.util.ProtoParser;
import com.google.fleetevents.util.TimeUtil;
import com.google.logging.v2.LogEntry;
import com.google.protobuf.InvalidProtocolBufferException;
import google.maps.fleetengine.delivery.v1.CreateTaskRequest;
import google.maps.fleetengine.delivery.v1.Task;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Create transaction to update Firestore about a new delivery task. Generates a
 * DeliveryTaskFleetEvent for the new task.
 */
public class CreateDeliveryTaskTransaction implements Transaction.Function<List<OutputEvent>> {

  private final List<FleetEventHandler> fleetEventHandlers;
  private final FirestoreDatabaseClient firestoreDatabaseClient;
  private final DocumentReference newDeliveryTaskDocRef;
  private final DocumentReference deliveryVehicleDocRef;
  private final DeliveryTaskData deliveryTaskData;
  private final DeliveryTaskFleetEvent.Builder deliveryTaskFleetEventBuilder;

  public CreateDeliveryTaskTransaction(
      LogEntry logEntry,
      List<FleetEventHandler> fleetEventHandlers,
      FirestoreDatabaseClient firestoreDatabaseClient)
      throws InvalidProtocolBufferException {
    this.fleetEventHandlers = fleetEventHandlers;
    this.firestoreDatabaseClient = firestoreDatabaseClient;
    CreateTaskRequest request =
        ProtoParser.parseLogEntryRequest(logEntry, CreateTaskRequest.getDefaultInstance());
    Task response = ProtoParser.parseLogEntryResponse(logEntry, Task.getDefaultInstance());
    String deliveryVehicleId = response.getDeliveryVehicleId();
    String deliveryTaskId = request.getTaskId();

    newDeliveryTaskDocRef = firestoreDatabaseClient.getTaskDocument(deliveryTaskId);
    // delivery_vehicle_id for create_task_log is usually empty
    if (deliveryVehicleId != null && !deliveryVehicleId.isEmpty()) {
      deliveryVehicleDocRef = firestoreDatabaseClient.getVehicleDocument(deliveryVehicleId);
    } else {
      deliveryVehicleDocRef = null;
    }

    deliveryTaskData =
        DeliveryTaskData.builder()
            .setEventTimestamp(TimeUtil.protobufToLong(logEntry.getTimestamp()))
            .setDeliveryTaskId(deliveryTaskId)
            .setTrackingId(response.getTrackingId())
            .setName(response.getName())
            .setState(response.getState().name())
            .setExpireAt(TimeUtil.offsetFromNow(TimeUtil.ONE_HOUR_IN_SECONDS))
            .build();

    deliveryTaskFleetEventBuilder =
        DeliveryTaskFleetEvent.builder()
            .setDeliveryTaskId(deliveryTaskId)
            .setNewDeliveryTask(deliveryTaskData);
  }

  @Override
  public List<OutputEvent> updateCallback(Transaction transaction)
      throws ExecutionException, InterruptedException {
    // Allow fleet event handlers to respond to these events and update state if needed.
    if (deliveryVehicleDocRef != null) {
      DeliveryVehicleData deliveryVehicleData =
          transaction.get(deliveryVehicleDocRef).get().toObject(DeliveryVehicleData.class);
      deliveryTaskFleetEventBuilder.setNewDeliveryVehicle(deliveryVehicleData);
    }

    List<OutputEvent> outputEvents =
        FleetEventCreator.callFleetEventHandlers(
            ImmutableList.of(deliveryTaskFleetEventBuilder.build()),
            fleetEventHandlers,
            transaction,
            firestoreDatabaseClient);

    // Create the task as a final step.
    transaction.set(newDeliveryTaskDocRef, deliveryTaskData);
    return outputEvents;
  }
}
