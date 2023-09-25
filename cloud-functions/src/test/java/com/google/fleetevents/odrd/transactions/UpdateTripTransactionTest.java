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

public class UpdateTripTransactionTest {

  @Test
  public void updateTrip_updatesTripVehicleIdStatus() throws Exception {
    LogEntry logEntry = FleetEventsTestHelper.updateTripLog1();
    var waypoints =
        ImmutableList.of(
            TripWaypointData.builder().setWaypointId("testTripId1-pickup").build(),
            TripWaypointData.builder().setWaypointId("testTripId1-dropoff").build());

    HashMap<String, Object> fakeBackend = new HashMap<>();

    fakeBackend.put(
        "trips/testTripId1",
        TripData.builder().setTripId("testTripId1").setWaypoints(waypoints).build());

    TripData expectedTripData =
        TripData.builder()
            .setVehicleId("testVehicleId1")
            .setTripStatus("NEW")
            .setTripId("testTripId1")
            .setIsSharedTrip(false)
            .setWaypoints(waypoints)
            .setCurrentWaypointIndex(-1L)
            .setEventTimestamp(Timestamp.parseTimestamp("2023-08-02T21:00:16.691814000Z"))
            .build();

    var firestoreDatabaseClient = FakeFirestoreHelper.getFakeFirestoreDatabaseClient();
    var transaction = FakeFirestoreHelper.getFakeTransaction(fakeBackend);

    UpdateTripTransaction updateTripTransaction =
        new UpdateTripTransaction(logEntry, ImmutableList.of(), firestoreDatabaseClient);
    updateTripTransaction.updateCallback(transaction);

    assertTrue(fakeBackend.containsKey("trips/testTripId1"));
    assertEquals(
        expectedTripData,
        ((TripData) fakeBackend.get("trips/testTripId1")).toBuilder().setExpireAt(null).build());
  }

  @Test
  public void updateTrip_updatesTripWaypointsChanged() throws Exception {
    LogEntry logEntry = FleetEventsTestHelper.updateTripLog2();

    var waypoints =
        ImmutableList.of(
            TripWaypointData.builder().setWaypointId("testTripId1-pickup").build(),
            TripWaypointData.builder().setWaypointId("testTripId1-dropoff").build());
    HashMap<String, Object> fakeBackend = new HashMap<>();
    fakeBackend.put(
        "trips/testTripId1",
        TripData.builder().setTripId("testTripId1").setWaypoints(waypoints).build());

    var newWaypoints =
        ImmutableList.of(
            TripWaypointData.builder()
                .setVehicleId("testVehicleId1")
                .setTripId("testTripId1")
                .setTripWaypointType(TripWaypointType.PICKUP_WAYPOINT_TYPE)
                .setWaypointId("testTripId1-pickup")
                .setWaypointIndex(0L)
                .build(),
            TripWaypointData.builder()
                .setVehicleId("testVehicleId1")
                .setTripId("testTripId1")
                .setTripWaypointType(TripWaypointType.INTERMEDIATE_WAYPOINT_TYPE)
                .setWaypointId("testTripId1-1")
                .setWaypointIndex(1L)
                .build(),
            TripWaypointData.builder()
                .setVehicleId("testVehicleId1")
                .setTripId("testTripId1")
                .setTripWaypointType(TripWaypointType.DROP_OFF_WAYPOINT_TYPE)
                .setWaypointId("testTripId1-dropoff")
                .setWaypointIndex(2L)
                .build());
    TripData expectedTripData =
        TripData.builder()
            .setVehicleId("testVehicleId1")
            .setTripStatus("ENROUTE_TO_PICKUP")
            .setTripId("testTripId1")
            .setIsSharedTrip(false)
            .setWaypoints(newWaypoints)
            .setCurrentWaypointIndex(0L)
            .setIntermediateDestinationsVersion(
                Timestamp.parseTimestamp("2023-08-02T21:07:15.281274000Z"))
            .setEventTimestamp(Timestamp.parseTimestamp("2023-08-02T21:00:16.691814000Z"))
            .build();

    var firestoreDatabaseClient = FakeFirestoreHelper.getFakeFirestoreDatabaseClient();
    var transaction = FakeFirestoreHelper.getFakeTransaction(fakeBackend);

    UpdateTripTransaction updateTripTransaction =
        new UpdateTripTransaction(logEntry, ImmutableList.of(), firestoreDatabaseClient);
    updateTripTransaction.updateCallback(transaction);

    assertTrue(fakeBackend.containsKey("trips/testTripId1"));
    assertEquals(
        expectedTripData,
        ((TripData) fakeBackend.get("trips/testTripId1")).toBuilder().setExpireAt(null).build());
  }
}
