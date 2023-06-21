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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;

import com.google.fleetevents.database.FirestoreDatabaseClient;
import com.google.fleetevents.models.Change;
import com.google.fleetevents.models.DeliveryVehicleData;
import com.google.fleetevents.models.DeliveryVehicleFleetEvent;
import com.google.fleetevents.models.TaskInfo;
import com.google.fleetevents.models.VehicleJourneySegment;
import com.google.fleetevents.models.VehicleStop;
import com.google.fleetevents.models.outputs.OutputEvent;
import com.google.fleetevents.models.outputs.TimeRemainingOutputEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Tests for the time remaining fleet event handler.
 */
public class TimeRemainingHandlerTest {

  @Test
  public void updateVehicleNoDuration_doesNotRespond() {
    DeliveryVehicleFleetEvent deliveryTaskFleetEvent =
        DeliveryVehicleFleetEvent.builder()
            .setDeliveryVehicleId("testDeliveryVehicleId1")
            .setOldDeliveryVehicle(DeliveryVehicleData.builder().build())
            .setNewDeliveryVehicle(DeliveryVehicleData.builder().build())
            .build();
    TimeRemainingHandler timeRemainingHandler = new TimeRemainingHandler();
    FirestoreDatabaseClient firestoreDatabaseClient = Mockito.mock(FirestoreDatabaseClient.class);
    doReturn(null).when(firestoreDatabaseClient).getVehicleDocument(any(String.class));
    assertFalse(
        timeRemainingHandler.respondsTo(deliveryTaskFleetEvent, null, firestoreDatabaseClient));
  }

  @Test
  public void updateVehicleDurationNotCrossThreshold_doesNotReport() {
    Map<String, Change> vehicleDifferences = new HashMap<>();
    vehicleDifferences.put("remainingDuration", new Change(350000L, 310000L));

    var taskInfo = new TaskInfo();
    taskInfo.setTaskId("testDeliveryTaskId1");
    taskInfo.setTaskDuration(500L);
    var vehicleStop = new VehicleStop();
    var taskInfos = new ArrayList<TaskInfo>();
    taskInfos.add(taskInfo);
    vehicleStop.setTaskInfos(taskInfos);
    var vehicleJourneySegment = VehicleJourneySegment.builder().setVehicleStop(vehicleStop).build();
    var vehicleJourneySegments = new ArrayList<VehicleJourneySegment>();
    vehicleJourneySegments.add(vehicleJourneySegment);

    var oldEventMetadata = new HashMap<String, Object>();
    var oldTimeRemainingHashMap = new HashMap<String, Long>();
    oldTimeRemainingHashMap.put("testDeliveryTaskId1", 350500L);
    oldEventMetadata.put("timeRemaining", oldTimeRemainingHashMap);

    var oldDeliveryVehicleInfo =
        DeliveryVehicleData.builder()
            .setRemainingDuration(350000L)
            .setEventTimestamp(1000L)
            .setRemainingVehicleJourneySegments(vehicleJourneySegments)
            .setEventMetadata(oldEventMetadata)
            .build();

    var newDeliveryVehicleInfo =
        DeliveryVehicleData.builder()
            .setRemainingDuration(310000L)
            .setEventTimestamp(1000L)
            .setRemainingVehicleJourneySegments(vehicleJourneySegments)
            .build();

    DeliveryVehicleFleetEvent deliveryVehicleFleetEvent =
        DeliveryVehicleFleetEvent.builder()
            .setDeliveryVehicleId("testDeliveryVehicleId1")
            .setOldDeliveryVehicle(oldDeliveryVehicleInfo)
            .setNewDeliveryVehicle(newDeliveryVehicleInfo)
            .setVehicleDifferences(vehicleDifferences)
            .build();
    TimeRemainingHandler timeRemainingHandler = new TimeRemainingHandler();
    assertTrue(timeRemainingHandler.respondsTo(deliveryVehicleFleetEvent, null, null));
    assertEquals(
        new ArrayList<>(), timeRemainingHandler.handleEvent(deliveryVehicleFleetEvent, null));
  }

