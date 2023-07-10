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

package com.google.fleetevents.models;

import com.google.cloud.Timestamp;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Objects;

/*
 * Represents a minimal information view of a delivery vehicle, containing information about the
 * vehicle id and task ids.
 */
public class DeliveryTaskData implements Serializable {

  // Timestamp in milliseconds when Fleet Engine writes the RPC to Cloud Logs.
  // We consider this to be the event timestamp.
  private Long eventTimestamp;
  private String deliveryVehicleId;
  private String deliveryTaskId;
  private String name;
  private String state;
  private String taskOutcome;
  private String trackingId;
  private HashMap<String, Object> eventMetadata;

  private Timestamp expireAt;

  private DeliveryTaskData() {}

  private DeliveryTaskData(DeliveryTaskData deliveryTaskData) {
    this.eventTimestamp = deliveryTaskData.eventTimestamp;
    this.deliveryVehicleId = deliveryTaskData.deliveryVehicleId;
    this.deliveryTaskId = deliveryTaskData.deliveryTaskId;
    this.name = deliveryTaskData.name;
    this.state = deliveryTaskData.state;
    this.taskOutcome = deliveryTaskData.taskOutcome;
    this.trackingId = deliveryTaskData.trackingId;
    this.eventMetadata = deliveryTaskData.eventMetadata;
    this.expireAt = deliveryTaskData.expireAt;
  }

  public static Builder builder() {
    return new DeliveryTaskData.Builder()
        .setEventMetadata(new HashMap<>())
        .setState("STATE_UNSPECIFIED")
        .setTaskOutcome("TASK_OUTCOME_UNSPECIFIED");
  }

  public Timestamp getExpireAt() {
    return expireAt;
  }

  public Builder toBuilder() {
    return new Builder(this);
  }

  public Long getEventTimestamp() {
    return eventTimestamp;
  }

  public String getDeliveryVehicleId() {
    return deliveryVehicleId;
  }

  public String getDeliveryTaskId() {
    return deliveryTaskId;
  }

  public String getName() {
    return name;
  }

  public String getState() {
    return state;
  }

  public String getTaskOutcome() {
    return taskOutcome;
  }

  public String getTrackingId() {
    return trackingId;
  }

  public HashMap<String, Object> getEventMetadata() {
    return eventMetadata;
  }

  @Override
  public String toString() {
    return "DeliveryTaskData{"
        + "eventTimestamp="
        + eventTimestamp
        + ", deliveryVehicleId='"
        + deliveryVehicleId
        + '\''
        + ", deliveryTaskId='"
        + deliveryTaskId
        + '\''
        + ", name='"
        + name
        + '\''
        + ", state='"
        + state
        + '\''
        + ", taskOutcome='"
        + taskOutcome
        + '\''
        + ", trackingId='"
        + trackingId
        + '\''
        + ", eventMetadata="
        + eventMetadata
        + ", expireAt="
        + expireAt
        + '}';
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof DeliveryTaskData that) {
      return (Objects.equals(that.eventTimestamp, this.eventTimestamp))
          && (Objects.equals(that.deliveryTaskId, this.deliveryTaskId))
          && (Objects.equals(that.name, this.name))
          && (Objects.equals(that.state, this.state))
          && (Objects.equals(that.taskOutcome, this.taskOutcome))
          && (Objects.equals(that.trackingId, this.trackingId))
          && (that.eventMetadata.equals(this.eventMetadata))
          && (Objects.equals(that.expireAt, this.expireAt));
    }
    return false;
  }

  public static class Builder {

    private final DeliveryTaskData deliveryTaskData;

    Builder() {
      deliveryTaskData = new DeliveryTaskData();
    }

    Builder(DeliveryTaskData deliveryTaskData) {
      this.deliveryTaskData = new DeliveryTaskData(deliveryTaskData);
    }

    public Builder setDeliveryVehicleId(String deliveryVehicleId) {
      deliveryTaskData.deliveryVehicleId = deliveryVehicleId;
      return this;
    }

    public Builder setDeliveryTaskId(String deliveryTaskId) {
      deliveryTaskData.deliveryTaskId = deliveryTaskId;
      return this;
    }

    public Builder setName(String name) {
      deliveryTaskData.name = name;
      return this;
    }

    public Builder setState(String state) {
      deliveryTaskData.state = state;
      return this;
    }

    public Builder setTaskOutcome(String taskOutcome) {
      deliveryTaskData.taskOutcome = taskOutcome;
      return this;
    }

    public Builder setTrackingId(String trackingId) {
      deliveryTaskData.trackingId = trackingId;
      return this;
    }

    public Builder setEventMetadata(HashMap<String, Object> eventMetadata) {
      deliveryTaskData.eventMetadata = eventMetadata;
      return this;
    }

    public Builder setEventTimestamp(Long eventTimestamp) {
      deliveryTaskData.eventTimestamp = eventTimestamp;
      return this;
    }

    public Builder setExpireAt(Timestamp expireAt) {
      this.deliveryTaskData.expireAt = expireAt;
      return this;
    }

    public DeliveryTaskData build() {
      return new DeliveryTaskData(deliveryTaskData);
    }
  }
}
