package com.google.fleetevents.odrd.transactions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.GeoPoint;
import com.google.common.collect.ImmutableList;
import com.google.fleetevents.helpers.FakeFirestoreHelper;
import com.google.fleetevents.helpers.FleetEventsTestHelper;
import com.google.fleetevents.odrd.models.TripData;
import com.google.fleetevents.odrd.models.TripWaypointData;
import com.google.fleetevents.odrd.models.TripWaypointType;
import com.google.fleetevents.odrd.models.VehicleData;
import com.google.logging.v2.LogEntry;
import google.maps.fleetengine.v1.NavigationStatus;
import google.maps.fleetengine.v1.TripStatus;
import google.maps.fleetengine.v1.VehicleState;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.junit.Test;

public class UpdateVehicleTransactionTest {

  @Test
  public void updateVehicleLog_remainingDistanceDurationChanges() throws Exception {
    /* Tests whether the cloudLogEntry is correctly routing log entries based on log name. */
    HashMap<String, Object> fakeBackend = new HashMap<>();
    List<String> currentTripIds = new ArrayList<>();
    var pickupWaypoint =
        TripWaypointData.builder()
            .setVehicleId("testVehicleId1")
            .setTripId("testTripId1")
            .setWaypointId("testTripId1-pickup")
            .setTripWaypointType(TripWaypointType.PICKUP_WAYPOINT_TYPE)
            .build();
    var dropoffWaypoint =
        TripWaypointData.builder()
            .setVehicleId("testVehicleId1")
            .setTripId("testTripId1")
            .setWaypointId("testTripId1-dropoff")
            .setTripWaypointType(TripWaypointType.DROP_OFF_WAYPOINT_TYPE)
            .build();
    List<TripWaypointData> waypoints = ImmutableList.of(pickupWaypoint, dropoffWaypoint);
    currentTripIds.add("testTripId1");
    fakeBackend.put(
        "vehicles/testVehicleId1",
        VehicleData.builder()
            .setVehicleId("testVehicleId1")
            .setName("providers/test-123/vehicles/testVehicleId1")
            .setState(VehicleState.ONLINE.name())
            .setNavigationStatus(NavigationStatus.ENROUTE_TO_DESTINATION.name())
            .setTripIds(currentTripIds)
            .build());
    fakeBackend.put(
        "trips/testTripId1",
        TripData.builder()
            .setVehicleId("testVehicleId1")
            .setTripId("testTripId1")
            .setWaypoints(waypoints)
            .build());

    LogEntry logEntry = FleetEventsTestHelper.updateVehicleLog1();
    VehicleData expectedVehicleData =
        VehicleData.builder()
            .setEventTimestamp(null)
            .setVehicleId("testVehicleId1")
            .setName("providers/test-123/vehicles/testVehicleId1")
            .setTripIds(ImmutableList.of("testTripId1"))
            .setRemainingDistanceMeters(6673L)
            .setRemainingDuration(479662L)
            .setEta(Timestamp.parseTimestamp("2023-08-02T21:04:34.283866874Z"))
            .setState("ONLINE")
            .setNavigationStatus("UNKNOWN_NAVIGATION_STATUS")
            .setLastLocation(new GeoPoint(35, -97))
            .setEventTimestamp(Timestamp.parseTimestamp("2023-08-02T20:56:34.621727000Z"))
            .build();

    TripWaypointData expectedPickupTripWaypointData =
        TripWaypointData.builder()
            .setVehicleId("testVehicleId1")
            .setTripId("testTripId1")
            .setWaypointId("testTripId1-pickup")
            .setRemainingDistanceMeters(6673L)
            .setRemainingDuration(685791L)
            .setEta(Timestamp.parseTimestamp("2023-08-02T21:08:00.412009000Z"))
            .setTripWaypointType(TripWaypointType.PICKUP_WAYPOINT_TYPE)
            .build();
    TripWaypointData expectedDropoffTripWaypointData =
        TripWaypointData.builder()
            .setVehicleId("testVehicleId1")
            .setTripId("testTripId1")
            .setWaypointId("testTripId1-dropoff")
            .setRemainingDistanceMeters(10985L)
            .setRemainingDuration(1148791L)
            .setEta(Timestamp.parseTimestamp("2023-08-02T21:15:43.412009000Z"))
            .setTripWaypointType(TripWaypointType.DROP_OFF_WAYPOINT_TYPE)
            .build();

    TripData expectedTripData =
        TripData.builder()
            .setVehicleId("testVehicleId1")
            .setTripId("testTripId1")
            .setRemainingDistanceMeters(10985L)
            .setRemainingDuration(1148791L)
            .setEta(Timestamp.parseTimestamp("2023-08-02T21:15:43.412009000Z"))
            .setWaypoints(
                ImmutableList.of(expectedPickupTripWaypointData, expectedDropoffTripWaypointData))
            .setCurrentWaypointIndex(0L)
            .setTripStatus(TripStatus.ENROUTE_TO_PICKUP.name())
            .setEventTimestamp(Timestamp.parseTimestamp("2023-08-02T20:56:34.621727000Z"))
            .build();

    var firestoreDatabaseClient = FakeFirestoreHelper.getFakeFirestoreDatabaseClient();
    var transaction = FakeFirestoreHelper.getFakeTransaction(fakeBackend);

    UpdateVehicleTransaction updateVehicleTransaction =
        new UpdateVehicleTransaction(logEntry, ImmutableList.of(), firestoreDatabaseClient);
    updateVehicleTransaction.updateCallback(transaction);

    assertTrue(fakeBackend.containsKey("vehicles/testVehicleId1"));
    assertEquals(
        expectedVehicleData,
        ((VehicleData) fakeBackend.get("vehicles/testVehicleId1"))
            .toBuilder().setExpireAt(null).build());
    assertEquals(
        expectedTripData,
        ((TripData) fakeBackend.get("trips/testTripId1")).toBuilder().setExpireAt(null).build());
  }