  @Test
  public void updateVehicleDurationPassedThresholdDoesNotCrossThreshold_doesNotReport() {
    Map<String, Change> vehicleDifferences = new HashMap<>();
    vehicleDifferences.put("remainingDuration", new Change(150000L, 110000L));
    var taskInfo = new TaskInfo();
    taskInfo.setTaskId("testDeliveryTaskId1");
    taskInfo.setTaskDuration(500L);
    var vehicleStop = new VehicleStop();
    var taskInfos = new ArrayList<TaskInfo>();
    taskInfos.add(taskInfo);
    vehicleStop.setTaskInfos(taskInfos);
    var vehicleJourneySegment = VehicleJourneySegment.builder().setVehicleStop(vehicleStop).build();
    var vehicleJourneySegments = new ArrayList<VehicleJourneySegment>();
    vehicleJourneySegments.add(vehicleJourneySegment);

    var oldEventMetadata = new HashMap<String, Object>();
    var oldTimeRemainingHashMap = new HashMap<String, Long>();
    oldTimeRemainingHashMap.put("testDeliveryTaskId1", 150500L);
    oldEventMetadata.put("timeRemaining", oldTimeRemainingHashMap);

    var oldDeliveryVehicleInfo =
        DeliveryVehicleData.builder()
            .setRemainingDuration(150000L)
            .setEventTimestamp(1000L)
            .setRemainingVehicleJourneySegments(vehicleJourneySegments)
            .setEventMetadata(oldEventMetadata)
            .build();

    var newDeliveryVehicleInfo =
        DeliveryVehicleData.builder()
            .setRemainingDuration(110000L)
            .setEventTimestamp(1000L)
            .setRemainingVehicleJourneySegments(vehicleJourneySegments)
            .build();
    DeliveryVehicleFleetEvent deliveryVehicleFleetEvent =
        DeliveryVehicleFleetEvent.builder()
            .setDeliveryVehicleId("testDeliveryVehicleId1")
            .setOldDeliveryVehicle(oldDeliveryVehicleInfo)
            .setNewDeliveryVehicle(newDeliveryVehicleInfo)
            .setVehicleDifferences(vehicleDifferences)
            .build();
    TimeRemainingHandler timeRemainingHandler = new TimeRemainingHandler();
    assertTrue(timeRemainingHandler.respondsTo(deliveryVehicleFleetEvent, null, null));
    assertEquals(
        new ArrayList<>(), timeRemainingHandler.handleEvent(deliveryVehicleFleetEvent, null));
  }

  @Test
  public void updateVehicleDurationCrossesThreshold_returnsOutputEvent() {
    Map<String, Change> vehicleDifferences = new HashMap<>();
    vehicleDifferences.put("remainingDuration", new Change(310000L, 290000L));
    var taskInfo = new TaskInfo();
    taskInfo.setTaskId("testDeliveryTaskId1");
    taskInfo.setTaskDuration(500L);
    var vehicleStop = new VehicleStop();
    var taskInfos = new ArrayList<TaskInfo>();
    taskInfos.add(taskInfo);
    vehicleStop.setTaskInfos(taskInfos);
    var vehicleJourneySegment = VehicleJourneySegment.builder().setVehicleStop(vehicleStop).build();
    var vehicleJourneySegments = new ArrayList<VehicleJourneySegment>();
    vehicleJourneySegments.add(vehicleJourneySegment);

    var oldEventMetadata = new HashMap<String, Object>();
    var oldTimeRemainingHashMap = new HashMap<String, Long>();
    oldTimeRemainingHashMap.put("testDeliveryTaskId1", 310500L);
    oldEventMetadata.put("timeRemaining", oldTimeRemainingHashMap);

    var oldDeliveryVehicleInfo =
        DeliveryVehicleData.builder()
            .setRemainingDuration(310000L)
            .setEventTimestamp(1000L)
            .setRemainingVehicleJourneySegments(vehicleJourneySegments)
            .setEventMetadata(oldEventMetadata)
            .build();

    var newDeliveryVehicleInfo =
        DeliveryVehicleData.builder()
            .setRemainingDuration(290000L)
            .setEventTimestamp(1000L)
            .setRemainingVehicleJourneySegments(vehicleJourneySegments)
            .build();
    DeliveryVehicleFleetEvent deliveryVehicleFleetEvent =
        DeliveryVehicleFleetEvent.builder()
            .setDeliveryVehicleId("testDeliveryVehicleId1")
            .setOldDeliveryVehicle(oldDeliveryVehicleInfo)
            .setNewDeliveryVehicle(newDeliveryVehicleInfo)
            .setVehicleDifferences(vehicleDifferences)
            .build();

    TimeRemainingHandler timeRemainingHandler = new TimeRemainingHandler();
    assertTrue(timeRemainingHandler.respondsTo(deliveryVehicleFleetEvent, null, null));
    List<OutputEvent> outputEventList = new ArrayList<>();

    var timeRemainingOutputEvent = new TimeRemainingOutputEvent();
    timeRemainingOutputEvent.setEventTimestamp(1000L);
    timeRemainingOutputEvent.setOldTimeRemainingSeconds(310500L);
    timeRemainingOutputEvent.setNewTimeRemainingSeconds(290500L);
    timeRemainingOutputEvent.setTaskId("testDeliveryTaskId1");
    timeRemainingOutputEvent.setFleetEvent(deliveryVehicleFleetEvent);

    outputEventList.add(timeRemainingOutputEvent);

    assertEquals(
        outputEventList, timeRemainingHandler.handleEvent(deliveryVehicleFleetEvent, null));
  }

