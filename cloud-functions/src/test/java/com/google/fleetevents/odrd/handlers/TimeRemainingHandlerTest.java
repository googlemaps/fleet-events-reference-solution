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
import com.google.fleetevents.odrd.models.outputs.TimeRemainingOutputEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import org.mockito.Mockito;

public class TimeRemainingHandlerTest {

  @Test
  public void updateVehicleNoDuration_doesNotRespond() {
    TripFleetEvent tripFleetEvent =
        TripFleetEvent.builder()
            .setVehicleId("testVehicleId1")
            .setTripId("testTripId1")
            .setOldTrip(TripData.builder().build())
            .setNewTrip(TripData.builder().build())
            .setTripDifferences(ImmutableMap.of())
            .build();
    TimeRemainingHandler timeRemainingHandler = new TimeRemainingHandler();
    FirestoreDatabaseClient firestoreDatabaseClient = Mockito.mock(FirestoreDatabaseClient.class);
    doReturn(null).when(firestoreDatabaseClient).getDeliveryVehicleDocument(any(String.class));
    assertFalse(timeRemainingHandler.respondsTo(tripFleetEvent, null, firestoreDatabaseClient));
  }

  @Test
  public void updateVehicleDurationNotCrossThreshold_doesNotReport() {
    Map<String, Change> tripDifferences = new HashMap<>();
    tripDifferences.put("remainingDuration", new Change(35000L, 34000L));

    var oldPickupWaypoint = TripWaypointData.builder().setRemainingDuration(32000L).build();
    var newPickupWaypoint = TripWaypointData.builder().setRemainingDuration(310000L).build();
    var pickupWaypointDifferences =
        ImmutableMap.of("remainingDuration", new Change(32000L, 310000L));

    var oldDropoffWaypoint = TripWaypointData.builder().setRemainingDuration(35000L).build();
    var newDropoffWaypoint = TripWaypointData.builder().setRemainingDuration(34000L).build();
    var dropoffWaypointDifferences =
        ImmutableMap.of("remainingDuration", new Change(35000L, 34000L));

    var oldTrip =
        TripData.builder()
            .setRemainingDuration(35000L)
            .setWaypoints(ImmutableList.of(oldPickupWaypoint, oldDropoffWaypoint))
            .setEventTimestamp(Timestamp.parseTimestamp("2023-08-02T20:56:34.621727000Z"))
            .build();

    var newTrip =
        TripData.builder()
            .setRemainingDuration(34000L)
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
    TimeRemainingHandler timeRemainingHandler = new TimeRemainingHandler();
    assertTrue(timeRemainingHandler.respondsTo(tripfleetEvent, null, null));
    assertEquals(new ArrayList<>(), timeRemainingHandler.handleEvent(tripfleetEvent, null));
  }

  @Test
  public void updateVehicleDurationPassedThresholdDoesNotCrossThreshold_doesNotReport() {
    Map<String, Change> tripDifferences = new HashMap<>();
    tripDifferences.put("remainingDuration", new Change(50000L, 40000L));

    var oldPickupWaypoint = TripWaypointData.builder().setRemainingDuration(20000L).build();
    var newPickupWaypoint = TripWaypointData.builder().setRemainingDuration(10000L).build();
    var pickupWaypointDifferences =
        ImmutableMap.of("remainingDuration", new Change(20000L, 10000L));

    var oldDropoffWaypoint = TripWaypointData.builder().setRemainingDuration(50000L).build();
    var newDropoffWaypoint = TripWaypointData.builder().setRemainingDuration(40000L).build();
    var dropoffWaypointDifferences =
        ImmutableMap.of("remainingDuration", new Change(50000L, 40000L));

    var oldTrip =
        TripData.builder()
            .setRemainingDuration(50000L)
            .setWaypoints(ImmutableList.of(oldPickupWaypoint, oldDropoffWaypoint))
            .setEventTimestamp(Timestamp.parseTimestamp("2023-08-02T20:56:34.621727000Z"))
            .build();

    var newTrip =
        TripData.builder()
            .setRemainingDuration(40000L)
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
    TimeRemainingHandler timeRemainingHandler = new TimeRemainingHandler();
    assertTrue(timeRemainingHandler.respondsTo(tripfleetEvent, null, null));
    assertEquals(new ArrayList<>(), timeRemainingHandler.handleEvent(tripfleetEvent, null));
  }

