package com.google.fleetevents.beam.model.output;

import google.maps.fleetengine.delivery.v1.Task;
import java.io.Serializable;

public class TaskOutcomeResult implements Serializable {
  private String prevState;
  private String newState;
  private Task task;

  public String getPrevState() {
    return prevState;
  }

  public void setPrevState(String prevState) {
    this.prevState = prevState;
  }

  public String getNewState() {
    return newState;
  }

  public void setNewState(String newState) {
    this.newState = newState;
  }

  public Task getTask() {
    return task;
  }

  public void setTask(Task task) {
    this.task = task;
  }

  @Override
  public String toString() {
    return "TaskOutcomeResult{"
        + ", prevState='"
        + prevState
        + '\''
        + ", newState='"
        + newState
        + '\''
        + ", task="
        + task
        + '}';
  }
}
