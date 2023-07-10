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

package com.google.fleetevents.models.outputs;

import java.util.Objects;

/** OutputEvent for task outcome. */
public class TaskOutcomeOutputEvent extends OutputEvent {

  private String taskId;
  private String oldTaskOutcome;
  private String newTaskOutcome;

  public TaskOutcomeOutputEvent() {
    this.type = Type.TASK_OUTCOME_CHANGED;
  }

  public String getTaskId() {
    return taskId;
  }

  public void setTaskId(String taskId) {
    this.taskId = taskId;
  }

  public String getOldTaskOutcome() {
    return oldTaskOutcome;
  }

  public void setOldTaskOutcome(String oldTaskOutcome) {
    this.oldTaskOutcome = oldTaskOutcome;
  }

  public String getNewTaskOutcome() {
    return newTaskOutcome;
  }

  public void setNewTaskOutcome(String newTaskOutcome) {
    this.newTaskOutcome = newTaskOutcome;
  }

  @Override
  public String toString() {
    return "TaskOutcomeOutputEvent{"
        + "taskId='"
        + taskId
        + '\''
        + ", oldTaskOutcome='"
        + oldTaskOutcome
        + '\''
        + ", newTaskOutcome='"
        + newTaskOutcome
        + '\''
        + ", fleetEvent="
        + fleetEvent
        + ", type="
        + type
        + '}';
  }

  @Override
  public boolean equals(Object object) {
    if (object instanceof TaskOutcomeOutputEvent that) {
      return Objects.equals(that.taskId, this.taskId)
          && Objects.equals(that.oldTaskOutcome, this.oldTaskOutcome)
          && Objects.equals(that.newTaskOutcome, this.newTaskOutcome)
          && Objects.equals(that.getFleetEvent(), this.getFleetEvent());
    }
    return false;
  }
}
