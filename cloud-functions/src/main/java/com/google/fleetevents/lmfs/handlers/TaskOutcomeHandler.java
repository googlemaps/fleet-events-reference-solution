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

package com.google.fleetevents.lmfs.handlers;

import com.google.cloud.firestore.Transaction;
import com.google.common.collect.ImmutableList;
import com.google.fleetevents.FleetEventHandler;
import com.google.fleetevents.common.database.FirestoreDatabaseClient;
import com.google.fleetevents.common.models.FleetEvent;
import com.google.fleetevents.common.models.OutputEvent;
import com.google.fleetevents.common.models.Pair;
import com.google.fleetevents.lmfs.models.DeliveryTaskFleetEvent;
import com.google.fleetevents.lmfs.models.outputs.TaskOutcomeOutputEvent;
import google.maps.fleetengine.delivery.v1.Task;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

/**
 * FleetEventHandler that alerts when a task's outcome has changed. Task outcome can either be
 * succeeded, failed, or undefined.
 */
public class TaskOutcomeHandler implements FleetEventHandler {

  private static final Logger logger = Logger.getLogger(TaskOutcomeHandler.class.getName());
  private final Set<Pair<String, String>> VALID_OUTPUTS =
      new HashSet<>() {
        {
          add(
              new Pair(
                  Task.TaskOutcome.TASK_OUTCOME_UNSPECIFIED.name(),
                  Task.TaskOutcome.SUCCEEDED.name()));
          add(
              new Pair(
                  Task.TaskOutcome.TASK_OUTCOME_UNSPECIFIED.name(),
                  Task.TaskOutcome.FAILED.name()));
          add(new Pair(null, Task.TaskOutcome.SUCCEEDED.name()));
          add(new Pair(null, Task.TaskOutcome.FAILED.name()));
        }
      };

  public List<OutputEvent> handleEvent(FleetEvent fleetEvent, Transaction transaction) {
    DeliveryTaskFleetEvent deliveryTaskFleetEvent = (DeliveryTaskFleetEvent) fleetEvent;
    logger.info(
        String.format(
            "TaskOutcome Status changed:\n%s,\ntask id: %s",
            deliveryTaskFleetEvent.taskDifferences().get("taskOutcome"),
            deliveryTaskFleetEvent.deliveryTaskId()));
    TaskOutcomeOutputEvent taskOutcomeOutputEvent = new TaskOutcomeOutputEvent();
    taskOutcomeOutputEvent.setFleetEvent(fleetEvent);
    taskOutcomeOutputEvent.setTaskId(deliveryTaskFleetEvent.deliveryTaskId());
    taskOutcomeOutputEvent.setOldTaskOutcome(
        deliveryTaskFleetEvent.oldDeliveryTask().getTaskOutcome());
    taskOutcomeOutputEvent.setNewTaskOutcome(
        deliveryTaskFleetEvent.newDeliveryTask().getTaskOutcome());
    return ImmutableList.of(taskOutcomeOutputEvent);
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
    return deliveryTaskFleetEvent.taskDifferences().containsKey("taskOutcome");
  }

  @Override
  public boolean verifyOutput(OutputEvent outputEvent) {
    if (!(outputEvent instanceof TaskOutcomeOutputEvent taskOutcomeOutputEvent)) {
      return false;
    }
    if (outputEvent.getType() != OutputEvent.Type.TASK_OUTCOME_CHANGED) {
      return false;
    }
    return VALID_OUTPUTS.contains(
        new Pair(
            taskOutcomeOutputEvent.getOldTaskOutcome(),
            taskOutcomeOutputEvent.getNewTaskOutcome()));
  }
}
