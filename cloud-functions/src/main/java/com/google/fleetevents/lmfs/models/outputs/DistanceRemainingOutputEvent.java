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

package com.google.fleetevents.lmfs.models.outputs;

import java.util.Objects;

/** OutputEvent for remaining distance. */
public class DistanceRemainingOutputEvent extends OutputEvent {

  private long oldDistanceRemainingMeters;
  private String taskId;
  private long newDistanceRemainingMeters;
  private long eventTimestamp;

  public DistanceRemainingOutputEvent() {
    this.type = Type.DISTANCE_REMAINING;
  }

  public long getEventTimestamp() {
    return eventTimestamp;
  }

  public void setEventTimestamp(long eventTimestamp) {
    this.eventTimestamp = eventTimestamp;
  }

  public long getOldDistanceRemainingMeters() {
    return oldDistanceRemainingMeters;
  }

  public void setOldDistanceRemainingMeters(long oldDistanceRemainingMeters) {
    this.oldDistanceRemainingMeters = oldDistanceRemainingMeters;
  }

  public String getTaskId() {
    return taskId;
  }

  public void setTaskId(String taskId) {
    this.taskId = taskId;
  }

  public long getNewDistanceRemainingMeters() {
    return newDistanceRemainingMeters;
  }

  public void setNewDistanceRemainingMeters(long newDistanceRemainingMeters) {
    this.newDistanceRemainingMeters = newDistanceRemainingMeters;
  }

  @Override
  public String toString() {
    return "DistanceRemainingOutputEvent{"
        + "oldDistanceRemainingMeters="
        + oldDistanceRemainingMeters
        + ", taskId='"
        + taskId
        + '\''
        + ", newDistanceRemainingMeters="
        + newDistanceRemainingMeters
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
      return Objects.equals(that.taskId, this.taskId)
          && Objects.equals(that.oldDistanceRemainingMeters, this.oldDistanceRemainingMeters)
          && Objects.equals(that.newDistanceRemainingMeters, this.newDistanceRemainingMeters)
          && Objects.equals(that.eventTimestamp, this.eventTimestamp)
          && Objects.equals(that.getFleetEvent(), this.getFleetEvent());
    }
    return false;
  }
}
