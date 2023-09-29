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

import com.google.auto.value.AutoValue;
import com.google.fleetevents.common.models.Change;
import com.google.fleetevents.common.models.FleetEvent;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;

/** Class to represent internal state changes for a vehicle. */
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