  @Test
  public void updateVehicleLog_remainingDistanceDurationChanges_intermediateDestinations()
      throws Exception {
    /* Tests whether the cloudLogEntry is correctly routing log entries based on log name. */
    HashMap<String, Object> fakeBackend = new HashMap<>();
    List<String> currentTripIds = new ArrayList<>();
    currentTripIds.add("testTripId1");
    var pickupWaypoint =
        TripWaypointData.builder()
            .setVehicleId("testVehicleId1")
            .setTripId("testTripId1")
            .setWaypointId("testTripId1-pickup")
            .setTripWaypointType(TripWaypointType.PICKUP_WAYPOINT_TYPE)
            .build();
    var intermediateWaypoint =
        TripWaypointData.builder()
            .setVehicleId("testVehicleId1")
            .setTripId("testTripId1")
            .setWaypointId("testTripId1-1")
            .setTripWaypointType(TripWaypointType.INTERMEDIATE_WAYPOINT_TYPE)
            .build();
    var dropoffWaypoint =
        TripWaypointData.builder()
            .setVehicleId("testVehicleId1")
            .setTripId("testTripId1")
            .setWaypointId("testTripId1-dropoff")
            .setTripWaypointType(TripWaypointType.DROP_OFF_WAYPOINT_TYPE)
            .build();
    List<TripWaypointData> waypoints =
        ImmutableList.of(pickupWaypoint, intermediateWaypoint, dropoffWaypoint);
    fakeBackend.put(
        "vehicles/testVehicleId1",
        VehicleData.builder()
            .setVehicleId("testVehicleId1")
            .setName("providers/test-123/vehicles/testVehicleId1")
            .setState(VehicleState.ONLINE.name())
            .setNavigationStatus(NavigationStatus.ENROUTE_TO_DESTINATION.name())
            .setTripIds(currentTripIds)
            .build());
    fakeBackend.put(
        "trips/testTripId1",
        TripData.builder()
            .setVehicleId("testVehicleId1")
            .setTripId("testTripId1")
            .setWaypoints(waypoints)
            .build());

    LogEntry logEntry = FleetEventsTestHelper.updateVehicleLog2();
    VehicleData expectedVehicleData =
        VehicleData.builder()
            .setEventTimestamp(null)
            .setVehicleId("testVehicleId1")
            .setName("providers/test-123/vehicles/testVehicleId1")
            .setTripIds(ImmutableList.of("testTripId1"))
            .setRemainingDistanceMeters(6673L)
            .setRemainingDuration(479662L)
            .setEta(Timestamp.parseTimestamp("2023-08-02T21:04:34.283866874Z"))
            .setState("ONLINE")
            .setNavigationStatus("UNKNOWN_NAVIGATION_STATUS")
            .setLastLocation(new GeoPoint(35, -97))
            .setEventTimestamp(Timestamp.parseTimestamp("2023-08-02T20:56:34.621727000Z"))
            .build();

    TripWaypointData expectedPickupTripWaypointData =
        TripWaypointData.builder()
            .setVehicleId("testVehicleId1")
            .setTripId("testTripId1")
            .setWaypointId("testTripId1-pickup")
            .setRemainingDistanceMeters(6673L)
            .setRemainingDuration(685791L)
            .setEta(Timestamp.parseTimestamp("2023-08-02T21:08:00.412009000Z"))
            .setTripWaypointType(TripWaypointType.PICKUP_WAYPOINT_TYPE)
            .build();

    TripWaypointData expectedIntermediateTripWaypointData =
        TripWaypointData.builder()
            .setVehicleId("testVehicleId1")
            .setTripId("testTripId1")
            .setWaypointId("testTripId1-1")
            .setRemainingDistanceMeters(7673L)
            .setRemainingDuration(928791L)
            .setEta(Timestamp.parseTimestamp("2023-08-02T21:12:03.412009000Z"))
            .setTripWaypointType(TripWaypointType.INTERMEDIATE_WAYPOINT_TYPE)
            .build();
    TripWaypointData expectedDropoffTripWaypointData =
        TripWaypointData.builder()
            .setVehicleId("testVehicleId1")
            .setTripId("testTripId1")
            .setWaypointId("testTripId1-dropoff")
            .setRemainingDistanceMeters(11985L)
            .setRemainingDuration(1148791L)
            .setEta(Timestamp.parseTimestamp("2023-08-02T21:15:43.412009000Z"))
            .setTripWaypointType(TripWaypointType.DROP_OFF_WAYPOINT_TYPE)
            .build();
    TripData expectedTripData =
        TripData.builder()
            .setVehicleId("testVehicleId1")
            .setTripId("testTripId1")
            .setRemainingDistanceMeters(11985L)
            .setRemainingDuration(1148791L)
            .setEta(Timestamp.parseTimestamp("2023-08-02T21:15:43.412009000Z"))
            .setWaypoints(
                ImmutableList.of(
                    expectedPickupTripWaypointData,
                    expectedIntermediateTripWaypointData,
                    expectedDropoffTripWaypointData))
            .setCurrentWaypointIndex(0L)
            .setTripStatus(TripStatus.ENROUTE_TO_PICKUP.name())
            .setEventTimestamp(Timestamp.parseTimestamp("2023-08-02T20:56:34.621727000Z"))
            .build();

    var firestoreDatabaseClient = FakeFirestoreHelper.getFakeFirestoreDatabaseClient();
    var transaction = FakeFirestoreHelper.getFakeTransaction(fakeBackend);

    UpdateVehicleTransaction updateVehicleTransaction =
        new UpdateVehicleTransaction(logEntry, ImmutableList.of(), firestoreDatabaseClient);
    updateVehicleTransaction.updateCallback(transaction);

    assertTrue(fakeBackend.containsKey("vehicles/testVehicleId1"));
    assertEquals(
        expectedVehicleData,
        ((VehicleData) fakeBackend.get("vehicles/testVehicleId1"))
            .toBuilder().setExpireAt(null).build());
    assertEquals(
        expectedTripData,
        ((TripData) fakeBackend.get("trips/testTripId1")).toBuilder().setExpireAt(null).build());
  }

