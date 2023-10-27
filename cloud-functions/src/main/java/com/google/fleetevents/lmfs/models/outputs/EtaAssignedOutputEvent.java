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

import com.google.fleetevents.common.models.FleetEvent;
import com.google.fleetevents.common.models.OutputEvent;
import java.util.Objects;

/** Output event for when an eta is assigned to a task. */
public class EtaAssignedOutputEvent extends OutputEvent {
  // Epoch time in milliseconds.
  private Long assignedEta;

  // Task duration in milliseconds.
  private Long assignedDuration;

  private String taskId;

  public EtaAssignedOutputEvent() {
    type = Type.ETA_ASSIGNED;
  }

  public Long getAssignedEta() {
    return this.assignedEta;
  }

  public void setAssignedEta(Long assignedEta) {
    this.assignedEta = assignedEta;
  }

  public String getTaskId() {
    return this.taskId;
  }

  public void setTaskId(String taskId) {
    this.taskId = taskId;
  }

  public Long getAssignedDuration() {
    return assignedDuration;
  }

  public void setAssignedDuration(Long assignedDuration) {
    this.assignedDuration = assignedDuration;
  }

  @Override
  public String toString() {
    return "EtaAssignedOutputEvent{"
        + "assignedEta="
        + assignedEta
        + ", assignedDuration="
        + assignedDuration
        + ", taskId='"
        + taskId
        + '\''
        + ", fleetEvent="
        + fleetEvent
        + ", type="
        + type
        + '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof EtaAssignedOutputEvent that)) {
      return false;
    }
    return type == that.type
        && Objects.equals(assignedEta, that.assignedEta)
        && Objects.equals(assignedDuration, that.assignedDuration)
        && Objects.equals(taskId, that.taskId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(type, assignedEta, assignedDuration, taskId);
  }

  public static class Builder {

    private Type type;
    private Long assignedEta;
    private Long assignedDuration;
    private String taskId;
    private FleetEvent fleetEvent;

    public Builder(EtaAssignedOutputEvent etaAssignedOutputEvent) {
      this.type = etaAssignedOutputEvent.type;
      this.assignedEta = etaAssignedOutputEvent.assignedEta;
      this.taskId = etaAssignedOutputEvent.taskId;
      this.fleetEvent = etaAssignedOutputEvent.getFleetEvent();
      this.assignedDuration = etaAssignedOutputEvent.assignedDuration;
    }

    public Builder() {}

    public EtaAssignedOutputEvent.Builder setAssignedEta(Long assignedEta) {
      this.assignedEta = assignedEta;
      return this;
    }

    public EtaAssignedOutputEvent.Builder setAssignedDuration(Long assignedDuration) {
      this.assignedDuration = assignedDuration;
      return this;
    }

    public EtaAssignedOutputEvent.Builder setTaskId(String taskId) {
      this.taskId = taskId;
      return this;
    }

    public EtaAssignedOutputEvent.Builder setFleetEvent(FleetEvent fleetEvent) {
      this.fleetEvent = fleetEvent;
      return this;
    }

    public EtaAssignedOutputEvent build() {
      EtaAssignedOutputEvent etaAssignedOutputEvent = new EtaAssignedOutputEvent();
      etaAssignedOutputEvent.setAssignedEta(this.assignedEta);
      etaAssignedOutputEvent.setTaskId(this.taskId);
      etaAssignedOutputEvent.setFleetEvent(this.fleetEvent);
      etaAssignedOutputEvent.setAssignedDuration(this.assignedDuration);
      return etaAssignedOutputEvent;
    }
  }
}
