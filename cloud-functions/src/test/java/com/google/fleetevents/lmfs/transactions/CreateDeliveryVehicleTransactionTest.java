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

import com.google.cloud.firestore.GeoPoint;
import com.google.common.collect.ImmutableList;
import com.google.fleetevents.helpers.FakeFirestoreHelper;
import com.google.fleetevents.helpers.FleetEventsTestHelper;
import com.google.fleetevents.lmfs.models.DeliveryVehicleData;
import com.google.logging.v2.LogEntry;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;
import org.junit.Test;

/** Tests for create delivery vehicle transaction. */
public class CreateDeliveryVehicleTransactionTest {

  @Test
  public void createDeliveryVehicleLog_createsDeliveryVehicle()
      throws IOException, ExecutionException, InterruptedException {
    LogEntry logEntry = FleetEventsTestHelper.createDeliveryVehicleLog1();
    DeliveryVehicleData expectedDeliveryVehicleData =
        DeliveryVehicleData.builder()
            .setDeliveryVehicleId("testDeliveryVehicleId1")
            .setName("providers/test-123/deliveryVehicles/testDeliveryVehicleId1")
            .setLastLocation(new GeoPoint(35, -97))
            .build();

    HashMap<String, Object> fakeBackend = new HashMap<>();

    var firestoreDatabaseClient = FakeFirestoreHelper.getFakeFirestoreDatabaseClient();
    var transaction = FakeFirestoreHelper.getFakeTransaction(fakeBackend);

    CreateDeliveryVehicleTransaction createDeliveryVehicleTransaction =
        new CreateDeliveryVehicleTransaction(logEntry, ImmutableList.of(), firestoreDatabaseClient);
    createDeliveryVehicleTransaction.updateCallback(transaction);

    assertTrue(fakeBackend.containsKey("deliveryVehicles/testDeliveryVehicleId1"));
    assertEquals(
        expectedDeliveryVehicleData,
        ((DeliveryVehicleData) fakeBackend.get("deliveryVehicles/testDeliveryVehicleId1"))
            .toBuilder().setExpireAt(null).build());
  }
}
