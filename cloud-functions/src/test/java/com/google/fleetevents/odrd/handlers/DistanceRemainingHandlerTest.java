package com.google.fleetevents.odrd.handlers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

import com.google.cloud.Timestamp;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.fleetevents.common.database.FirestoreDatabaseClient;
import com.google.fleetevents.common.models.Change;
import com.google.fleetevents.common.models.OutputEvent;
import com.google.fleetevents.odrd.models.TripData;
import com.google.fleetevents.odrd.models.TripFleetEvent;
import com.google.fleetevents.odrd.models.TripWaypointData;
import com.google.fleetevents.odrd.models.outputs.DistanceRemainingOutputEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import org.mockito.Mockito;

public class DistanceRemainingHandlerTest {

  @Test
  public void updateVehicleNoDistance_doesNotRespond() {
    TripFleetEvent tripFleetEvent =
        TripFleetEvent.builder()
            .setVehicleId("testVehicleId1")
            .setTripId("testTripId1")
            .setOldTrip(TripData.builder().build())
            .setNewTrip(TripData.builder().build())
            .setTripDifferences(ImmutableMap.of())
            .build();
    DistanceRemainingHandler distanceRemainingHandler = new DistanceRemainingHandler();
    FirestoreDatabaseClient firestoreDatabaseClient = Mockito.mock(FirestoreDatabaseClient.class);
    doReturn(null).when(firestoreDatabaseClient).getDeliveryVehicleDocument(any(String.class));
    assertFalse(distanceRemainingHandler.respondsTo(tripFleetEvent, null, firestoreDatabaseClient));
  }

  @Test
  public void updateVehicleDistanceNotCrossThreshold_doesNotReport() {
    Map<String, Change> tripDifferences = new HashMap<>();
    tripDifferences.put("remainingDistanceMeters", new Change(1500L, 1400L));

    var oldPickupWaypoint = TripWaypointData.builder().setRemainingDistanceMeters(1200L).build();
    var newPickupWaypoint = TripWaypointData.builder().setRemainingDistanceMeters(1100L).build();
    var pickupWaypointDifferences =
        ImmutableMap.of("remainingDistanceMeters", new Change(1200L, 1100L));

    var oldDropoffWaypoint = TripWaypointData.builder().setRemainingDistanceMeters(1500L).build();
    var newDropoffWaypoint = TripWaypointData.builder().setRemainingDistanceMeters(1400L).build();
    var dropoffWaypointDifferences =
        ImmutableMap.of("remainingDistanceMeters", new Change(1500L, 1400L));

    var oldTrip =
        TripData.builder()
            .setRemainingDistanceMeters(1500L)
            .setWaypoints(ImmutableList.of(oldPickupWaypoint, oldDropoffWaypoint))
            .setEventTimestamp(Timestamp.parseTimestamp("2023-08-02T20:56:34.621727000Z"))
            .build();

    var newTrip =
        TripData.builder()
            .setRemainingDistanceMeters(1400L)
            .setWaypoints(ImmutableList.of(newPickupWaypoint, newDropoffWaypoint))
            .setEventTimestamp(Timestamp.parseTimestamp("2023-08-02T20:56:34.621727000Z"))
            .build();

    TripFleetEvent tripfleetEvent =
        TripFleetEvent.builder()
            .setVehicleId("testVehicleId1")
            .setTripId("testTripId1")
            .setOldTrip(oldTrip)
            .setNewTrip(newTrip)
            .setOldTripWaypoints(ImmutableList.of(oldPickupWaypoint, oldDropoffWaypoint))
            .setNewTripWaypoints(ImmutableList.of(newPickupWaypoint, newDropoffWaypoint))
            .setTripDifferences(tripDifferences)
            .setTripWaypointDifferences(
                ImmutableList.of(pickupWaypointDifferences, dropoffWaypointDifferences))
            .build();
    DistanceRemainingHandler distanceRemainingHandler = new DistanceRemainingHandler();
    assertTrue(distanceRemainingHandler.respondsTo(tripfleetEvent, null, null));
    assertEquals(new ArrayList<>(), distanceRemainingHandler.handleEvent(tripfleetEvent, null));
  }

