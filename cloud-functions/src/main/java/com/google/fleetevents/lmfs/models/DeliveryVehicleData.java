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

package com.google.fleetevents.lmfs.models;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.GeoPoint;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

/**
 * Represents a minimal information view of a delivery vehicle, containing information about the
 * vehicle id and task ids.
 */
public class DeliveryVehicleData implements Serializable {

  // Timestamp in milliseconds when Fleet Engine writes the RPC to Cloud Logs.
  // We consider this to be the event timestamp.
  private Long eventTimestamp;

  // duration in milliseconds.
  private Long remainingDuration;
  private Long remainingDistanceMeters;
  private String deliveryVehicleId;
  private String name;
  private GeoPoint lastLocation;
  private String navigationStatus;
  private List<String> currentDeliveryTaskIds;
  private List<String> plannedDeliveryTaskIds;
  private List<VehicleJourneySegment> remainingVehicleJourneySegments;
  private HashMap<String, Object> eventMetadata;
  private Timestamp expireAt;

  private DeliveryVehicleData() {}

  private DeliveryVehicleData(DeliveryVehicleData deliveryVehicleData) {
    this.eventTimestamp = deliveryVehicleData.eventTimestamp;
    this.remainingDuration = deliveryVehicleData.remainingDuration;
    this.remainingDistanceMeters = deliveryVehicleData.remainingDistanceMeters;
    this.deliveryVehicleId = deliveryVehicleData.deliveryVehicleId;
    this.name = deliveryVehicleData.name;
    this.lastLocation = deliveryVehicleData.lastLocation;
    this.navigationStatus = deliveryVehicleData.navigationStatus;
    this.currentDeliveryTaskIds = deliveryVehicleData.currentDeliveryTaskIds;
    this.plannedDeliveryTaskIds = deliveryVehicleData.plannedDeliveryTaskIds;
    this.remainingVehicleJourneySegments = deliveryVehicleData.remainingVehicleJourneySegments;
    this.eventMetadata = deliveryVehicleData.eventMetadata;
    this.expireAt = deliveryVehicleData.expireAt;
  }

  public static Builder builder() {
    return new DeliveryVehicleData.Builder()
        .setCurrentDeliveryTaskIds(new ArrayList<>())
        .setPlannedDeliveryTaskIds(new ArrayList<>())
        .setRemainingVehicleJourneySegments(new ArrayList<>())
        .setNavigationStatus("UNKNOWN_NAVIGATION_STATUS")
        .setEventMetadata(new HashMap<>());
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

  public Long getRemainingDuration() {
    return remainingDuration;
  }

  public Long getRemainingDistanceMeters() {
    return remainingDistanceMeters;
  }

  public String getDeliveryVehicleId() {
    return deliveryVehicleId;
  }

  public String getName() {
    return name;
  }

  public GeoPoint getLastLocation() {
    return lastLocation;
  }

  public String getNavigationStatus() {
    return navigationStatus;
  }

  public List<String> getCurrentDeliveryTaskIds() {
    return currentDeliveryTaskIds;
  }

  public List<String> getPlannedDeliveryTaskIds() {
    return plannedDeliveryTaskIds;
  }

  public List<VehicleJourneySegment> getRemainingVehicleJourneySegments() {
    return remainingVehicleJourneySegments;
  }

  public HashMap<String, Object> getEventMetadata() {
    return eventMetadata;
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof DeliveryVehicleData that) {
      return Objects.equals(that.eventTimestamp, this.eventTimestamp)
          && Objects.equals(that.remainingDuration, this.remainingDuration)
          && Objects.equals(that.remainingDistanceMeters, this.remainingDistanceMeters)
          && Objects.equals(that.deliveryVehicleId, this.deliveryVehicleId)
          && Objects.equals(that.name, this.name)
          && Objects.equals(that.lastLocation, this.lastLocation)
          && Objects.equals(that.navigationStatus, this.navigationStatus)
          && that.currentDeliveryTaskIds.equals(this.currentDeliveryTaskIds)
          && that.plannedDeliveryTaskIds.equals(this.plannedDeliveryTaskIds)
          && that.remainingVehicleJourneySegments.equals(this.remainingVehicleJourneySegments)
          && that.eventMetadata.equals(this.eventMetadata)
          && Objects.equals(that.expireAt, this.expireAt);
    }
    return false;
  }

