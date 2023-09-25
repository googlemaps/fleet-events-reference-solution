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

import com.google.fleetevents.common.models.OutputEvent;
import com.google.fleetevents.helpers.FleetEventsTestHelper;
import com.google.fleetevents.lmfs.models.DeliveryVehicleData;
import com.google.fleetevents.lmfs.models.DeliveryVehicleFleetEvent;
import com.google.fleetevents.lmfs.models.TaskInfo;
import com.google.fleetevents.lmfs.models.VehicleJourneySegment;
import com.google.fleetevents.lmfs.models.VehicleStop;
import com.google.fleetevents.lmfs.models.outputs.EtaOutputEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;

/** Tests for the ETA change fleet event handler. */
public class EtaChangeHandlerTest {

  private static final long ORIGINAL_ETA_THRESHOLD_MILLIS = 300000;
  private static final String RELATIVE_ETA_CHANGE_METADATA_ID = "relativeEtaChange";
  private static final String ETA_CHANGE_METADATA_ID = "etaChange";
  private final EtaChangeHandler handler = new EtaChangeHandler();

  public EtaChangeHandlerTest() {}

  private VehicleJourneySegment getOneStopOneTask(
      Long vjsDuration, Long taskDuration, String taskName) {
    return VehicleJourneySegment.builder()
        .setVehicleStop(
            new VehicleStop.Builder()
                .setTaskInfos(
                    List.of(
                        new TaskInfo.Builder()
                            .setTaskId(taskName)
                            .setTaskDuration(taskDuration)
                            .build()))
                .build())
        .setDuration(vjsDuration)
        .build();
  }

  @Test
  public void testSimpleEtaChange() throws IOException {
    DeliveryVehicleData oldDeliveryVehicleInfo =
        FleetEventsTestHelper.updateDeliveryVehicleData1().toBuilder().build();
    DeliveryVehicleData newDeliveryVehicleInfo =
        FleetEventsTestHelper.updateDeliveryVehicleData1().toBuilder()
            .setRemainingDuration(ORIGINAL_ETA_THRESHOLD_MILLIS * 1234)
            .addRemainingVehicleJourneySegment(
                getOneStopOneTask(ORIGINAL_ETA_THRESHOLD_MILLIS * 5, 0L, "taskId1"))
            .setEventTimestamp(0L)
            .build();
    DeliveryVehicleFleetEvent deliveryVehicleFleetEvent =
        DeliveryVehicleFleetEvent.builder()
            .setDeliveryVehicleId("testDeliveryVehicleId1")
            .setNewDeliveryVehicle(newDeliveryVehicleInfo)
            .setOldDeliveryVehicle(oldDeliveryVehicleInfo)
            .build();
    List<OutputEvent> outputs = this.handler.handleEvent(deliveryVehicleFleetEvent, null);
    List<OutputEvent> expecteds = new ArrayList();
    expecteds.add(
        (new EtaOutputEvent.Builder())
            .setOriginalEta(ORIGINAL_ETA_THRESHOLD_MILLIS * 1234)
            .setType(EtaOutputEvent.Type.ETA)
            .setNewEta(ORIGINAL_ETA_THRESHOLD_MILLIS * 1234)
            .setOriginalDuration(ORIGINAL_ETA_THRESHOLD_MILLIS * 1234)
            .setRelativeDelta(0.0F)
            .setDelta(0L)
            .setTaskId("taskId1")
            .build());
    expecteds.add(
        (new EtaOutputEvent.Builder())
            .setOriginalEta(ORIGINAL_ETA_THRESHOLD_MILLIS * 1234)
            .setType(EtaOutputEvent.Type.RELATIVE_ETA)
            .setNewEta(ORIGINAL_ETA_THRESHOLD_MILLIS * 1234)
            .setOriginalDuration(ORIGINAL_ETA_THRESHOLD_MILLIS * 1234)
            .setRelativeDelta(0.0F)
            .setDelta(0L)
            .setTaskId("taskId1")
            .build());
    Assert.assertEquals(expecteds, outputs);
  }

