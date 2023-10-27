package com.google.fleetevents.odrd.handlers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

import com.google.cloud.Timestamp;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.fleetevents.common.database.FirestoreDatabaseClient;
import com.google.fleetevents.common.models.Change;
import com.google.fleetevents.odrd.models.TripData;
import com.google.fleetevents.odrd.models.TripFleetEvent;
import com.google.fleetevents.odrd.models.TripWaypointData;
import com.google.fleetevents.odrd.models.outputs.EtaAbsoluteChangeOutputEvent;
import com.google.fleetevents.odrd.models.outputs.EtaAssignedOutputEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import org.mockito.Mockito;

public class EtaAbsoluteChangeHandlerTest {

  @Test
  public void etaAbsoluteChangeHandler_noDuration_doesNotRespond() {
    TripFleetEvent tripFleetEvent =
        TripFleetEvent.builder()
            .setVehicleId("testVehicleId1")
            .setTripId("testTripId1")
            .setOldTrip(TripData.builder().build())
            .setNewTrip(TripData.builder().build())
            .setTripDifferences(ImmutableMap.of())
            .build();
    EtaAbsoluteChangeHandler etaAbsoluteChangeHandler = new EtaAbsoluteChangeHandler();
    FirestoreDatabaseClient firestoreDatabaseClient = Mockito.mock(FirestoreDatabaseClient.class);
    doReturn(null).when(firestoreDatabaseClient).getDeliveryVehicleDocument(any(String.class));
    assertFalse(etaAbsoluteChangeHandler.respondsTo(tripFleetEvent, null, firestoreDatabaseClient));
  }

  @Test
  public void etaAbsoluteChangeHandler_noPreviousEtaAssignedEtaOutputResponse() {
    Map<String, Change> tripDifferences = new HashMap<>();
    tripDifferences.put(
        "eta", new Change(null, Timestamp.parseTimestamp("2023-08-02T21:06:00.412009Z")));

    var oldPickupWaypoint = TripWaypointData.builder().build();
    var newPickupWaypoint =
        TripWaypointData.builder()
            .setTripId("testTripId1")
            .setWaypointId("testTripId1-pickup")
            .setEta(Timestamp.parseTimestamp("2023-08-02T20:56:00.412009Z"))
            .build();
    var pickupWaypointDifferences =
        ImmutableMap.of(
            "eta", new Change(null, Timestamp.parseTimestamp("2023-08-02T20:56:00.412009Z")));

    var oldDropoffWaypoint = TripWaypointData.builder().build();
    var newDropoffWaypoint =
        TripWaypointData.builder()
            .setTripId("dropoff")
            .setWaypointId("testTripId1-pickup")
            .setEta(Timestamp.parseTimestamp("2023-08-02T21:06:00.412009Z"))
            .setEventMetadata(
                ImmutableMap.of(
                    "originalEta", Timestamp.parseTimestamp("2023-08-02T21:06:00.412009Z")))
            .build();
    var dropoffWaypointDifferences =
        ImmutableMap.of(
            "eta", new Change(null, Timestamp.parseTimestamp("2023-08-02T21:06:00.412009Z")));

    var oldTrip =
        TripData.builder()
            .setTripId("testTripId1")
            .setWaypoints(ImmutableList.of(oldPickupWaypoint, oldDropoffWaypoint))
            .setEventMetadata(
                ImmutableMap.of(
                    "originalEta", Timestamp.parseTimestamp("2023-08-02T21:06:00.412009Z")))
            .build();

    var newTrip =
        TripData.builder()
            .setTripId("testTripId1")
            .setEta(Timestamp.parseTimestamp("2023-08-02T21:06:00.412009Z"))
            .setWaypoints(ImmutableList.of(newPickupWaypoint, newDropoffWaypoint))
            .setEventTimestamp(Timestamp.parseTimestamp("2023-08-02T20:50:34.621727000Z"))
            .setEventMetadata(
                ImmutableMap.of(
                    "originalEta", Timestamp.parseTimestamp("2023-08-02T21:06:00.412009Z")))
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
    var assignedEtaOutputEvent = new EtaAssignedOutputEvent();
    assignedEtaOutputEvent.setIdentifier("testTripId1-pickup");
    assignedEtaOutputEvent.setAssignedEta(
        Timestamp.parseTimestamp("2023-08-02T20:56:00.412009000Z"));
    assignedEtaOutputEvent.setIsTripOutputEvent(true);
    assignedEtaOutputEvent.setEventTimestamp(
        Timestamp.parseTimestamp("2023-08-02T20:50:34.621727000Z"));
    assignedEtaOutputEvent.setFleetEvent(tripfleetEvent);

    EtaAbsoluteChangeHandler etaAbsoluteChangeHandler = new EtaAbsoluteChangeHandler();
    assertTrue(etaAbsoluteChangeHandler.respondsTo(tripfleetEvent, null, null));
    assertEquals(
        ImmutableList.of(assignedEtaOutputEvent),
        etaAbsoluteChangeHandler.handleEvent(tripfleetEvent, null));
    assertEquals(
        Timestamp.parseTimestamp("2023-08-02T21:06:00.412009Z"),
        newTrip.getEventMetadata().get("originalEta"));
    assertEquals(
        Timestamp.parseTimestamp("2023-08-02T21:06:00.412009Z"),
        newDropoffWaypoint.getEventMetadata().get("originalEta"));
    assertEquals(
        Timestamp.parseTimestamp("2023-08-02T20:56:00.412009Z"),
        newPickupWaypoint.getEventMetadata().get("originalEta"));
  }

