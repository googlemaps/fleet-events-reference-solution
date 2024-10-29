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

package com.google.fleetevents.odrd;

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
import com.google.fleetevents.lmfs.transactions.UpdateWatermarkTransaction;
import com.google.fleetevents.odrd.transactions.CreateTripTransaction;
import com.google.fleetevents.odrd.transactions.CreateVehicleTransaction;
import com.google.fleetevents.odrd.transactions.UpdateTripTransaction;
import com.google.fleetevents.odrd.transactions.UpdateVehicleTransaction;
import com.google.logging.v2.LogEntry;
import com.google.maps.fleetengine.v1.Trip;
import com.google.maps.fleetengine.v1.Vehicle;
import com.google.protobuf.InvalidProtocolBufferException;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

/** Default implementation of the fleet event creator class for ODRD. Modify for custom logic. */
public class FleetEventCreator extends FleetEventCreatorBase {

  private static FirestoreDatabaseClient db;
  private static FleetEngineClient fleetEngineClient;

  private static final Logger logger =
      Logger.getLogger(com.google.fleetevents.odrd.FleetEventCreator.class.getName());

  private static final String CREATE_VEHICLE_LOG_NAME = "create_vehicle";
  private static final String UPDATE_VEHICLE_LOG_NAME = "update_vehicle";
  private static final String CREATE_TRIP_LOG_NAME = "create_trip";
  private static final String UPDATE_TRIP_LOG_NAME = "update_trip";

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
      case CREATE_VEHICLE_LOG_NAME:
        {
          logger.info("Create Vehicle Log processing");
          if (FleetEventConfig.measureOutOfOrder()) {
            Vehicle vehicle =
                ProtoParser.parseLogEntryResponse(logEntry, Vehicle.getDefaultInstance());
            db.runTransaction(
                new UpdateWatermarkTransaction(vehicle.getName(), logEntry, getDatabase()));
          }
          ApiFuture<List<OutputEvent>> createVehicleResult =
              db.runTransaction(
                  new CreateVehicleTransaction(logEntry, fleetEventHandlers, getDatabase()));
          outputEventsBuilder.addAll(createVehicleResult.get());
          break;
        }
      case UPDATE_VEHICLE_LOG_NAME:
        {
          logger.info("Update Vehicle Log processing");
          if (FleetEventConfig.measureOutOfOrder()) {
            Vehicle vehicle =
                ProtoParser.parseLogEntryResponse(logEntry, Vehicle.getDefaultInstance());
            db.runTransaction(
                new UpdateWatermarkTransaction(vehicle.getName(), logEntry, getDatabase()));
          }
          ApiFuture<List<OutputEvent>> updateVehicleResult =
              db.runTransaction(
                  new UpdateVehicleTransaction(logEntry, fleetEventHandlers, getDatabase()));
          outputEventsBuilder.addAll(updateVehicleResult.get());
          break;
        }
      case CREATE_TRIP_LOG_NAME:
        {
          logger.info("Create Trip Log processing");
          if (FleetEventConfig.measureOutOfOrder()) {
            Trip trip = ProtoParser.parseLogEntryResponse(logEntry, Trip.getDefaultInstance());
            db.runTransaction(
                new UpdateWatermarkTransaction(trip.getName(), logEntry, getDatabase()));
          }
          ApiFuture<List<OutputEvent>> createTripResult =
              db.runTransaction(
                  new CreateTripTransaction(logEntry, fleetEventHandlers, getDatabase()));
          outputEventsBuilder.addAll(createTripResult.get());
          break;
        }
      case UPDATE_TRIP_LOG_NAME:
        {
          logger.info("Update Trip Log processing");
          if (FleetEventConfig.measureOutOfOrder()) {
            Trip trip = ProtoParser.parseLogEntryResponse(logEntry, Trip.getDefaultInstance());
            db.runTransaction(
                new UpdateWatermarkTransaction(trip.getName(), logEntry, getDatabase()));
          }
          ApiFuture<List<OutputEvent>> updateTripResult =
              db.runTransaction(
                  new UpdateTripTransaction(logEntry, fleetEventHandlers, getDatabase()));
          outputEventsBuilder.addAll(updateTripResult.get());
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
