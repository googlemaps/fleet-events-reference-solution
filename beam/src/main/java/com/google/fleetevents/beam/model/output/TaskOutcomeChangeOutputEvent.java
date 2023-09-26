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
