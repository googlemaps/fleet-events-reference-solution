package com.google.fleetevents.odrd.models;

import com.google.auto.value.AutoValue;
import com.google.fleetevents.common.models.Change;
import com.google.fleetevents.common.models.FleetEvent;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;

@AutoValue
public abstract class VehicleFleetEvent implements FleetEvent, Serializable {
  public abstract String vehicleId();

  @Nullable
  public abstract VehicleData oldVehicle();

  @Nullable
  public abstract VehicleData newVehicle();

  public abstract Map<String, Change> vehicleDifferences();

  public static Builder builder() {
    return new AutoValue_VehicleFleetEvent.Builder().setVehicleDifferences(new HashMap<>());
  }

  public abstract Builder toBuilder();

  @AutoValue.Builder
  public abstract static class Builder {
    public abstract VehicleFleetEvent build();

    public abstract Builder setVehicleId(String vehicleId);

    public abstract Builder setOldVehicle(VehicleData oldVehicle);

    public abstract Builder setNewVehicle(VehicleData newVehicle);

    public abstract Builder setVehicleDifferences(Map<String, Change> vehicleDifferences);
  }

  @Override
  public Type getEventType() {
    return Type.VEHICLE_FLEET_EVENT;
  }
}
