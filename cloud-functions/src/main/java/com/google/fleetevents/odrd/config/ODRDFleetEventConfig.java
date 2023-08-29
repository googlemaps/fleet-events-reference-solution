package com.google.fleetevents.odrd.config;

import static com.google.fleetevents.common.config.FleetEventConfig.getEnvironmentVariable;

import java.util.logging.Logger;

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
