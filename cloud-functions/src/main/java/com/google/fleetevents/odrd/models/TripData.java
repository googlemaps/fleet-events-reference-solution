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

package com.google.fleetevents.odrd.models;

import com.google.cloud.Timestamp;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Class to represent the minimal information needed about a trip to create alerts. */
public class TripData implements Serializable {

  private String tripId;
  private String vehicleId;
  private List<TripWaypointData> waypoints;
  private Long currentWaypointIndex;
  private Boolean isSharedTrip;

  private String tripStatus;
  private Timestamp intermediateDestinationsVersion;
  private Long remainingDistanceMeters;
  private Long remainingDuration;
  private Timestamp eta;
  private Map<String, Object> eventMetadata;
  private Timestamp eventTimestamp;
  private Timestamp expireAt;

  public String getTripId() {
    return tripId;
  }

  public String getVehicleId() {
    return vehicleId;
  }

  public List<TripWaypointData> getWaypoints() {
    return waypoints;
  }

  public Long getCurrentWaypointIndex() {
    return currentWaypointIndex;
  }

  public Boolean getIsSharedTrip() {
    return isSharedTrip;
  }

  public String getTripStatus() {
    return tripStatus;
  }

  public Timestamp getIntermediateDestinationsVersion() {
    return intermediateDestinationsVersion;
  }

  public Long getRemainingDistanceMeters() {
    return remainingDistanceMeters;
  }

  public Long getRemainingDuration() {
    return remainingDuration;
  }

  public Timestamp getEta() {
    return eta;
  }

  public Map<String, Object> getEventMetadata() {
    return eventMetadata;
  }

  public Timestamp getEventTimestamp() {
    return eventTimestamp;
  }

  public Timestamp getExpireAt() {
    return expireAt;
  }

  TripData() {}

  TripData(TripData tripData) {
    this.tripId = tripData.tripId;
    this.vehicleId = tripData.vehicleId;
    this.waypoints = tripData.waypoints;
    this.currentWaypointIndex = tripData.getCurrentWaypointIndex();
    this.isSharedTrip = tripData.isSharedTrip;
    this.tripStatus = tripData.tripStatus;
    this.intermediateDestinationsVersion = tripData.intermediateDestinationsVersion;
    this.remainingDistanceMeters = tripData.remainingDistanceMeters;
    this.remainingDuration = tripData.remainingDuration;
    this.eta = tripData.eta;
    this.eventMetadata = tripData.eventMetadata;
    this.eventTimestamp = tripData.eventTimestamp;
    this.expireAt = tripData.expireAt;
  }

  public static Builder builder() {
    return new Builder()
        .setEventMetadata(new HashMap<>())
        .setWaypoints(new ArrayList<>())
        .setIsSharedTrip(false);
  }

  public Builder toBuilder() {
    return new Builder(this);
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof TripData that) {
      return Objects.equals(tripId, that.tripId)
          && Objects.equals(vehicleId, that.vehicleId)
          && Objects.equals(waypoints, that.waypoints)
          && Objects.equals(currentWaypointIndex, that.currentWaypointIndex)
          && Objects.equals(isSharedTrip, that.isSharedTrip)
          && Objects.equals(tripStatus, that.tripStatus)
          && Objects.equals(intermediateDestinationsVersion, that.intermediateDestinationsVersion)
          && Objects.equals(remainingDistanceMeters, that.remainingDistanceMeters)
          && Objects.equals(remainingDuration, that.remainingDuration)
          && Objects.equals(eta, that.eta)
          && Objects.equals(eventMetadata, that.eventMetadata)
          && Objects.equals(eventTimestamp, that.eventTimestamp)
          && Objects.equals(expireAt, that.expireAt);
    }
    return false;
  }

  @Override
  public String toString() {
    return "TripData {"
        + "\ntripId="
        + tripId
        + ",\nvehicleId="
        + vehicleId
        + ",\nwaypointIds="
        + waypoints
        + ",\ncurrentWaypointIndex="
        + currentWaypointIndex
        + ",\nisSharedTrip="
        + isSharedTrip
        + ",\ntripStatus="
        + tripStatus
        + ",\nintermediateDestinationsVersion="
        + intermediateDestinationsVersion
        + ",\nremainingDistanceMeters="
        + remainingDistanceMeters
        + ",\nremainingDuration="
        + remainingDuration
        + ",\neta="
        + eta
        + ",\neventMetadata="
        + eventMetadata
        + ",\neventTimestamp="
        + eventTimestamp
        + ",\nexpireAt="
        + expireAt
        + "\n}";
  }

  public static class Builder {
    TripData tripData;

    Builder() {
      tripData = new TripData();
    }

    Builder(TripData tripData) {
      this.tripData = new TripData(tripData);
    }

    public TripData build() {
      return new TripData(tripData);
    }

    public Builder setTripId(String tripId) {
      this.tripData.tripId = tripId;
      return this;
    }

    public Builder setVehicleId(String vehicleId) {
      this.tripData.vehicleId = vehicleId;
      return this;
    }

    public Builder setWaypoints(List<TripWaypointData> waypoints) {
      this.tripData.waypoints = waypoints;
      return this;
    }

    public Builder setCurrentWaypointIndex(Long currentWaypointIndex) {
      this.tripData.currentWaypointIndex = currentWaypointIndex;
      return this;
    }

    public Builder setIsSharedTrip(Boolean sharedTrip) {
      this.tripData.isSharedTrip = sharedTrip;
      return this;
    }

    public Builder setTripStatus(String tripStatus) {
      this.tripData.tripStatus = tripStatus;
      return this;
    }

    public Builder setIntermediateDestinationsVersion(Timestamp intermediateDestinationsVersion) {
      this.tripData.intermediateDestinationsVersion = intermediateDestinationsVersion;
      return this;
    }

    public Builder setRemainingDistanceMeters(Long remainingDistanceMeters) {
      this.tripData.remainingDistanceMeters = remainingDistanceMeters;
      return this;
    }

    public Builder setRemainingDuration(Long remainingDuration) {
      this.tripData.remainingDuration = remainingDuration;
      return this;
    }

    public Builder setEta(Timestamp eta) {
      this.tripData.eta = eta;
      return this;
    }

    public Builder setEventMetadata(Map<String, Object> eventMetadata) {
      this.tripData.eventMetadata = eventMetadata;
      return this;
    }

    public Builder setEventTimestamp(Timestamp eventTimestamp) {
      this.tripData.eventTimestamp = eventTimestamp;
      return this;
    }

    public Builder setExpireAt(Timestamp expireAt) {
      this.tripData.expireAt = expireAt;
      return this;
    }
  }
}
