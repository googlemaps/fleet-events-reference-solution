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

package com.google.fleetevents.transactions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.cloud.firestore.GeoPoint;
import com.google.common.collect.ImmutableList;
import com.google.fleetevents.helpers.FakeFirestoreHelper;
import com.google.fleetevents.helpers.FleetEventsTestHelper;
import com.google.fleetevents.mocks.MockFleetHandler;
import com.google.fleetevents.models.Change;
import com.google.fleetevents.models.DeliveryTaskData;
import com.google.fleetevents.models.DeliveryTaskFleetEvent;
import com.google.fleetevents.models.DeliveryVehicleData;
import com.google.fleetevents.models.TaskInfo;
import com.google.fleetevents.models.VehicleJourneySegment;
import com.google.fleetevents.models.VehicleStop;
import com.google.fleetevents.models.outputs.OutputEvent;
import com.google.logging.v2.LogEntry;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;
import org.junit.Test;

/**
 * Tests for update delivery vehicle transaction.
 */
public class UpdateDeliveryVehicleTransactionTest {


  @Test
  public void updateDeliveryVehicleLog_remainingDistanceDurationLongitudeUpdated()
      throws IOException, ExecutionException, InterruptedException {
    /* Tests whether the cloudLogEntry is correctly routing log entries based on log name. */
    HashMap<String, Object> fakeBackend = new HashMap<>();
    List<String> currentTaskIds = new ArrayList<>();
    currentTaskIds.add("testDeliveryTaskId1");
    fakeBackend.put(
        "deliveryVehicles/testDeliveryVehicleId1",
        DeliveryVehicleData.builder()
            .setDeliveryVehicleId("testDeliveryVehicleId1")
            .setName("providers/test-123/deliveryVehicles/testDeliveryVehicleId1")
            .setCurrentDeliveryTaskIds(currentTaskIds)
            .build());

    LogEntry logEntry = FleetEventsTestHelper.updateDeliveryVehicleLog1();
    DeliveryVehicleData expectedDeliveryVehicleData =
        DeliveryVehicleData.builder()
            .setEventTimestamp(1678256128384L)
            .setDeliveryVehicleId("testDeliveryVehicleId1")
            .setRemainingDuration(580820L)
            .setRemainingDistanceMeters(2804L)
            .setName("providers/test-123/deliveryVehicles/testDeliveryVehicleId1")
            .setCurrentDeliveryTaskIds(currentTaskIds)
            .setNavigationStatus("NO_GUIDANCE")
            .setLastLocation(new GeoPoint(17, 79))
            .build();

    var firestoreDatabaseClient = FakeFirestoreHelper.getFakeFirestoreDatabaseClient();
    var transaction = FakeFirestoreHelper.getFakeTransaction(fakeBackend);

    UpdateDeliveryVehicleTransaction updateDeliveryVehicleTransaction =
        new UpdateDeliveryVehicleTransaction(logEntry, ImmutableList.of(), firestoreDatabaseClient);
    updateDeliveryVehicleTransaction.updateCallback(transaction);

    assertTrue(fakeBackend.containsKey("deliveryVehicles/testDeliveryVehicleId1"));
    assertEquals(
        expectedDeliveryVehicleData,
        ((DeliveryVehicleData) fakeBackend.get("deliveryVehicles/testDeliveryVehicleId1"))
            .toBuilder().setExpireAt(null).build());
  }

