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

package com.google.fleetevents;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;

import com.google.api.core.ApiFutures;
import com.google.cloud.firestore.Transaction.Function;
import com.google.common.collect.ImmutableList;
import com.google.fleetevents.common.database.FirestoreDatabaseClient;
import com.google.fleetevents.common.util.FleetEngineClient;
import com.google.fleetevents.helpers.FleetEventsTestHelper;
import com.google.fleetevents.lmfs.models.DeliveryTaskData;
import com.google.fleetevents.lmfs.models.DeliveryTaskFleetEvent;
import com.google.fleetevents.lmfs.models.DeliveryVehicleData;
import com.google.fleetevents.lmfs.models.DeliveryVehicleFleetEvent;
import com.google.fleetevents.lmfs.models.outputs.OutputEvent;
import com.google.fleetevents.mocks.MockFleetEventCreator;
import com.google.logging.v2.LogEntry;
import com.google.type.LatLng;
import google.maps.fleetengine.delivery.v1.LocationInfo;
import google.maps.fleetengine.delivery.v1.Task;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import org.junit.Test;
import org.mockito.Mockito;

/** Tests for fleet event creator. */
public class FleetEventCreatorTests {

  @Test
  public void processCloudLogEntry_createDeliveryVehicleLog_routedCorrectly()
      throws IOException, ExecutionException, InterruptedException {
    /* Tests whether the cloudLogEntry is correctly routing log entries based on log name. */
    LogEntry logEntry = FleetEventsTestHelper.createDeliveryVehicleLog1();
    DeliveryVehicleData expectedDeliveryVehicleData =
        DeliveryVehicleData.builder()
            .setDeliveryVehicleId("testDeliveryVehicleId1")
            .setName("providers/test-123/deliveryVehicles/testDeliveryVehicleId1")
            .build();
    DeliveryVehicleFleetEvent expectedDeliveryVehicleFleetEvent =
        DeliveryVehicleFleetEvent.builder()
            .setDeliveryVehicleId("testDeliveryVehicleId1")
            .setNewDeliveryVehicle(expectedDeliveryVehicleData)
            .build();
    OutputEvent expectedOutputEvent = new OutputEvent();
    expectedOutputEvent.setFleetEvent(expectedDeliveryVehicleFleetEvent);

    FleetEventCreator spyFleetEventCreator = Mockito.spy(new MockFleetEventCreator());
    FirestoreDatabaseClient mockFirestore = spyFleetEventCreator.getDatabase();

    doReturn(ApiFutures.immediateFuture(ImmutableList.of(expectedOutputEvent)))
        .when(mockFirestore)
        .runTransaction(any(Function.class));

    List<OutputEvent> outputEvents =
        spyFleetEventCreator.processCloudLog(logEntry, ImmutableList.of());
    assertEquals(ImmutableList.of(expectedOutputEvent), outputEvents);
  }

  @Test
  public void updateDeliveryVehicleLog_routedCorrectly()
      throws IOException, ExecutionException, InterruptedException {
    /* Tests whether the cloudLogEntry is correctly routing log entries based on log name. */
    LogEntry logEntry = FleetEventsTestHelper.updateDeliveryVehicleLog1();
    DeliveryVehicleData expectedDeliveryVehicleData =
        DeliveryVehicleData.builder()
            .setDeliveryVehicleId("testDeliveryVehicleId1")
            .setName("providers/test-123/deliveryVehicles/testDeliveryVehicleId1")
            .build();
    DeliveryVehicleFleetEvent expectedDeliveryVehicleFleetEvent =
        DeliveryVehicleFleetEvent.builder()
            .setDeliveryVehicleId("testDeliveryVehicleId1")
            .setNewDeliveryVehicle(expectedDeliveryVehicleData)
            .build();
    OutputEvent expectedOutputEvent = new OutputEvent();
    expectedOutputEvent.setFleetEvent(expectedDeliveryVehicleFleetEvent);

    FleetEventCreator spyFleetEventCreator = Mockito.spy(new MockFleetEventCreator());
    FirestoreDatabaseClient mockFirestore = spyFleetEventCreator.getDatabase();

    doReturn(ApiFutures.immediateFuture(ImmutableList.of(expectedOutputEvent)))
        .when(mockFirestore)
        .runTransaction(any(Function.class));

    List<OutputEvent> outputEvents =
        spyFleetEventCreator.processCloudLog(logEntry, ImmutableList.of());
    assertEquals(ImmutableList.of(expectedOutputEvent), outputEvents);
  }

