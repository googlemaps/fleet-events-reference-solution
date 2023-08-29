package com.google.fleetevents.odrd.transactions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.cloud.Timestamp;
import com.google.common.collect.ImmutableList;
import com.google.fleetevents.helpers.FakeFirestoreHelper;
import com.google.fleetevents.helpers.FleetEventsTestHelper;
import com.google.fleetevents.odrd.models.TripData;
import com.google.fleetevents.odrd.models.TripWaypointData;
import com.google.fleetevents.odrd.models.TripWaypointType;
import com.google.logging.v2.LogEntry;
import java.util.HashMap;
import org.junit.Test;

public class CreateTripTransactionTest {

  @Test
  public void createTrip_createsTripAndTwoTripWaypoints() throws Exception {
    LogEntry logEntry = FleetEventsTestHelper.createTripLog1();
    TripWaypointData expectedPickupWaypoint =
        TripWaypointData.builder()
            .setVehicleId("")
            .setTripId("testTripId1")
            .setWaypointId("testTripId1-pickup")
            .setTripWaypointType(TripWaypointType.PICKUP_WAYPOINT_TYPE)
            .setWaypointIndex(0L)
            .build();
    TripWaypointData expectedIntermediateWaypoint =
        TripWaypointData.builder()
            .setVehicleId("")
            .setTripId("testTripId1")
            .setWaypointId("testTripId1-1")
            .setTripWaypointType(TripWaypointType.INTERMEDIATE_WAYPOINT_TYPE)
            .setWaypointIndex(1L)
            .build();
    TripWaypointData expectedDropoffWaypoint =
        TripWaypointData.builder()
            .setVehicleId("")
            .setTripId("testTripId1")
            .setWaypointId("testTripId1-dropoff")
            .setTripWaypointType(TripWaypointType.DROP_OFF_WAYPOINT_TYPE)
            .setWaypointIndex(2L)
            .build();
    TripData expectedTripData =
        TripData.builder()
            .setVehicleId("")
            .setTripId("testTripId1")
            .setIsSharedTrip(false)
            .setWaypoints(
                ImmutableList.of(
                    expectedPickupWaypoint, expectedIntermediateWaypoint, expectedDropoffWaypoint))
            .setEventTimestamp(Timestamp.parseTimestamp("2023-07-17T23:31:22.355367000Z"))
            .build();

    HashMap<String, Object> fakeBackend = new HashMap<>();

    var firestoreDatabaseClient = FakeFirestoreHelper.getFakeFirestoreDatabaseClient();
    var transaction = FakeFirestoreHelper.getFakeTransaction(fakeBackend);

    CreateTripTransaction createTripTransaction =
        new CreateTripTransaction(logEntry, ImmutableList.of(), firestoreDatabaseClient);
    createTripTransaction.updateCallback(transaction);

    assertTrue(fakeBackend.containsKey("trips/testTripId1"));
    assertEquals(
        expectedTripData,
        ((TripData) fakeBackend.get("trips/testTripId1")).toBuilder().setExpireAt(null).build());
  }
}
