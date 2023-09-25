package com.google.fleetevents.odrd.transactions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.GeoPoint;
import com.google.common.collect.ImmutableList;
import com.google.fleetevents.helpers.FakeFirestoreHelper;
import com.google.fleetevents.helpers.FleetEventsTestHelper;
import com.google.fleetevents.odrd.models.VehicleData;
import com.google.logging.v2.LogEntry;
import java.io.IOException;
import java.util.HashMap;
import org.junit.Test;

public class CreateVehicleTransactionTest {

  @Test
  public void createVehicleLog_createsVehicle() throws IOException {
    LogEntry logEntry = FleetEventsTestHelper.createVehicleLog1();
    VehicleData expectedVehicleData =
        VehicleData.builder()
            .setVehicleId("testVehicleId1")
            .setName("providers/test-123/vehicles/testVehicleId1")
            .setLastLocation(new GeoPoint(38, -122))
            .setEventTimestamp(Timestamp.parseTimestamp("2023-07-17T22:36:19.500093000Z"))
            .build();

    HashMap<String, Object> fakeBackend = new HashMap<>();

    var firestoreDatabaseClient = FakeFirestoreHelper.getFakeFirestoreDatabaseClient();
    var transaction = FakeFirestoreHelper.getFakeTransaction(fakeBackend);

    CreateVehicleTransaction createVehicleTransaction =
        new CreateVehicleTransaction(logEntry, ImmutableList.of(), firestoreDatabaseClient);
    createVehicleTransaction.updateCallback(transaction);

    assertTrue(fakeBackend.containsKey("vehicles/testVehicleId1"));
    assertEquals(
        expectedVehicleData,
        ((VehicleData) fakeBackend.get("vehicles/testVehicleId1"))
            .toBuilder().setExpireAt(null).build());
  }
}
