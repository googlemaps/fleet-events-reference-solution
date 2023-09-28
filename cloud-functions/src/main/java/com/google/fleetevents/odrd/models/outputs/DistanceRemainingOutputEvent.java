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

package com.google.fleetevents.odrd.models.outputs;

import com.google.cloud.Timestamp;
import com.google.fleetevents.common.models.OutputEvent;
import java.util.Objects;

/** OutputEvent for remaining distance for a trip/waypoint. */
public class DistanceRemainingOutputEvent extends OutputEvent {

  private Long oldDistanceRemainingMeters;
  private String waypointId;

  private String tripId;
  private boolean isTripOutputEvent;

  private Long newDistanceRemainingMeters;
  private Timestamp eventTimestamp;
  private Long thresholdMilliseconds;

  public DistanceRemainingOutputEvent() {
    this.type = Type.DISTANCE_REMAINING;
  }

  public Timestamp getEventTimestamp() {
    return eventTimestamp;
  }

  public void setEventTimestamp(Timestamp eventTimestamp) {
    this.eventTimestamp = eventTimestamp;
  }

  public Long getOldDistanceRemainingMeters() {
    return oldDistanceRemainingMeters;
  }

  public void setOldDistanceRemainingMeters(Long oldDistanceRemainingMeters) {
    this.oldDistanceRemainingMeters = oldDistanceRemainingMeters;
  }

  public Long getThresholdMilliseconds() {
    return thresholdMilliseconds;
  }

  public void setThresholdMilliseconds(Long thresholdMilliseconds) {
    this.thresholdMilliseconds = thresholdMilliseconds;
  }

  public String getWaypointId() {
    return waypointId;
  }

  public void setWaypointId(String waypointId) {
    this.waypointId = waypointId;
  }

  public String getTripId() {
    return tripId;
  }

  public void setTripId(String tripId) {
    this.tripId = tripId;
  }

  public boolean isTripOutputEvent() {
    return isTripOutputEvent;
  }

  public void setIsTripOutputEvent(boolean tripOutputEvent) {
    isTripOutputEvent = tripOutputEvent;
  }

  public Long getNewDistanceRemainingMeters() {
    return newDistanceRemainingMeters;
  }

  public void setNewDistanceRemainingMeters(Long newDistanceRemainingMeters) {
    this.newDistanceRemainingMeters = newDistanceRemainingMeters;
  }

  @Override
  public String toString() {
    return "DistanceRemainingOutputEvent{"
        + "oldDistanceRemainingMeters="
        + oldDistanceRemainingMeters
        + ", tripId=\n"
        + tripId
        + ", waypointId=\n"
        + waypointId
        + ", isTripOutputEvent=\n"
        + isTripOutputEvent
        + ", newDistanceRemainingMeters="
        + newDistanceRemainingMeters
        + ", threshold="
        + thresholdMilliseconds
        + ", eventTimestamp="
        + eventTimestamp
        + ", fleetEvent="
        + fleetEvent
        + ", type="
        + type
        + '}';
  }

  @Override
  public boolean equals(Object object) {
    if (object instanceof DistanceRemainingOutputEvent that) {
      return Objects.equals(tripId, that.tripId)
          && Objects.equals(that.waypointId, this.waypointId)
          && Objects.equals(isTripOutputEvent, that.isTripOutputEvent)
          && Objects.equals(that.oldDistanceRemainingMeters, this.oldDistanceRemainingMeters)
          && Objects.equals(that.newDistanceRemainingMeters, this.newDistanceRemainingMeters)
          && Objects.equals(that.thresholdMilliseconds, this.thresholdMilliseconds)
          && Objects.equals(that.eventTimestamp, this.eventTimestamp)
          && Objects.equals(that.getFleetEvent(), this.getFleetEvent());
    }
    return false;
  }
}