  @Test
  public void testSimpleEtaChangeWithEventTimestamp() throws IOException {
    DeliveryVehicleData oldDeliveryVehicleInfo =
        FleetEventsTestHelper.updateDeliveryVehicleData1().toBuilder().build();
    DeliveryVehicleData newDeliveryVehicleInfo =
        FleetEventsTestHelper.updateDeliveryVehicleData1().toBuilder()
            .setRemainingDuration(ORIGINAL_ETA_THRESHOLD_MILLIS * 1234)
            .addRemainingVehicleJourneySegment(
                getOneStopOneTask(ORIGINAL_ETA_THRESHOLD_MILLIS * 5, 0L, "taskId1"))
            .setEventTimestamp(1L)
            .build();
    DeliveryVehicleFleetEvent deliveryVehicleFleetEvent =
        DeliveryVehicleFleetEvent.builder()
            .setDeliveryVehicleId("testDeliveryVehicleId1")
            .setNewDeliveryVehicle(newDeliveryVehicleInfo)
            .setOldDeliveryVehicle(oldDeliveryVehicleInfo)
            .build();
    List<OutputEvent> outputs = this.handler.handleEvent(deliveryVehicleFleetEvent, null);
    List<OutputEvent> expecteds = new ArrayList();
    expecteds.add(
        (new EtaOutputEvent.Builder())
            .setOriginalEta(ORIGINAL_ETA_THRESHOLD_MILLIS * 1234 + 1L)
            .setType(EtaOutputEvent.Type.ETA)
            .setNewEta(ORIGINAL_ETA_THRESHOLD_MILLIS * 1234 + 1L)
            .setOriginalDuration(ORIGINAL_ETA_THRESHOLD_MILLIS * 1234)
            .setRelativeDelta(0.0F)
            .setDelta(0L)
            .setTaskId("taskId1")
            .build());
    expecteds.add(
        (new EtaOutputEvent.Builder())
            .setOriginalEta(ORIGINAL_ETA_THRESHOLD_MILLIS * 1234 + 1L)
            .setType(EtaOutputEvent.Type.RELATIVE_ETA)
            .setNewEta(ORIGINAL_ETA_THRESHOLD_MILLIS * 1234 + 1L)
            .setOriginalDuration(ORIGINAL_ETA_THRESHOLD_MILLIS * 1234)
            .setRelativeDelta(0.0F)
            .setDelta(0L)
            .setTaskId("taskId1")
            .build());
    Assert.assertEquals(expecteds, outputs);
  }