  @Test
  public void updateTaskLog_routedCorrectly()
      throws IOException, ExecutionException, InterruptedException {
    /* Tests whether the cloudLogEntry is correctly routing log entries based on log name. */
    LogEntry logEntry = FleetEventsTestHelper.updateTaskLog1();
    DeliveryVehicleData expectedDeliveryVehicleData =
        DeliveryVehicleData.builder()
            .setDeliveryVehicleId("testDeliveryVehicleId1")
            .setName("providers/test-123/deliveryVehicles/testDeliveryVehicleId1")
            .build();
    DeliveryTaskData expectedDeliveryTaskData =
        DeliveryTaskData.builder()
            .setDeliveryVehicleId("testDeliveryVehicleId1")
            .setDeliveryTaskId("testDeliveryTaskId1")
            .setName("providers/test-123/deliveryVehicles/testDeliveryVehicleId1")
            .build();
    DeliveryTaskFleetEvent expectedDeliveryTaskFleetEvent =
        DeliveryTaskFleetEvent.builder()
            .setDeliveryTaskId("testDeliveryTaskId1")
            .setNewDeliveryTask(expectedDeliveryTaskData)
            .setNewDeliveryVehicle(expectedDeliveryVehicleData)
            .setTaskMovedFromCurrentToPlanned(false)
            .build();
    OutputEvent expectedOutputEvent = new OutputEvent();
    expectedOutputEvent.setFleetEvent(expectedDeliveryTaskFleetEvent);

    FleetEventCreator spyFleetEventCreator = Mockito.spy(new MockFleetEventCreator());
    FirestoreDatabaseClient mockFirestore = spyFleetEventCreator.getDatabase();

    doReturn(ApiFutures.immediateFuture(ImmutableList.of(expectedOutputEvent)))
        .when(mockFirestore)
        .runTransaction(any(Function.class));

    List<OutputEvent> outputEvents =
        spyFleetEventCreator.processCloudLog(logEntry, ImmutableList.of());
    assertEquals(ImmutableList.of(expectedOutputEvent), outputEvents);
  }

  @Test
  public void createTaskLog_routedCorrectly()
      throws IOException, ExecutionException, InterruptedException {
    /* Tests whether the cloudLogEntry is correctly routing log entries based on log name. */
    LogEntry logEntry = FleetEventsTestHelper.createTaskLog1();
    DeliveryTaskData expectedDeliveryTaskData =
        DeliveryTaskData.builder()
            .setDeliveryVehicleId("testDeliveryVehicleId1")
            .setDeliveryTaskId("testDeliveryTaskId1")
            .setName("providers/test-123/deliveryVehicles/testDeliveryVehicleId1")
            .build();
    DeliveryTaskFleetEvent expectedDeliveryTaskFleetEvent =
        DeliveryTaskFleetEvent.builder()
            .setDeliveryTaskId("testDeliveryTaskId1")
            .setNewDeliveryTask(expectedDeliveryTaskData)
            .build();
    OutputEvent expectedOutputEvent = new OutputEvent();
    expectedOutputEvent.setFleetEvent(expectedDeliveryTaskFleetEvent);

    FleetEventCreator spyFleetEventCreator = Mockito.spy(new MockFleetEventCreator());
    FirestoreDatabaseClient mockFirestore = spyFleetEventCreator.getDatabase();

    doReturn(ApiFutures.immediateFuture(ImmutableList.of(expectedOutputEvent)))
        .when(mockFirestore)
        .runTransaction(any(Function.class));

    List<OutputEvent> outputEvents =
        spyFleetEventCreator.processCloudLog(logEntry, ImmutableList.of());
    assertEquals(ImmutableList.of(expectedOutputEvent), outputEvents);
  }

