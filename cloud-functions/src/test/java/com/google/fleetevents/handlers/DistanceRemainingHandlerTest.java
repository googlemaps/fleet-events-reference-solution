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
import com.google.fleetevents.models.outputs.DistanceRemainingOutputEvent;
import com.google.fleetevents.models.outputs.OutputEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Tests for the distance remaining fleet event handler.
 */
public class DistanceRemainingHandlerTest {

  @Test
  public void updateVehicleNoDistance_doesNotRespond() {
    DeliveryVehicleFleetEvent deliveryTaskFleetEvent =
        DeliveryVehicleFleetEvent.builder()
            .setDeliveryVehicleId("testDeliveryVehicleId1")
            .setOldDeliveryVehicle(DeliveryVehicleData.builder().build())
            .setNewDeliveryVehicle(DeliveryVehicleData.builder().build())
            .build();
    DistanceRemainingHandler distanceRemainingHandler = new DistanceRemainingHandler();
    FirestoreDatabaseClient firestoreDatabaseClient = Mockito.mock(FirestoreDatabaseClient.class);
    doReturn(null).when(firestoreDatabaseClient).getVehicleDocument(any(String.class));
    assertFalse(
        distanceRemainingHandler.respondsTo(deliveryTaskFleetEvent, null, firestoreDatabaseClient));
  }

  @Test
  public void updateVehicleDistanceNotCrossThreshold_doesNotReport() {
    Map<String, Change> vehicleDifferences = new HashMap<>();
    vehicleDifferences.put("remainingDistanceMeters", new Change(150L, 140L));
    var taskInfo = new TaskInfo();
    taskInfo.setTaskId("testDeliveryTaskId1");
    var vehicleStop = new VehicleStop();
    var taskInfos = new ArrayList<TaskInfo>();
    taskInfos.add(taskInfo);
    vehicleStop.setTaskInfos(taskInfos);
    var vehicleJourneySegment = VehicleJourneySegment.builder().setVehicleStop(vehicleStop).build();
    var vehicleJourneySegments = new ArrayList<VehicleJourneySegment>();
    vehicleJourneySegments.add(vehicleJourneySegment);

    var oldEventMetadata = new HashMap<String, Object>();
    var oldDistanceRemainingHashMap = new HashMap<String, Long>();
    oldDistanceRemainingHashMap.put("testDeliveryTaskId1", 150L);
    oldEventMetadata.put("distanceRemaining", oldDistanceRemainingHashMap);

    var oldDeliveryVehicleInfo =
        DeliveryVehicleData.builder()
            .setRemainingDistanceMeters(150L)
            .setEventTimestamp(1000L)
            .setRemainingVehicleJourneySegments(vehicleJourneySegments)
            .setEventMetadata(oldEventMetadata)
            .build();

    var newDeliveryVehicleInfo =
        DeliveryVehicleData.builder()
            .setRemainingDistanceMeters(140L)
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
    DistanceRemainingHandler distanceRemainingHandler = new DistanceRemainingHandler();
    assertTrue(distanceRemainingHandler.respondsTo(deliveryVehicleFleetEvent, null, null));
    assertEquals(
        new ArrayList<>(), distanceRemainingHandler.handleEvent(deliveryVehicleFleetEvent, null));
  }