  @Test
  public void testSecondStopEtaChanges() throws IOException {
    String vehicleId = "vehicle1";
    Long originalEta = ORIGINAL_ETA_THRESHOLD_MILLIS;
    DeliveryVehicleData oldDeliveryVehicleInfo =
        FleetEventsTestHelper.updateDeliveryVehicleData1().toBuilder()
            .setDeliveryVehicleId(vehicleId)
            .setEventMetadata(new HashMap())
            .build();
    DeliveryVehicleData newDeliveryVehicleInfo =
        FleetEventsTestHelper.updateDeliveryVehicleData1().toBuilder()
            .setDeliveryVehicleId(vehicleId)
            .setRemainingDuration(0L)
            .addRemainingVehicleJourneySegment(getOneStopOneTask(originalEta * 3, 0L, "taskId1"))
            .addRemainingVehicleJourneySegment(getOneStopOneTask(originalEta * 3, 0L, "taskId2"))
            .setEventTimestamp(0L)
            .build();
    DeliveryVehicleFleetEvent deliveryVehicleFleetEvent =
        DeliveryVehicleFleetEvent.builder()
            .setDeliveryVehicleId("testDeliveryVehicleId1")
            .setNewDeliveryVehicle(newDeliveryVehicleInfo)
            .setOldDeliveryVehicle(oldDeliveryVehicleInfo)
            .build();
    setOriginalEta(oldDeliveryVehicleInfo, "taskId1", 0L);
    setOriginalEta(oldDeliveryVehicleInfo, "taskId2", originalEta);
    setOriginalDuration(oldDeliveryVehicleInfo, "taskId1", 0L);
    setOriginalDuration(oldDeliveryVehicleInfo, "taskId2", originalEta);
    List<OutputEvent> outputs = this.handler.handleEvent(deliveryVehicleFleetEvent, null);
    List<OutputEvent> expecteds = new ArrayList();
    expecteds.add(
        (new EtaOutputEvent.Builder())
            .setOriginalEta(originalEta)
            .setType(EtaOutputEvent.Type.ETA)
            .setNewEta(originalEta * 3)
            .setOriginalDuration(originalEta)
            .setRelativeDelta(2.0F)
            .setDelta((originalEta * 3) - originalEta)
            .setTaskId("taskId2")
            .build());
    expecteds.add(
        (new EtaOutputEvent.Builder())
            .setOriginalEta(originalEta)
            .setType(EtaOutputEvent.Type.RELATIVE_ETA)
            .setNewEta(originalEta * 3)
            .setOriginalDuration(originalEta)
            .setRelativeDelta(2.0F)
            .setDelta((originalEta * 3) - originalEta)
            .setTaskId("taskId2")
            .build());
    Assert.assertEquals(expecteds, outputs);
  }

  @Test
  public void testFirstStopEtaChanges() throws IOException {
    String vehicleId = "vehicle1";
    Long originalEta = ORIGINAL_ETA_THRESHOLD_MILLIS;
    DeliveryVehicleData oldDeliveryVehicleInfo =
        FleetEventsTestHelper.updateDeliveryVehicleData1().toBuilder()
            .setDeliveryVehicleId(vehicleId)
            .setEventMetadata(new HashMap())
            .build();
    DeliveryVehicleData newDeliveryVehicleInfo =
        FleetEventsTestHelper.updateDeliveryVehicleData1().toBuilder()
            .setDeliveryVehicleId(vehicleId)
            // brings this over threshold
            .setRemainingDuration(originalEta * 3)
            .addRemainingVehicleJourneySegment(getOneStopOneTask(0L, 0L, "taskId1"))
            .addRemainingVehicleJourneySegment(getOneStopOneTask(0L, 0L, "taskId2"))
            .setEventTimestamp(0L)
            .build();
    DeliveryVehicleFleetEvent deliveryVehicleFleetEvent =
        DeliveryVehicleFleetEvent.builder()
            .setDeliveryVehicleId("testDeliveryVehicleId1")
            .setNewDeliveryVehicle(newDeliveryVehicleInfo)
            .setOldDeliveryVehicle(oldDeliveryVehicleInfo)
            .build();
    setOriginalEta(oldDeliveryVehicleInfo, "taskId1", originalEta);
    setOriginalEta(oldDeliveryVehicleInfo, "taskId2", originalEta * 3);
    setOriginalDuration(oldDeliveryVehicleInfo, "taskId1", originalEta);
    setOriginalDuration(oldDeliveryVehicleInfo, "taskId2", originalEta * 3);
    List<OutputEvent> outputs = this.handler.handleEvent(deliveryVehicleFleetEvent, null);
    List<OutputEvent> expecteds = new ArrayList();
    expecteds.add(
        (new EtaOutputEvent.Builder())
            .setOriginalEta(originalEta)
            .setType(EtaOutputEvent.Type.ETA)
            .setNewEta(originalEta * 3)
            .setOriginalDuration(originalEta)
            .setRelativeDelta(2.0F)
            .setDelta((originalEta * 3) - originalEta)
            .setTaskId("taskId1")
            .build());
    expecteds.add(
        (new EtaOutputEvent.Builder())
            .setOriginalEta(originalEta)
            .setType(EtaOutputEvent.Type.RELATIVE_ETA)
            .setNewEta(originalEta * 3)
            .setOriginalDuration(originalEta)
            .setRelativeDelta(2.0F)
            .setDelta((originalEta * 3) - originalEta)
            .setTaskId("taskId1")
            .build());
    Assert.assertEquals(expecteds, outputs);
  }

