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

package com.google.fleetevents.lmfs;

import com.google.api.core.ApiFuture;
import com.google.common.collect.ImmutableList;
import com.google.fleetengine.auth.token.factory.signer.SignerInitializationException;
import com.google.fleetevents.FleetEventCreatorBase;
import com.google.fleetevents.FleetEventHandler;
import com.google.fleetevents.common.config.FleetEventConfig;
import com.google.fleetevents.common.database.FirestoreDatabaseClient;
import com.google.fleetevents.common.models.OutputEvent;
import com.google.fleetevents.common.util.FleetEngineClient;
import com.google.fleetevents.common.util.ProtoParser;
import com.google.fleetevents.lmfs.transactions.BatchCreateDeliveryTasksTransaction;
import com.google.fleetevents.lmfs.transactions.CreateDeliveryTaskTransaction;
import com.google.fleetevents.lmfs.transactions.CreateDeliveryVehicleTransaction;
import com.google.fleetevents.lmfs.transactions.UpdateDeliveryTaskTransaction;
import com.google.fleetevents.lmfs.transactions.UpdateDeliveryVehicleTransaction;
import com.google.fleetevents.lmfs.transactions.UpdateWatermarkTransaction;
import com.google.logging.v2.LogEntry;
import com.google.maps.fleetengine.delivery.v1.DeliveryVehicle;
import com.google.maps.fleetengine.delivery.v1.Task;
import com.google.protobuf.InvalidProtocolBufferException;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

/** Default implementation of the fleet event creator class. Modify for custom logic. */
public class FleetEventCreator extends FleetEventCreatorBase {
  private static FirestoreDatabaseClient db;
  private static FleetEngineClient fleetEngineClient;
  private static final Logger logger =
      Logger.getLogger(com.google.fleetevents.lmfs.FleetEventCreator.class.getName());
  private static final String CREATE_DELIVERY_VEHICLE_LOG_NAME = "create_delivery_vehicle";
  private static final String UPDATE_DELIVERY_VEHICLE_LOG_NAME = "update_delivery_vehicle";
  private static final String UPDATE_TASK_LOG_NAME = "update_task";
  private static final String CREATE_TASK_LOG_NAME = "create_task";
  private static final String BATCH_CREATE_TASKS_LOG_NAME = "batch_create_tasks";

  public FleetEventCreator() throws IOException, SignerInitializationException {
    super();
    db = new FirestoreDatabaseClient();
    fleetEngineClient = new FleetEngineClient();
  }

  public List<OutputEvent> processCloudLog(
      LogEntry logEntry, List<FleetEventHandler> fleetEventHandlers)
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
          if (FleetEventConfig.measureOutOfOrder()) {
            DeliveryVehicle vehicle =
                ProtoParser.parseLogEntryResponse(logEntry, DeliveryVehicle.getDefaultInstance());
            db.runTransaction(
                new UpdateWatermarkTransaction(vehicle.getName(), logEntry, getDatabase()));
          }
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
          if (FleetEventConfig.measureOutOfOrder()) {
            DeliveryVehicle vehicle =
                ProtoParser.parseLogEntryResponse(logEntry, DeliveryVehicle.getDefaultInstance());
            db.runTransaction(
                new UpdateWatermarkTransaction(vehicle.getName(), logEntry, getDatabase()));
          }
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
          if (FleetEventConfig.measureOutOfOrder()) {
            Task task = ProtoParser.parseLogEntryResponse(logEntry, Task.getDefaultInstance());
            db.runTransaction(
                new UpdateWatermarkTransaction(task.getName(), logEntry, getDatabase()));
          }
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
          if (FleetEventConfig.measureOutOfOrder()) {
            Task task = ProtoParser.parseLogEntryResponse(logEntry, Task.getDefaultInstance());
            db.runTransaction(
                new UpdateWatermarkTransaction(task.getName(), logEntry, getDatabase()));
          }
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

  @Override
  public FirestoreDatabaseClient getDatabase() {
    return db;
  }

  @Override
  public FleetEngineClient getFleetEngineClient() {
    return fleetEngineClient;
  }
}