  @Test
  public void etaAbsoluteChangeHandler_hasPreviousEtaChangeDoesNotExceedThreshold() {
    Map<String, Change> tripDifferences = new HashMap<>();
    tripDifferences.put(
        "eta",
        new Change(
            Timestamp.parseTimestamp("2023-08-02T21:06:00.412009Z"),
            Timestamp.parseTimestamp("2023-08-02T21:10:00.412009Z")));

    var oldPickupWaypoint =
        TripWaypointData.builder()
            .setTripId("testTripId1")
            .setWaypointId("testTripId1-pickup")
            .setEta(Timestamp.parseTimestamp("2023-08-02T20:57:00.412009Z"))
            .setEventMetadata(
                ImmutableMap.of(
                    "originalEta", Timestamp.parseTimestamp("2023-08-02T20:56:00.412009Z")))
            .build();
    var newPickupWaypoint =
        TripWaypointData.builder()
            .setTripId("testTripId1")
            .setWaypointId("testTripId1-pickup")
            .setEta(Timestamp.parseTimestamp("2023-08-02T21:00:00.412009Z"))
            .setEventMetadata(
                ImmutableMap.of(
                    "originalEta", Timestamp.parseTimestamp("2023-08-02T20:56:00.412009Z")))
            .build();
    var pickupWaypointDifferences =
        ImmutableMap.of(
            "eta",
            new Change(
                Timestamp.parseTimestamp("2023-08-02T20:57:00.412009Z"),
                Timestamp.parseTimestamp("2023-08-02T21:00:00.412009Z")));

    var oldDropoffWaypoint =
        TripWaypointData.builder()
            .setTripId("testTripId1")
            .setWaypointId("testTripId1-dropoff")
            .setEta(Timestamp.parseTimestamp("2023-08-02T21:06:00.412009Z"))
            .setEventMetadata(
                ImmutableMap.of(
                    "originalEta", Timestamp.parseTimestamp("2023-08-02T21:06:00.412009Z")))
            .build();
    var newDropoffWaypoint =
        TripWaypointData.builder()
            .setTripId("testTripId1")
            .setWaypointId("testTripId1-pickup")
            .setEta(Timestamp.parseTimestamp("2023-08-02T21:10:00.412009Z"))
            .setEventMetadata(
                ImmutableMap.of(
                    "originalEta", Timestamp.parseTimestamp("2023-08-02T21:06:00.412009Z")))
            .build();
    var dropoffWaypointDifferences =
        ImmutableMap.of(
            "eta",
            new Change(
                Timestamp.parseTimestamp("2023-08-02T21:06:00.412009Z"),
                Timestamp.parseTimestamp("2023-08-02T21:10:00.412009Z")));

    var oldTrip =
        TripData.builder()
            .setTripId("testTripId1")
            .setEta(Timestamp.parseTimestamp("2023-08-02T21:06:00.412009Z"))
            .setWaypoints(ImmutableList.of(oldPickupWaypoint, oldDropoffWaypoint))
            .setEventMetadata(
                ImmutableMap.of(
                    "originalEta", Timestamp.parseTimestamp("2023-08-02T21:06:00.412009Z")))
            .build();

    var newTrip =
        TripData.builder()
            .setTripId("testTripId1")
            .setEta(Timestamp.parseTimestamp("2023-08-02T21:10:00.412009Z"))
            .setWaypoints(ImmutableList.of(newPickupWaypoint, newDropoffWaypoint))
            .setEventTimestamp(Timestamp.parseTimestamp("2023-08-02T20:50:34.621727000Z"))
            .setEventMetadata(
                ImmutableMap.of(
                    "originalEta", Timestamp.parseTimestamp("2023-08-02T21:06:00.412009Z")))
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
    EtaAbsoluteChangeHandler etaAbsoluteChangeHandler = new EtaAbsoluteChangeHandler();
    assertTrue(etaAbsoluteChangeHandler.respondsTo(tripfleetEvent, null, null));
    assertEquals(new ArrayList<>(), etaAbsoluteChangeHandler.handleEvent(tripfleetEvent, null));
    assertEquals(
        Timestamp.parseTimestamp("2023-08-02T21:06:00.412009Z"),
        newTrip.getEventMetadata().get("originalEta"));
    assertEquals(
        Timestamp.parseTimestamp("2023-08-02T21:06:00.412009Z"),
        newDropoffWaypoint.getEventMetadata().get("originalEta"));
    assertEquals(
        Timestamp.parseTimestamp("2023-08-02T20:56:00.412009Z"),
        newPickupWaypoint.getEventMetadata().get("originalEta"));
  }

