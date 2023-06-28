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

package com.google.fleetevents.config;

import java.util.logging.Logger;


/**
 * Class containing configuration for the project. Contains convenience methods for getting project
 * id, pub/sub topic names, firestore collection names, etc. from environment variables.
 */
public final class FleetEventConfig {

  private static final Logger logger = Logger.getLogger(FleetEventConfig.class.getName());
  private static final String DEFAULT_VEHICLE_COLLECTION_NAME = "deliveryVehicles";
  private static final String DEFAULT_TASK_COLLECTION_NAME = "deliveryTasks";
  private static final String DEFAULT_FLEET_ENGINE_ENDPOINT = "fleetengine.googleapis.com";
  private static final String DEFAULT_TOPIC_OUTPUT_ID = "FleetEventsOutputTopic";

  private static String getEnvironmentVariable(String variableName) {
    var env = System.getenv();
    if (env.containsKey(variableName)) {
      return env.get(variableName);
    }
    logger.warning(String.format("Couldn't find %s variable in the environment.", variableName));
    return null;
  }

  public static String getProjectId() {
    var projectId = getEnvironmentVariable("PROJECT_APP");
    if (projectId == null) {
      throw new RuntimeException("Project Id is not set, can't create events function");
    }
    return projectId;
  }

  public static String getOutputProjectId() {
    var projectId = getEnvironmentVariable("FUNCTION_OUTPUT_PROJECT_ID");
    if (projectId == null) {
      projectId = getProjectId();
      logger.info(
          String.format(
              "No output project id set, defaulting to current project id: %s", projectId));
    }
    return projectId;
  }

  public static String getOutputTopicId() {

    var topicOutputId = getEnvironmentVariable("TOPIC_OUTPUT");
    if (topicOutputId == null) {
      topicOutputId = DEFAULT_TOPIC_OUTPUT_ID;
      logger.info(
          String.format(
              "No topic output set, defaulting to: %s", topicOutputId));

    }
    return topicOutputId;
  }

  public static String getDeliveryVehicleCollectionName() {
    var vehicleCollectionName = getEnvironmentVariable("DELIVERY_VEHICLE_COLLECTION_NAME");
    if (vehicleCollectionName == null) {
      logger.info(
          String.format(
              "No vehicle collection name found in environment variables, using default: %s",
              DEFAULT_VEHICLE_COLLECTION_NAME));
      return DEFAULT_VEHICLE_COLLECTION_NAME;
    }
    return vehicleCollectionName;
  }

  public static String getTaskCollectionName() {
    var taskCollectionName = getEnvironmentVariable("DELIVERY_TASK_COLLECTION_NAME");
    if (taskCollectionName == null) {
      logger.info(
          String.format(
              "No task collection name found in environment variables, using default: %s",
              DEFAULT_TASK_COLLECTION_NAME));
      return DEFAULT_TASK_COLLECTION_NAME;
    }
    return taskCollectionName;
  }


  public static String getFleetEngineServiceAccountName() {
    var fleetEngineServiceAccount = getEnvironmentVariable("SA_FLEETENGINE");
    if (fleetEngineServiceAccount == null) {
      throw new RuntimeException("Fleet Engine Service Account is not set");
    }
    return fleetEngineServiceAccount;
  }


  public static String getFleetEngineEndpoint() {
    var fleetEngineEndpoint = getEnvironmentVariable("FLEETENGINE_ENDPOINT");
    if (fleetEngineEndpoint == null) {
      logger.info(
          String.format(
              "Fleet Engine Endpoint is not set, defaulting to: %s",
              DEFAULT_FLEET_ENGINE_ENDPOINT));
      fleetEngineEndpoint = DEFAULT_FLEET_ENGINE_ENDPOINT;
    }
    return fleetEngineEndpoint;
  }
}
