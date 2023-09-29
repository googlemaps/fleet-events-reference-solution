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

package com.google.fleetevents.lmfs.transactions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import com.google.fleetevents.helpers.FakeFirestoreHelper;
import com.google.fleetevents.helpers.FleetEventsTestHelper;
import com.google.fleetevents.lmfs.models.DeliveryTaskData;
import com.google.logging.v2.LogEntry;
import java.io.IOException;
import java.util.HashMap;
import org.junit.Test;

/** Tests for batch create delivery tasks transaction. */
public class BatchCreateDeliveryTasksTransactionTest {

  @Test
  public void batchCreateTasksLog_createsMultipleTasks() throws IOException {
    LogEntry logEntry = FleetEventsTestHelper.batchCreateTasksLog1();
    DeliveryTaskData expectedDeliveryTaskData1 =
        DeliveryTaskData.builder()
            .setEventTimestamp(1679955118895L)
            .setDeliveryTaskId("testDeliveryTaskId1")
            .setName("providers/test-123/tasks/testDeliveryTaskId1")
            .setTrackingId("testDeliveryTrackingId1")
            .setState("OPEN")
            .build();
    DeliveryTaskData expectedDeliveryTaskData2 =
        DeliveryTaskData.builder()
            .setEventTimestamp(1679955118895L)
            .setDeliveryTaskId("testDeliveryTaskId2")
            .setName("providers/test-123/tasks/testDeliveryTaskId2")
            .setTrackingId("testDeliveryTrackingId2")
            .setState("OPEN")
            .build();

    HashMap<String, Object> fakeBackend = new HashMap<>();

    var firestoreDatabaseClient = FakeFirestoreHelper.getFakeFirestoreDatabaseClient();
    var transaction = FakeFirestoreHelper.getFakeTransaction(fakeBackend);
    BatchCreateDeliveryTasksTransaction batchCreateDeliveryTasksTransaction =
        new BatchCreateDeliveryTasksTransaction(
            logEntry, ImmutableList.of(), firestoreDatabaseClient);

    batchCreateDeliveryTasksTransaction.updateCallback(transaction);

    assertTrue(fakeBackend.containsKey("deliveryTasks/testDeliveryTaskId1"));
    assertTrue(fakeBackend.containsKey("deliveryTasks/testDeliveryTaskId2"));
    assertEquals(
        expectedDeliveryTaskData1,
        ((DeliveryTaskData) fakeBackend.get("deliveryTasks/testDeliveryTaskId1"))
            .toBuilder().setExpireAt(null).build());
    assertEquals(
        expectedDeliveryTaskData2,
        ((DeliveryTaskData) fakeBackend.get("deliveryTasks/testDeliveryTaskId2"))
            .toBuilder().setExpireAt(null).build());
  }
}