  @Test
  public void etaAbsoluteChangeHandler_pickupHasPreviousEtaChangeExceedsThreshold() {
    Map<String, Change> tripDifferences = new HashMap<>();
    tripDifferences.put(
        "eta",
        new Change(
            Timestamp.parseTimestamp("2023-08-02T21:06:00.412009Z"),
            Timestamp.parseTimestamp("2023-08-02T21:10:00.412009Z")));

    var oldPickupWaypoint =
        TripWaypointData.builder()
            .setTripId("testTripId1")
            .setWaypointId("testTripId1-pickup")
            .setEta(Timestamp.parseTimestamp("2023-08-02T20:57:00.412009Z"))
            .setEventMetadata(
                ImmutableMap.of(
                    "originalEta", Timestamp.parseTimestamp("2023-08-02T20:56:00.412009Z")))
            .build();
    var newPickupWaypoint =
        TripWaypointData.builder()
            .setTripId("testTripId1")
            .setWaypointId("testTripId1-pickup")
            .setEta(Timestamp.parseTimestamp("2023-08-02T21:01:00.412009Z"))
            .setEventMetadata(
                ImmutableMap.of(
                    "originalEta", Timestamp.parseTimestamp("2023-08-02T20:56:00.412009Z")))
            .build();
    var pickupWaypointDifferences =
        ImmutableMap.of(
            "eta",
            new Change(
                Timestamp.parseTimestamp("2023-08-02T20:57:00.412009Z"),
                Timestamp.parseTimestamp("2023-08-02T21:01:00.412009Z")));

    var oldDropoffWaypoint =
        TripWaypointData.builder()
            .setTripId("testTripId1")
            .setWaypointId("testTripId1-dropoff")
            .setEta(Timestamp.parseTimestamp("2023-08-02T21:06:00.412009Z"))
            .setEventMetadata(
                ImmutableMap.of(
                    "originalEta", Timestamp.parseTimestamp("2023-08-02T21:06:00.412009Z")))
            .build();
    var newDropoffWaypoint =
        TripWaypointData.builder()
            .setTripId("testTripId1")
            .setWaypointId("testTripId1-pickup")
            .setEta(Timestamp.parseTimestamp("2023-08-02T21:10:00.412009Z"))
            .setEventMetadata(
                ImmutableMap.of(
                    "originalEta", Timestamp.parseTimestamp("2023-08-02T21:06:00.412009Z")))
            .build();
    var dropoffWaypointDifferences =
        ImmutableMap.of(
            "eta",
            new Change(
                Timestamp.parseTimestamp("2023-08-02T21:06:00.412009Z"),
                Timestamp.parseTimestamp("2023-08-02T21:10:00.412009Z")));

    var oldTrip =
        TripData.builder()
            .setTripId("testTripId1")
            .setEta(Timestamp.parseTimestamp("2023-08-02T21:06:00.412009Z"))
            .setWaypoints(ImmutableList.of(oldPickupWaypoint, oldDropoffWaypoint))
            .setEventMetadata(
                ImmutableMap.of(
                    "originalEta", Timestamp.parseTimestamp("2023-08-02T21:06:00.412009Z")))
            .build();

    var newTrip =
        TripData.builder()
            .setTripId("testTripId1")
            .setEta(Timestamp.parseTimestamp("2023-08-02T21:10:00.412009Z"))
            .setWaypoints(ImmutableList.of(newPickupWaypoint, newDropoffWaypoint))
            .setEventTimestamp(Timestamp.parseTimestamp("2023-08-02T20:50:34.621727000Z"))
            .setEventMetadata(
                ImmutableMap.of(
                    "originalEta", Timestamp.parseTimestamp("2023-08-02T21:06:00.412009Z")))
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
    var pickupEtaChangeOutputEvent = new EtaAbsoluteChangeOutputEvent();
    pickupEtaChangeOutputEvent.setIdentifier("testTripId1-pickup");
    pickupEtaChangeOutputEvent.setOriginalEta(
        Timestamp.parseTimestamp("2023-08-02T20:56:00.412009Z"));
    pickupEtaChangeOutputEvent.setNewEta(Timestamp.parseTimestamp("2023-08-02T21:01:00.412009Z"));
    pickupEtaChangeOutputEvent.setThresholdMilliseconds(5 * 60 * 1000);
    pickupEtaChangeOutputEvent.setEventTimestamp(
        Timestamp.parseTimestamp("2023-08-02T20:50:34.621727000Z"));
    pickupEtaChangeOutputEvent.setFleetEvent(tripfleetEvent);
    EtaAbsoluteChangeHandler etaAbsoluteChangeHandler = new EtaAbsoluteChangeHandler();
    assertTrue(etaAbsoluteChangeHandler.respondsTo(tripfleetEvent, null, null));
    assertEquals(
        ImmutableList.of(pickupEtaChangeOutputEvent),
        etaAbsoluteChangeHandler.handleEvent(tripfleetEvent, null));
    assertEquals(
        Timestamp.parseTimestamp("2023-08-02T21:06:00.412009Z"),
        newTrip.getEventMetadata().get("originalEta"));
    assertEquals(
        Timestamp.parseTimestamp("2023-08-02T21:06:00.412009Z"),
        newDropoffWaypoint.getEventMetadata().get("originalEta"));
    assertEquals(
        Timestamp.parseTimestamp("2023-08-02T20:56:00.412009Z"),
        newPickupWaypoint.getEventMetadata().get("originalEta"));
  }

