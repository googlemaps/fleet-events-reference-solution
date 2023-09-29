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
import google.maps.fleetengine.delivery.v1.Task;
import java.io.Serializable;

public class TaskOutcomeChangeOutputEvent extends OutputEvent implements Serializable {
  private String previousOutcome;
  private String newOutcome;

  @JsonSerialize(using = ProtobufSerializer.class)
  private Task task;

  public TaskOutcomeChangeOutputEvent() {
    this.outputType = OutputType.TASK_OUTCOME_CHANGE_OUTPUT;
  }

  public String getPreviousOutcome() {
    return previousOutcome;
  }

  public void setPreviousOutcome(String previousOutcome) {
    this.previousOutcome = previousOutcome;
  }

  public String getNewOutcome() {
    return newOutcome;
  }

  public void setNewOutcome(String newOutcome) {
    this.newOutcome = newOutcome;
  }

  public Task getTask() {
    return task;
  }

  public void setTask(Task task) {
    this.task = task;
  }

  @Override
  public String toString() {
    return "TaskOutcomeChangeOutputEvent{"
        + "previousOutcome='"
        + previousOutcome
        + '\''
        + ", newOutcome='"
        + newOutcome
        + '\''
        + ", task="
        + task
        + ", outputType="
        + outputType
        + '}';
  }
}
