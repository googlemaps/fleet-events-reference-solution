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

/**
 * OutputEvent for relative eta change handler for a trip/waypoint. When waypoints in a trip are
 * detected as changed, metadata for the eta is reset.
 */
public class EtaRelativeChangeOutputEvent extends OutputEvent {

  private Timestamp originalEta;
  private Long originalDuration;
  private Timestamp newEta;

  private double percentDurationChange;
  private double thresholdPercent;

  private String identifier;

  private boolean isTripOutputEvent;

  private Timestamp eventTimestamp;

  public EtaRelativeChangeOutputEvent() {
    type = Type.RELATIVE_ETA;
  }

  public Timestamp getOriginalEta() {
    return originalEta;
  }

  public void setOriginalEta(Timestamp originalEta) {
    this.originalEta = originalEta;
  }

  public Long getOriginalDuration() {
    return originalDuration;
  }

  public void setOriginalDuration(Long originalDurationMilliseconds) {
    this.originalDuration = originalDurationMilliseconds;
  }

  public double getPercentDurationChange() {
    return percentDurationChange;
  }

  public void setPercentDurationChange(double percentDurationChange) {
    this.percentDurationChange = percentDurationChange;
  }

  public Timestamp getNewEta() {
    return newEta;
  }

  public void setNewEta(Timestamp newEta) {
    this.newEta = newEta;
  }

  public double getThresholdPercent() {
    return thresholdPercent;
  }

  public void setThresholdPercent(double thresholdPercent) {
    this.thresholdPercent = thresholdPercent;
  }

  public String getIdentifier() {
    return identifier;
  }

  public void setIdentifier(String identifier) {
    this.identifier = identifier;
  }

  public boolean getIsTripOutputEvent() {
    return isTripOutputEvent;
  }

  @Override
  public String toString() {
    return "EtaRelativeChangeOutputEvent{"
        + "originalEta="
        + originalEta
        + ", originalDuration="
        + originalDuration
        + ", newEta="
        + newEta
        + ", percentDurationChange="
        + percentDurationChange
        + ", thresholdPercent="
        + thresholdPercent
        + ", identifier='"
        + identifier
        + '\''
        + ", isTripOutputEvent="
        + isTripOutputEvent
        + ", eventTimestamp="
        + eventTimestamp
        + ", fleetEvent="
        + fleetEvent
        + ", type="
        + type
        + '}';
  }

  public void setIsTripOutputEvent(boolean tripOutputEvent) {
    isTripOutputEvent = tripOutputEvent;
  }

  public Timestamp getEventTimestamp() {
    return eventTimestamp;
  }

  public void setEventTimestamp(Timestamp eventTimestamp) {
    this.eventTimestamp = eventTimestamp;
  }

  @Override
  public boolean equals(Object object) {
    if (object instanceof EtaRelativeChangeOutputEvent that) {
      return Objects.equals(this.fleetEvent, that.fleetEvent)
          && Objects.equals(this.newEta, that.newEta)
          && Objects.equals(this.originalDuration, that.originalDuration)
          && Objects.equals(this.originalEta, that.originalEta)
          && Objects.equals(this.percentDurationChange, that.percentDurationChange)
          && Objects.equals(this.thresholdPercent, that.thresholdPercent)
          && Objects.equals(this.identifier, that.identifier)
          && Objects.equals(this.isTripOutputEvent, that.isTripOutputEvent)
          && Objects.equals(this.eventTimestamp, that.eventTimestamp)
          && Objects.equals(this.type, that.type);
    }
    return false;
  }
}