  @Test
  public void batchCreateTasksLog_routedCorrectly()
      throws IOException, ExecutionException, InterruptedException {
    /* Tests whether the cloudLogEntry is correctly routing log entries based on log name. */
    LogEntry logEntry = FleetEventsTestHelper.batchCreateTasksLog1();
    DeliveryTaskData expectedDeliveryTaskData1 =
        DeliveryTaskData.builder()
            .setDeliveryTaskId("testDeliveryTaskId1")
            .setName("providers/test-123/tasks/testDeliveryTaskId1")
            .build();
    DeliveryTaskData expectedDeliveryTaskData2 =
        DeliveryTaskData.builder()
            .setDeliveryTaskId("testDeliveryTaskId2")
            .setName("providers/test-123/tasks/testDeliveryTaskId2")
            .build();
    DeliveryTaskFleetEvent expectedDeliveryTaskFleetEvent1 =
        DeliveryTaskFleetEvent.builder()
            .setDeliveryTaskId("testDeliveryTaskId1")
            .setNewDeliveryTask(expectedDeliveryTaskData1)
            .build();
    DeliveryTaskFleetEvent expectedDeliveryTaskFleetEvent2 =
        DeliveryTaskFleetEvent.builder()
            .setDeliveryTaskId("testDeliveryTaskId1")
            .setNewDeliveryTask(expectedDeliveryTaskData2)
            .build();
    OutputEvent expectedOutputEvent1 = new OutputEvent();
    expectedOutputEvent1.setFleetEvent(expectedDeliveryTaskFleetEvent1);
    OutputEvent expectedOutputEvent2 = new OutputEvent();
    expectedOutputEvent2.setFleetEvent(expectedDeliveryTaskFleetEvent2);

    FleetEventCreator spyFleetEventCreator = Mockito.spy(new MockFleetEventCreator());
    FirestoreDatabaseClient mockFirestore = spyFleetEventCreator.getDatabase();

    doReturn(
            ApiFutures.immediateFuture(
                ImmutableList.of(expectedOutputEvent1, expectedOutputEvent2)))
        .when(mockFirestore)
        .runTransaction(any(Function.class));

    List<OutputEvent> outputEvents =
        spyFleetEventCreator.processCloudLog(logEntry, ImmutableList.of());
    assertEquals(ImmutableList.of(expectedOutputEvent1, expectedOutputEvent2), outputEvents);
  }

  @Test
  public void processCloudLogEntry_returnsEmptyForNonFleetLogs()
      throws IOException, ExecutionException, InterruptedException {
    /* Tests whether the cloudLogEntry is correctly routing log entries based on log name. */
    LogEntry logEntry = LogEntry.newBuilder().setLogName("test123").build();

    FleetEventCreator spyFleetEventCreator = Mockito.spy(new MockFleetEventCreator());

    List<OutputEvent> outputEvents =
        spyFleetEventCreator.processCloudLog(logEntry, ImmutableList.of());
    assertEquals(Collections.emptyList(), outputEvents);
  }

  @Test
  public void addExtraInfo_addPlannedLocationToTask() {
    FleetEventCreator spyFleetEventCreator = Mockito.spy(new MockFleetEventCreator());
    FleetEngineClient mockFleetEngineClient = spyFleetEventCreator.getFleetEngineClient();

    Task task =
        Task.newBuilder()
            .setPlannedLocation(
                LocationInfo.newBuilder()
                    .setPoint(LatLng.newBuilder().setLatitude(111).setLongitude(222)))
            .build();
    doReturn(task).when(mockFleetEngineClient).getTask(any(String.class));

    DeliveryTaskData taskData =
        DeliveryTaskData.builder()
            .setDeliveryVehicleId("testDeliveryVehicleId1")
            .setDeliveryTaskId("testDeliveryTaskId1")
            .setName("providers/test-123/deliveryVehicles/testDeliveryVehicleId1")
            .build();
    DeliveryTaskFleetEvent taskFleetEvent =
        DeliveryTaskFleetEvent.builder()
            .setDeliveryTaskId("testDeliveryTaskId1")
            .setNewDeliveryTask(taskData)
            .build();
    OutputEvent outputEvent = new OutputEvent();
    outputEvent.setFleetEvent(taskFleetEvent);
    List<OutputEvent> outputEvents = Arrays.asList(outputEvent);
    spyFleetEventCreator.addExtraInfo(outputEvents);
    assertEquals(outputEvents.size(), 1);
    OutputEvent enrichedOutputEvent = outputEvents.get(0);
    DeliveryTaskFleetEvent deliveryTaskFleetEvent =
        (DeliveryTaskFleetEvent) enrichedOutputEvent.getFleetEvent();
    assertEquals(
        deliveryTaskFleetEvent.plannedLocation().getLatitute().doubleValue(),
        task.getPlannedLocation().getPoint().getLatitude(),
        0);
    assertEquals(
        deliveryTaskFleetEvent.plannedLocation().getLongitude().doubleValue(),
        task.getPlannedLocation().getPoint().getLongitude(),
        0);
  }
}
