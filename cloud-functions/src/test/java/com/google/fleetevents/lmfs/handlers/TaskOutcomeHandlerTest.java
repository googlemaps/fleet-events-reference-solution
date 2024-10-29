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

import static com.google.maps.fleetengine.delivery.v1.Task.TaskOutcome.FAILED;
import static com.google.maps.fleetengine.delivery.v1.Task.TaskOutcome.SUCCEEDED;
import static com.google.maps.fleetengine.delivery.v1.Task.TaskOutcome.TASK_OUTCOME_UNSPECIFIED;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.fleetevents.common.models.Change;
import com.google.fleetevents.common.models.OutputEvent;
import com.google.fleetevents.common.models.Pair;
import com.google.fleetevents.lmfs.models.DeliveryTaskData;
import com.google.fleetevents.lmfs.models.DeliveryTaskFleetEvent;
import com.google.fleetevents.lmfs.models.outputs.TaskOutcomeOutputEvent;
import com.google.maps.fleetengine.delivery.v1.Task.TaskOutcome;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

/** Tests for the task outcome fleet event handler. */
public class TaskOutcomeHandlerTest {

  @Test
  public void updateTaskNoTaskOutcome_doesNotRespond() {
    DeliveryTaskFleetEvent deliveryTaskFleetEvent =
        DeliveryTaskFleetEvent.builder()
            .setDeliveryTaskId("deliveryTaskId1")
            .setNewDeliveryTask(
                DeliveryTaskData.builder().setDeliveryTaskId("deliveryTaskId1").build())
            .build();
    TaskOutcomeHandler taskOutcomeHandler = new TaskOutcomeHandler();
    assertFalse(taskOutcomeHandler.respondsTo(deliveryTaskFleetEvent, null, null));
  }

  @ParameterizedTest
  @ValueSource(
      strings = {"TASK_OUTCOME_UNSPECIFIED", "TASK_OUTCOME_SUCCEEDED", "TASK_OUTCOME_FAILED"})
  public void updateTaskTaskOutcome_respondsReturnsOutputEvent(String taskOutcome) {
    DeliveryTaskFleetEvent deliveryTaskFleetEvent =
        DeliveryTaskFleetEvent.builder()
            .setDeliveryTaskId("deliveryTaskId1")
            .setOldDeliveryTask(
                DeliveryTaskData.builder().setDeliveryTaskId("deliveryTaskId1").build())
            .setNewDeliveryTask(
                DeliveryTaskData.builder()
                    .setDeliveryTaskId("deliveryTaskId1")
                    .setTaskOutcome(taskOutcome)
                    .build())
            .setTaskDifferences(ImmutableMap.of("taskOutcome", new Change(null, taskOutcome)))
            .build();
    TaskOutcomeHandler taskOutcomeHandler = new TaskOutcomeHandler();
    assertTrue(taskOutcomeHandler.respondsTo(deliveryTaskFleetEvent, null, null));
    TaskOutcomeOutputEvent expectedOutputEvent = new TaskOutcomeOutputEvent();
    expectedOutputEvent.setFleetEvent(deliveryTaskFleetEvent);
    expectedOutputEvent.setOldTaskOutcome("TASK_OUTCOME_UNSPECIFIED");
    expectedOutputEvent.setNewTaskOutcome(taskOutcome);
    expectedOutputEvent.setTaskId("deliveryTaskId1");

    List<OutputEvent> expected = ImmutableList.of(expectedOutputEvent);
    assertIterableEquals(expected, taskOutcomeHandler.handleEvent(deliveryTaskFleetEvent, null));
  }

  @ParameterizedTest
  @EnumSource(ValidTestCase.class)
  public void testTaskOutcomeValidOutputs_success(ValidTestCase validTestCase) {
    TaskOutcomeOutputEvent validOutput = new TaskOutcomeOutputEvent();
    validOutput.setOldTaskOutcome(validTestCase.oldOutcome);
    validOutput.setNewTaskOutcome(validTestCase.newOutcome);
    TaskOutcomeHandler taskOutcomeHandler = new TaskOutcomeHandler();
    assertTrue(taskOutcomeHandler.verifyOutput(validOutput));
  }

  @Test
  public void testTaskOutcomeInvalidOutputs_fail() {
    // process valid cases into a set
    Set<Pair<String, String>> validPairs = new HashSet<>();
    for (ValidTestCase valid : ValidTestCase.values()) {
      validPairs.add(new Pair(valid.oldOutcome, valid.newOutcome));
    }
    TaskOutcomeHandler taskOutcomeHandler = new TaskOutcomeHandler();
    for (TaskOutcome oldOutcome : TaskOutcome.values()) {
      for (TaskOutcome newOutcome : TaskOutcome.values()) {
        Pair<TaskOutcome, TaskOutcome> outcomePair = new Pair(oldOutcome.name(), newOutcome.name());
        if (validPairs.contains(outcomePair)) {
          continue;
        }
        TaskOutcomeOutputEvent invalidOutput = new TaskOutcomeOutputEvent();
        invalidOutput.setOldTaskOutcome(oldOutcome.name());
        invalidOutput.setNewTaskOutcome(newOutcome.name());
        assertFalse(taskOutcomeHandler.verifyOutput(invalidOutput));
      }
    }
  }

  enum ValidTestCase {
    TASK_FAILED(TASK_OUTCOME_UNSPECIFIED.name(), FAILED.name()),
    TASK_SUCCEEDED(TASK_OUTCOME_UNSPECIFIED.name(), SUCCEEDED.name()),
    TASK_FAILED_NULL(null, FAILED.name()),
    TASK_SUCCEEDED_NULL(null, SUCCEEDED.name());
    final String oldOutcome;
    final String newOutcome;

    ValidTestCase(String oldOutcome, String newOutcome) {
      this.oldOutcome = oldOutcome;
      this.newOutcome = newOutcome;
    }
  }
}
