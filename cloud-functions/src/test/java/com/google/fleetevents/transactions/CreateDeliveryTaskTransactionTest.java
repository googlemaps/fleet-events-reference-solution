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

import com.google.common.collect.ImmutableList;
import com.google.fleetevents.helpers.FakeFirestoreHelper;
import com.google.fleetevents.helpers.FleetEventsTestHelper;
import com.google.fleetevents.models.DeliveryTaskData;
import com.google.logging.v2.LogEntry;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import org.junit.Test;

/**
 * Tests for create delivery tasks transaction.
 */
public class CreateDeliveryTaskTransactionTest {

  @Test
  public void createTaskLog_createsTask()
      throws IOException, ExecutionException, InterruptedException {
    LogEntry logEntry = FleetEventsTestHelper.createTaskLog1();
    DeliveryTaskData expectedDeliveryTaskData =
        DeliveryTaskData.builder()
            .setEventTimestamp(1679070233949L)
            .setDeliveryTaskId("testDeliveryTaskId1")
            .setName("providers/test-123/tasks/testDeliveryTaskId1")
            .setState("OPEN")
            .setTrackingId("")
            .build();
    HashMap<String, Object> fakeBackend = new HashMap<>();

    var firestoreDatabaseClient = FakeFirestoreHelper.getFakeFirestoreDatabaseClient();
    var transaction = FakeFirestoreHelper.getFakeTransaction(fakeBackend);

    CreateDeliveryTaskTransaction createDeliveryTaskTransaction =
        new CreateDeliveryTaskTransaction(logEntry, ImmutableList.of(), firestoreDatabaseClient);
    createDeliveryTaskTransaction.updateCallback(transaction);

    assertTrue(fakeBackend.containsKey("deliveryTasks/testDeliveryTaskId1"));
    assertEquals(
        expectedDeliveryTaskData,
        ((DeliveryTaskData) fakeBackend.get("deliveryTasks/testDeliveryTaskId1"))
            .toBuilder().setExpireAt(null).build());
  }
}
