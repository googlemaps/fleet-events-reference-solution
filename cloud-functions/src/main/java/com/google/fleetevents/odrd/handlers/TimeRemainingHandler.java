package com.google.fleetevents.odrd.handlers;

import com.google.cloud.firestore.Transaction;
import com.google.fleetevents.FleetEventHandler;
import com.google.fleetevents.common.database.FirestoreDatabaseClient;
import com.google.fleetevents.common.models.Change;
import com.google.fleetevents.common.models.FleetEvent;
import com.google.fleetevents.common.models.OutputEvent;
import com.google.fleetevents.odrd.models.TripData;
import com.google.fleetevents.odrd.models.TripFleetEvent;
import com.google.fleetevents.odrd.models.outputs.TimeRemainingOutputEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class TimeRemainingHandler implements FleetEventHandler {

  private static final long DEFAULT_THRESHOLD_MILISECONDS = 5 * 60 * 1000L;
  private final long threshold;
  private static final Logger logger = Logger.getLogger(TimeRemainingHandler.class.getName());

  public TimeRemainingHandler() {
    threshold = DEFAULT_THRESHOLD_MILISECONDS;
  }

  public TimeRemainingHandler(long threshold) {
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
      if (waypointDifferences.containsKey("remainingDuration")) {
        var durationChange = waypointDifferences.get("remainingDuration");
        var hasOriginalTimeRemaining = durationChange.oldValue != null;
        if (crossesUnderThreshold(hasOriginalTimeRemaining, durationChange)) {
          var timeRemainingOutputEvent = new TimeRemainingOutputEvent();
          timeRemainingOutputEvent.setTripId(tripFleetEvent.tripId());
          timeRemainingOutputEvent.setWaypointId(waypoint.getWaypointId());
          timeRemainingOutputEvent.setOldDurationRemainingMiliseconds(
              (Long) durationChange.oldValue);
          timeRemainingOutputEvent.setNewDurationRemainingMiliseconds(
              (Long) durationChange.newValue);
          timeRemainingOutputEvent.setThresholdMilliseconds(threshold);
          timeRemainingOutputEvent.setEventTimestamp(tripFleetEvent.newTrip().getEventTimestamp());
          timeRemainingOutputEvent.setFleetEvent(fleetEvent);
          outputEvents.add(timeRemainingOutputEvent);
        }
      }
    }
    var tripDurationChange = tripFleetEvent.tripDifferences().get("remainingDuration");
    var tripHasOriginalTimeRemaining = tripDurationChange.oldValue != null;
    if (crossesUnderThreshold(tripHasOriginalTimeRemaining, tripDurationChange)) {
      var timeRemainingOutputEvent = new TimeRemainingOutputEvent();
      timeRemainingOutputEvent.setOldDurationRemainingMiliseconds(
          oldTripData.getRemainingDuration());
      timeRemainingOutputEvent.setNewDurationRemainingMiliseconds(
          newTripData.getRemainingDuration());
      timeRemainingOutputEvent.setThresholdMilliseconds(threshold);
      timeRemainingOutputEvent.setEventTimestamp(newTripData.getEventTimestamp());
      timeRemainingOutputEvent.setTripId(tripFleetEvent.tripId());
      timeRemainingOutputEvent.setIsTripOutputEvent(true);
      timeRemainingOutputEvent.setFleetEvent(fleetEvent);
      outputEvents.add(timeRemainingOutputEvent);
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
    /* If the trip has remaining duration change then at least the dropoff waypoint duration has
     * changed. */
    return tripFleetEvent.tripDifferences().containsKey("remainingDuration");
  }

  @Override
  public boolean verifyOutput(OutputEvent outputEvent) {
    if (!(outputEvent instanceof TimeRemainingOutputEvent)) {
      return false;
    }
    return outputEvent.getType() == OutputEvent.Type.TIME_REMAINING;
  }

  private boolean crossesUnderThreshold(boolean hasOriginalTimeRemaining, Change durationChange) {
    /* We care about the duration remaining crossing under the threshold, not continuing to be
     * under or staying above it. */
    if (!hasOriginalTimeRemaining) {
      return (long) durationChange.newValue < threshold;
    }
    return (long) durationChange.oldValue >= threshold
        && (long) durationChange.newValue < threshold;
  }
}
