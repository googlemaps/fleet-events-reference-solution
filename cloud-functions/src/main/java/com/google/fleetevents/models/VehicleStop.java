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

import java.util.List;
import java.util.Objects;

/** Firestore serializable representation of the vehicle stop. */
public class VehicleStop {

  private List<TaskInfo> taskInfos;

  public List<TaskInfo> getTaskInfos() {
    return taskInfos;
  }

  public void setTaskInfos(List<TaskInfo> taskInfos) {
    this.taskInfos = taskInfos;
  }

  @Override
  public String toString() {
    return "VehicleStop{" + "taskInfos=" + taskInfos + '}';
  }

  @Override
  public boolean equals(Object object) {
    if (object instanceof VehicleStop that) {
      return Objects.equals(that.taskInfos, this.taskInfos);
    }
    return false;
  }

  public static final class Builder {

    private List<TaskInfo> taskInfos;

    public Builder setTaskInfos(List<TaskInfo> taskInfos) {
      this.taskInfos = taskInfos;
      return this;
    }

    public VehicleStop build() {
      VehicleStop vehicleStop = new VehicleStop();
      vehicleStop.setTaskInfos(taskInfos);
      return vehicleStop;
    }
  }
}
