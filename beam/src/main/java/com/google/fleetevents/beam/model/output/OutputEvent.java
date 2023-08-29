package com.google.fleetevents.beam.model.output;

import java.io.Serializable;

public class OutputEvent implements Serializable {
  protected OutputType outputType;

  public OutputType getOutputType() {
    return outputType;
  }

  enum OutputType {
    TASK_OUTCOME_CHANGE_OUTPUT,
    VEHICLE_NOT_UPDATING_OUTPUT;
  }
}
