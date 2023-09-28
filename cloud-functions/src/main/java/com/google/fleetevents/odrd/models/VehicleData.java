package com.google.fleetevents.odrd.models;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.GeoPoint;
import com.google.fleetevents.common.util.NameFormatter;
import com.google.fleetevents.common.util.TimeUtil;
import google.maps.fleetengine.v1.Vehicle;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Class to represent minimal information about a vehicle. */
public class VehicleData implements Serializable {
  private String vehicleId;
  private String name;

  private List<String> tripIds;
  private GeoPoint lastLocation;
  private String navigationStatus;
  private String state;
  private Long remainingDistanceMeters;
  private Long remainingDuration;
  private Timestamp eta;
  private Map<String, Object> eventMetadata;

  private Timestamp eventTimestamp;
  private Timestamp expireAt;

  VehicleData() {}

  VehicleData(VehicleData vehicleData) {
    this.vehicleId = vehicleData.vehicleId;
    this.name = vehicleData.name;
    this.tripIds = vehicleData.tripIds;
    this.lastLocation = vehicleData.lastLocation;
    this.navigationStatus = vehicleData.navigationStatus;
    this.state = vehicleData.state;
    this.eta = vehicleData.eta;
    this.remainingDistanceMeters = vehicleData.remainingDistanceMeters;
    this.remainingDuration = vehicleData.remainingDuration;
    this.eventMetadata = vehicleData.eventMetadata;
    this.eventTimestamp = vehicleData.eventTimestamp;
    this.expireAt = vehicleData.expireAt;
  }

  public String getVehicleId() {
    return vehicleId;
  }

  public String getName() {
    return name;
  }

  public List<String> getTripIds() {
    return tripIds;
  }

  public GeoPoint getLastLocation() {
    return lastLocation;
  }

  public String getNavigationStatus() {
    return navigationStatus;
  }

  public String getState() {
    return state;
  }

  public Timestamp getEta() {
    return eta;
  }

  public Long getRemainingDistanceMeters() {
    return remainingDistanceMeters;
  }

  public Long getRemainingDuration() {
    return remainingDuration;
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

  public static VehicleData fromVehicle(Vehicle vehicle, Timestamp eventTimestamp) {
    var vehicleId = NameFormatter.getIdFromName(vehicle.getName());
    var lastLocation = vehicle.getLastLocation().getLocation();
    var vehicleBuilder =
        VehicleData.builder()
            .setVehicleId(vehicleId)
            .setName(vehicle.getName())
            .setLastLocation(new GeoPoint(lastLocation.getLatitude(), lastLocation.getLongitude()))
            .setNavigationStatus(vehicle.getNavigationStatus().toString())
            .setEventTimestamp(eventTimestamp)
            .setExpireAt(TimeUtil.offsetFromNow(TimeUtil.ONE_HOUR_IN_SECONDS));
    if (vehicle.getCurrentTripsCount() < 0) {
      vehicleBuilder.setTripIds(vehicle.getCurrentTripsList().stream().toList());
    }
    return vehicleBuilder.build();
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof VehicleData that) {
      return Objects.equals(vehicleId, that.vehicleId)
          && Objects.equals(name, that.name)
          && Objects.equals(tripIds, that.tripIds)
          && Objects.equals(lastLocation, that.lastLocation)
          && Objects.equals(navigationStatus, that.navigationStatus)
          && Objects.equals(state, that.state)
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
    return "VehicleData{\n"
        + "vehicleId="
        + vehicleId
        + ",\n"
        + "name="
        + name
        + ",\n"
        + "tripIds="
        + tripIds
        + ",\n"
        + "lastLocation="
        + lastLocation
        + ",\n"
        + "navigationStatus="
        + navigationStatus
        + ",\n"
        + "state="
        + state
        + ",\n"
        + "remainingDistanceMeters="
        + remainingDistanceMeters
        + ",\n"
        + "remainingDuration="
        + remainingDuration
        + ",\n"
        + "eta="
        + eta
        + ",\n"
        + "eventMetadata="
        + eventMetadata
        + ",\n"
        + ",\n"
        + "eventTimestamp="
        + eventTimestamp
        + ",\n"
        + "expireAt="
        + expireAt
        + "\n}";
  }

  public static Builder builder() {
    return new Builder()
        .setEventMetadata(new HashMap<>())
        .setState("STATE_UNSPECIFIED")
        .setNavigationStatus("UNKNOWN_NAVIGATION_STATUS");
  }

  public Builder toBuilder() {
    return new Builder(this);
  }

  public static class Builder {

    VehicleData vehicleData;

    Builder() {
      this.vehicleData = new VehicleData();
    }

    Builder(VehicleData vehicleData) {
      this.vehicleData = new VehicleData(vehicleData);
    }

    public Builder setVehicleId(String vehicleId) {
      this.vehicleData.vehicleId = vehicleId;
      return this;
    }

    public Builder setName(String name) {
      this.vehicleData.name = name;
      return this;
    }

    public Builder setTripIds(List<String> tripIds) {
      this.vehicleData.tripIds = tripIds;
      return this;
    }

    public Builder setLastLocation(GeoPoint lastLocation) {
      this.vehicleData.lastLocation = lastLocation;
      return this;
    }

    public Builder setNavigationStatus(String navigationStatus) {
      this.vehicleData.navigationStatus = navigationStatus;
      return this;
    }

    public Builder setState(String state) {
      this.vehicleData.state = state;
      return this;
    }

    public Builder setEta(Timestamp eta) {
      this.vehicleData.eta = eta;
      return this;
    }

    public Builder setRemainingDistanceMeters(Long remainingDistanceMeters) {
      this.vehicleData.remainingDistanceMeters = remainingDistanceMeters;
      return this;
    }

    public Builder setRemainingDuration(Long remainingDuration) {
      this.vehicleData.remainingDuration = remainingDuration;
      return this;
    }

    public Builder setEventMetadata(Map<String, Object> eventMetadata) {
      this.vehicleData.eventMetadata = eventMetadata;
      return this;
    }

    public Builder setEventTimestamp(Timestamp eventTimestamp) {
      this.vehicleData.eventTimestamp = eventTimestamp;
      return this;
    }

    public Builder setExpireAt(Timestamp expireAt) {
      this.vehicleData.expireAt = expireAt;
      return this;
    }

    public VehicleData build() {
      return new VehicleData(this.vehicleData);
    }
  }
}
