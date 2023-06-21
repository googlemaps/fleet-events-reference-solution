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

package com.google.fleetevents.models.outputs;

import com.google.fleetevents.models.FleetEvent;
import java.util.Objects;

/**
 * OutputEvent for eta change.
 */
public class EtaOutputEvent extends OutputEvent {
  // Epoch time in milliseconds.
  private Long originalEta;

  // Task duration in milliseconds.
  private Long originalDuration;

  // Epoch time in milliseconds.
  private Long newEta;

  // Duration in milliseconds.
  // Percent change in duration compared to original eta.
  private Long delta;

  // Percent change in duration compared to original delta.
  private Float relativeDelta;
  private String taskId;

  public EtaOutputEvent() {
  }

  public Long getOriginalEta() {
    return this.originalEta;
  }

  public void setOriginalEta(Long originalEta) {
    this.originalEta = originalEta;
  }

  public Long getNewEta() {
    return this.newEta;
  }

  public void setNewEta(Long newEta) {
    this.newEta = newEta;
  }

  public Long getDelta() {
    return this.delta;
  }

  // Delta in seconds
  public void setDelta(Long delta) {
    this.delta = delta;
  }

  public String getTaskId() {
    return this.taskId;
  }

  public void setTaskId(String taskId) {
    this.taskId = taskId;
  }

  public Float getRelativeDelta() {
    return relativeDelta;
  }

  public void setRelativeDelta(Float relativeDelta) {
    this.relativeDelta = relativeDelta;
  }

  public Long getOriginalDuration() {
    return originalDuration;
  }

  public void setOriginalDuration(Long originalDuration) {
    this.originalDuration = originalDuration;
  }

  @Override
  public String toString() {
    return "EtaOutputEvent{" +
            "originalEta=" + originalEta +
            ", originalDuration=" + originalDuration +
            ", newEta=" + newEta +
            ", delta=" + delta +
            ", relativeDelta=" + relativeDelta +
            ", taskId='" + taskId + '\'' +
            ", fleetEvent=" + fleetEvent +
            ", type=" + type +
            '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof EtaOutputEvent)) {
      return false;
    }
    EtaOutputEvent that = (EtaOutputEvent) o;
    return type == that.type
        && Objects.equals(originalEta, that.originalEta)
        && Objects.equals(originalDuration, that.originalDuration)
        && Objects.equals(newEta, that.newEta)
        && Objects.equals(delta, that.delta)
        && Objects.equals(relativeDelta, that.relativeDelta)
        && Objects.equals(taskId, that.taskId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(type, originalEta, originalDuration, newEta, delta, relativeDelta, taskId);
  }

  public static class Builder {

    private Type type;
    private Long originalEta;
    private Long originalDuration;
    private Long newEta;
    private Long delta;
    private Float relativeDelta;
    private String taskId;
    private FleetEvent fleetEvent;


    public Builder(EtaOutputEvent etaOutputEvent) {
      this.type = etaOutputEvent.type;
      this.originalEta = etaOutputEvent.originalEta;
      this.newEta = etaOutputEvent.newEta;
      this.delta = etaOutputEvent.delta;
      this.relativeDelta = etaOutputEvent.relativeDelta;
      this.taskId = etaOutputEvent.taskId;
      this.fleetEvent = etaOutputEvent.getFleetEvent();
      this.originalDuration = etaOutputEvent.originalDuration;
    }

    public Builder() {
    }

    public Builder setType(Type type) {
      this.type = type;
      return this;
    }

    public Builder setOriginalEta(Long originalEta) {
      this.originalEta = originalEta;
      return this;
    }

    public Builder setOriginalDuration(Long originalDuration) {
      this.originalDuration = originalDuration;
      return this;
    }

    public Builder setNewEta(Long newEta) {
      this.newEta = newEta;
      return this;
    }

    public Builder setDelta(Long delta) {
      this.delta = delta;
      return this;
    }

    public Builder setRelativeDelta(Float relativeDelta) {
      this.relativeDelta = relativeDelta;
      return this;
    }

    public Builder setTaskId(String taskId) {
      this.taskId = taskId;
      return this;
    }

    public Builder setFleetEvent(FleetEvent fleetEvent) {
      this.fleetEvent = fleetEvent;
      return this;
    }

    public EtaOutputEvent build() {
      EtaOutputEvent etaOutputEvent = new EtaOutputEvent();
      etaOutputEvent.setType(this.type);
      etaOutputEvent.setOriginalEta(this.originalEta);
      etaOutputEvent.setNewEta(this.newEta);
      etaOutputEvent.setDelta(this.delta);
      etaOutputEvent.setTaskId(this.taskId);
      etaOutputEvent.setFleetEvent(this.fleetEvent);
      etaOutputEvent.setRelativeDelta(this.relativeDelta);
      etaOutputEvent.setOriginalDuration(this.originalDuration);
      return etaOutputEvent;
    }
  }
}
