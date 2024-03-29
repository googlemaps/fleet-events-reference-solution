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

package com.google.fleetevents.odrd.handlers;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.Transaction;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.fleetevents.FleetEventHandler;
import com.google.fleetevents.common.database.FirestoreDatabaseClient;
import com.google.fleetevents.common.models.Change;
import com.google.fleetevents.common.models.FleetEvent;
import com.google.fleetevents.common.models.OutputEvent;
import com.google.fleetevents.common.util.TimeUtil;
import com.google.fleetevents.odrd.models.TripFleetEvent;
import com.google.fleetevents.odrd.models.outputs.EtaRelativeChangeOutputEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Fleet Events Handler for alerting when the eta for a trip or waypoint has a defined percentage
 * change in the duration from its original eta.
 */
public class EtaRelativeChangeHandler implements FleetEventHandler {
  private static final String RELATIVE_ETA_PAIR_KEY = "relativeEtaPair";
  public static final String ORIGINAL_DURATION_MILLISECONDS = "originalDurationMilliseconds";
  public static final String ORIGINAL_ETA = "originalEta";
  private static final double DEFAULT_THRESHOLD_PERCENT = 0.1;
  private double thresholdPercent;

  public EtaRelativeChangeHandler() {
    this.thresholdPercent = DEFAULT_THRESHOLD_PERCENT;
  }

  @Override
  public List<OutputEvent> handleEvent(FleetEvent fleetEvent, Transaction transaction) {
    var tripFleetEvent = (TripFleetEvent) fleetEvent;
    var eventTimestamp = Objects.requireNonNull(tripFleetEvent.newTrip()).getEventTimestamp();
    var outputEvents = new ArrayList<OutputEvent>();
    var waypointsChanged = tripFleetEvent.waypointsChanged();
    for (var i = 0; i < tripFleetEvent.newTripWaypoints().size(); i++) {
      var waypointDiff = tripFleetEvent.tripWaypointDifferences().get(i);
      var waypoint = tripFleetEvent.newTripWaypoints().get(i);
      var etaOutputEvent =
          tryGetRelativeEtaOutputEvent(
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
        tryGetRelativeEtaOutputEvent(
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
    return outputEvent instanceof EtaRelativeChangeOutputEvent;
  }

  @SuppressWarnings("unchecked")
  public Optional<OutputEvent> tryGetRelativeEtaOutputEvent(
      String identifier,
      Map<String, Object> eventMetadata,
      Map<String, Change> differences,
      Timestamp newEta,
      Timestamp eventTimestamp,
      FleetEvent fleetEvent,
      boolean isTripOutputEvent,
      boolean waypointsChanged) {
    var outputEvent = Optional.<OutputEvent>empty();
    if (!differences.containsKey("eta")) {
      return outputEvent;
    }
    if (waypointsChanged) {
      eventMetadata.remove(RELATIVE_ETA_PAIR_KEY);
    }
    var hasRelativeEtaPair = eventMetadata.containsKey(RELATIVE_ETA_PAIR_KEY);
    if (hasRelativeEtaPair) {
      var relativeEtaPair = (Map<String, Object>) eventMetadata.get(RELATIVE_ETA_PAIR_KEY);
      var originalDurationMilliseconds = (Long) relativeEtaPair.get(ORIGINAL_DURATION_MILLISECONDS);
      var originalEta = (Timestamp) relativeEtaPair.get(ORIGINAL_ETA);
      double etaDelta =
          TimeUtil.timestampDifferenceMillis(newEta.toSqlTimestamp(), originalEta.toSqlTimestamp());
      var percentDurationChange = Math.abs(etaDelta / originalDurationMilliseconds);
      outputEvent =
          checkRelativeEta(
              percentDurationChange,
              identifier,
              originalEta,
              newEta,
              eventTimestamp,
              originalDurationMilliseconds,
              fleetEvent,
              isTripOutputEvent);
    } else {
      var originalEta = (Timestamp) differences.get("eta").newValue;
      var originalDurationMilliseconds =
          TimeUtil.timestampDifferenceMillis(
              originalEta.toSqlTimestamp(), eventTimestamp.toSqlTimestamp());
      if (originalDurationMilliseconds <= 0) {
        // If the duration is non-positive use a dummy value of 1 second (1000 milliseconds)
        originalDurationMilliseconds = 1000;
      }
      eventMetadata.put(
          RELATIVE_ETA_PAIR_KEY,
          Maps.newHashMap(
              ImmutableMap.of(
                  ORIGINAL_DURATION_MILLISECONDS,
                  originalDurationMilliseconds,
                  ORIGINAL_ETA,
                  originalEta)));
    }
    return outputEvent;
  }

  Optional<OutputEvent> checkRelativeEta(
      double percentDurationChange,
      String identifier,
      Timestamp originalEta,
      Timestamp newEta,
      Timestamp eventTimestamp,
      Long originalDurationMilliseconds,
      FleetEvent fleetEvent,
      boolean isTripOutputEvent) {
    if (percentDurationChange < thresholdPercent) {
      return Optional.empty();
    }
    var etaOutputEvent = new EtaRelativeChangeOutputEvent();
    etaOutputEvent.setIdentifier(identifier);
    etaOutputEvent.setOriginalEta(originalEta);
    etaOutputEvent.setNewEta(newEta);
    etaOutputEvent.setOriginalDuration(originalDurationMilliseconds);
    etaOutputEvent.setPercentDurationChange(percentDurationChange);
    etaOutputEvent.setThresholdPercent(thresholdPercent);
    etaOutputEvent.setEventTimestamp(eventTimestamp);
    etaOutputEvent.setFleetEvent(fleetEvent);
    etaOutputEvent.setIsTripOutputEvent(isTripOutputEvent);

    return Optional.of(etaOutputEvent);
  }
}
