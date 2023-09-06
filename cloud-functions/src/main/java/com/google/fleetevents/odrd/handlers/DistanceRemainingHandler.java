package com.google.fleetevents.odrd.handlers;

import com.google.cloud.firestore.Transaction;
import com.google.fleetevents.FleetEventHandler;
import com.google.fleetevents.common.database.FirestoreDatabaseClient;
import com.google.fleetevents.common.models.Change;
import com.google.fleetevents.common.models.FleetEvent;
import com.google.fleetevents.common.models.OutputEvent;
import com.google.fleetevents.odrd.models.TripData;
import com.google.fleetevents.odrd.models.TripFleetEvent;
import com.google.fleetevents.odrd.models.outputs.DistanceRemainingOutputEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class DistanceRemainingHandler implements FleetEventHandler {

  private static final long DEFAULT_THRESHOLD_METERS = 1000L;
  private final long threshold;
  private static final Logger logger =
      Logger.getLogger(
          com.google.fleetevents.odrd.handlers.DistanceRemainingHandler.class.getName());

  public DistanceRemainingHandler() {
    threshold = DEFAULT_THRESHOLD_METERS;
  }

  public DistanceRemainingHandler(long threshold) {
    this.threshold = threshold;
  }

  @Override
  public List<OutputEvent> handleEvent(FleetEvent fleetEvent, Transaction transaction) {
    TripFleetEvent tripFleetEvent = (TripFleetEvent) fleetEvent;
    TripData oldTripData =
        tripFleetEvent.oldTrip() != null ? tripFleetEvent.oldTrip() : TripData.builder().build();
    TripData newTripData =
        tripFleetEvent.newTrip() != null ? tripFleetEvent.newTrip() : TripData.builder().build();
    var waypointDifferencesList = tripFleetEvent.tripWaypointDifferences();
    var outputEvents = new ArrayList<OutputEvent>();
    var waypoints = tripFleetEvent.newTripWaypoints();
    for (var i = 0; waypoints != null && i < waypoints.size(); i++) {
      var waypoint = waypoints.get(i);
      var waypointDifferences = waypointDifferencesList.get(i);
      if (waypointDifferences.containsKey("remainingDistanceMeters")) {
        var distanceChange = waypointDifferences.get("remainingDistanceMeters");
        var hasOriginalDistanceRemaining = distanceChange.oldValue != null;
        if (crossesUnderThreshold(hasOriginalDistanceRemaining, distanceChange)) {
          var distanceOutputEvent = new DistanceRemainingOutputEvent();
          distanceOutputEvent.setTripId(tripFleetEvent.tripId());
          distanceOutputEvent.setWaypointId(waypoint.getWaypointId());
          distanceOutputEvent.setOldDistanceRemainingMeters((Long) distanceChange.oldValue);
          distanceOutputEvent.setNewDistanceRemainingMeters((Long) distanceChange.newValue);
          distanceOutputEvent.setThresholdMilliseconds(threshold);
          distanceOutputEvent.setEventTimestamp(tripFleetEvent.newTrip().getEventTimestamp());
          distanceOutputEvent.setFleetEvent(fleetEvent);
          outputEvents.add(distanceOutputEvent);
        }
      }
    }
    var tripDistanceChange = tripFleetEvent.tripDifferences().get("remainingDistanceMeters");
    var tripHasOriginalDistanceRemaining = tripDistanceChange.oldValue != null;
    if (crossesUnderThreshold(tripHasOriginalDistanceRemaining, tripDistanceChange)) {
      var distanceOutputEvent = new DistanceRemainingOutputEvent();
      distanceOutputEvent.setOldDistanceRemainingMeters(oldTripData.getRemainingDistanceMeters());
      distanceOutputEvent.setNewDistanceRemainingMeters(newTripData.getRemainingDistanceMeters());
      distanceOutputEvent.setThresholdMilliseconds(threshold);
      distanceOutputEvent.setEventTimestamp(newTripData.getEventTimestamp());
      distanceOutputEvent.setTripId(tripFleetEvent.tripId());
      distanceOutputEvent.setIsTripOutputEvent(true);
      distanceOutputEvent.setFleetEvent(fleetEvent);
      outputEvents.add(distanceOutputEvent);
    }
    return outputEvents;
  }

  @Override
  public boolean respondsTo(
      FleetEvent fleetEvent,
      Transaction transaction,
      FirestoreDatabaseClient firestoreDatabaseClient) {
    if (fleetEvent.getEventType() != FleetEvent.Type.TRIP_FLEET_EVENT) {
      return false;
    }
    TripFleetEvent tripFleetEvent = (TripFleetEvent) fleetEvent;
    /* If the trip has remaining distance change then at least the dropoff waypoint distance has
     * changed. */
    return tripFleetEvent.tripDifferences().containsKey("remainingDistanceMeters");
  }

  @Override
  public boolean verifyOutput(OutputEvent outputEvent) {
    if (!(outputEvent instanceof DistanceRemainingOutputEvent)) {
      return false;
    }
    return outputEvent.getType() == OutputEvent.Type.DISTANCE_REMAINING;
  }

  private boolean crossesUnderThreshold(
      boolean hasOriginalDistanceRemaining, Change distanceChange) {
    /* We care about the distance remaining going under the threshold, not continuing to be under or staying above it. */
    if (!hasOriginalDistanceRemaining) {
      return (long) distanceChange.newValue < threshold;
    }
    return (long) distanceChange.oldValue >= threshold
        && (long) distanceChange.newValue < threshold;
  }
}
