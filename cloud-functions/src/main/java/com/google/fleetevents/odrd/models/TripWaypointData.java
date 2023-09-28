package com.google.fleetevents.odrd.models;

import com.google.cloud.Timestamp;
import com.google.fleetevents.common.util.Constants;
import com.google.fleetevents.common.util.NameFormatter;
import google.maps.fleetengine.v1.TerminalLocation;
import google.maps.fleetengine.v1.Trip;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Class to represent minimal information for a trip waypoint. */
public class TripWaypointData implements Serializable {
  private String vehicleId;
  private String tripId;
  private String waypointId;
  private Long s2CellId;
  private Long waypointIndex;
  private boolean isTerminal;
  private Timestamp eta;
  private Long remainingDistanceMeters;
  private Long remainingDuration;
  private Map<String, Object> eventMetadata;
  private TripWaypointType tripWaypointType;

  TripWaypointData() {}

  TripWaypointData(TripWaypointData tripWaypointData) {
    this.vehicleId = tripWaypointData.vehicleId;
    this.tripId = tripWaypointData.tripId;
    this.waypointId = tripWaypointData.waypointId;
    this.s2CellId = tripWaypointData.s2CellId;
    this.waypointIndex = tripWaypointData.waypointIndex;
    this.isTerminal = tripWaypointData.isTerminal;
    this.eta = tripWaypointData.eta;
    this.remainingDistanceMeters = tripWaypointData.remainingDistanceMeters;
    this.remainingDuration = tripWaypointData.remainingDuration;
    this.eventMetadata = tripWaypointData.eventMetadata;
    this.tripWaypointType = tripWaypointData.tripWaypointType;
  }

  public String getVehicleId() {
    return vehicleId;
  }

  public String getTripId() {
    return tripId;
  }

  public String getWaypointId() {
    return waypointId;
  }

  public Long getS2CellId() {
    return s2CellId;
  }

  public Long getWaypointIndex() {
    return waypointIndex;
  }

  public boolean getIsTerminal() {
    return isTerminal;
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

  public TripWaypointType getTripWaypointType() {
    return tripWaypointType;
  }

  public Map<String, Object> getEventMetadata() {
    return eventMetadata;
  }

  public static Builder builder() {
    return new Builder().setEventMetadata(new HashMap<>());
  }

  public Builder toBuilder() {
    return new Builder(this);
  }

  public static String generateWaypointId(
      String tripId, Long waypointIndex, TripWaypointType tripWaypointType) {
    if (tripWaypointType == TripWaypointType.PICKUP_WAYPOINT_TYPE) {
      return String.format("%s-%s", tripId, Constants.PICKUP_INDICATOR);
    }
    if (tripWaypointType == TripWaypointType.DROP_OFF_WAYPOINT_TYPE) {
      return String.format("%s-%s", tripId, Constants.DROP_OFF_INDICATOR);
    }
    return String.format("%s-%d", tripId, waypointIndex);
  }

  public static List<TripWaypointData> getWaypoints(
      Trip response, List<TripWaypointData> oldWaypoints) {
    var waypoints = new ArrayList<TripWaypointData>();
    var pickupPoint = response.getPickupPoint();
    var dropoffPoint = response.getDropoffPoint();
    var intermediateDestinations = response.getIntermediateDestinationsList();
    var tripId = NameFormatter.getIdFromName(response.getName());
    var vehicleId = response.getVehicleId();
    waypoints.add(
        terminalLocationToWaypointData(
            pickupPoint,
            0,
            TripWaypointType.PICKUP_WAYPOINT_TYPE,
            tripId,
            vehicleId,
            oldWaypoints.size() > 0 ? oldWaypoints.get(0) : TripWaypointData.builder().build()));

    int i = 1;

    for (var terminalLocation : intermediateDestinations) {
      TripWaypointData waypoint = TripWaypointData.builder().build();
      /* Try to match the old waypoints to the same index if the index is still there. */
      if (i < oldWaypoints.size() - 1) {
        waypoint = oldWaypoints.get(i);
      }
      waypoints.add(
          terminalLocationToWaypointData(
              terminalLocation,
              i,
              TripWaypointType.INTERMEDIATE_WAYPOINT_TYPE,
              tripId,
              vehicleId,
              waypoint));
      i += 1;
    }
    waypoints.add(
        terminalLocationToWaypointData(
            dropoffPoint,
            i,
            TripWaypointType.DROP_OFF_WAYPOINT_TYPE,
            tripId,
            vehicleId,
            waypoints.get(waypoints.size() - 1)));
    return waypoints;
  }

  public static TripWaypointData terminalLocationToWaypointData(
      TerminalLocation terminalLocation,
      long index,
      TripWaypointType tripWaypointType,
      String tripId,
      String vehicleId,
      TripWaypointData oldTripWaypointData) {
    var waypointId = TripWaypointData.generateWaypointId(tripId, index, tripWaypointType);
    return TripWaypointData.builder()
        .setWaypointId(waypointId)
        .setVehicleId(vehicleId)
        .setTripId(tripId)
        .setWaypointIndex(index)
        .setTripWaypointType(tripWaypointType)
        .setEta(oldTripWaypointData.getEta())
        .setRemainingDistanceMeters(oldTripWaypointData.getRemainingDistanceMeters())
        .setRemainingDuration(oldTripWaypointData.getRemainingDuration())
        .build();
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof TripWaypointData that) {
      return Objects.equals(vehicleId, that.vehicleId)
          && Objects.equals(tripId, that.tripId)
          && Objects.equals(waypointId, that.waypointId)
          && Objects.equals(s2CellId, that.s2CellId)
          && Objects.equals(waypointIndex, that.waypointIndex)
          && Objects.equals(isTerminal, that.isTerminal)
          && Objects.equals(eta, that.eta)
          && Objects.equals(remainingDistanceMeters, that.remainingDistanceMeters)
          && Objects.equals(remainingDuration, that.remainingDuration)
          && Objects.equals(tripWaypointType, that.tripWaypointType)
          && Objects.equals(eventMetadata, that.eventMetadata);
    }
    return false;
  }

