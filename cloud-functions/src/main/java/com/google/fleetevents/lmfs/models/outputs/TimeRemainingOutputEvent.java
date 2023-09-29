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

import com.google.fleetevents.common.models.OutputEvent;
import java.util.Objects;

/** OutputEvent for remaining duration. */
public class TimeRemainingOutputEvent extends OutputEvent {

  private long oldTimeRemainingSeconds;
  private String taskId;
  private long newTimeRemainingSeconds;
  private long eventTimestamp;

  public TimeRemainingOutputEvent() {
    this.type = Type.TIME_REMAINING;
  }

  public long getEventTimestamp() {
    return eventTimestamp;
  }

  public void setEventTimestamp(long eventTimestamp) {
    this.eventTimestamp = eventTimestamp;
  }

  public long getOldTimeRemainingSeconds() {
    return oldTimeRemainingSeconds;
  }

  public void setOldTimeRemainingSeconds(long oldTimeRemainingSeconds) {
    this.oldTimeRemainingSeconds = oldTimeRemainingSeconds;
  }

  public String getTaskId() {
    return taskId;
  }

  public void setTaskId(String taskId) {
    this.taskId = taskId;
  }

  public long getNewTimeRemainingSeconds() {
    return newTimeRemainingSeconds;
  }

  public void setNewTimeRemainingSeconds(long newTimeRemainingSeconds) {
    this.newTimeRemainingSeconds = newTimeRemainingSeconds;
  }

  @Override
  public String toString() {
    return "TimeRemainingOutputEvent{"
        + "oldTimeRemainingSeconds="
        + oldTimeRemainingSeconds
        + ", taskId='"
        + taskId
        + '\''
        + ", newTimeRemainingSeconds="
        + newTimeRemainingSeconds
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
    if (object instanceof TimeRemainingOutputEvent that) {
      return Objects.equals(that.taskId, this.taskId)
          && Objects.equals(that.oldTimeRemainingSeconds, this.oldTimeRemainingSeconds)
          && Objects.equals(that.newTimeRemainingSeconds, this.newTimeRemainingSeconds)
          && Objects.equals(that.eventTimestamp, this.eventTimestamp)
          && Objects.equals(that.getFleetEvent(), this.getFleetEvent());
    }
    return false;
  }
}