  @Test
  public void updateVehicleDurationPassedThresholdCrossesThresholdInReverse_doesNotReport() {
    Map<String, Change> tripDifferences = new HashMap<>();
    tripDifferences.put("remainingDuration", new Change(290000L, 310000L));

    var oldPickupWaypoint = TripWaypointData.builder().setRemainingDuration(20000L).build();
    var newPickupWaypoint = TripWaypointData.builder().setRemainingDuration(40000L).build();
    var pickupWaypointDifferences =
        ImmutableMap.of("remainingDuration", new Change(20000L, 40000L));

    var oldDropoffWaypoint = TripWaypointData.builder().setRemainingDuration(290000L).build();
    var newDropoffWaypoint = TripWaypointData.builder().setRemainingDuration(310000L).build();
    var dropoffWaypointDifferences =
        ImmutableMap.of("remainingDuration", new Change(290000L, 310000L));

    var oldTrip =
        TripData.builder()
            .setRemainingDuration(290000L)
            .setWaypoints(ImmutableList.of(oldPickupWaypoint, oldDropoffWaypoint))
            .setEventTimestamp(Timestamp.parseTimestamp("2023-08-02T20:56:34.621727000Z"))
            .build();

    var newTrip =
        TripData.builder()
            .setRemainingDuration(310000L)
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
    TimeRemainingHandler timeRemainingHandler = new TimeRemainingHandler();
    assertTrue(timeRemainingHandler.respondsTo(tripfleetEvent, null, null));
    assertEquals(new ArrayList<>(), timeRemainingHandler.handleEvent(tripfleetEvent, null));
  }

  @Test
  public void updateVehicleDurationCrossesThreshold_returnsOutputEvent() {
    Map<String, Change> tripDifferences = new HashMap<>();
    tripDifferences.put("remainingDuration", new Change(310000L, 290000L));

    var oldPickupWaypoint =
        TripWaypointData.builder()
            .setWaypointId("testTripId1-pickup")
            .setRemainingDuration(40000L)
            .build();
    var newPickupWaypoint =
        TripWaypointData.builder()
            .setWaypointId("testTripId1-pickup")
            .setRemainingDuration(20000L)
            .build();
    var pickupWaypointDifferences =
        ImmutableMap.of("remainingDuration", new Change(40000L, 20000L));

    var oldDropoffWaypoint =
        TripWaypointData.builder()
            .setWaypointId("testTripId1-dropoff")
            .setRemainingDuration(310000L)
            .build();
    var newDropoffWaypoint =
        TripWaypointData.builder()
            .setWaypointId("testTripId1-dropoff")
            .setRemainingDuration(290000L)
            .build();
    var dropoffWaypointDifferences =
        ImmutableMap.of("remainingDuration", new Change(310000L, 290000L));

    var oldTrip =
        TripData.builder()
            .setRemainingDuration(310000L)
            .setWaypoints(ImmutableList.of(oldPickupWaypoint, oldDropoffWaypoint))
            .setEventTimestamp(Timestamp.parseTimestamp("2023-08-02T20:51:34.621727000Z"))
            .build();

    var newTrip =
        TripData.builder()
            .setRemainingDuration(290000L)
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
    TimeRemainingHandler timeRemainingHandler = new TimeRemainingHandler();
    assertTrue(timeRemainingHandler.respondsTo(tripfleetEvent, null, null));
    List<OutputEvent> outputEventList = new ArrayList<>();

    var dropoffWaypointTimeRemainingOutputEvent = new TimeRemainingOutputEvent();
    dropoffWaypointTimeRemainingOutputEvent.setEventTimestamp(
        Timestamp.parseTimestamp("2023-08-02T20:56:34.621727000Z"));
    dropoffWaypointTimeRemainingOutputEvent.setOldDurationRemainingMiliseconds(310000L);
    dropoffWaypointTimeRemainingOutputEvent.setNewDurationRemainingMiliseconds(290000L);

    dropoffWaypointTimeRemainingOutputEvent.setThresholdMilliseconds(300000L);
    dropoffWaypointTimeRemainingOutputEvent.setTripId("testTripId1");
    dropoffWaypointTimeRemainingOutputEvent.setWaypointId("testTripId1-dropoff");
    dropoffWaypointTimeRemainingOutputEvent.setFleetEvent(tripfleetEvent);

    var tripTimeRemainingOutputEvent = new TimeRemainingOutputEvent();
    tripTimeRemainingOutputEvent.setEventTimestamp(
        Timestamp.parseTimestamp("2023-08-02T20:56:34.621727000Z"));
    tripTimeRemainingOutputEvent.setOldDurationRemainingMiliseconds(310000L);
    tripTimeRemainingOutputEvent.setNewDurationRemainingMiliseconds(290000L);
    tripTimeRemainingOutputEvent.setThresholdMilliseconds(300000L);
    tripTimeRemainingOutputEvent.setIsTripOutputEvent(true);
    tripTimeRemainingOutputEvent.setTripId("testTripId1");
    tripTimeRemainingOutputEvent.setFleetEvent(tripfleetEvent);

    outputEventList.add(dropoffWaypointTimeRemainingOutputEvent);
    outputEventList.add(tripTimeRemainingOutputEvent);

    var output = timeRemainingHandler.handleEvent(tripfleetEvent, null);
    assertEquals(outputEventList, output);
  }