  @Test
  public void updateVehicleDurationCrossesThresholdNoPreviousMetadata_returnsOutputEvent() {
    Map<String, Change> vehicleDifferences = new HashMap<>();
    vehicleDifferences.put("remainingDuration", new Change(0L, 290000L));
    var taskInfo = new TaskInfo();
    taskInfo.setTaskId("testDeliveryTaskId1");
    taskInfo.setTaskDuration(500L);
    var vehicleStop = new VehicleStop();
    var taskInfos = new ArrayList<TaskInfo>();
    taskInfos.add(taskInfo);
    vehicleStop.setTaskInfos(taskInfos);
    var vehicleJourneySegment = VehicleJourneySegment.builder().setVehicleStop(vehicleStop).build();
    var vehicleJourneySegments = new ArrayList<VehicleJourneySegment>();
    vehicleJourneySegments.add(vehicleJourneySegment);

    var oldDeliveryVehicleInfo =
        DeliveryVehicleData.builder()
            .setRemainingDuration(310000L)
            .setEventTimestamp(1000L)
            .setRemainingVehicleJourneySegments(vehicleJourneySegments)
            .setEventMetadata(new HashMap<>())
            .build();

    var newDeliveryVehicleInfo =
        DeliveryVehicleData.builder()
            .setRemainingDuration(290000L)
            .setEventTimestamp(1000L)
            .setRemainingVehicleJourneySegments(vehicleJourneySegments)
            .build();
    DeliveryVehicleFleetEvent deliveryVehicleFleetEvent =
        DeliveryVehicleFleetEvent.builder()
            .setDeliveryVehicleId("testDeliveryVehicleId1")
            .setOldDeliveryVehicle(oldDeliveryVehicleInfo)
            .setNewDeliveryVehicle(newDeliveryVehicleInfo)
            .setVehicleDifferences(vehicleDifferences)
            .build();

    TimeRemainingHandler timeRemainingHandler = new TimeRemainingHandler();
    assertTrue(timeRemainingHandler.respondsTo(deliveryVehicleFleetEvent, null, null));
    List<OutputEvent> outputEventList = new ArrayList<>();

    var timeRemainingOutputEvent = new TimeRemainingOutputEvent();
    timeRemainingOutputEvent.setEventTimestamp(1000L);
    timeRemainingOutputEvent.setOldTimeRemainingSeconds(0L);
    timeRemainingOutputEvent.setNewTimeRemainingSeconds(290500L);
    timeRemainingOutputEvent.setTaskId("testDeliveryTaskId1");
    timeRemainingOutputEvent.setFleetEvent(deliveryVehicleFleetEvent);

    outputEventList.add(timeRemainingOutputEvent);

    assertEquals(
        outputEventList, timeRemainingHandler.handleEvent(deliveryVehicleFleetEvent, null));
  }

  @Test
  public void updateVehicleDurationPassedThresholdCrossesThresholdInReverse_doesNotReport() {
    Map<String, Change> vehicleDifferences = new HashMap<>();
    vehicleDifferences.put("remainingDuration", new Change(110000L, 150000L));
    var taskInfo = new TaskInfo();
    taskInfo.setTaskId("testDeliveryTaskId1");
    taskInfo.setTaskDuration(500L);
    var vehicleStop = new VehicleStop();
    var taskInfos = new ArrayList<TaskInfo>();
    taskInfos.add(taskInfo);
    vehicleStop.setTaskInfos(taskInfos);
    var vehicleJourneySegment = VehicleJourneySegment.builder().setVehicleStop(vehicleStop).build();
    var vehicleJourneySegments = new ArrayList<VehicleJourneySegment>();
    vehicleJourneySegments.add(vehicleJourneySegment);

    var oldEventMetadata = new HashMap<String, Object>();
    var oldTimeRemainingHashMap = new HashMap<String, Long>();
    oldTimeRemainingHashMap.put("testDeliveryTaskId1", 110500L);
    oldEventMetadata.put("timeRemaining", oldTimeRemainingHashMap);

    var oldDeliveryVehicleInfo =
        DeliveryVehicleData.builder()
            .setRemainingDuration(110000L)
            .setEventTimestamp(1000L)
            .setRemainingVehicleJourneySegments(vehicleJourneySegments)
            .setEventMetadata(oldEventMetadata)
            .build();

    var newDeliveryVehicleInfo =
        DeliveryVehicleData.builder()
            .setRemainingDuration(150000L)
            .setEventTimestamp(1000L)
            .setRemainingVehicleJourneySegments(vehicleJourneySegments)
            .build();
    DeliveryVehicleFleetEvent deliveryVehicleFleetEvent =
        DeliveryVehicleFleetEvent.builder()
            .setDeliveryVehicleId("testDeliveryVehicleId1")
            .setOldDeliveryVehicle(oldDeliveryVehicleInfo)
            .setNewDeliveryVehicle(newDeliveryVehicleInfo)
            .setVehicleDifferences(vehicleDifferences)
            .build();
    TimeRemainingHandler timeRemainingHandler = new TimeRemainingHandler();
    assertTrue(timeRemainingHandler.respondsTo(deliveryVehicleFleetEvent, null, null));
    assertEquals(
        new ArrayList<>(), timeRemainingHandler.handleEvent(deliveryVehicleFleetEvent, null));
  }
}
