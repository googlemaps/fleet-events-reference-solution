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

import com.google.fleetevents.common.util.TimeUtil;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/** Firestore serializable representation of the vehicle journey segment object. */
public class VehicleJourneySegment implements Serializable {
  private Long duration;
  private int distance;
  private List<String> taskIds;
  private VehicleStop vehicleStop;

  private VehicleJourneySegment() {}

  public static Builder builder() {
    return new VehicleJourneySegment.Builder()
        .setDistance(0)
        .setDuration(0L)
        .setTaskIds(new ArrayList<>());
  }

  public static VehicleJourneySegment fromVehicleJourneySegmentProto(
      com.google.maps.fleetengine.delivery.v1.VehicleJourneySegment vjs) {
    var latLng = vjs.getStop().getPlannedLocation().getPoint();
    return com.google.fleetevents.lmfs.models.VehicleJourneySegment.builder()
        .setTaskIds(
            vjs.getStop().getTasksList().stream()
                .map(com.google.maps.fleetengine.delivery.v1.VehicleStop.TaskInfo::getTaskId)
                .collect(Collectors.toList()))
        .setDistance(vjs.getDrivingDistanceMeters().getValue())
        .setDuration(TimeUtil.protobufToLong(vjs.getDrivingDuration()))
        .setVehicleStop(
            new VehicleStop.Builder()
                .setTaskInfos(
                    vjs.getStop().getTasksList().stream()
                        .map(
                            x ->
                                new com.google.fleetevents.lmfs.models.TaskInfo.Builder()
                                    .setTaskDuration(TimeUtil.protobufToLong(x.getTaskDuration()))
                                    .setTaskId(x.getTaskId())
                                    .build())
                        .collect(Collectors.toList()))
                .build())
        .build();
  }

  public Builder toBuilder() {
    return new Builder(this);
  }

  // duration in milliseconds.
  public Long getDuration() {
    return duration;
  }

  public int getDistance() {
    return distance;
  }

  public List<String> getTaskIds() {
    return taskIds;
  }

  public VehicleStop getVehicleStop() {
    return vehicleStop;
  }

  @Override
  public String toString() {
    return "VehicleJourneySegment{"
        + ", duration="
        + duration
        + ", distance="
        + distance
        + ", taskIds="
        + taskIds
        + ", vehicleStop="
        + vehicleStop
        + '}';
  }

  @Override
  public boolean equals(Object object) {
    if (object instanceof VehicleJourneySegment that) {
      return Objects.equals(that.duration, this.duration)
          && Objects.equals(that.distance, this.distance)
          && Objects.equals(that.taskIds, this.taskIds)
          && Objects.equals(that.vehicleStop, this.vehicleStop);
    }
    return false;
  }

  public static class Builder {

    VehicleJourneySegment vehicleJourneySegment;

    Builder() {
      vehicleJourneySegment = new VehicleJourneySegment();
    }

    Builder(VehicleJourneySegment vehicleJourneySegment) {
      this.vehicleJourneySegment = vehicleJourneySegment;
    }

    public Builder setDuration(Long duration) {
      vehicleJourneySegment.duration = duration;
      return this;
    }

    public Builder setDistance(int distance) {
      vehicleJourneySegment.distance = distance;
      return this;
    }

    public Builder setTaskIds(List<String> taskIds) {
      vehicleJourneySegment.taskIds = taskIds;
      return this;
    }

    public Builder setVehicleStop(VehicleStop stop) {
      vehicleJourneySegment.vehicleStop = stop;
      return this;
    }

    public VehicleJourneySegment build() {
      return vehicleJourneySegment;
    }
  }
}