  @Override
  public String toString() {
    return "DeliveryVehicleData{"
        + "eventTimestamp="
        + eventTimestamp
        + ", remainingDuration="
        + remainingDuration
        + ", remainingDistanceMeters="
        + remainingDistanceMeters
        + ", deliveryVehicleId='"
        + deliveryVehicleId
        + '\''
        + ", name='"
        + name
        + '\''
        + ", lastLocation="
        + lastLocation
        + ", navigationStatus='"
        + navigationStatus
        + '\''
        + ", currentDeliveryTaskIds="
        + currentDeliveryTaskIds
        + ", plannedDeliveryTaskIds="
        + plannedDeliveryTaskIds
        + ", remainingVehicleJourneySegments="
        + remainingVehicleJourneySegments
        + ", eventMetadata="
        + eventMetadata
        + ", expireAt="
        + expireAt
        + '}';
  }

  public static class Builder {

    DeliveryVehicleData deliveryVehicleData;

    Builder() {
      this.deliveryVehicleData = new DeliveryVehicleData();
    }

    Builder(DeliveryVehicleData deliveryVehicleData) {
      this.deliveryVehicleData = new DeliveryVehicleData(deliveryVehicleData);
    }

    public Builder setEventTimestamp(Long eventTimestamp) {
      this.deliveryVehicleData.eventTimestamp = eventTimestamp;
      return this;
    }

    public Builder setDeliveryVehicleId(String deliveryVehicleId) {
      this.deliveryVehicleData.deliveryVehicleId = deliveryVehicleId;
      return this;
    }

    public Builder setName(String name) {
      this.deliveryVehicleData.name = name;
      return this;
    }

    public Builder setLastLocation(GeoPoint lastLocation) {
      this.deliveryVehicleData.lastLocation = lastLocation;
      return this;
    }

    public Builder setNavigationStatus(String navigationStatus) {
      this.deliveryVehicleData.navigationStatus = navigationStatus;
      return this;
    }

    public Builder setRemainingDistanceMeters(long remainingDistanceMeters) {
      this.deliveryVehicleData.remainingDistanceMeters = remainingDistanceMeters;
      return this;
    }

    public Builder setCurrentDeliveryTaskIds(List<String> currentDeliveryTaskIds) {
      this.deliveryVehicleData.currentDeliveryTaskIds = currentDeliveryTaskIds;
      return this;
    }

    public Builder setPlannedDeliveryTaskIds(List<String> plannedDeliveryTaskIds) {
      this.deliveryVehicleData.plannedDeliveryTaskIds = plannedDeliveryTaskIds;
      return this;
    }

    public Builder setRemainingVehicleJourneySegments(
        List<VehicleJourneySegment> remainingVehicleJourneySegments) {
      this.deliveryVehicleData.remainingVehicleJourneySegments = remainingVehicleJourneySegments;
      return this;
    }

    public Builder addRemainingVehicleJourneySegment(VehicleJourneySegment vehicleJourneySegment) {
      this.deliveryVehicleData.remainingVehicleJourneySegments.add(vehicleJourneySegment);
      return this;
    }

    public Builder setEventMetadata(HashMap<String, Object> eventMetadata) {
      this.deliveryVehicleData.eventMetadata = eventMetadata;
      return this;
    }

    public Builder setRemainingDuration(Long remainingDuration) {
      this.deliveryVehicleData.remainingDuration = remainingDuration;
      return this;
    }

    public Builder setExpireAt(Timestamp expireAt) {
      this.deliveryVehicleData.expireAt = expireAt;
      return this;
    }

    public DeliveryVehicleData build() {
      return new DeliveryVehicleData(deliveryVehicleData);
    }
  }
}
