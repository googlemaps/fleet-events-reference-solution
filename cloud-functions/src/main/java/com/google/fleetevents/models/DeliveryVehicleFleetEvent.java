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

import com.google.auto.value.AutoValue;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;

/**
 * Builder class for the delivery vehicle fleet event.
 */
@AutoValue
public abstract class DeliveryVehicleFleetEvent implements FleetEvent, Serializable {

  public static Builder builder() {
    return new AutoValue_DeliveryVehicleFleetEvent.Builder().setVehicleDifferences(new HashMap<>());
  }

  public abstract Builder toBuilder();

  @Override
  public FleetEvent.Type getEventType() {
    return Type.DELIVERY_VEHICLE_FLEET_EVENT;
  }

  public abstract String deliveryVehicleId();

  @Nullable
  public abstract DeliveryVehicleData oldDeliveryVehicle();

  @Nullable
  public abstract DeliveryVehicleData newDeliveryVehicle();

  public abstract Map<String, Change> vehicleDifferences();

  @AutoValue.Builder
  public abstract static class Builder {

    public abstract Builder setDeliveryVehicleId(String deliveryVehicleId);

    public abstract Builder setOldDeliveryVehicle(DeliveryVehicleData oldDeliveryVehicleData);

    public abstract Builder setNewDeliveryVehicle(DeliveryVehicleData newDeliveryVehicleData);

    public abstract Builder setVehicleDifferences(Map<String, Change> vehicleDifferences);

    public abstract DeliveryVehicleFleetEvent build();
  }
}