  @Override
  public String toString() {
    return "TripWaypointData{\n"
        + "vehicleId="
        + vehicleId
        + ",\n"
        + "tripId="
        + tripId
        + ",\n"
        + "waypointId="
        + waypointId
        + ",\n"
        + "s2CellId="
        + s2CellId
        + ",\n"
        + "waypointIndex="
        + waypointIndex
        + ",\n"
        + "getIsTerminal="
        + isTerminal
        + ",\n"
        + "eta="
        + eta
        + ",\n"
        + "remainingDistanceMeters="
        + remainingDistanceMeters
        + ",\n"
        + "remainingDuration="
        + remainingDuration
        + ",\n"
        + "tripWaypointType="
        + tripWaypointType
        + ",\n"
        + "eventMetadata="
        + eventMetadata
        + "\n}";
  }

  public static class Builder {
    TripWaypointData tripWaypointData;

    Builder() {
      tripWaypointData = new TripWaypointData();
    }

    Builder(TripWaypointData tripWaypointData) {
      this.tripWaypointData = tripWaypointData;
    }

    public TripWaypointData build() {
      return new TripWaypointData(tripWaypointData);
    }

    public Builder setVehicleId(String vehicleId) {
      this.tripWaypointData.vehicleId = vehicleId;
      return this;
    }

    public Builder setTripId(String tripId) {
      this.tripWaypointData.tripId = tripId;
      return this;
    }

    public Builder setWaypointId(String waypointId) {
      this.tripWaypointData.waypointId = waypointId;
      return this;
    }

    public Builder setS2CellId(Long s2CellId) {
      this.tripWaypointData.s2CellId = s2CellId;
      return this;
    }

    public Builder setWaypointIndex(Long waypointIndex) {
      this.tripWaypointData.waypointIndex = waypointIndex;
      return this;
    }

    public Builder setIsTerminal(boolean terminal) {
      this.tripWaypointData.isTerminal = terminal;
      return this;
    }

    public Builder setEta(Timestamp eta) {
      this.tripWaypointData.eta = eta;
      return this;
    }

    public Builder setRemainingDistanceMeters(Long remainingDistanceMeters) {
      this.tripWaypointData.remainingDistanceMeters = remainingDistanceMeters;
      return this;
    }

    public Builder setRemainingDuration(Long remainingDuration) {
      this.tripWaypointData.remainingDuration = remainingDuration;
      return this;
    }

    public Builder setTripWaypointType(TripWaypointType tripWaypointType) {
      this.tripWaypointData.tripWaypointType = tripWaypointType;
      return this;
    }

    public Builder setEventMetadata(Map<String, Object> eventMetadata) {
      this.tripWaypointData.eventMetadata = eventMetadata;
      return this;
    }
  }
}
