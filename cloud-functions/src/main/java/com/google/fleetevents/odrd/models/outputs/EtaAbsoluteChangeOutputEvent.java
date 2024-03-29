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
 * OutputEvent for absolute eta change handler for a trip/waypoint. When waypoints in a trip are
 * detected as changed, metadata for the eta is reset.
 */
public class EtaAbsoluteChangeOutputEvent extends OutputEvent {

  private Timestamp originalEta;
  private Timestamp newEta;
  private long thresholdMilliseconds;

  private boolean isTripOutputEvent;

  private Timestamp eventTimestamp;

  public String getIdentifier() {
    return identifier;
  }

  public void setIdentifier(String identifier) {
    this.identifier = identifier;
  }

  private String identifier;

  public EtaAbsoluteChangeOutputEvent() {
    type = Type.ETA;
  }

  public Timestamp getOriginalEta() {
    return originalEta;
  }

  public void setOriginalEta(Timestamp originalEta) {
    this.originalEta = originalEta;
  }

  public Timestamp getNewEta() {
    return newEta;
  }

  public void setNewEta(Timestamp newEta) {
    this.newEta = newEta;
  }

  public long getThresholdMilliseconds() {
    return thresholdMilliseconds;
  }

  public void setThresholdMilliseconds(long thresholdMilliseconds) {
    this.thresholdMilliseconds = thresholdMilliseconds;
  }

  public boolean getIsTripOutputEvent() {
    return isTripOutputEvent;
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
    if (object instanceof EtaAbsoluteChangeOutputEvent that) {
      return Objects.equals(this.fleetEvent, that.fleetEvent)
          && Objects.equals(this.newEta, that.newEta)
          && Objects.equals(this.originalEta, that.originalEta)
          && Objects.equals(this.thresholdMilliseconds, that.thresholdMilliseconds)
          && Objects.equals(this.identifier, that.identifier)
          && Objects.equals(this.eventTimestamp, that.eventTimestamp)
          && Objects.equals(this.type, that.type);
    }
    return false;
  }

  @Override
  public String toString() {
    return "EtaAbsoluteChangeOutputEvent{"
        + "originalEta="
        + originalEta
        + ", newEta="
        + newEta
        + ", thresholdMiliseconds="
        + thresholdMilliseconds
        + ", triggerId="
        + identifier
        + ", eventTimestamp="
        + eventTimestamp
        + ", type="
        + type
        + '}';
  }
}