  @Test
  public void updateVehicleDistancePassedThresholdDoesNotCrossThreshold_doesNotReport() {
    Map<String, Change> vehicleDifferences = new HashMap<>();
    vehicleDifferences.put("remainingDistanceMeters", new Change(99L, 80L));
    var taskInfo = new TaskInfo();
    taskInfo.setTaskId("testDeliveryTaskId1");
    var vehicleStop = new VehicleStop();
    var taskInfos = new ArrayList<TaskInfo>();
    taskInfos.add(taskInfo);
    vehicleStop.setTaskInfos(taskInfos);
    var vehicleJourneySegment = VehicleJourneySegment.builder().setVehicleStop(vehicleStop).build();
    var vehicleJourneySegments = new ArrayList<VehicleJourneySegment>();
    vehicleJourneySegments.add(vehicleJourneySegment);

    var oldEventMetadata = new HashMap<String, Object>();
    var oldDistanceRemainingHashMap = new HashMap<String, Long>();
    oldDistanceRemainingHashMap.put("testDeliveryTaskId1", 99L);
    oldEventMetadata.put("distanceRemaining", oldDistanceRemainingHashMap);

    var oldDeliveryVehicleInfo =
        DeliveryVehicleData.builder()
            .setRemainingDistanceMeters(99L)
            .setEventTimestamp(1000L)
            .setRemainingVehicleJourneySegments(vehicleJourneySegments)
            .setEventMetadata(oldEventMetadata)
            .build();

    var newDeliveryVehicleInfo =
        DeliveryVehicleData.builder()
            .setRemainingDistanceMeters(80L)
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
    DistanceRemainingHandler distanceRemainingHandler = new DistanceRemainingHandler();
    assertTrue(distanceRemainingHandler.respondsTo(deliveryVehicleFleetEvent, null, null));
    assertEquals(
        new ArrayList<>(), distanceRemainingHandler.handleEvent(deliveryVehicleFleetEvent, null));
  }

  @Test
  public void updateVehicleDistancePassedThresholdCrossesThresholdInReverse_doesNotReport() {
    Map<String, Change> vehicleDifferences = new HashMap<>();
    vehicleDifferences.put("remainingDistanceMeters", new Change(800L, 1001L));
    var taskInfo = new TaskInfo();
    taskInfo.setTaskId("testDeliveryTaskId1");
    var vehicleStop = new VehicleStop();
    var taskInfos = new ArrayList<TaskInfo>();
    taskInfos.add(taskInfo);
    vehicleStop.setTaskInfos(taskInfos);
    var vehicleJourneySegment = VehicleJourneySegment.builder().setVehicleStop(vehicleStop).build();
    var vehicleJourneySegments = new ArrayList<VehicleJourneySegment>();
    vehicleJourneySegments.add(vehicleJourneySegment);

    var oldEventMetadata = new HashMap<String, Object>();
    var oldDistanceRemainingHashMap = new HashMap<String, Long>();
    oldDistanceRemainingHashMap.put("testDeliveryTaskId1", 800L);
    oldEventMetadata.put("distanceRemaining", oldDistanceRemainingHashMap);

    var oldDeliveryVehicleInfo =
        DeliveryVehicleData.builder()
            .setRemainingDistanceMeters(800L)
            .setEventTimestamp(1000L)
            .setRemainingVehicleJourneySegments(vehicleJourneySegments)
            .setEventMetadata(oldEventMetadata)
            .build();

    var newDeliveryVehicleInfo =
        DeliveryVehicleData.builder()
            .setRemainingDistanceMeters(1001L)
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
    DistanceRemainingHandler distanceRemainingHandler = new DistanceRemainingHandler();
    assertTrue(distanceRemainingHandler.respondsTo(deliveryVehicleFleetEvent, null, null));
    assertEquals(
        new ArrayList<>(), distanceRemainingHandler.handleEvent(deliveryVehicleFleetEvent, null));
  }