  @Test
  public void updateVehicleDurationCrossesThresholdNoPreviousDurationRecorded_returnsOutputEvent() {
    Map<String, Change> tripDifferences = new HashMap<>();
    tripDifferences.put("remainingDuration", new Change(310000L, 30050000L));

    var oldPickupWaypoint = TripWaypointData.builder().setWaypointId("testTripId1-pickup").build();
    var newPickupWaypoint =
        TripWaypointData.builder()
            .setWaypointId("testTripId1-pickup")
            .setRemainingDuration(15000L)
            .build();
    var pickupWaypointDifferences = ImmutableMap.of("remainingDuration", new Change(null, 15000L));

    var oldDropoffWaypoint =
        TripWaypointData.builder()
            .setWaypointId("testTripId1-dropoff")
            .setRemainingDuration(310000L)
            .build();
    var newDropoffWaypoint =
        TripWaypointData.builder()
            .setWaypointId("testTripId1-dropoff")
            .setRemainingDuration(30050000L)
            .build();
    var dropoffWaypointDifferences =
        ImmutableMap.of("remainingDuration", new Change(310000L, 30050000L));

    var oldTrip =
        TripData.builder()
            .setRemainingDuration(310000L)
            .setWaypoints(ImmutableList.of(oldPickupWaypoint, oldDropoffWaypoint))
            .setEventTimestamp(Timestamp.parseTimestamp("2023-08-02T20:51:34.621727000Z"))
            .build();

    var newTrip =
        TripData.builder()
            .setRemainingDuration(30050000L)
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
    TimeRemainingHandler timeRemainingHandler = new TimeRemainingHandler();
    assertTrue(timeRemainingHandler.respondsTo(tripfleetEvent, null, null));
    List<OutputEvent> outputEventList = new ArrayList<>();

    var pickupWaypointTimeRemainingOutputEvent = new TimeRemainingOutputEvent();
    pickupWaypointTimeRemainingOutputEvent.setEventTimestamp(
        Timestamp.parseTimestamp("2023-08-02T20:56:34.621727000Z"));
    pickupWaypointTimeRemainingOutputEvent.setNewDurationRemainingMiliseconds(15000L);
    pickupWaypointTimeRemainingOutputEvent.setThresholdMilliseconds(300000L);
    pickupWaypointTimeRemainingOutputEvent.setTripId("testTripId1");
    pickupWaypointTimeRemainingOutputEvent.setWaypointId("testTripId1-pickup");
    pickupWaypointTimeRemainingOutputEvent.setFleetEvent(tripfleetEvent);

    outputEventList.add(pickupWaypointTimeRemainingOutputEvent);

    var output = timeRemainingHandler.handleEvent(tripfleetEvent, null);
    assertEquals(outputEventList, output);
  }
}
