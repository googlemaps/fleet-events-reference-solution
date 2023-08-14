package com.google.fleetevents.beam.model;

public class TaskMetadata {
  private String name;
  private String taskOutcome;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getTaskOutcome() {
    return taskOutcome;
  }

  public void setTaskOutcome(String taskOutcome) {
    this.taskOutcome = taskOutcome;
  }

  @Override
  public String toString() {
    return "TaskMetadata{" + "name='" + name + '\'' + ", taskOutcome='" + taskOutcome + '\'' + '}';
  }
}