  @Test
  public void updateVehicleLog_remainingDistanceDurationChanges_sharedTrips() throws Exception {
    /* Tests whether the cloudLogEntry is correctly routing log entries based on log name. */
    HashMap<String, Object> fakeBackend = new HashMap<>();
    var pickupWaypoint1 =
        TripWaypointData.builder()
            .setVehicleId("testVehicleId1")
            .setTripId("testTripId1")
            .setWaypointId("testTripId1-pickup")
            .setTripWaypointType(TripWaypointType.PICKUP_WAYPOINT_TYPE)
            .build();
    var pickupWaypoint2 =
        TripWaypointData.builder()
            .setVehicleId("testVehicleId1")
            .setTripId("testTripId2")
            .setWaypointId("testTripId2-pickup")
            .setTripWaypointType(TripWaypointType.PICKUP_WAYPOINT_TYPE)
            .build();
    var dropoffWaypoint1 =
        TripWaypointData.builder()
            .setVehicleId("testVehicleId1")
            .setTripId("testTripId1")
            .setWaypointId("testTripId1-dropoff")
            .setTripWaypointType(TripWaypointType.DROP_OFF_WAYPOINT_TYPE)
            .build();
    var dropoffWaypoint2 =
        TripWaypointData.builder()
            .setVehicleId("testVehicleId1")
            .setTripId("testTripId2")
            .setWaypointId("testTripId2-dropoff")
            .setTripWaypointType(TripWaypointType.DROP_OFF_WAYPOINT_TYPE)
            .build();
    List<TripWaypointData> waypointsTrip1 = ImmutableList.of(pickupWaypoint1, dropoffWaypoint1);
    List<TripWaypointData> waypointsTrip2 = ImmutableList.of(pickupWaypoint2, dropoffWaypoint2);
    List<String> currentTripIds = new ArrayList<>();
    currentTripIds.add("testTripId1");
    currentTripIds.add("testTripId2");
    fakeBackend.put(
        "vehicles/testVehicleId1",
        VehicleData.builder()
            .setVehicleId("testVehicleId1")
            .setName("providers/test-123/vehicles/testVehicleId1")
            .setState(VehicleState.ONLINE.name())
            .setNavigationStatus(NavigationStatus.ENROUTE_TO_DESTINATION.name())
            .setTripIds(currentTripIds)
            .build());
    fakeBackend.put(
        "trips/testTripId1",
        TripData.builder()
            .setVehicleId("testVehicleId1")
            .setTripId("testTripId1")
            .setWaypoints(waypointsTrip1)
            .build());
    fakeBackend.put(
        "trips/testTripId2",
        TripData.builder()
            .setVehicleId("testVehicleId1")
            .setTripId("testTripId2")
            .setWaypoints(waypointsTrip2)
            .build());

    LogEntry logEntry = FleetEventsTestHelper.updateVehicleLog3();
    VehicleData expectedVehicleData =
        VehicleData.builder()
            .setEventTimestamp(null)
            .setVehicleId("testVehicleId1")
            .setName("providers/test-123/vehicles/testVehicleId1")
            .setTripIds(currentTripIds)
            .setRemainingDistanceMeters(6673L)
            .setRemainingDuration(479662L)
            .setEta(Timestamp.parseTimestamp("2023-08-02T21:04:34.283866874Z"))
            .setState("ONLINE")
            .setNavigationStatus("UNKNOWN_NAVIGATION_STATUS")
            .setLastLocation(new GeoPoint(35, -97))
            .setEventTimestamp(Timestamp.parseTimestamp("2023-08-02T20:56:34.621727000Z"))
            .build();

    TripWaypointData expectedPickupTripWaypointDataTrip1 =
        TripWaypointData.builder()
            .setVehicleId("testVehicleId1")
            .setTripId("testTripId1")
            .setWaypointId("testTripId1-pickup")
            .setRemainingDistanceMeters(6673L)
            .setRemainingDuration(685791L)
            .setEta(Timestamp.parseTimestamp("2023-08-02T21:08:00.412009000Z"))
            .setTripWaypointType(TripWaypointType.PICKUP_WAYPOINT_TYPE)
            .build();

    TripWaypointData expectedPickupTripWaypointDataTrip2 =
        TripWaypointData.builder()
            .setVehicleId("testVehicleId1")
            .setTripId("testTripId2")
            .setWaypointId("testTripId2-pickup")
            .setRemainingDistanceMeters(7673L)
            .setRemainingDuration(928791L)
            .setEta(Timestamp.parseTimestamp("2023-08-02T21:12:03.412009000Z"))
            .setTripWaypointType(TripWaypointType.PICKUP_WAYPOINT_TYPE)
            .build();
    TripWaypointData expectedDropoffTripWaypointDataTrip1 =
        TripWaypointData.builder()
            .setVehicleId("testVehicleId1")
            .setTripId("testTripId1")
            .setWaypointId("testTripId1-dropoff")
            .setRemainingDistanceMeters(11985L)
            .setRemainingDuration(1148791L)
            .setEta(Timestamp.parseTimestamp("2023-08-02T21:15:43.412009000Z"))
            .setTripWaypointType(TripWaypointType.DROP_OFF_WAYPOINT_TYPE)
            .build();
    TripWaypointData expectedDropoffTripWaypointDataTrip2 =
        TripWaypointData.builder()
            .setVehicleId("testVehicleId1")
            .setTripId("testTripId2")
            .setWaypointId("testTripId2-dropoff")
            .setRemainingDistanceMeters(12985L)
            .setRemainingDuration(1225791L)
            .setEta(Timestamp.parseTimestamp("2023-08-02T21:17:00.412009000Z"))
            .setTripWaypointType(TripWaypointType.DROP_OFF_WAYPOINT_TYPE)
            .build();

    TripData expectedTripData =
        TripData.builder()
            .setVehicleId("testVehicleId1")
            .setTripId("testTripId1")
            .setRemainingDistanceMeters(11985L)
            .setRemainingDuration(1148791L)
            .setEta(Timestamp.parseTimestamp("2023-08-02T21:15:43.412009000Z"))
            .setWaypoints(
                ImmutableList.of(
                    expectedPickupTripWaypointDataTrip1, expectedDropoffTripWaypointDataTrip1))
            .setCurrentWaypointIndex(0L)
            .setTripStatus(TripStatus.ENROUTE_TO_PICKUP.name())
            .setEventTimestamp(Timestamp.parseTimestamp("2023-08-02T20:56:34.621727000Z"))
            .build();
    TripData expectedTrip2Data =
        TripData.builder()
            .setVehicleId("testVehicleId1")
            .setTripId("testTripId2")
            .setRemainingDistanceMeters(12985L)
            .setRemainingDuration(1225791L)
            .setEta(Timestamp.parseTimestamp("2023-08-02T21:17:00.412009000Z"))
            .setWaypoints(
                ImmutableList.of(
                    expectedPickupTripWaypointDataTrip2, expectedDropoffTripWaypointDataTrip2))
            .setCurrentWaypointIndex(0L)
            .setTripStatus(TripStatus.ENROUTE_TO_PICKUP.name())
            .setEventTimestamp(Timestamp.parseTimestamp("2023-08-02T20:56:34.621727000Z"))
            .build();

    var firestoreDatabaseClient = FakeFirestoreHelper.getFakeFirestoreDatabaseClient();
    var transaction = FakeFirestoreHelper.getFakeTransaction(fakeBackend);

    UpdateVehicleTransaction updateVehicleTransaction =
        new UpdateVehicleTransaction(logEntry, ImmutableList.of(), firestoreDatabaseClient);
    updateVehicleTransaction.updateCallback(transaction);

    assertTrue(fakeBackend.containsKey("vehicles/testVehicleId1"));
    assertEquals(
        expectedVehicleData,
        ((VehicleData) fakeBackend.get("vehicles/testVehicleId1"))
            .toBuilder().setExpireAt(null).build());
    assertEquals(
        expectedTripData,
        ((TripData) fakeBackend.get("trips/testTripId1")).toBuilder().setExpireAt(null).build());
    assertEquals(
        expectedTrip2Data,
        ((TripData) fakeBackend.get("trips/testTripId2")).toBuilder().setExpireAt(null).build());
  }
}
