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
import com.google.fleetevents.models.Change;
import com.google.fleetevents.models.DeliveryTaskData;
import com.google.fleetevents.models.DeliveryTaskFleetEvent;
import com.google.fleetevents.models.UpdateAndDifference;
import com.google.fleetevents.models.outputs.OutputEvent;
import com.google.fleetevents.util.NameFormatter;
import com.google.fleetevents.util.ProtoParser;
import com.google.fleetevents.util.TimeUtil;
import com.google.logging.v2.LogEntry;
import com.google.protobuf.InvalidProtocolBufferException;
import google.maps.fleetengine.delivery.v1.Task;
import google.maps.fleetengine.delivery.v1.UpdateTaskRequest;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;
import javax.annotation.Nullable;

/**
 * Creates transaction to update Firestore with an updates to an existing delivery task. Generates a
 * DeliveryTaskFleetEvent if the state or taskOutcome has changed.
 */
public class UpdateDeliveryTaskTransaction implements Transaction.Function<List<OutputEvent>> {

  private static final Logger logger =
      Logger.getLogger(UpdateDeliveryTaskTransaction.class.getName());
  private final List<FleetEventHandler> fleetEventHandlers;
  private final FirestoreDatabaseClient firestoreDatabaseClient;
  private final DocumentReference updatedDeliveryTaskDocRef;
  private final LogEntry logEntry;
  private final String deliveryTaskId;

  public UpdateDeliveryTaskTransaction(
      LogEntry logEntry,
      List<FleetEventHandler> fleetEventHandlers,
      FirestoreDatabaseClient firestoreDatabaseClient)
      throws InvalidProtocolBufferException {
    this.logEntry = logEntry;
    this.fleetEventHandlers = fleetEventHandlers;
    this.firestoreDatabaseClient = firestoreDatabaseClient;
    Task response = ProtoParser.parseLogEntryResponse(logEntry, Task.getDefaultInstance());

    deliveryTaskId = NameFormatter.getIdFromName(response.getName());

    updatedDeliveryTaskDocRef = firestoreDatabaseClient.getTaskDocument(deliveryTaskId);
  }

  static UpdateAndDifference<DeliveryTaskData> getTaskAndDiff(
      LogEntry logEntry, @Nullable DeliveryTaskData oldTaskData)
      throws InvalidProtocolBufferException {
    UpdateTaskRequest request =
        ProtoParser.parseLogEntryRequest(logEntry, UpdateTaskRequest.getDefaultInstance());
    Task task = ProtoParser.parseLogEntryResponse(logEntry, Task.getDefaultInstance());

    List<String> updateMask = request.getUpdateMask().getPathsList();
    if (oldTaskData == null) {
      oldTaskData = DeliveryTaskData.builder().build();
    }
    Set<String> updatedFields = new HashSet<>(updateMask);

    DeliveryTaskData.Builder deliveryTaskBuilder =
        oldTaskData.toBuilder()
            .setEventTimestamp(TimeUtil.protobufToLong(logEntry.getTimestamp()))
            .setExpireAt(TimeUtil.offsetFromNow(TimeUtil.ONE_HOUR_IN_SECONDS));

    Map<String, Change> taskDifferences = new HashMap<>();

    if (updatedFields.contains("state") || !Objects.equals(oldTaskData.getState(),
        task.getState().name())) {
      String state = task.getState().name();
      deliveryTaskBuilder.setState(state);
      taskDifferences.put("state", new Change<>(oldTaskData.getState(), state));
      updatedFields.remove("state");
    }
    if (updatedFields.contains("task_outcome") || !Objects.equals(oldTaskData.getTaskOutcome(),
        task.getTaskOutcome().name())) {
      String taskOutcome = task.getTaskOutcome().name();
      deliveryTaskBuilder.setTaskOutcome(taskOutcome);
      taskDifferences.put(
          "taskOutcome", new Change<>(oldTaskData.getTaskOutcome(), taskOutcome));
      updatedFields.remove("task_outcome");
    }
    if (updatedFields.contains("delivery_vehicle_id") || !Objects.equals(
        oldTaskData.getDeliveryVehicleId(),
        task.getDeliveryVehicleId())) {
      String deliveryVehicleId = task.getDeliveryVehicleId();
      deliveryTaskBuilder.setDeliveryVehicleId(deliveryVehicleId);
      taskDifferences.put(
          "deliveryVehicleId", new Change<>(oldTaskData.getDeliveryVehicleId(), deliveryVehicleId));
      updatedFields.remove("delivery_vehicle_id");
    }
    for (var notHandledUpdateFields : updatedFields) {
      logger.info(String.format("Not currently handling %s updates\n", notHandledUpdateFields));
    }
    return new UpdateAndDifference<>(deliveryTaskBuilder.build(), taskDifferences);
  }

  @Override
  public List<OutputEvent> updateCallback(Transaction transaction)
      throws InvalidProtocolBufferException, ExecutionException, InterruptedException {
    DeliveryTaskData oldDeliveryTaskData =
        transaction.get(updatedDeliveryTaskDocRef).get().toObject(DeliveryTaskData.class);
    if (oldDeliveryTaskData == null) {
      oldDeliveryTaskData = DeliveryTaskData.builder().build();
    }
    UpdateAndDifference<DeliveryTaskData> newTaskAndTaskDifferences =
        getTaskAndDiff(logEntry, oldDeliveryTaskData);
    DeliveryTaskData newDeliveryTaskData = newTaskAndTaskDifferences.updated;
    Map<String, Change> taskDifferences = newTaskAndTaskDifferences.differences;
    var deliveryTaskFleetEvent =
        DeliveryTaskFleetEvent.builder()
            .setDeliveryTaskId(deliveryTaskId)
            .setOldDeliveryTask(oldDeliveryTaskData)
            .setNewDeliveryTask(newDeliveryTaskData)
            .setTaskDifferences(taskDifferences)
            .build();

    List<OutputEvent> outputEvents =
        FleetEventCreator.callFleetEventHandlers(
            ImmutableList.of(deliveryTaskFleetEvent),
            fleetEventHandlers,
            transaction,
            firestoreDatabaseClient);

    // Update the task as the final step.
    transaction.set(updatedDeliveryTaskDocRef, newDeliveryTaskData);
    return outputEvents;
  }
}
