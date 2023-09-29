// Copyright 2023 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.fleetevents.beam.model.output;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.fleetevents.beam.util.ProtobufSerializer;
import google.maps.fleetengine.delivery.v1.DeliveryVehicle;
import java.io.Serializable;

public class VehicleNotUpdatingOutputEvent extends OutputEvent implements Serializable {
  private long firstUpdateTime;
  private long lastUpdateTime;
  private int gapDuration;

  @JsonSerialize(using = ProtobufSerializer.class)
  private DeliveryVehicle deliveryVehicle;

  public VehicleNotUpdatingOutputEvent() {
    this.outputType = OutputType.VEHICLE_NOT_UPDATING_OUTPUT;
  }

  public long getFirstUpdateTime() {
    return firstUpdateTime;
  }

  public void setFirstUpdateTime(long firstUpdateTime) {
    this.firstUpdateTime = firstUpdateTime;
  }

  public long getLastUpdateTime() {
    return lastUpdateTime;
  }

  public void setLastUpdateTime(long lastUpdateTime) {
    this.lastUpdateTime = lastUpdateTime;
  }

  public int getGapDuration() {
    return gapDuration;
  }

  public void setGapDuration(int gapDuration) {
    this.gapDuration = gapDuration;
  }

  public DeliveryVehicle getDeliveryVehicle() {
    return deliveryVehicle;
  }

  public void setDeliveryVehicle(DeliveryVehicle deliveryVehicle) {
    this.deliveryVehicle = deliveryVehicle;
  }

  @Override
  public String toString() {
    return "VehicleNotUpdatingOutputEvent{"
        + "firstUpdateTime="
        + firstUpdateTime
        + ", lastUpdateTime="
        + lastUpdateTime
        + ", gapDuration="
        + gapDuration
        + ", deliveryVehicle="
        + deliveryVehicle
        + ", outputType="
        + outputType
        + '}';
  }
}
