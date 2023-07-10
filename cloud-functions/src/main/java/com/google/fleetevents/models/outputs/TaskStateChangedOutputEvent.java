package com.google.fleetevents.models.outputs;

import java.util.Objects;

/** Output event for the task state changes. */
public class TaskStateChangedOutputEvent extends OutputEvent {

  private String taskId;
  private String oldTaskState;
  private String newTaskState;

  public TaskStateChangedOutputEvent() {
    this.setType(Type.TASK_STATE_CHANGED);
  }

  public String getTaskId() {
    return taskId;
  }

  public void setTaskId(String taskId) {
    this.taskId = taskId;
  }

  public String getOldTaskState() {
    return oldTaskState;
  }

  public void setOldTaskState(String oldTaskState) {
    this.oldTaskState = oldTaskState;
  }

  public String getNewTaskState() {
    return newTaskState;
  }

  public void setNewTaskState(String newTaskState) {
    this.newTaskState = newTaskState;
  }

  public String toString() {
    return String.format(
        "TaskStateChangedOutputEvent{\n"
            + "\ttaskId: %s\n"
            + "\toldTaskState: %s\n"
            + "\tnewTaskState: %s\n"
            + "\tfleetEvent:%s\n"
            + "}",
        taskId, oldTaskState, newTaskState, getFleetEvent());
  }

  @Override
  public boolean equals(Object object) {
    if (object instanceof TaskStateChangedOutputEvent that) {
      return Objects.equals(that.taskId, this.taskId)
          && Objects.equals(that.oldTaskState, this.oldTaskState)
          && Objects.equals(that.newTaskState, this.newTaskState)
          && Objects.equals(that.getFleetEvent(), this.getFleetEvent());
    }
    return false;
  }
}