  @Test
  public void updateVehicleDistancePassedThresholdDoesNotCrossThreshold_doesNotReport() {
    Map<String, Change> tripDifferences = new HashMap<>();
    tripDifferences.put("remainingDistanceMeters", new Change(500L, 400L));

    var oldPickupWaypoint = TripWaypointData.builder().setRemainingDistanceMeters(200L).build();
    var newPickupWaypoint = TripWaypointData.builder().setRemainingDistanceMeters(100L).build();
    var pickupWaypointDifferences =
        ImmutableMap.of("remainingDistanceMeters", new Change(200L, 100L));

    var oldDropoffWaypoint = TripWaypointData.builder().setRemainingDistanceMeters(500L).build();
    var newDropoffWaypoint = TripWaypointData.builder().setRemainingDistanceMeters(400L).build();
    var dropoffWaypointDifferences =
        ImmutableMap.of("remainingDistanceMeters", new Change(500L, 400L));

    var oldTrip =
        TripData.builder()
            .setRemainingDistanceMeters(500L)
            .setWaypoints(ImmutableList.of(oldPickupWaypoint, oldDropoffWaypoint))
            .setEventTimestamp(Timestamp.parseTimestamp("2023-08-02T20:56:34.621727000Z"))
            .build();

    var newTrip =
        TripData.builder()
            .setRemainingDistanceMeters(400L)
            .setWaypoints(ImmutableList.of(newPickupWaypoint, newDropoffWaypoint))
            .setEventTimestamp(Timestamp.parseTimestamp("2023-08-02T20:56:34.621727000Z"))
            .build();

    TripFleetEvent tripfleetEvent =
        TripFleetEvent.builder()
            .setVehicleId("testVehicleId1")
            .setTripId("testTripId1")
            .setOldTrip(oldTrip)
            .setNewTrip(newTrip)
            .setOldTripWaypoints(ImmutableList.of(oldPickupWaypoint, oldDropoffWaypoint))
            .setNewTripWaypoints(ImmutableList.of(newPickupWaypoint, newDropoffWaypoint))
            .setTripDifferences(tripDifferences)
            .setTripWaypointDifferences(
                ImmutableList.of(pickupWaypointDifferences, dropoffWaypointDifferences))
            .build();
    DistanceRemainingHandler distanceRemainingHandler = new DistanceRemainingHandler();
    assertTrue(distanceRemainingHandler.respondsTo(tripfleetEvent, null, null));
    assertEquals(new ArrayList<>(), distanceRemainingHandler.handleEvent(tripfleetEvent, null));
  }

  @Test
  public void updateVehicleDistancePassedThresholdCrossesThresholdInReverse_doesNotReport() {
    Map<String, Change> tripDifferences = new HashMap<>();
    tripDifferences.put("remainingDistanceMeters", new Change(900L, 1100L));

    var oldPickupWaypoint = TripWaypointData.builder().setRemainingDistanceMeters(200L).build();
    var newPickupWaypoint = TripWaypointData.builder().setRemainingDistanceMeters(400L).build();
    var pickupWaypointDifferences =
        ImmutableMap.of("remainingDistanceMeters", new Change(200L, 400L));

    var oldDropoffWaypoint = TripWaypointData.builder().setRemainingDistanceMeters(900L).build();
    var newDropoffWaypoint = TripWaypointData.builder().setRemainingDistanceMeters(1100L).build();
    var dropoffWaypointDifferences =
        ImmutableMap.of("remainingDistanceMeters", new Change(900L, 1100L));

    var oldTrip =
        TripData.builder()
            .setRemainingDistanceMeters(900L)
            .setWaypoints(ImmutableList.of(oldPickupWaypoint, oldDropoffWaypoint))
            .setEventTimestamp(Timestamp.parseTimestamp("2023-08-02T20:56:34.621727000Z"))
            .build();

    var newTrip =
        TripData.builder()
            .setRemainingDistanceMeters(1100L)
            .setWaypoints(ImmutableList.of(newPickupWaypoint, newDropoffWaypoint))
            .setEventTimestamp(Timestamp.parseTimestamp("2023-08-02T20:56:34.621727000Z"))
            .build();

    TripFleetEvent tripfleetEvent =
        TripFleetEvent.builder()
            .setVehicleId("testVehicleId1")
            .setTripId("testTripId1")
            .setOldTrip(oldTrip)
            .setNewTrip(newTrip)
            .setOldTripWaypoints(ImmutableList.of(oldPickupWaypoint, oldDropoffWaypoint))
            .setNewTripWaypoints(ImmutableList.of(newPickupWaypoint, newDropoffWaypoint))
            .setTripDifferences(tripDifferences)
            .setTripWaypointDifferences(
                ImmutableList.of(pickupWaypointDifferences, dropoffWaypointDifferences))
            .build();
    DistanceRemainingHandler distanceRemainingHandler = new DistanceRemainingHandler();
    assertTrue(distanceRemainingHandler.respondsTo(tripfleetEvent, null, null));
    assertEquals(new ArrayList<>(), distanceRemainingHandler.handleEvent(tripfleetEvent, null));
  }