  @Test
  public void etaAbsoluteChangeHandler_pickupDropoffAndTripHavePreviousEtaChangeExceedsThreshold() {
    Map<String, Change> tripDifferences = new HashMap<>();
    tripDifferences.put(
        "eta",
        new Change(
            Timestamp.parseTimestamp("2023-08-02T21:06:00.412009Z"),
            Timestamp.parseTimestamp("2023-08-02T21:12:00.412009Z")));

    var oldPickupWaypoint =
        TripWaypointData.builder()
            .setTripId("testTripId1")
            .setWaypointId("testTripId1-pickup")
            .setEta(Timestamp.parseTimestamp("2023-08-02T20:57:00.412009Z"))
            .setEventMetadata(
                ImmutableMap.of(
                    "originalEta", Timestamp.parseTimestamp("2023-08-02T20:56:00.412009Z")))
            .build();
    var newPickupWaypoint =
        TripWaypointData.builder()
            .setTripId("testTripId1")
            .setWaypointId("testTripId1-pickup")
            .setEta(Timestamp.parseTimestamp("2023-08-02T21:01:00.412009Z"))
            .setEventMetadata(
                ImmutableMap.of(
                    "originalEta", Timestamp.parseTimestamp("2023-08-02T20:56:00.412009Z")))
            .build();
    var pickupWaypointDifferences =
        ImmutableMap.of(
            "eta",
            new Change(
                Timestamp.parseTimestamp("2023-08-02T20:57:00.412009Z"),
                Timestamp.parseTimestamp("2023-08-02T21:01:00.412009Z")));

    var oldDropoffWaypoint =
        TripWaypointData.builder()
            .setTripId("testTripId1")
            .setWaypointId("testTripId1-dropoff")
            .setEta(Timestamp.parseTimestamp("2023-08-02T21:06:00.412009Z"))
            .setEventMetadata(
                ImmutableMap.of(
                    "originalEta", Timestamp.parseTimestamp("2023-08-02T21:06:00.412009Z")))
            .build();
    var newDropoffWaypoint =
        TripWaypointData.builder()
            .setTripId("testTripId1")
            .setWaypointId("testTripId1-dropoff")
            .setEta(Timestamp.parseTimestamp("2023-08-02T21:12:00.412009Z"))
            .setEventMetadata(
                ImmutableMap.of(
                    "originalEta", Timestamp.parseTimestamp("2023-08-02T21:06:00.412009Z")))
            .build();
    var dropoffWaypointDifferences =
        ImmutableMap.of(
            "eta",
            new Change(
                Timestamp.parseTimestamp("2023-08-02T21:06:00.412009Z"),
                Timestamp.parseTimestamp("2023-08-02T21:10:00.412009Z")));

    var oldTrip =
        TripData.builder()
            .setTripId("testTripId1")
            .setEta(Timestamp.parseTimestamp("2023-08-02T21:06:00.412009Z"))
            .setWaypoints(ImmutableList.of(oldPickupWaypoint, oldDropoffWaypoint))
            .setEventMetadata(
                ImmutableMap.of(
                    "originalEta", Timestamp.parseTimestamp("2023-08-02T21:06:00.412009Z")))
            .build();

    var newTrip =
        TripData.builder()
            .setTripId("testTripId1")
            .setEta(Timestamp.parseTimestamp("2023-08-02T21:12:00.412009Z"))
            .setWaypoints(ImmutableList.of(newPickupWaypoint, newDropoffWaypoint))
            .setEventTimestamp(Timestamp.parseTimestamp("2023-08-02T20:50:34.621727000Z"))
            .setEventMetadata(
                ImmutableMap.of(
                    "originalEta", Timestamp.parseTimestamp("2023-08-02T21:06:00.412009Z")))
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
    var pickupEtaChangeOutputEvent = new EtaAbsoluteChangeOutputEvent();
    pickupEtaChangeOutputEvent.setIdentifier("testTripId1-pickup");
    pickupEtaChangeOutputEvent.setOriginalEta(
        Timestamp.parseTimestamp("2023-08-02T20:56:00.412009Z"));
    pickupEtaChangeOutputEvent.setNewEta(Timestamp.parseTimestamp("2023-08-02T21:01:00.412009Z"));
    pickupEtaChangeOutputEvent.setThresholdMilliseconds(5 * 60 * 1000);
    pickupEtaChangeOutputEvent.setEventTimestamp(
        Timestamp.parseTimestamp("2023-08-02T20:50:34.621727000Z"));
    pickupEtaChangeOutputEvent.setFleetEvent(tripfleetEvent);

    var dropoffEtaChangeOutputEvent = new EtaAbsoluteChangeOutputEvent();
    dropoffEtaChangeOutputEvent.setIdentifier("testTripId1-dropoff");
    dropoffEtaChangeOutputEvent.setOriginalEta(
        Timestamp.parseTimestamp("2023-08-02T21:06:00.412009Z"));
    dropoffEtaChangeOutputEvent.setNewEta(Timestamp.parseTimestamp("2023-08-02T21:12:00.412009Z"));
    dropoffEtaChangeOutputEvent.setThresholdMilliseconds(5 * 60 * 1000);
    dropoffEtaChangeOutputEvent.setEventTimestamp(
        Timestamp.parseTimestamp("2023-08-02T20:50:34.621727000Z"));
    dropoffEtaChangeOutputEvent.setFleetEvent(tripfleetEvent);

    var tripEtaChangeOutputEvent = new EtaAbsoluteChangeOutputEvent();
    tripEtaChangeOutputEvent.setIdentifier("testTripId1");
    tripEtaChangeOutputEvent.setOriginalEta(
        Timestamp.parseTimestamp("2023-08-02T21:06:00.412009Z"));
    tripEtaChangeOutputEvent.setNewEta(Timestamp.parseTimestamp("2023-08-02T21:12:00.412009Z"));
    tripEtaChangeOutputEvent.setThresholdMilliseconds(5 * 60 * 1000);
    tripEtaChangeOutputEvent.setIsTripOutputEvent(true);
    tripEtaChangeOutputEvent.setEventTimestamp(
        Timestamp.parseTimestamp("2023-08-02T20:50:34.621727000Z"));
    tripEtaChangeOutputEvent.setFleetEvent(tripfleetEvent);

    EtaAbsoluteChangeHandler etaAbsoluteChangeHandler = new EtaAbsoluteChangeHandler();
    assertTrue(etaAbsoluteChangeHandler.respondsTo(tripfleetEvent, null, null));

    assertEquals(
        ImmutableList.of(
            pickupEtaChangeOutputEvent, dropoffEtaChangeOutputEvent, tripEtaChangeOutputEvent),
        etaAbsoluteChangeHandler.handleEvent(tripfleetEvent, null));
    assertEquals(
        Timestamp.parseTimestamp("2023-08-02T21:06:00.412009Z"),
        newTrip.getEventMetadata().get("originalEta"));
    assertEquals(
        Timestamp.parseTimestamp("2023-08-02T21:06:00.412009Z"),
        newDropoffWaypoint.getEventMetadata().get("originalEta"));
    assertEquals(
        Timestamp.parseTimestamp("2023-08-02T20:56:00.412009Z"),
        newPickupWaypoint.getEventMetadata().get("originalEta"));
  }

