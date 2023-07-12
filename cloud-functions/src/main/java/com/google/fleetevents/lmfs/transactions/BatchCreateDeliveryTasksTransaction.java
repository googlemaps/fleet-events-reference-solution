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
import com.google.cloud.firestore.Transaction;
import com.google.common.collect.ImmutableList;
import com.google.fleetevents.FleetEventCreator;
import com.google.fleetevents.FleetEventHandler;
import com.google.fleetevents.common.database.FirestoreDatabaseClient;
import com.google.fleetevents.common.util.ProtoParser;
import com.google.fleetevents.common.util.TimeUtil;
import com.google.fleetevents.lmfs.models.DeliveryTaskData;
import com.google.fleetevents.lmfs.models.DeliveryTaskFleetEvent;
import com.google.fleetevents.lmfs.models.outputs.OutputEvent;
import com.google.logging.v2.LogEntry;
import com.google.protobuf.InvalidProtocolBufferException;
import google.maps.fleetengine.delivery.v1.BatchCreateTasksRequest;
import google.maps.fleetengine.delivery.v1.BatchCreateTasksResponse;
import google.maps.fleetengine.delivery.v1.CreateTaskRequest;
import google.maps.fleetengine.delivery.v1.Task;
import java.util.ArrayList;
import java.util.List;

/** Transaction for the batch create task log. Generates fleet events or each created task. */
public class BatchCreateDeliveryTasksTransaction
    implements Transaction.Function<List<OutputEvent>> {

  private final List<FleetEventHandler> fleetEventHandlers;
  private final FirestoreDatabaseClient firestoreDatabaseClient;
  private final ArrayList<DocumentReference> newDeliveryTaskDocRefs;
  private final ArrayList<DeliveryTaskData> deliveryTaskDatas;
  private final ArrayList<DeliveryTaskFleetEvent> deliveryTaskFleetEvents;

  public BatchCreateDeliveryTasksTransaction(
      LogEntry logEntry,
      List<FleetEventHandler> fleetEventHandlers,
      FirestoreDatabaseClient firestoreDatabaseClient)
      throws InvalidProtocolBufferException {
    this.fleetEventHandlers = fleetEventHandlers;
    this.firestoreDatabaseClient = firestoreDatabaseClient;
    BatchCreateTasksRequest requests =
        ProtoParser.parseLogEntryRequest(logEntry, BatchCreateTasksRequest.getDefaultInstance());
    BatchCreateTasksResponse responses =
        ProtoParser.parseLogEntryResponse(logEntry, BatchCreateTasksResponse.getDefaultInstance());

    newDeliveryTaskDocRefs = new ArrayList<DocumentReference>();
    deliveryTaskDatas = new ArrayList<DeliveryTaskData>();
    deliveryTaskFleetEvents = new ArrayList<DeliveryTaskFleetEvent>();

    for (var i = 0; i < responses.getTasksCount(); i++) {
      CreateTaskRequest request = requests.getRequests(i);
      Task response = responses.getTasks(i);
      String deliveryVehicleId = response.getDeliveryVehicleId();
      String deliveryTaskId = request.getTaskId();

      newDeliveryTaskDocRefs.add(firestoreDatabaseClient.getTaskDocument(deliveryTaskId));
      deliveryTaskDatas.add(
          DeliveryTaskData.builder()
              .setEventTimestamp(TimeUtil.protobufToLong(logEntry.getTimestamp()))
              .setDeliveryTaskId(deliveryTaskId)
              .setTrackingId(response.getTrackingId())
              .setName(response.getName())
              .setState(response.getState().name())
              .setExpireAt(TimeUtil.offsetFromNow(TimeUtil.ONE_HOUR_IN_SECONDS))
              .build());
      deliveryTaskFleetEvents.add(
          DeliveryTaskFleetEvent.builder()
              .setDeliveryTaskId(deliveryTaskId)
              .setNewDeliveryTask(deliveryTaskDatas.get(i))
              .build());
    }
  }

  @Override
  public List<OutputEvent> updateCallback(Transaction transaction) {
    List<OutputEvent> outputEvents = new ArrayList<>();
    for (var i = 0; i < deliveryTaskFleetEvents.size(); i++) {
      var deliveryTaskFleetEvent = deliveryTaskFleetEvents.get(i);
      // Allow fleet event handlers to respond to these events and update state if needed.
      outputEvents.addAll(
          FleetEventCreator.callFleetEventHandlers(
              ImmutableList.of(deliveryTaskFleetEvent),
              fleetEventHandlers,
              transaction,
              firestoreDatabaseClient));
    }
    for (var i = 0; i < deliveryTaskDatas.size(); i++) {
      var newDeliveryTaskDocRef = newDeliveryTaskDocRefs.get(i);
      var deliveryTaskData = deliveryTaskDatas.get(i);
      // Create the tasks as a final step.
      transaction.set(newDeliveryTaskDocRef, deliveryTaskData);
    }
    return outputEvents;
  }
}
