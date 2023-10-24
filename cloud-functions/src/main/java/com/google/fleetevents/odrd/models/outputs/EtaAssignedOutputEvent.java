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

/** Output event for when an eta is assigned to a trip/waypoint. */
public class EtaAssignedOutputEvent extends OutputEvent {

  private Timestamp assignedEta;

  private boolean isTripOutputEvent;

  private Timestamp eventTimestamp;

  public String getIdentifier() {
    return identifier;
  }

  public void setIdentifier(String identifier) {
    this.identifier = identifier;
  }

  private String identifier;

  public EtaAssignedOutputEvent() {
    type = Type.ETA;
  }

  public Timestamp getAssignedEta() {
    return assignedEta;
  }

  public void setAssignedEta(Timestamp assignedEta) {
    this.assignedEta = assignedEta;
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
    if (object instanceof EtaAssignedOutputEvent that) {
      return Objects.equals(this.fleetEvent, that.fleetEvent)
          && Objects.equals(this.assignedEta, that.assignedEta)
          && Objects.equals(this.identifier, that.identifier)
          && Objects.equals(this.eventTimestamp, that.eventTimestamp)
          && Objects.equals(this.type, that.type);
    }
    return false;
  }

  @Override
  public String toString() {
    return "EtaAssignedOutputEvent{"
        + "originalEta="
        + assignedEta
        + ", triggerId="
        + identifier
        + ", eventTimestamp="
        + eventTimestamp
        + ", type="
        + type
        + '}';
  }
}