  @Test
  public void updateVehicleDistanceCrossesThreshold_returnsOutputEvent() {
    Map<String, Change> vehicleDifferences = new HashMap<>();
    vehicleDifferences.put("remainingDistanceMeters", new Change(1500L, 900L));
    var taskInfo = new TaskInfo();
    taskInfo.setTaskId("testDeliveryTaskId1");
    var vehicleStop = new VehicleStop();
    var taskInfos = new ArrayList<TaskInfo>();
    taskInfos.add(taskInfo);
    vehicleStop.setTaskInfos(taskInfos);
    var vehicleJourneySegment = VehicleJourneySegment.builder().setVehicleStop(vehicleStop).build();
    var vehicleJourneySegments = new ArrayList<VehicleJourneySegment>();
    vehicleJourneySegments.add(vehicleJourneySegment);

    var oldEventMetadata = new HashMap<String, Object>();
    var oldDistanceRemainingHashMap = new HashMap<String, Long>();
    oldDistanceRemainingHashMap.put("testDeliveryTaskId1", 1500L);
    oldEventMetadata.put("distanceRemaining", oldDistanceRemainingHashMap);

    var oldDeliveryVehicleInfo =
        DeliveryVehicleData.builder()
            .setRemainingDistanceMeters(1500L)
            .setEventTimestamp(1000L)
            .setRemainingVehicleJourneySegments(vehicleJourneySegments)
            .setEventMetadata(oldEventMetadata)
            .build();

    var newDeliveryVehicleInfo =
        DeliveryVehicleData.builder()
            .setRemainingDistanceMeters(900L)
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

    DistanceRemainingHandler distanceRemainingHandler = new DistanceRemainingHandler();
    assertTrue(distanceRemainingHandler.respondsTo(deliveryVehicleFleetEvent, null, null));
    List<OutputEvent> outputEventList = new ArrayList<>();

    var distanceRemainingOutputEvent = new DistanceRemainingOutputEvent();
    distanceRemainingOutputEvent.setEventTimestamp(1000L);
    distanceRemainingOutputEvent.setOldDistanceRemainingMeters(1500L);
    distanceRemainingOutputEvent.setNewDistanceRemainingMeters(900L);
    distanceRemainingOutputEvent.setTaskId("testDeliveryTaskId1");
    distanceRemainingOutputEvent.setFleetEvent(deliveryVehicleFleetEvent);

    outputEventList.add(distanceRemainingOutputEvent);

    assertEquals(
        outputEventList, distanceRemainingHandler.handleEvent(deliveryVehicleFleetEvent, null));
  }

  @Test
  public void updateVehicleDistanceCrossesThresholdNoPreviousMetadata_returnsOutputEvent() {
    Map<String, Change> vehicleDifferences = new HashMap<>();
    vehicleDifferences.put("remainingDistanceMeters", new Change(0L, 900L));
    var taskInfo = new TaskInfo();
    taskInfo.setTaskId("testDeliveryTaskId1");
    var vehicleStop = new VehicleStop();
    var taskInfos = new ArrayList<TaskInfo>();
    taskInfos.add(taskInfo);
    vehicleStop.setTaskInfos(taskInfos);
    var vehicleJourneySegment = VehicleJourneySegment.builder().setVehicleStop(vehicleStop).build();
    var vehicleJourneySegments = new ArrayList<VehicleJourneySegment>();
    vehicleJourneySegments.add(vehicleJourneySegment);

    var oldDeliveryVehicleInfo =
        DeliveryVehicleData.builder()
            .setRemainingDistanceMeters(0L)
            .setEventTimestamp(1000L)
            .setRemainingVehicleJourneySegments(vehicleJourneySegments)
            .setEventMetadata(new HashMap<>())
            .build();

    var newDeliveryVehicleInfo =
        DeliveryVehicleData.builder()
            .setRemainingDistanceMeters(900L)
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

    DistanceRemainingHandler distanceRemainingHandler = new DistanceRemainingHandler();
    assertTrue(distanceRemainingHandler.respondsTo(deliveryVehicleFleetEvent, null, null));
    List<OutputEvent> outputEventList = new ArrayList<>();

    var timeRemainingOutputEvent = new DistanceRemainingOutputEvent();
    timeRemainingOutputEvent.setEventTimestamp(1000L);
    timeRemainingOutputEvent.setOldDistanceRemainingMeters(0L);
    timeRemainingOutputEvent.setNewDistanceRemainingMeters(900L);
    timeRemainingOutputEvent.setTaskId("testDeliveryTaskId1");
    timeRemainingOutputEvent.setFleetEvent(deliveryVehicleFleetEvent);

    outputEventList.add(timeRemainingOutputEvent);

    assertEquals(
        outputEventList, distanceRemainingHandler.handleEvent(deliveryVehicleFleetEvent, null));
  }
}
