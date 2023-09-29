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

package com.google.fleetevents.common.models;

import java.util.Objects;

/**
 * Parent class for the output events. By default, contains the event that triggered the output
 * event.
 */
public class OutputEvent {

  protected FleetEvent fleetEvent;
  protected Type type;

  public FleetEvent getFleetEvent() {
    return fleetEvent;
  }

  public void setFleetEvent(FleetEvent fleetEvent) {
    this.fleetEvent = fleetEvent;
  }

  public Type getType() {
    return type;
  }

  public void setType(Type type) {
    this.type = type;
  }

  @Override
  public boolean equals(Object object) {
    if (object instanceof OutputEvent that) {
      return Objects.equals(that.fleetEvent, this.fleetEvent) && that.type == this.type;
    }
    return false;
  }

  @Override
  public String toString() {
    return String.format("OutputEvent{\n\tfleetEvent: %s}", fleetEvent);
  }

  public enum Type {
    TASK_OUTCOME_CHANGED,
    ETA,
    RELATIVE_ETA,
    DISTANCE_REMAINING,
    TIME_REMAINING,
    TASK_STATE_CHANGED,
    TRIP_STATUS_CHANGED
  }
}