  @Test
  public void updateDeliveryVehicleLog_taskRemoved()
      throws IOException, ExecutionException, InterruptedException {
    HashMap<String, Object> fakeBackend = new HashMap<>();
    List<VehicleJourneySegment> newVehicleJourneySegments = new ArrayList<>();
    List<String> oldTaskIds = new ArrayList<>();
    oldTaskIds.add("testDeliveryTaskId1");
    List<String> newTaskIds = new ArrayList<>();
    newTaskIds.add("testDeliveryTaskId2");
    List<TaskInfo> newTaskInfos = new ArrayList<>();
    var newTaskInfo = new TaskInfo();
    newTaskInfo.setTaskId("testDeliveryTaskId2");
    newTaskInfo.setTaskDuration(5000L);
    newTaskInfos.add(newTaskInfo);
    var newVehicleStop = new VehicleStop();
    newVehicleStop.setTaskInfos(newTaskInfos);
    newVehicleJourneySegments.add(
        VehicleJourneySegment.builder().setTaskIds(newTaskIds).setVehicleStop(newVehicleStop)
            .build());
    var oldDeliveryVehicle = DeliveryVehicleData.builder()
        .setDeliveryVehicleId("testDeliveryVehicleId2")
        .setName("providers/test-123/deliveryVehicles/testDeliveryVehicleId2")
        .setCurrentDeliveryTaskIds(oldTaskIds)
        .setPlannedDeliveryTaskIds(newTaskIds)
        .build();
    var oldDeliveryTask = DeliveryTaskData.builder()
        .setDeliveryVehicleId("testDeliveryVehicleId2")
        .setDeliveryTaskId("testDeliveryTaskId1")
        .setName("providers/test-123/tasks/testDeliveryTaskId1")
        .build();
    fakeBackend.put(
        "deliveryVehicles/testDeliveryVehicleId2", oldDeliveryVehicle);
    fakeBackend.put(
        "deliveryTasks/testDeliveryTaskId1", oldDeliveryTask);

    LogEntry logEntry = FleetEventsTestHelper.updateDeliveryVehicleLog2();
    DeliveryVehicleData expectedDeliveryVehicleData =
        DeliveryVehicleData.builder()
            .setEventTimestamp(1678256128384L)
            .setDeliveryVehicleId("testDeliveryVehicleId2")
            .setRemainingVehicleJourneySegments(newVehicleJourneySegments)
            .setCurrentDeliveryTaskIds(newTaskIds)
            .setName("providers/test-123/deliveryVehicles/testDeliveryVehicleId2")
            .setNavigationStatus("NO_GUIDANCE")
            .setLastLocation(new GeoPoint(17, 79))
            .setRemainingDistanceMeters(2804)
            .setRemainingDuration(580820L)
            .build();

    var firestoreDatabaseClient = FakeFirestoreHelper.getFakeFirestoreDatabaseClient();
    var transaction = FakeFirestoreHelper.getFakeTransaction(fakeBackend);

    UpdateDeliveryVehicleTransaction updateDeliveryVehicleTransaction =
        new UpdateDeliveryVehicleTransaction(logEntry, ImmutableList.of(new MockFleetHandler()),
            firestoreDatabaseClient);
    var outputEvents = updateDeliveryVehicleTransaction.updateCallback(transaction);
    assertEquals(outputEvents.size(), 3);

    var taskDifferences = new HashMap<String, Change>();
    taskDifferences.put("state", new Change<>("STATE_UNSPECIFIED", "CLOSED"));

    var taskStateChangeEvent = DeliveryTaskFleetEvent.builder()
        .setDeliveryTaskId("testDeliveryVehicleId2")
        .setDeliveryTaskId("testDeliveryTaskId1")
        .setOldDeliveryTask(oldDeliveryTask)
        .setNewDeliveryTask(oldDeliveryTask.toBuilder().setState("CLOSED").build())
        .setTaskDifferences(taskDifferences)
        .setVehicleDifferences(new HashMap<>())
        .build();
    var taskOutputEvent = new OutputEvent();
    taskOutputEvent.setFleetEvent(taskStateChangeEvent);
    for (OutputEvent outputEvent : outputEvents) {
      if (outputEvent.getFleetEvent() instanceof DeliveryTaskFleetEvent taskEvent) {
        outputEvent.setFleetEvent(taskEvent.toBuilder()
            .setNewDeliveryTask(taskEvent.newDeliveryTask().toBuilder().setExpireAt(null).build())
            .setVehicleDifferences(new HashMap<>())
            .setOldDeliveryVehicle(null)
            .setNewDeliveryVehicle(null)
            .build());
      }
    }
    assertTrue(outputEvents.contains(taskOutputEvent));
    assertTrue(fakeBackend.containsKey("deliveryVehicles/testDeliveryVehicleId2"));
    assertEquals(
        expectedDeliveryVehicleData,
        ((DeliveryVehicleData) fakeBackend.get("deliveryVehicles/testDeliveryVehicleId2"))
            .toBuilder().setExpireAt(null).build());
    assertTrue(fakeBackend.containsKey("deliveryTasks/testDeliveryTaskId1"));
    assertEquals("CLOSED",
        ((DeliveryTaskData) fakeBackend.get("deliveryTasks/testDeliveryTaskId1")).getState());
  }

}