  @Test
  public void testFirstStopEtaIncludesTaskDuration() throws IOException {
    String vehicleId = "vehicle1";
    DeliveryVehicleData oldDeliveryVehicleInfo =
        FleetEventsTestHelper.updateDeliveryVehicleData1().toBuilder()
            .setDeliveryVehicleId(vehicleId)
            .setEventMetadata(new HashMap())
            .build();
    Long originalEta = 1L;
    VehicleJourneySegment vjs1 = getOneStopOneTask(originalEta, 2L, "taskId1");
    VehicleJourneySegment vjs2 = getOneStopOneTask(originalEta, 3L, "taskId2");
    DeliveryVehicleData newDeliveryVehicleInfo =
        FleetEventsTestHelper.updateDeliveryVehicleData1().toBuilder()
            .setDeliveryVehicleId(vehicleId)
            .setRemainingDuration(ORIGINAL_ETA_THRESHOLD_MILLIS)
            .addRemainingVehicleJourneySegment(vjs1)
            .addRemainingVehicleJourneySegment(vjs2)
            .setEventTimestamp(0L)
            .setEventMetadata(new HashMap())
            .build();
    DeliveryVehicleFleetEvent deliveryVehicleFleetEvent =
        DeliveryVehicleFleetEvent.builder()
            .setDeliveryVehicleId("testDeliveryVehicleId1")
            .setNewDeliveryVehicle(newDeliveryVehicleInfo)
            .setOldDeliveryVehicle(oldDeliveryVehicleInfo)
            .build();
    setOriginalEta(oldDeliveryVehicleInfo, "taskId1", originalEta);
    setOriginalEta(
        oldDeliveryVehicleInfo, "taskId2", newDeliveryVehicleInfo.getRemainingDuration());
    setOriginalDuration(oldDeliveryVehicleInfo, "taskId1", originalEta);
    setOriginalDuration(
        oldDeliveryVehicleInfo, "taskId2", newDeliveryVehicleInfo.getRemainingDuration());
    List<OutputEvent> outputs = this.handler.handleEvent(deliveryVehicleFleetEvent, null);
    List<OutputEvent> expecteds = new ArrayList();
    expecteds.add(
        (new EtaOutputEvent.Builder())
            .setOriginalEta(originalEta)
            .setType(EtaOutputEvent.Type.ETA)
            .setNewEta(300002L)
            .setOriginalDuration(originalEta)
            .setRelativeDelta(300001.0F)
            .setDelta(300001L)
            .setTaskId("taskId1")
            .build());
    expecteds.add(
        (new EtaOutputEvent.Builder())
            .setOriginalEta(originalEta)
            .setType(EtaOutputEvent.Type.RELATIVE_ETA)
            .setNewEta(300002L)
            .setOriginalDuration(originalEta)
            .setRelativeDelta(300001.0F)
            .setDelta(300001L)
            .setTaskId("taskId1")
            .build());
    Assert.assertEquals(expecteds, outputs);
  }

