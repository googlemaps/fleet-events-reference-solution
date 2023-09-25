package com.google.fleetevents.odrd.models;

import com.google.auto.value.AutoValue;
import com.google.fleetevents.common.models.Change;
import com.google.fleetevents.common.models.FleetEvent;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

@AutoValue
public abstract class TripFleetEvent implements FleetEvent, Serializable {
  public abstract String tripId();

  public abstract String vehicleId();

  @Nullable
  public abstract TripData oldTrip();

  @Nullable
  public abstract TripData newTrip();

  public abstract Map<String, Change> tripDifferences();

  @Nullable
  public abstract List<TripWaypointData> oldTripWaypoints();

  @Nullable
  public abstract List<TripWaypointData> newTripWaypoints();

  public abstract List<Map<String, Change>> tripWaypointDifferences();

  public static Builder builder() {
    return new AutoValue_TripFleetEvent.Builder()
        .setTripDifferences(new HashMap<>())
        .setOldTripWaypoints(new ArrayList<>())
        .setNewTripWaypoints(new ArrayList<>())
        .setTripWaypointDifferences(new ArrayList<>());
  }

  public abstract Builder toBuilder();

  @Override
  public Type getEventType() {
    return Type.TRIP_FLEET_EVENT;
  }

  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder setTripId(String tripId);

    public abstract Builder setVehicleId(String vehicleId);

    public abstract Builder setOldTrip(TripData oldTrip);

    public abstract Builder setNewTrip(TripData newTrip);

    public abstract Builder setTripDifferences(Map<String, Change> tripDifferences);

    public abstract Builder setOldTripWaypoints(List<TripWaypointData> oldTripWaypoints);

    public abstract Builder setNewTripWaypoints(List<TripWaypointData> newTripWaypoints);

    public abstract Builder setTripWaypointDifferences(
        List<Map<String, Change>> tripWaypointDifferences);

    public abstract TripFleetEvent build();
  }
}
