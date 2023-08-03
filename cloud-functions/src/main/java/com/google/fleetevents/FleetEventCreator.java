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

package com.google.fleetevents;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.Transaction;
import com.google.common.collect.ImmutableList;
import com.google.fleetevents.common.database.FirestoreDatabaseClient;
import com.google.fleetevents.common.models.FleetEvent;
import com.google.fleetevents.common.models.Pair;
import com.google.fleetevents.common.util.FleetEngineClient;
import com.google.fleetevents.lmfs.models.DeliveryTaskFleetEvent;
import com.google.fleetevents.lmfs.models.DeliveryVehicleFleetEvent;
import com.google.fleetevents.lmfs.models.LatLng;
import com.google.fleetevents.lmfs.models.TaskInfo;
import com.google.fleetevents.lmfs.models.VehicleJourneySegment;
import com.google.fleetevents.lmfs.models.VehicleStop;
import com.google.fleetevents.lmfs.models.outputs.OutputEvent;
import com.google.fleetevents.lmfs.transactions.BatchCreateDeliveryTasksTransaction;
import com.google.fleetevents.lmfs.transactions.CreateDeliveryTaskTransaction;
import com.google.fleetevents.lmfs.transactions.CreateDeliveryVehicleTransaction;
import com.google.fleetevents.lmfs.transactions.UpdateDeliveryTaskTransaction;
import com.google.fleetevents.lmfs.transactions.UpdateDeliveryVehicleTransaction;
import com.google.logging.v2.LogEntry;
import com.google.protobuf.InvalidProtocolBufferException;
import google.maps.fleetengine.delivery.v1.DeliveryVehicle;
import google.maps.fleetengine.delivery.v1.Task;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/** Creates events from Fleet Engine cloud logs. */
public abstract class FleetEventCreator {

  private static final Logger logger = Logger.getLogger(FleetEventCreator.class.getName());

  private static final String CREATE_DELIVERY_VEHICLE_LOG_NAME = "create_delivery_vehicle";
  private static final String UPDATE_DELIVERY_VEHICLE_LOG_NAME = "update_delivery_vehicle";
  private static final String UPDATE_TASK_LOG_NAME = "update_task";
  private static final String CREATE_TASK_LOG_NAME = "create_task";
  private static final String BATCH_CREATE_TASKS_LOG_NAME = "batch_create_tasks";

  public FleetEventCreator() {}

  public static List<OutputEvent> callFleetEventHandlers(
      List<FleetEvent> fleetEvents,
      List<FleetEventHandler> fleetEventHandlers,
      Transaction transaction,
      FirestoreDatabaseClient firestoreDatabaseClient) {
    List<OutputEvent> outputEvents = new ArrayList<>();
    List<Pair<FleetEvent, List<FleetEventHandler>>> respondingHandlers = new ArrayList<>();
    /* Determine whether the handler needs to respond and then call the handle function seperately
     * after if any state needs to be updated. All gets from the transaction object should occur
     * inside the respondsTo method. */
    for (FleetEvent fleetEvent : fleetEvents) {
      List<FleetEventHandler> responders = new ArrayList<>();
      for (FleetEventHandler fleetEventHandler : fleetEventHandlers) {
        if (fleetEventHandler.respondsTo(fleetEvent, transaction, firestoreDatabaseClient)) {
          responders.add(fleetEventHandler);
        }
      }
      respondingHandlers.add(new Pair<>(fleetEvent, responders));
    }
    /* Now the responding handlers can be called, any custom state updates can be made inside the
     * handleEvent method. */
    for (var respondingHandler : respondingHandlers) {
      for (FleetEventHandler responder : respondingHandler.getValue()) {
        var fleetEvent = respondingHandler.getKey();
        List<OutputEvent> outputs = responder.handleEvent(fleetEvent, transaction);
        for (OutputEvent output : outputs) {
          if (responder.verifyOutput(output)) {
            outputEvents.add(output);
          } else {
            logger.warning(String.format("Dropped malformed output: %s", output.toString()));
          }
        }
      }
    }
    return outputEvents;
  }