  @Test
  public void testSecondStopEtaIncludesTaskDuration() throws IOException {
    String vehicleId = "vehicle1";
    DeliveryVehicleData oldDeliveryVehicleInfo =
        FleetEventsTestHelper.updateDeliveryVehicleData1().toBuilder()
            .setDeliveryVehicleId(vehicleId)
            .setEventMetadata(new HashMap())
            .build();
    Long originalEta = 1L;
    VehicleJourneySegment vjs1 = getOneStopOneTask(originalEta, 0L, "taskId1");
    VehicleJourneySegment vjs2 = getOneStopOneTask(ORIGINAL_ETA_THRESHOLD_MILLIS, 2L, "taskId2");
    DeliveryVehicleData newDeliveryVehicleInfo =
        FleetEventsTestHelper.updateDeliveryVehicleData1().toBuilder()
            .setDeliveryVehicleId(vehicleId)
            .setRemainingDuration(1L)
            .addRemainingVehicleJourneySegment(vjs1)
            .addRemainingVehicleJourneySegment(vjs2)
            .setEventTimestamp(0L)
            .setEventMetadata(new HashMap())
            .build();
    DeliveryVehicleFleetEvent deliveryVehicleFleetEvent =
        DeliveryVehicleFleetEvent.builder()
            .setDeliveryVehicleId("testDeliveryVehicleId1")
            .setNewDeliveryVehicle(newDeliveryVehicleInfo)
            .setOldDeliveryVehicle(oldDeliveryVehicleInfo)
            .build();
    setOriginalEta(oldDeliveryVehicleInfo, "taskId1", originalEta);
    setOriginalEta(
        oldDeliveryVehicleInfo, "taskId2", newDeliveryVehicleInfo.getRemainingDuration());
    setOriginalDuration(oldDeliveryVehicleInfo, "taskId1", originalEta);
    setOriginalDuration(
        oldDeliveryVehicleInfo, "taskId2", newDeliveryVehicleInfo.getRemainingDuration());
    List<OutputEvent> outputs = this.handler.handleEvent(deliveryVehicleFleetEvent, null);
    List<OutputEvent> expecteds = new ArrayList();
    expecteds.add(
        (new EtaOutputEvent.Builder())
            .setOriginalEta(originalEta)
            .setType(EtaOutputEvent.Type.ETA)
            .setNewEta(300003L)
            .setOriginalDuration(originalEta)
            .setRelativeDelta(300002.0F)
            .setDelta(300002L)
            .setTaskId("taskId2")
            .build());
    expecteds.add(
        (new EtaOutputEvent.Builder())
            .setOriginalEta(originalEta)
            .setType(EtaOutputEvent.Type.RELATIVE_ETA)
            .setNewEta(300003L)
            .setOriginalDuration(originalEta)
            .setRelativeDelta(300002.0F)
            .setDelta(300002L)
            .setTaskId("taskId2")
            .build());
    Assert.assertEquals(expecteds, outputs);
  }

  private void setOriginalEta(DeliveryVehicleData deliveryVehicleData, String taskId, Long eta) {
    String id = deliveryVehicleData.getDeliveryVehicleId();
    deliveryVehicleData
        .getEventMetadata()
        .putIfAbsent(ETA_CHANGE_METADATA_ID, new HashMap<String, Map<String, Long>>());
    Map<String, Map<String, Long>> vehicleIdToEtaMetadata =
        (Map<String, Map<String, Long>>)
            deliveryVehicleData.getEventMetadata().get(ETA_CHANGE_METADATA_ID);
    vehicleIdToEtaMetadata.putIfAbsent(id, new HashMap<String, Long>());
    Map<String, Long> metadata = vehicleIdToEtaMetadata.get(id);
    metadata.put(taskId, eta);
  }

  private void setOriginalDuration(
      DeliveryVehicleData deliveryVehicleInfo, String taskId, Long duration) {
    String id = deliveryVehicleInfo.getDeliveryVehicleId();
    deliveryVehicleInfo
        .getEventMetadata()
        .putIfAbsent(RELATIVE_ETA_CHANGE_METADATA_ID, new HashMap<String, Map<String, Long>>());
    HashMap<String, Map<String, Long>> vehicleIdToEtaMetadata =
        (HashMap<String, Map<String, Long>>)
            deliveryVehicleInfo.getEventMetadata().get(RELATIVE_ETA_CHANGE_METADATA_ID);
    vehicleIdToEtaMetadata.putIfAbsent(id, new HashMap<String, Long>());
    Map<String, Long> metadata = vehicleIdToEtaMetadata.get(id);
    metadata.put(taskId, duration);
  }
}