  @Test
  public void
      etaAbsoluteChangeHandler_intermediateWaypointAlsoHasPreviousEtaChangeExceedsThreshold() {
    Map<String, Change> tripDifferences = new HashMap<>();
    tripDifferences.put(
        "eta",
        new Change(
            Timestamp.parseTimestamp("2023-08-02T21:06:00.412009Z"),
            Timestamp.parseTimestamp("2023-08-02T21:12:00.412009Z")));

    var oldPickupWaypoint =
        TripWaypointData.builder()
            .setTripId("testTripId1")
            .setWaypointId("testTripId1-pickup")
            .setEta(Timestamp.parseTimestamp("2023-08-02T20:57:00.412009Z"))
            .setEventMetadata(
                ImmutableMap.of(
                    "originalEta", Timestamp.parseTimestamp("2023-08-02T20:56:00.412009Z")))
            .build();
    var newPickupWaypoint =
        TripWaypointData.builder()
            .setTripId("testTripId1")
            .setWaypointId("testTripId1-pickup")
            .setEta(Timestamp.parseTimestamp("2023-08-02T21:01:00.412009Z"))
            .setEventMetadata(
                ImmutableMap.of(
                    "originalEta", Timestamp.parseTimestamp("2023-08-02T20:56:00.412009Z")))
            .build();
    var pickupWaypointDifferences =
        ImmutableMap.of(
            "eta",
            new Change(
                Timestamp.parseTimestamp("2023-08-02T20:57:00.412009Z"),
                Timestamp.parseTimestamp("2023-08-02T21:01:00.412009Z")));

    var oldIntermediateWaypoint =
        TripWaypointData.builder()
            .setTripId("testTripId1")
            .setWaypointId("testTripId1-1")
            .setEta(Timestamp.parseTimestamp("2023-08-02T20:58:00.412009Z"))
            .setEventMetadata(
                ImmutableMap.of(
                    "originalEta", Timestamp.parseTimestamp("2023-08-02T20:57:00.412009Z")))
            .build();
    var newIntermediateWaypoint =
        TripWaypointData.builder()
            .setTripId("testTripId1")
            .setWaypointId("testTripId1-1")
            .setEta(Timestamp.parseTimestamp("2023-08-02T21:02:00.412009Z"))
            .setEventMetadata(
                ImmutableMap.of(
                    "originalEta", Timestamp.parseTimestamp("2023-08-02T20:57:00.412009Z")))
            .build();
    var intermediateWaypointDifferences =
        ImmutableMap.of(
            "eta",
            new Change(
                Timestamp.parseTimestamp("2023-08-02T20:58:00.412009Z"),
                Timestamp.parseTimestamp("2023-08-02T21:02:00.412009Z")));

    var oldDropoffWaypoint =
        TripWaypointData.builder()
            .setTripId("testTripId1")
            .setWaypointId("testTripId1-dropoff")
            .setEta(Timestamp.parseTimestamp("2023-08-02T21:06:00.412009Z"))
            .setEventMetadata(
                ImmutableMap.of(
                    "originalEta", Timestamp.parseTimestamp("2023-08-02T21:06:00.412009Z")))
            .build();
    var newDropoffWaypoint =
        TripWaypointData.builder()
            .setTripId("testTripId1")
            .setWaypointId("testTripId1-dropoff")
            .setEta(Timestamp.parseTimestamp("2023-08-02T21:12:00.412009Z"))
            .setEventMetadata(
                ImmutableMap.of(
                    "originalEta", Timestamp.parseTimestamp("2023-08-02T21:06:00.412009Z")))
            .build();
    var dropoffWaypointDifferences =
        ImmutableMap.of(
            "eta",
            new Change(
                Timestamp.parseTimestamp("2023-08-02T21:06:00.412009Z"),
                Timestamp.parseTimestamp("2023-08-02T21:10:00.412009Z")));

    var oldTrip =
        TripData.builder()
            .setTripId("testTripId1")
            .setEta(Timestamp.parseTimestamp("2023-08-02T21:06:00.412009Z"))
            .setWaypoints(
                ImmutableList.of(oldPickupWaypoint, oldIntermediateWaypoint, oldDropoffWaypoint))
            .build();

    var newTrip =
        TripData.builder()
            .setTripId("testTripId1")
            .setEta(Timestamp.parseTimestamp("2023-08-02T21:12:00.412009Z"))
            .setWaypoints(
                ImmutableList.of(newPickupWaypoint, newIntermediateWaypoint, newDropoffWaypoint))
            .setEventTimestamp(Timestamp.parseTimestamp("2023-08-02T20:50:34.621727000Z"))
            .setEventMetadata(
                ImmutableMap.of(
                    "originalEta", Timestamp.parseTimestamp("2023-08-02T21:06:00.412009Z")))
            .build();

    TripFleetEvent tripfleetEvent =
        TripFleetEvent.builder()
            .setVehicleId("testVehicleId1")
            .setTripId("testTripId1")
            .setOldTrip(oldTrip)
            .setNewTrip(newTrip)
            .setOldTripWaypoints(
                ImmutableList.of(oldPickupWaypoint, oldIntermediateWaypoint, oldDropoffWaypoint))
            .setNewTripWaypoints(
                ImmutableList.of(newPickupWaypoint, newIntermediateWaypoint, newDropoffWaypoint))
            .setTripDifferences(tripDifferences)
            .setTripWaypointDifferences(
                ImmutableList.of(
                    pickupWaypointDifferences,
                    intermediateWaypointDifferences,
                    dropoffWaypointDifferences))
            .build();
    var pickupEtaChangeOutputEvent = new EtaAbsoluteChangeOutputEvent();
    pickupEtaChangeOutputEvent.setIdentifier("testTripId1-pickup");
    pickupEtaChangeOutputEvent.setOriginalEta(
        Timestamp.parseTimestamp("2023-08-02T20:56:00.412009Z"));
    pickupEtaChangeOutputEvent.setNewEta(Timestamp.parseTimestamp("2023-08-02T21:01:00.412009Z"));
    pickupEtaChangeOutputEvent.setThresholdMilliseconds(5 * 60 * 1000);
    pickupEtaChangeOutputEvent.setEventTimestamp(
        Timestamp.parseTimestamp("2023-08-02T20:50:34.621727000Z"));
    pickupEtaChangeOutputEvent.setFleetEvent(tripfleetEvent);

    var intermediateEtaChangeOutputEvent = new EtaAbsoluteChangeOutputEvent();
    intermediateEtaChangeOutputEvent.setIdentifier("testTripId1-1");
    intermediateEtaChangeOutputEvent.setOriginalEta(
        Timestamp.parseTimestamp("2023-08-02T20:57:00.412009Z"));
    intermediateEtaChangeOutputEvent.setNewEta(
        Timestamp.parseTimestamp("2023-08-02T21:02:00.412009Z"));
    intermediateEtaChangeOutputEvent.setThresholdMilliseconds(5 * 60 * 1000);
    intermediateEtaChangeOutputEvent.setEventTimestamp(
        Timestamp.parseTimestamp("2023-08-02T20:50:34.621727000Z"));
    intermediateEtaChangeOutputEvent.setFleetEvent(tripfleetEvent);

    var dropoffEtaChangeOutputEvent = new EtaAbsoluteChangeOutputEvent();
    dropoffEtaChangeOutputEvent.setIdentifier("testTripId1-dropoff");
    dropoffEtaChangeOutputEvent.setOriginalEta(
        Timestamp.parseTimestamp("2023-08-02T21:06:00.412009Z"));
    dropoffEtaChangeOutputEvent.setNewEta(Timestamp.parseTimestamp("2023-08-02T21:12:00.412009Z"));
    dropoffEtaChangeOutputEvent.setThresholdMilliseconds(5 * 60 * 1000);
    dropoffEtaChangeOutputEvent.setEventTimestamp(
        Timestamp.parseTimestamp("2023-08-02T20:50:34.621727000Z"));
    dropoffEtaChangeOutputEvent.setFleetEvent(tripfleetEvent);

    var tripEtaChangeOutputEvent = new EtaAbsoluteChangeOutputEvent();
    tripEtaChangeOutputEvent.setIdentifier("testTripId1");
    tripEtaChangeOutputEvent.setOriginalEta(
        Timestamp.parseTimestamp("2023-08-02T21:06:00.412009Z"));
    tripEtaChangeOutputEvent.setNewEta(Timestamp.parseTimestamp("2023-08-02T21:12:00.412009Z"));
    tripEtaChangeOutputEvent.setThresholdMilliseconds(5 * 60 * 1000);
    tripEtaChangeOutputEvent.setIsTripOutputEvent(true);
    tripEtaChangeOutputEvent.setEventTimestamp(
        Timestamp.parseTimestamp("2023-08-02T20:50:34.621727000Z"));
    tripEtaChangeOutputEvent.setFleetEvent(tripfleetEvent);

    EtaAbsoluteChangeHandler etaAbsoluteChangeHandler = new EtaAbsoluteChangeHandler();
    assertTrue(etaAbsoluteChangeHandler.respondsTo(tripfleetEvent, null, null));

    assertEquals(
        ImmutableList.of(
            pickupEtaChangeOutputEvent,
            intermediateEtaChangeOutputEvent,
            dropoffEtaChangeOutputEvent,
            tripEtaChangeOutputEvent),
        etaAbsoluteChangeHandler.handleEvent(tripfleetEvent, null));
    assertEquals(
        Timestamp.parseTimestamp("2023-08-02T21:06:00.412009Z"),
        newTrip.getEventMetadata().get("originalEta"));
    assertEquals(
        Timestamp.parseTimestamp("2023-08-02T21:06:00.412009Z"),
        newDropoffWaypoint.getEventMetadata().get("originalEta"));
    assertEquals(
        Timestamp.parseTimestamp("2023-08-02T20:56:00.412009Z"),
        newPickupWaypoint.getEventMetadata().get("originalEta"));
  }

