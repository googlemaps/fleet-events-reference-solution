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

package com.google.fleetevents.handlers;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.fleetevents.models.Change;
import com.google.fleetevents.models.DeliveryTaskData;
import com.google.fleetevents.models.DeliveryTaskFleetEvent;
import com.google.fleetevents.models.outputs.OutputEvent;
import com.google.fleetevents.models.outputs.TaskStateChangedOutputEvent;
import java.util.List;
import org.junit.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/** Tests for the task outcome fleet event handler. */
public class TaskStateHandlerTest {

  @Test
  public void updateTaskNoTaskState_doesNotRespond() {
    DeliveryTaskFleetEvent deliveryTaskFleetEvent =
        DeliveryTaskFleetEvent.builder()
            .setDeliveryTaskId("deliveryTaskId1")
            .setNewDeliveryTask(
                DeliveryTaskData.builder().setDeliveryTaskId("deliveryTaskId1").build())
            .build();
    TaskStateHandler taskStateHandler = new TaskStateHandler();
    assertFalse(taskStateHandler.respondsTo(deliveryTaskFleetEvent, null, null));
  }

  @ParameterizedTest
  @ValueSource(strings = {"STATE_UNSPECIFIED", "OPEN", "CLOSED"})
  public void updateTaskTaskState_respondsReturnsOutputEvent(String taskState) {
    DeliveryTaskFleetEvent deliveryTaskFleetEvent =
        DeliveryTaskFleetEvent.builder()
            .setDeliveryTaskId("deliveryTaskId1")
            .setOldDeliveryTask(
                DeliveryTaskData.builder().setDeliveryTaskId("deliveryTaskId1").build())
            .setNewDeliveryTask(
                DeliveryTaskData.builder()
                    .setDeliveryTaskId("deliveryTaskId1")
                    .setState(taskState)
                    .build())
            .setTaskDifferences(
                ImmutableMap.of("state", new Change("STATE_UNSPECIFIED", taskState)))
            .build();
    TaskStateHandler taskStateHandler = new TaskStateHandler();
    assertTrue(taskStateHandler.respondsTo(deliveryTaskFleetEvent, null, null));
    TaskStateChangedOutputEvent expectedOutputEvent = new TaskStateChangedOutputEvent();
    expectedOutputEvent.setFleetEvent(deliveryTaskFleetEvent);
    expectedOutputEvent.setOldTaskState("STATE_UNSPECIFIED");
    expectedOutputEvent.setNewTaskState(taskState);
    expectedOutputEvent.setTaskId("deliveryTaskId1");

    List<OutputEvent> expected = ImmutableList.of(expectedOutputEvent);
    assertIterableEquals(expected, taskStateHandler.handleEvent(deliveryTaskFleetEvent, null));
  }
}