  @Test
  public void updateVehicleDistanceCrossesThreshold_returnsOutputEvent() {
    Map<String, Change> tripDifferences = new HashMap<>();
    tripDifferences.put("remainingDistanceMeters", new Change(1100L, 900L));

    var oldPickupWaypoint =
        TripWaypointData.builder()
            .setWaypointId("testTripId1-pickup")
            .setRemainingDistanceMeters(400L)
            .build();
    var newPickupWaypoint =
        TripWaypointData.builder()
            .setWaypointId("testTripId1-pickup")
            .setRemainingDistanceMeters(200L)
            .build();
    var pickupWaypointDifferences =
        ImmutableMap.of("remainingDistanceMeters", new Change(400L, 200L));

    var oldDropoffWaypoint =
        TripWaypointData.builder()
            .setWaypointId("testTripId1-dropoff")
            .setRemainingDistanceMeters(1100L)
            .build();
    var newDropoffWaypoint =
        TripWaypointData.builder()
            .setWaypointId("testTripId1-dropoff")
            .setRemainingDistanceMeters(900L)
            .build();
    var dropoffWaypointDifferences =
        ImmutableMap.of("remainingDistanceMeters", new Change(1100L, 900L));

    var oldTrip =
        TripData.builder()
            .setRemainingDistanceMeters(1100L)
            .setWaypoints(ImmutableList.of(oldPickupWaypoint, oldDropoffWaypoint))
            .setEventTimestamp(Timestamp.parseTimestamp("2023-08-02T20:51:34.621727000Z"))
            .build();

    var newTrip =
        TripData.builder()
            .setRemainingDistanceMeters(900L)
            .setWaypoints(ImmutableList.of(newPickupWaypoint, newDropoffWaypoint))
            .setEventTimestamp(Timestamp.parseTimestamp("2023-08-02T20:56:34.621727000Z"))
            .build();

    TripFleetEvent tripfleetEvent =
        TripFleetEvent.builder()
            .setVehicleId("testVehicleId1")
            .setTripId("testTripId1")
            .setOldTrip(oldTrip)
            .setNewTrip(newTrip)
            .setOldTripWaypoints(ImmutableList.of(oldPickupWaypoint, oldDropoffWaypoint))
            .setNewTripWaypoints(ImmutableList.of(newPickupWaypoint, newDropoffWaypoint))
            .setTripDifferences(tripDifferences)
            .setTripWaypointDifferences(
                ImmutableList.of(pickupWaypointDifferences, dropoffWaypointDifferences))
            .build();
    DistanceRemainingHandler distanceRemainingHandler = new DistanceRemainingHandler();
    assertTrue(distanceRemainingHandler.respondsTo(tripfleetEvent, null, null));
    List<OutputEvent> outputEventList = new ArrayList<>();

    var dropoffWaypointDistanceRemainingOutputEvent = new DistanceRemainingOutputEvent();
    dropoffWaypointDistanceRemainingOutputEvent.setEventTimestamp(
        Timestamp.parseTimestamp("2023-08-02T20:56:34.621727000Z"));
    dropoffWaypointDistanceRemainingOutputEvent.setOldDistanceRemainingMeters(1100L);
    dropoffWaypointDistanceRemainingOutputEvent.setNewDistanceRemainingMeters(900L);
    dropoffWaypointDistanceRemainingOutputEvent.setThresholdMilliseconds(1000L);
    dropoffWaypointDistanceRemainingOutputEvent.setTripId("testTripId1");
    dropoffWaypointDistanceRemainingOutputEvent.setWaypointId("testTripId1-dropoff");
    dropoffWaypointDistanceRemainingOutputEvent.setFleetEvent(tripfleetEvent);

    var tripDistanceRemainingOutputEvent = new DistanceRemainingOutputEvent();
    tripDistanceRemainingOutputEvent.setEventTimestamp(
        Timestamp.parseTimestamp("2023-08-02T20:56:34.621727000Z"));
    tripDistanceRemainingOutputEvent.setOldDistanceRemainingMeters(1100L);
    tripDistanceRemainingOutputEvent.setNewDistanceRemainingMeters(900L);
    tripDistanceRemainingOutputEvent.setThresholdMilliseconds(1000L);
    tripDistanceRemainingOutputEvent.setIsTripOutputEvent(true);
    tripDistanceRemainingOutputEvent.setTripId("testTripId1");
    tripDistanceRemainingOutputEvent.setFleetEvent(tripfleetEvent);

    outputEventList.add(dropoffWaypointDistanceRemainingOutputEvent);
    outputEventList.add(tripDistanceRemainingOutputEvent);

    var output = distanceRemainingHandler.handleEvent(tripfleetEvent, null);
    assertEquals(outputEventList, output);
  }

