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

import java.util.Objects;

/** POJO class to represent TaskInfo in the delivery vehicle stop. */
public class TaskInfo {

  // duration in milliseconds.
  private Long taskDuration;
  private String taskId;
  private LatLng plannedlocation;

  public Long getTaskDuration() {
    return taskDuration;
  }

  public void setTaskDuration(Long taskDuration) {
    this.taskDuration = taskDuration;
  }

  public String getTaskId() {
    return taskId;
  }

  public void setTaskId(String taskId) {
    this.taskId = taskId;
  }

  public LatLng getPlannedlocation() {
    return plannedlocation;
  }

  public void setPlannedlocation(LatLng plannedlocation) {
    this.plannedlocation = plannedlocation;
  }

  @Override
  public boolean equals(Object object) {
    if (object instanceof TaskInfo that) {
      return Objects.equals(that.taskId, this.taskId)
          && Objects.equals(that.taskDuration, this.taskDuration)
          && Objects.equals(that.plannedlocation, this.plannedlocation);
    }
    return false;
  }

  @Override
  public String toString() {
    return "TaskInfo{"
        + "taskDuration="
        + taskDuration
        + ", taskId='"
        + taskId
        + '\''
        + ", plannedlocation="
        + plannedlocation
        + '}';
  }

  public static final class Builder {

    private Long taskDuration;
    private String taskId;

    private LatLng plannedlocation;

    public Builder setTaskDuration(Long taskDuration) {
      this.taskDuration = taskDuration;
      return this;
    }

    public Builder setTaskId(String taskId) {
      this.taskId = taskId;
      return this;
    }

    public Builder setPlannedLocation(LatLng plannedLocation) {
      this.plannedlocation = plannedLocation;
      return this;
    }

    public TaskInfo build() {
      TaskInfo taskInfo = new TaskInfo();
      taskInfo.setTaskDuration(taskDuration);
      taskInfo.setTaskId(taskId);
      taskInfo.setPlannedlocation(plannedlocation);
      return taskInfo;
    }
  }
}
