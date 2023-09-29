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

package com.google.fleetevents.odrd.config;

import static com.google.fleetevents.common.config.FleetEventConfig.getEnvironmentVariable;

import java.util.logging.Logger;

/** Configuration class for the ODRD reference solution. */
public class ODRDFleetEventConfig {
  private static final Logger logger = Logger.getLogger(ODRDFleetEventConfig.class.getName());

  private static final String DEFAULT_VEHICLE_COLLECTION_NAME = "vehicles";
  private static final String DEFAULT_TRIP_COLLECTION_NAME = "trips";
  private static final String DEFAULT_TRIP_WAYPOINT_COLLECTION_NAME = "waypoints";

  public static String getVehicleCollectionName() {
    var vehicleCollectionName = getEnvironmentVariable("VEHICLE_COLLECTION_NAME");
    if (vehicleCollectionName == null) {
      logger.info(
          String.format(
              "No vehicle collection name found in environment variables, using default: %s",
              DEFAULT_VEHICLE_COLLECTION_NAME));
      return DEFAULT_VEHICLE_COLLECTION_NAME;
    }
    return vehicleCollectionName;
  }

  public static String getTripCollectionName() {
    var tripCollectionName = getEnvironmentVariable("TRIP_COLLECTION_NAME");
    if (tripCollectionName == null) {
      logger.info(
          String.format(
              "No trip collection name found in environment variables, using default: %s",
              DEFAULT_TRIP_COLLECTION_NAME));
      return DEFAULT_TRIP_COLLECTION_NAME;
    }
    return tripCollectionName;
  }

  public static String getTripWaypointCollectionName() {
    var tripWaypointCollectionName = getEnvironmentVariable("TRIP_WAYPOINT_COLLECTION_NAME");
    if (tripWaypointCollectionName == null) {
      logger.info(
          String.format(
              "No trip waypoint collection name found in environment variables, using default: %s",
              DEFAULT_TRIP_WAYPOINT_COLLECTION_NAME));
      return DEFAULT_TRIP_WAYPOINT_COLLECTION_NAME;
    }
    return tripWaypointCollectionName;
  }
}