  @Test
  public void updateVehicleDistanceCrossesThresholdNoPreviousDistanceRecorded_returnsOutputEvent() {
    Map<String, Change> tripDifferences = new HashMap<>();
    tripDifferences.put("remainingDistanceMeters", new Change(1100L, 1050L));

    var oldPickupWaypoint = TripWaypointData.builder().setWaypointId("testTripId1-pickup").build();
    var newPickupWaypoint =
        TripWaypointData.builder()
            .setWaypointId("testTripId1-pickup")
            .setRemainingDistanceMeters(350L)
            .build();
    var pickupWaypointDifferences =
        ImmutableMap.of("remainingDistanceMeters", new Change(null, 350L));

    var oldDropoffWaypoint =
        TripWaypointData.builder()
            .setWaypointId("testTripId1-dropoff")
            .setRemainingDistanceMeters(1100L)
            .build();
    var newDropoffWaypoint =
        TripWaypointData.builder()
            .setWaypointId("testTripId1-dropoff")
            .setRemainingDistanceMeters(1050L)
            .build();
    var dropoffWaypointDifferences =
        ImmutableMap.of("remainingDistanceMeters", new Change(1100L, 1050L));

    var oldTrip =
        TripData.builder()
            .setRemainingDistanceMeters(1100L)
            .setWaypoints(ImmutableList.of(oldPickupWaypoint, oldDropoffWaypoint))
            .setEventTimestamp(Timestamp.parseTimestamp("2023-08-02T20:51:34.621727000Z"))
            .build();

    var newTrip =
        TripData.builder()
            .setRemainingDistanceMeters(1050L)
            .setWaypoints(ImmutableList.of(newPickupWaypoint, newDropoffWaypoint))
            .setEventTimestamp(Timestamp.parseTimestamp("2023-08-02T20:56:34.621727000Z"))
            .build();

    TripFleetEvent tripfleetEvent =
        TripFleetEvent.builder()
            .setVehicleId("testVehicleId1")
            .setTripId("testTripId1")
            .setOldTrip(oldTrip)
            .setNewTrip(newTrip)
            .setOldTripWaypoints(ImmutableList.of(oldPickupWaypoint, oldDropoffWaypoint))
            .setNewTripWaypoints(ImmutableList.of(newPickupWaypoint, newDropoffWaypoint))
            .setTripDifferences(tripDifferences)
            .setTripWaypointDifferences(
                ImmutableList.of(pickupWaypointDifferences, dropoffWaypointDifferences))
            .build();
    DistanceRemainingHandler distanceRemainingHandler = new DistanceRemainingHandler();
    assertTrue(distanceRemainingHandler.respondsTo(tripfleetEvent, null, null));
    List<OutputEvent> outputEventList = new ArrayList<>();

    var pickupWaypointDistanceRemainingOutputEvent = new DistanceRemainingOutputEvent();
    pickupWaypointDistanceRemainingOutputEvent.setEventTimestamp(
        Timestamp.parseTimestamp("2023-08-02T20:56:34.621727000Z"));
    pickupWaypointDistanceRemainingOutputEvent.setNewDistanceRemainingMeters(350L);
    pickupWaypointDistanceRemainingOutputEvent.setThresholdMilliseconds(1000L);
    pickupWaypointDistanceRemainingOutputEvent.setTripId("testTripId1");
    pickupWaypointDistanceRemainingOutputEvent.setWaypointId("testTripId1-pickup");
    pickupWaypointDistanceRemainingOutputEvent.setFleetEvent(tripfleetEvent);

    outputEventList.add(pickupWaypointDistanceRemainingOutputEvent);

    var output = distanceRemainingHandler.handleEvent(tripfleetEvent, null);
    assertEquals(outputEventList, output);
  }
}