  @Test
  public void etaAbsoluteChangeHandler_intermediateWaypointAddedNoUpdatesMetadataEmpty() {
    Map<String, Change> tripDifferences = new HashMap<>();
    tripDifferences.put(
        "eta",
        new Change(
            Timestamp.parseTimestamp("2023-08-02T21:06:00.412009Z"),
            Timestamp.parseTimestamp("2023-08-02T21:12:00.412009Z")));

    var oldPickupWaypoint =
        TripWaypointData.builder()
            .setTripId("testTripId1")
            .setWaypointId("testTripId1-pickup")
            .setEta(Timestamp.parseTimestamp("2023-08-02T20:57:00.412009Z"))
            .setEventMetadata(
                ImmutableMap.of(
                    "originalEta", Timestamp.parseTimestamp("2023-08-02T20:56:00.412009Z")))
            .build();
    var newPickupWaypoint =
        TripWaypointData.builder()
            .setTripId("testTripId1")
            .setWaypointId("testTripId1-pickup")
            .setEta(Timestamp.parseTimestamp("2023-08-02T21:01:00.412009Z"))
            .setEventMetadata(new HashMap<>())
            .build();

    var addedIntermediateWaypoint =
        TripWaypointData.builder()
            .setTripId("testTripId1")
            .setWaypointId("testTripId1-1")
            .setEta(Timestamp.parseTimestamp("2023-08-02T20:58:00.412009Z"))
            .setEventMetadata(
                Maps.newHashMap(
                    ImmutableMap.of(
                        "originalEta", Timestamp.parseTimestamp("2023-08-02T21:06:00.412009Z"))))
            .build();

    var oldDropoffWaypoint =
        TripWaypointData.builder()
            .setTripId("testTripId1")
            .setWaypointId("testTripId1-dropoff")
            .setEta(Timestamp.parseTimestamp("2023-08-02T21:06:00.412009Z"))
            .setEventMetadata(
                ImmutableMap.of(
                    "originalEta", Timestamp.parseTimestamp("2023-08-02T21:06:00.412009Z")))
            .build();
    var newDropoffWaypoint =
        TripWaypointData.builder()
            .setTripId("testTripId1")
            .setWaypointId("testTripId1-dropoff")
            .setEta(Timestamp.parseTimestamp("2023-08-02T21:12:00.412009Z"))
            .setEventMetadata(new HashMap<>())
            .build();
    var firstPickupPointDifferences =
        ImmutableMap.of(
            "eta",
            new Change(
                Timestamp.parseTimestamp("2023-08-02T20:57:00.412009Z"),
                Timestamp.parseTimestamp("2023-08-02T21:01:00.412009Z")));
    var secondWaypointDifferences =
        ImmutableMap.of(
            "eta",
            new Change(
                Timestamp.parseTimestamp("2023-08-02T21:06:00.412009Z"),
                Timestamp.parseTimestamp("2023-08-02T21:10:00.412009Z")));
    var thirdWaypointDifferences =
        ImmutableMap.of(
            "eta", new Change(null, Timestamp.parseTimestamp("2023-08-02T21:12:00.412009000Z")));
    var oldTrip =
        TripData.builder()
            .setTripId("testTripId1")
            .setEta(Timestamp.parseTimestamp("2023-08-02T21:06:00.412009Z"))
            .setWaypoints(ImmutableList.of(oldPickupWaypoint, oldDropoffWaypoint))
            .build();

    var newTrip =
        TripData.builder()
            .setTripId("testTripId1")
            .setEta(Timestamp.parseTimestamp("2023-08-02T21:12:00.412009Z"))
            .setWaypoints(
                ImmutableList.of(newPickupWaypoint, addedIntermediateWaypoint, newDropoffWaypoint))
            .setEventTimestamp(Timestamp.parseTimestamp("2023-08-02T20:50:34.621727000Z"))
            .setEventMetadata(new HashMap<>())
            .build();

    TripFleetEvent tripfleetEvent =
        TripFleetEvent.builder()
            .setVehicleId("testVehicleId1")
            .setTripId("testTripId1")
            .setOldTrip(oldTrip)
            .setNewTrip(newTrip)
            .setOldTripWaypoints(ImmutableList.of(oldPickupWaypoint, oldDropoffWaypoint))
            .setNewTripWaypoints(
                ImmutableList.of(newPickupWaypoint, addedIntermediateWaypoint, newDropoffWaypoint))
            .setTripDifferences(tripDifferences)
            .setTripWaypointDifferences(
                ImmutableList.of(
                    firstPickupPointDifferences,
                    secondWaypointDifferences,
                    thirdWaypointDifferences))
            .setWaypointsChanged(true)
            .build();

    EtaAbsoluteChangeHandler etaAbsoluteChangeHandler = new EtaAbsoluteChangeHandler();

    var pickupEtaAssignedOutputEvent = new EtaAssignedOutputEvent();
    pickupEtaAssignedOutputEvent.setIdentifier("testTripId1-pickup");
    pickupEtaAssignedOutputEvent.setAssignedEta(
        Timestamp.parseTimestamp("2023-08-02T21:01:00.412009000Z"));
    pickupEtaAssignedOutputEvent.setEventTimestamp(
        Timestamp.parseTimestamp("2023-08-02T20:50:34.621727000Z"));
    pickupEtaAssignedOutputEvent.setFleetEvent(tripfleetEvent);

    var intermediateEtaAssignedOutputEvent = new EtaAssignedOutputEvent();
    intermediateEtaAssignedOutputEvent.setIdentifier("testTripId1-1");
    intermediateEtaAssignedOutputEvent.setAssignedEta(
        Timestamp.parseTimestamp("2023-08-02T21:10:00.412009000Z"));
    intermediateEtaAssignedOutputEvent.setEventTimestamp(
        Timestamp.parseTimestamp("2023-08-02T20:50:34.621727000Z"));
    intermediateEtaAssignedOutputEvent.setFleetEvent(tripfleetEvent);

    var dropoffEtaAssignedOutputEvent = new EtaAssignedOutputEvent();
    dropoffEtaAssignedOutputEvent.setIdentifier("testTripId1-dropoff");
    dropoffEtaAssignedOutputEvent.setAssignedEta(
        Timestamp.parseTimestamp("2023-08-02T21:12:00.412009Z"));
    dropoffEtaAssignedOutputEvent.setEventTimestamp(
        Timestamp.parseTimestamp("2023-08-02T20:50:34.621727000Z"));
    dropoffEtaAssignedOutputEvent.setFleetEvent(tripfleetEvent);

    var tripEtaAssignedOutputEvent = new EtaAssignedOutputEvent();
    tripEtaAssignedOutputEvent.setIdentifier("testTripId1");
    tripEtaAssignedOutputEvent.setAssignedEta(
        Timestamp.parseTimestamp("2023-08-02T21:12:00.412009Z"));
    tripEtaAssignedOutputEvent.setEventTimestamp(
        Timestamp.parseTimestamp("2023-08-02T20:50:34.621727000Z"));
    tripEtaAssignedOutputEvent.setIsTripOutputEvent(true);
    tripEtaAssignedOutputEvent.setFleetEvent(tripfleetEvent);

    assertTrue(etaAbsoluteChangeHandler.respondsTo(tripfleetEvent, null, null));

    assertEquals(
        ImmutableList.of(
            pickupEtaAssignedOutputEvent,
            intermediateEtaAssignedOutputEvent,
            dropoffEtaAssignedOutputEvent,
            tripEtaAssignedOutputEvent),
        etaAbsoluteChangeHandler.handleEvent(tripfleetEvent, null));
    assertEquals(
        Timestamp.parseTimestamp("2023-08-02T21:12:00.412009000Z"),
        newTrip.getEventMetadata().get("originalEta"));
    assertEquals(
        Timestamp.parseTimestamp("2023-08-02T21:12:00.412009000Z"),
        newDropoffWaypoint.getEventMetadata().get("originalEta"));
    assertEquals(
        Timestamp.parseTimestamp("2023-08-02T21:10:00.412009000Z"),
        addedIntermediateWaypoint.getEventMetadata().get("originalEta"));
    assertEquals(
        Timestamp.parseTimestamp("2023-08-02T21:01:00.412009000Z"),
        newPickupWaypoint.getEventMetadata().get("originalEta"));
  }
}