  public List<OutputEvent> processCloudLog(
      final LogEntry logEntry, final List<FleetEventHandler> fleetEventHandlers)
      throws InvalidProtocolBufferException, ExecutionException, InterruptedException {
    ImmutableList.Builder<OutputEvent> outputEventsBuilder = ImmutableList.builder();
    int split = logEntry.getLogName().indexOf("%2F");
    if (split == -1) {
      // this is not a fleet log.
      return outputEventsBuilder.build();
    }
    String truncatedLogName = logEntry.getLogName().substring(split + 3);
    var db = getDatabase();
    switch (truncatedLogName) {
      case CREATE_DELIVERY_VEHICLE_LOG_NAME:
        {
          logger.info("Create Delivery Vehicle Log processing");
          ApiFuture<List<OutputEvent>> createDeliveryVehicleResult =
              db.runTransaction(
                  new CreateDeliveryVehicleTransaction(
                      logEntry, fleetEventHandlers, getDatabase()));

          outputEventsBuilder.addAll(createDeliveryVehicleResult.get());
          break;
        }
      case UPDATE_DELIVERY_VEHICLE_LOG_NAME:
        {
          logger.info("Update Delivery Vehicle Log processing");
          ApiFuture<List<OutputEvent>> updateDeliveryVehicleResult =
              db.runTransaction(
                  new UpdateDeliveryVehicleTransaction(
                      logEntry, fleetEventHandlers, getDatabase()));
          outputEventsBuilder.addAll(updateDeliveryVehicleResult.get());
          break;
        }
      case CREATE_TASK_LOG_NAME:
        {
          logger.info("Create Task Log processing");
          ApiFuture<List<OutputEvent>> createDeliveryTaskResult =
              db.runTransaction(
                  new CreateDeliveryTaskTransaction(logEntry, fleetEventHandlers, getDatabase()));

          outputEventsBuilder.addAll(createDeliveryTaskResult.get());
          break;
        }
      case BATCH_CREATE_TASKS_LOG_NAME:
        {
          logger.info("Batch Create Tasks Log processing");
          ApiFuture<List<OutputEvent>> batchCreateDeliveryTaskResult =
              db.runTransaction(
                  new BatchCreateDeliveryTasksTransaction(
                      logEntry, fleetEventHandlers, getDatabase()));

          outputEventsBuilder.addAll(batchCreateDeliveryTaskResult.get());
          break;
        }
      case UPDATE_TASK_LOG_NAME:
        {
          logger.info("Update Task Log processing");
          ApiFuture<List<OutputEvent>> updateDeliveryTaskResult =
              db.runTransaction(
                  new UpdateDeliveryTaskTransaction(logEntry, fleetEventHandlers, getDatabase()));
          outputEventsBuilder.addAll(updateDeliveryTaskResult.get());
          break;
        }
      default:
        logger.warning(
            String.format("No such log entry is handled currently: %s\n", logEntry.getLogName()));
        break;
    }
    return outputEventsBuilder.build();
  }

  // Enrich output events with information retrieved from the Fleet Engine service
  public void addExtraInfo(List<OutputEvent> outputs) {
    for (OutputEvent output : outputs) {
      if (output.getFleetEvent() == null) {
        continue;
      }
      if (output.getFleetEvent().getEventType() == FleetEvent.Type.DELIVERY_TASK_FLEET_EVENT) {
        DeliveryTaskFleetEvent taskFleetEvent = (DeliveryTaskFleetEvent) output.getFleetEvent();
        Optional<Task> optionalTask =
            getFleetEngineClient().getTask(taskFleetEvent.newDeliveryTask().getDeliveryTaskId());
        if (optionalTask.isEmpty()) {
          logger.log(
              Level.WARNING,
              String.format(
                  "Failed to retrieve planned_location from Fleet Engine for task %s",
                  taskFleetEvent.newDeliveryTask().getDeliveryTaskId()));
          continue;
        }
        Task task = optionalTask.get();
        DeliveryTaskFleetEvent enrichedTaskFleetEvent =
            taskFleetEvent.toBuilder()
                .setPlannedLocation(
                    new LatLng.Builder()
                        .setLatitude(task.getPlannedLocation().getPoint().getLatitude())
                        .setLongitude(task.getPlannedLocation().getPoint().getLongitude())
                        .build())
                .build();
        output.setFleetEvent(enrichedTaskFleetEvent);
      } else if (output.getFleetEvent().getEventType()
          == FleetEvent.Type.DELIVERY_VEHICLE_FLEET_EVENT) {
        DeliveryVehicleFleetEvent deliveryVehicleFleetEvent =
            (DeliveryVehicleFleetEvent) output.getFleetEvent();
        Optional<DeliveryVehicle> optionalDeliveryVehicle =
            getFleetEngineClient()
                .getDeliveryVehicle(deliveryVehicleFleetEvent.deliveryVehicleId());
        if (optionalDeliveryVehicle.isEmpty()) {
          logger.log(
              Level.WARNING,
              String.format(
                  "Failed to retrieve planned_location from Fleet Engine for vehicle %s",
                  deliveryVehicleFleetEvent.deliveryVehicleId()));
          continue;
        }
        DeliveryVehicle deliveryVehicle = optionalDeliveryVehicle.get();
        // Match each stop with a fleet stop, if available, and enrich with planned_location.
        for (VehicleJourneySegment rvjs :
            deliveryVehicleFleetEvent.newDeliveryVehicle().getRemainingVehicleJourneySegments()) {
          Optional<google.maps.fleetengine.delivery.v1.VehicleStop> matchedStop =
              deliveryVehicle.getRemainingVehicleJourneySegmentsList().stream()
                  .map(x -> x.getStop())
                  .filter(stop -> isMatchingStop(rvjs.getVehicleStop(), stop))
                  .findFirst();
          if (matchedStop.isPresent()) {
            com.google.type.LatLng matchedPlannedLocation =
                matchedStop.get().getPlannedLocation().getPoint();
            rvjs.getVehicleStop()
                .setPlannedLocation(
                    new LatLng.Builder()
                        .setLongitude(matchedPlannedLocation.getLongitude())
                        .setLatitude(matchedPlannedLocation.getLatitude())
                        .build());
          }
        }
      }
    }
  }

  // Returns true if any taskid from stop matches one from fleetStop
  private boolean isMatchingStop(
      VehicleStop stop, google.maps.fleetengine.delivery.v1.VehicleStop fleetStop) {
    for (TaskInfo info : stop.getTaskInfos()) {
      List<String> matches =
          fleetStop.getTasksList().stream()
              .map(t -> t.getTaskId())
              .filter(t -> t.equals(info.getTaskId()))
              .collect(Collectors.toList());
      if (matches.size() > 0) return true;
    }
    return false;
  }

  public abstract FirestoreDatabaseClient getDatabase();

  public abstract FleetEngineClient getFleetEngineClient();
}
