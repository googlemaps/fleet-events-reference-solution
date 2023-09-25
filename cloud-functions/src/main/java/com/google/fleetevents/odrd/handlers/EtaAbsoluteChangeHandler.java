package com.google.fleetevents.odrd.handlers;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.Transaction;
import com.google.fleetevents.FleetEventHandler;
import com.google.fleetevents.common.database.FirestoreDatabaseClient;
import com.google.fleetevents.common.models.Change;
import com.google.fleetevents.common.models.FleetEvent;
import com.google.fleetevents.common.models.OutputEvent;
import com.google.fleetevents.common.util.TimeUtil;
import com.google.fleetevents.odrd.models.TripFleetEvent;
import com.google.fleetevents.odrd.models.outputs.EtaAbsoluteChangeOutputEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class EtaAbsoluteChangeHandler implements FleetEventHandler {
  private static final String ORIGINAL_ETA_KEY = "originalEta";
  private static final long DEFAULT_THRESHOLD_MILISECONDS = 5 * 60 * 1000;
  private long thresholdMilliseconds;

  public EtaAbsoluteChangeHandler() {
    this.thresholdMilliseconds = DEFAULT_THRESHOLD_MILISECONDS;
  }

  @Override
  public List<OutputEvent> handleEvent(FleetEvent fleetEvent, Transaction transaction) {
    var tripFleetEvent = (TripFleetEvent) fleetEvent;
    var outputEvents = new ArrayList<OutputEvent>();
    var eventTimestamp = Objects.requireNonNull(tripFleetEvent.newTrip()).getEventTimestamp();
    var waypointsChanged = tripFleetEvent.waypointsChanged();
    for (var i = 0; i < Objects.requireNonNull(tripFleetEvent.newTripWaypoints()).size(); i++) {
      var waypointDiff = tripFleetEvent.tripWaypointDifferences().get(i);
      var waypoint = tripFleetEvent.newTripWaypoints().get(i);
      var etaOutputEvent =
          tryGetEtaOutputEvent(
              waypoint.getWaypointId(),
              waypoint.getEventMetadata(),
              waypointDiff,
              waypoint.getEta(),
              eventTimestamp,
              fleetEvent,
              false,
              waypointsChanged);
      etaOutputEvent.ifPresent(outputEvents::add);
    }
    var newTrip = tripFleetEvent.newTrip();
    var etaOutputEvent =
        tryGetEtaOutputEvent(
            newTrip.getTripId(),
            newTrip.getEventMetadata(),
            tripFleetEvent.tripDifferences(),
            newTrip.getEta(),
            eventTimestamp,
            fleetEvent,
            true,
            waypointsChanged);
    etaOutputEvent.ifPresent(outputEvents::add);
    return outputEvents;
  }

  @Override
  public boolean respondsTo(
      FleetEvent fleetEvent,
      Transaction transaction,
      FirestoreDatabaseClient firestoreDatabaseClient) {
    if (fleetEvent instanceof TripFleetEvent tripFleetEvent) {
      return tripFleetEvent.tripDifferences().containsKey("eta")
          || tripFleetEvent.tripWaypointDifferences().stream()
              .map(differences -> differences.containsKey("eta"))
              .reduce(false, Boolean::logicalOr);
    }
    return false;
  }

  @Override
  public boolean verifyOutput(OutputEvent outputEvent) {
    return outputEvent instanceof EtaAbsoluteChangeOutputEvent;
  }

  public Optional<OutputEvent> tryGetEtaOutputEvent(
      String identifier,
      Map<String, Object> eventMetadata,
      Map<String, Change> differences,
      Timestamp newEta,
      Timestamp eventTimestamp,
      FleetEvent fleetEvent,
      boolean isTripOutputEvent,
      boolean waypointsChanged) {
    Optional<OutputEvent> optionalEtaOutputEvent = Optional.empty();
    if (differences.containsKey("eta")) {
      if (waypointsChanged) {
        eventMetadata.remove(ORIGINAL_ETA_KEY);
      }
      var hasOriginalEta = eventMetadata.containsKey(ORIGINAL_ETA_KEY);
      if (hasOriginalEta) {
        var originalEta = Timestamp.parseTimestamp(eventMetadata.get(ORIGINAL_ETA_KEY).toString());
        if (Math.abs(
                TimeUtil.timestampDifferenceMillis(
                    newEta.toSqlTimestamp(), originalEta.toSqlTimestamp()))
            >= thresholdMilliseconds) {
          var etaOutputEvent = new EtaAbsoluteChangeOutputEvent();
          etaOutputEvent.setIdentifier(identifier);
          etaOutputEvent.setOriginalEta(originalEta);
          etaOutputEvent.setNewEta(newEta);
          etaOutputEvent.setThresholdMilliseconds(thresholdMilliseconds);
          etaOutputEvent.setEventTimestamp(eventTimestamp);
          etaOutputEvent.setIsTripOutputEvent(isTripOutputEvent);
          etaOutputEvent.setFleetEvent(fleetEvent);
          optionalEtaOutputEvent = Optional.of(etaOutputEvent);
        }
      } else {
        var originalEta = differences.get("eta").newValue;
        eventMetadata.put(ORIGINAL_ETA_KEY, originalEta);
      }
    }
    return optionalEtaOutputEvent;
  }
}
