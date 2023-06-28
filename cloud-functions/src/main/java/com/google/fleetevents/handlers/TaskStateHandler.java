package com.google.fleetevents.handlers;

import com.google.cloud.firestore.Transaction;
import com.google.common.collect.ImmutableList;
import com.google.fleetevents.FleetEventHandler;
import com.google.fleetevents.database.FirestoreDatabaseClient;
import com.google.fleetevents.models.DeliveryTaskFleetEvent;
import com.google.fleetevents.models.FleetEvent;
import com.google.fleetevents.models.Pair;
import com.google.fleetevents.models.outputs.OutputEvent;
import com.google.fleetevents.models.outputs.TaskStateChangedOutputEvent;
import google.maps.fleetengine.delivery.v1.Task.State;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

public class TaskStateHandler implements FleetEventHandler {

  private static final Logger logger = Logger.getLogger(TaskStateHandler.class.getName());

  private final Set<Pair<String, String>> VALID_OUTPUTS = new HashSet<>() {{
    add(new Pair<>(State.STATE_UNSPECIFIED.name(),
        State.OPEN.name()));
    add(new Pair<>(State.STATE_UNSPECIFIED.name(),
        State.CLOSED.name()));
    add(new Pair<>(null, State.OPEN.name()));
    add(new Pair<>(null, State.CLOSED.name()));
    add(new Pair<>(State.OPEN.name(), State.CLOSED.name()));
  }};


  public List<OutputEvent> handleEvent(FleetEvent fleetEvent, Transaction transaction) {
    DeliveryTaskFleetEvent deliveryTaskFleetEvent = (DeliveryTaskFleetEvent) fleetEvent;
    logger.info(
        String.format(
            "Task State changed:\n%s,\ntask id: %s",
            deliveryTaskFleetEvent.taskDifferences().get("state"),
            deliveryTaskFleetEvent.deliveryTaskId()));
    TaskStateChangedOutputEvent taskStateOutputEvent = new TaskStateChangedOutputEvent();
    taskStateOutputEvent.setFleetEvent(fleetEvent);
    taskStateOutputEvent.setTaskId(deliveryTaskFleetEvent.deliveryTaskId());
    taskStateOutputEvent.setOldTaskState(
        deliveryTaskFleetEvent.oldDeliveryTask().getState());
    taskStateOutputEvent.setNewTaskState(
        deliveryTaskFleetEvent.newDeliveryTask().getState());
    return ImmutableList.of(taskStateOutputEvent);
  }

  @Override
  public boolean respondsTo(
      FleetEvent fleetEvent,
      Transaction transaction,
      FirestoreDatabaseClient firestoreDatabaseClient) {
    if (fleetEvent.getEventType() != FleetEvent.Type.DELIVERY_TASK_FLEET_EVENT) {
      return false;
    }

    DeliveryTaskFleetEvent deliveryTaskFleetEvent = (DeliveryTaskFleetEvent) fleetEvent;
    return deliveryTaskFleetEvent.taskDifferences().containsKey("state");
  }

  @Override
  public boolean verifyOutput(OutputEvent outputEvent) {
    if (!(outputEvent instanceof TaskStateChangedOutputEvent taskStateChangedOutputEvent)) {
      return false;
    }
    return outputEvent.getType() == OutputEvent.Type.TASK_STATE_CHANGED && VALID_OUTPUTS.contains(
        new Pair(taskStateChangedOutputEvent.getOldTaskState(),
            taskStateChangedOutputEvent.getNewTaskState()));
  }
}