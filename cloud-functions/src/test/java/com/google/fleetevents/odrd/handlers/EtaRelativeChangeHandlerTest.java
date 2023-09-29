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
import com.google.fleetevents.odrd.models.outputs.EtaRelativeChangeOutputEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import org.mockito.Mockito;

public class EtaRelativeChangeHandlerTest {

  @Test
  public void etaRelativeChangeHandler_noDuration_doesNotRespond() {
    TripFleetEvent tripFleetEvent =
        TripFleetEvent.builder()
            .setVehicleId("testVehicleId1")
            .setTripId("testTripId1")
            .setOldTrip(
                TripData.builder().setVehicleId("testVehicleId1").setTripId("testTripId1").build())
            .setNewTrip(
                TripData.builder().setVehicleId("testVehicleId1").setTripId("testTripId1").build())
            .setTripDifferences(ImmutableMap.of())
            .build();
    EtaRelativeChangeHandler etaRelativeChangeHandler = new EtaRelativeChangeHandler();
    FirestoreDatabaseClient firestoreDatabaseClient = Mockito.mock(FirestoreDatabaseClient.class);
    doReturn(null).when(firestoreDatabaseClient).getDeliveryVehicleDocument(any(String.class));
    assertFalse(etaRelativeChangeHandler.respondsTo(tripFleetEvent, null, firestoreDatabaseClient));
  }

  @Test
  public void etaRelativeChangeHandler_noPreviousEtaNoResponse() {
    Map<String, Change> tripDifferences = new HashMap<>();
    tripDifferences.put(
        "eta", new Change(null, Timestamp.parseTimestamp("2023-08-02T21:06:00.412009Z")));

    var oldPickupWaypoint = TripWaypointData.builder().build();
    var newPickupWaypoint =
        TripWaypointData.builder()
            .setEta(Timestamp.parseTimestamp("2023-08-02T20:56:00.412009Z"))
            .build();
    var pickupWaypointDifferences =
        ImmutableMap.of(
            "eta", new Change(null, Timestamp.parseTimestamp("2023-08-02T20:56:00.412009Z")));

    var oldDropoffWaypoint =
        TripWaypointData.builder()
            .setTripId("testTripId1")
            .setWaypointId("testTripId1-pickup")
            .build();

    var newDropoffWaypoint =
        TripWaypointData.builder()
            .setTripId("testTripId1")
            .setWaypointId("testTripId1-pickup")
            .setEta(Timestamp.parseTimestamp("2023-08-02T21:06:00.412009Z"))
            .build();
    var dropoffWaypointDifferences =
        ImmutableMap.of(
            "eta", new Change(null, Timestamp.parseTimestamp("2023-08-02T21:06:00.412009Z")));

    var oldTrip =
        TripData.builder()
            .setVehicleId("testVehicleId1")
            .setTripId("testTripId1")
            .setWaypoints(ImmutableList.of(oldPickupWaypoint, oldDropoffWaypoint))
            .setCurrentWaypointIndex(0L)
            .build();

    var newTrip =
        TripData.builder()
            .setVehicleId("testVehicleId1")
            .setTripId("testTripId1")
            .setEta(Timestamp.parseTimestamp("2023-08-02T21:06:00.412009Z"))
            .setWaypoints(ImmutableList.of(newPickupWaypoint, newDropoffWaypoint))
            .setEventTimestamp(Timestamp.parseTimestamp("2023-08-02T20:50:34.621727000Z"))
            .setCurrentWaypointIndex(0L)
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
    EtaRelativeChangeHandler etaRelativeChangeHandler = new EtaRelativeChangeHandler();
    assertTrue(etaRelativeChangeHandler.respondsTo(tripfleetEvent, null, null));
    assertEquals(new ArrayList<>(), etaRelativeChangeHandler.handleEvent(tripfleetEvent, null));
    assertEquals(
        ImmutableMap.of(
            EtaRelativeChangeHandler.ORIGINAL_DURATION_MILLISECONDS,
            325791L,
            EtaRelativeChangeHandler.ORIGINAL_ETA,
            Timestamp.parseTimestamp("2023-08-02T20:56:00.412009Z")),
        newPickupWaypoint.getEventMetadata().get("relativeEtaPair"));
  }

  @Test
  public void etaRelativeChangeHandler_pickupHasPreviousEtaChangeDoesNotExceedThreshold() {
    Map<String, Change> tripDifferences = new HashMap<>();
    tripDifferences.put(
        "eta",
        new Change(
            Timestamp.parseTimestamp("2023-08-02T21:06:00.412009Z"),
            Timestamp.parseTimestamp("2023-08-02T21:10:00.412009Z")));

    Map<String, Object> pickupEventMetadata =
        Maps.newHashMap(
            ImmutableMap.of(
                "relativeEtaPair",
                ImmutableMap.of(
                    EtaRelativeChangeHandler.ORIGINAL_DURATION_MILLISECONDS,
                    4000000L,
                    EtaRelativeChangeHandler.ORIGINAL_ETA,
                    Timestamp.parseTimestamp("2023-08-02T21:06:00.412009Z"))));
    var oldPickupWaypoint =
        TripWaypointData.builder()
            .setTripId("testTripId1")
            .setWaypointId("testTripId1-pickup")
            .setEta(Timestamp.parseTimestamp("2023-08-02T20:57:00.412009Z"))
            .setEventMetadata(pickupEventMetadata)
            .build();
    var newPickupWaypoint =
        TripWaypointData.builder()
            .setTripId("testTripId1")
            .setWaypointId("testTripId1-pickup")
            .setEta(Timestamp.parseTimestamp("2023-08-02T21:00:00.412009Z"))
            .setEventMetadata(pickupEventMetadata)
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
            .build();
    var newDropoffWaypoint =
        TripWaypointData.builder()
            .setTripId("testTripId1")
            .setWaypointId("testTripId1-pickup")
            .setEta(Timestamp.parseTimestamp("2023-08-02T21:10:00.412009Z"))
            .build();
    var dropoffWaypointDifferences =
        ImmutableMap.of(
            "eta",
            new Change(
                Timestamp.parseTimestamp("2023-08-02T21:06:00.412009Z"),
                Timestamp.parseTimestamp("2023-08-02T21:10:00.412009Z")));

    var oldTrip =
        TripData.builder()
            .setVehicleId("testVehicleId1")
            .setTripId("testTripId1")
            .setEta(Timestamp.parseTimestamp("2023-08-02T21:06:00.412009Z"))
            .setWaypoints(ImmutableList.of(oldPickupWaypoint, oldDropoffWaypoint))
            .setCurrentWaypointIndex(0L)
            .build();

    var newTrip =
        TripData.builder()
            .setVehicleId("testVehicleId1")
            .setTripId("testTripId1")
            .setEta(Timestamp.parseTimestamp("2023-08-02T21:10:00.412009Z"))
            .setWaypoints(ImmutableList.of(newPickupWaypoint, newDropoffWaypoint))
            .setCurrentWaypointIndex(0L)
            .setEventTimestamp(Timestamp.parseTimestamp("2023-08-02T20:50:34.621727000Z"))
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
    EtaRelativeChangeHandler etaRelativeChangeHandler = new EtaRelativeChangeHandler();
    assertTrue(etaRelativeChangeHandler.respondsTo(tripfleetEvent, null, null));
    assertEquals(new ArrayList<>(), etaRelativeChangeHandler.handleEvent(tripfleetEvent, null));
    assertEquals(
        ImmutableMap.of(
            EtaRelativeChangeHandler.ORIGINAL_DURATION_MILLISECONDS,
            1165791L,
            EtaRelativeChangeHandler.ORIGINAL_ETA,
            Timestamp.parseTimestamp("2023-08-02T21:10:00.412009000Z")),
        newTrip.getEventMetadata().get("relativeEtaPair"));
    assertEquals(
        ImmutableMap.of(
            EtaRelativeChangeHandler.ORIGINAL_DURATION_MILLISECONDS,
            1165791L,
            EtaRelativeChangeHandler.ORIGINAL_ETA,
            Timestamp.parseTimestamp("2023-08-02T21:10:00.412009000Z")),
        newDropoffWaypoint.getEventMetadata().get("relativeEtaPair"));
    assertEquals(
        ImmutableMap.of(
            EtaRelativeChangeHandler.ORIGINAL_DURATION_MILLISECONDS,
            4000000L,
            EtaRelativeChangeHandler.ORIGINAL_ETA,
            Timestamp.parseTimestamp("2023-08-02T21:06:00.412009Z")),
        newPickupWaypoint.getEventMetadata().get("relativeEtaPair"));
  }

  @Test
  public void etaRelativeChangeHandler_pickupHasPreviousEtaChangeExceedsThreshold() {
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
                Maps.newHashMap(
                    ImmutableMap.of(
                        "relativeEtaPair",
                        ImmutableMap.of(
                            EtaRelativeChangeHandler.ORIGINAL_DURATION_MILLISECONDS,
                            1000000L,
                            EtaRelativeChangeHandler.ORIGINAL_ETA,
                            Timestamp.parseTimestamp("2023-08-02T20:56:00.412009Z")))))
            .build();
    var newPickupWaypoint =
        TripWaypointData.builder()
            .setTripId("testTripId1")
            .setWaypointId("testTripId1-pickup")
            .setEta(Timestamp.parseTimestamp("2023-08-02T21:01:00.412009Z"))
            .setEventMetadata(
                Maps.newHashMap(
                    ImmutableMap.of(
                        "relativeEtaPair",
                        ImmutableMap.of(
                            EtaRelativeChangeHandler.ORIGINAL_DURATION_MILLISECONDS,
                            1000000L,
                            EtaRelativeChangeHandler.ORIGINAL_ETA,
                            Timestamp.parseTimestamp("2023-08-02T20:56:00.412009Z")))))
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
                Maps.newHashMap(
                    ImmutableMap.of(
                        "relativeEtaPair",
                        ImmutableMap.of(
                            EtaRelativeChangeHandler.ORIGINAL_DURATION_MILLISECONDS,
                            1000000L,
                            EtaRelativeChangeHandler.ORIGINAL_ETA,
                            Timestamp.parseTimestamp("2023-08-02T21:06:00.412009Z")))))
            .build();
    var newDropoffWaypoint =
        TripWaypointData.builder()
            .setTripId("testTripId1")
            .setWaypointId("testTripId1-pickup")
            .setEta(Timestamp.parseTimestamp("2023-08-02T21:10:00.412009Z"))
            .build();
    var dropoffWaypointDifferences =
        ImmutableMap.of(
            "eta",
            new Change(
                Timestamp.parseTimestamp("2023-08-02T21:06:00.412009Z"),
                Timestamp.parseTimestamp("2023-08-02T21:10:00.412009Z")));

    var oldTrip =
        TripData.builder()
            .setVehicleId("testVehicleId1")
            .setTripId("testTripId1")
            .setEta(Timestamp.parseTimestamp("2023-08-02T21:06:00.412009Z"))
            .setWaypoints(ImmutableList.of(oldPickupWaypoint, oldDropoffWaypoint))
            .build();

    var newTrip =
        TripData.builder()
            .setVehicleId("testVehicleId1")
            .setTripId("testTripId1")
            .setEta(Timestamp.parseTimestamp("2023-08-02T21:10:00.412009Z"))
            .setWaypoints(ImmutableList.of(newPickupWaypoint, newDropoffWaypoint))
            .setEventTimestamp(Timestamp.parseTimestamp("2023-08-02T20:50:34.621727000Z"))
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
    var pickupEtaChangeOutputEvent = new EtaRelativeChangeOutputEvent();
    pickupEtaChangeOutputEvent.setOriginalEta(
        Timestamp.parseTimestamp("2023-08-02T20:56:00.412009Z"));
    pickupEtaChangeOutputEvent.setIdentifier("testTripId1-pickup");
    pickupEtaChangeOutputEvent.setNewEta(Timestamp.parseTimestamp("2023-08-02T21:01:00.412009Z"));
    pickupEtaChangeOutputEvent.setThresholdPercent(0.1);
    pickupEtaChangeOutputEvent.setPercentDurationChange(0.3);
    pickupEtaChangeOutputEvent.setOriginalDuration(1000000L);
    pickupEtaChangeOutputEvent.setEventTimestamp(
        Timestamp.parseTimestamp("2023-08-02T20:50:34.621727000Z"));
    pickupEtaChangeOutputEvent.setFleetEvent(tripfleetEvent);
    EtaRelativeChangeHandler etaRelativeChangeHandler = new EtaRelativeChangeHandler();
    assertTrue(etaRelativeChangeHandler.respondsTo(tripfleetEvent, null, null));
    assertEquals(
        ImmutableList.of(pickupEtaChangeOutputEvent),
        etaRelativeChangeHandler.handleEvent(tripfleetEvent, null));
    assertEquals(
        ImmutableMap.of(
            EtaRelativeChangeHandler.ORIGINAL_DURATION_MILLISECONDS,
            1165791L,
            EtaRelativeChangeHandler.ORIGINAL_ETA,
            Timestamp.parseTimestamp("2023-08-02T21:10:00.412009000Z")),
        newTrip.getEventMetadata().get("relativeEtaPair"));
    assertEquals(
        ImmutableMap.of(
            EtaRelativeChangeHandler.ORIGINAL_DURATION_MILLISECONDS,
            1165791L,
            EtaRelativeChangeHandler.ORIGINAL_ETA,
            Timestamp.parseTimestamp("2023-08-02T21:10:00.412009000Z")),
        newDropoffWaypoint.getEventMetadata().get("relativeEtaPair"));
    assertEquals(
        ImmutableMap.of(
            EtaRelativeChangeHandler.ORIGINAL_DURATION_MILLISECONDS,
            1000000L,
            EtaRelativeChangeHandler.ORIGINAL_ETA,
            Timestamp.parseTimestamp("2023-08-02T20:56:00.412009Z")),
        newPickupWaypoint.getEventMetadata().get("relativeEtaPair"));
  }

  @Test
  public void etaRelativeChangeHandler_pickupDropoffAndTripHavePreviousEtaChangeExceedsThreshold() {
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
                Maps.newHashMap(
                    ImmutableMap.of(
                        "relativeEtaPair",
                        ImmutableMap.of(
                            EtaRelativeChangeHandler.ORIGINAL_DURATION_MILLISECONDS,
                            1000000L,
                            EtaRelativeChangeHandler.ORIGINAL_ETA,
                            Timestamp.parseTimestamp("2023-08-02T20:56:00.412009Z")))))
            .build();
    var newPickupWaypoint =
        TripWaypointData.builder()
            .setTripId("testTripId1")
            .setWaypointId("testTripId1-pickup")
            .setEta(Timestamp.parseTimestamp("2023-08-02T21:01:00.412009Z"))
            .setEventMetadata(
                Maps.newHashMap(
                    ImmutableMap.of(
                        "relativeEtaPair",
                        ImmutableMap.of(
                            EtaRelativeChangeHandler.ORIGINAL_DURATION_MILLISECONDS,
                            1000000L,
                            EtaRelativeChangeHandler.ORIGINAL_ETA,
                            Timestamp.parseTimestamp("2023-08-02T20:56:00.412009Z")))))
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
                Maps.newHashMap(
                    ImmutableMap.of(
                        "relativeEtaPair",
                        ImmutableMap.of(
                            EtaRelativeChangeHandler.ORIGINAL_DURATION_MILLISECONDS,
                            1000000L,
                            EtaRelativeChangeHandler.ORIGINAL_ETA,
                            Timestamp.parseTimestamp("2023-08-02T21:06:00.412009Z")))))
            .setEventMetadata(
                Maps.newHashMap(
                    ImmutableMap.of(
                        "relativeEtaPair",
                        ImmutableMap.of(
                            EtaRelativeChangeHandler.ORIGINAL_DURATION_MILLISECONDS,
                            1000000L,
                            EtaRelativeChangeHandler.ORIGINAL_ETA,
                            Timestamp.parseTimestamp("2023-08-02T21:06:00.412009Z")))))
            .build();
    var newDropoffWaypoint =
        TripWaypointData.builder()
            .setTripId("testTripId1")
            .setWaypointId("testTripId1-dropoff")
            .setEta(Timestamp.parseTimestamp("2023-08-02T21:12:00.412009Z"))
            .setEventMetadata(
                Maps.newHashMap(
                    ImmutableMap.of(
                        "relativeEtaPair",
                        ImmutableMap.of(
                            EtaRelativeChangeHandler.ORIGINAL_DURATION_MILLISECONDS,
                            1000000L,
                            EtaRelativeChangeHandler.ORIGINAL_ETA,
                            Timestamp.parseTimestamp("2023-08-02T21:06:00.412009Z")))))
            .build();
    var dropoffWaypointDifferences =
        ImmutableMap.of(
            "eta",
            new Change(
                Timestamp.parseTimestamp("2023-08-02T21:06:00.412009Z"),
                Timestamp.parseTimestamp("2023-08-02T21:10:00.412009Z")));

    var oldTrip =
        TripData.builder()
            .setVehicleId("testVehicleId1")
            .setTripId("testTripId1")
            .setEta(Timestamp.parseTimestamp("2023-08-02T21:06:00.412009Z"))
            .setWaypoints(ImmutableList.of(oldPickupWaypoint, oldDropoffWaypoint))
            .setEventMetadata(
                Maps.newHashMap(
                    ImmutableMap.of(
                        "relativeEtaPair",
                        ImmutableMap.of(
                            EtaRelativeChangeHandler.ORIGINAL_DURATION_MILLISECONDS,
                            1000000L,
                            EtaRelativeChangeHandler.ORIGINAL_ETA,
                            Timestamp.parseTimestamp("2023-08-02T21:06:00.412009Z")))))
            .build();

    var newTrip =
        TripData.builder()
            .setVehicleId("testVehicleId1")
            .setTripId("testTripId1")
            .setEta(Timestamp.parseTimestamp("2023-08-02T21:12:00.412009Z"))
            .setWaypoints(ImmutableList.of(newPickupWaypoint, newDropoffWaypoint))
            .setEventTimestamp(Timestamp.parseTimestamp("2023-08-02T20:50:34.621727000Z"))
            .setEventMetadata(
                Maps.newHashMap(
                    ImmutableMap.of(
                        "relativeEtaPair",
                        ImmutableMap.of(
                            EtaRelativeChangeHandler.ORIGINAL_DURATION_MILLISECONDS,
                            1000000L,
                            EtaRelativeChangeHandler.ORIGINAL_ETA,
                            Timestamp.parseTimestamp("2023-08-02T21:06:00.412009Z")))))
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
    var pickupEtaChangeOutputEvent = new EtaRelativeChangeOutputEvent();
    pickupEtaChangeOutputEvent.setOriginalEta(
        Timestamp.parseTimestamp("2023-08-02T20:56:00.412009Z"));
    pickupEtaChangeOutputEvent.setIdentifier("testTripId1-pickup");
    pickupEtaChangeOutputEvent.setNewEta(Timestamp.parseTimestamp("2023-08-02T21:01:00.412009Z"));
    pickupEtaChangeOutputEvent.setThresholdPercent(0.1);
    pickupEtaChangeOutputEvent.setPercentDurationChange(0.3);
    pickupEtaChangeOutputEvent.setOriginalDuration(1000000L);
    pickupEtaChangeOutputEvent.setEventTimestamp(
        Timestamp.parseTimestamp("2023-08-02T20:50:34.621727000Z"));
    pickupEtaChangeOutputEvent.setFleetEvent(tripfleetEvent);

    var dropoffEtaChangeOutputEvent = new EtaRelativeChangeOutputEvent();
    dropoffEtaChangeOutputEvent.setOriginalEta(
        Timestamp.parseTimestamp("2023-08-02T21:06:00.412009Z"));
    dropoffEtaChangeOutputEvent.setIdentifier("testTripId1-dropoff");
    dropoffEtaChangeOutputEvent.setNewEta(Timestamp.parseTimestamp("2023-08-02T21:12:00.412009Z"));
    dropoffEtaChangeOutputEvent.setThresholdPercent(0.1);
    dropoffEtaChangeOutputEvent.setPercentDurationChange(0.36);
    dropoffEtaChangeOutputEvent.setOriginalDuration(1000000L);
    dropoffEtaChangeOutputEvent.setEventTimestamp(
        Timestamp.parseTimestamp("2023-08-02T20:50:34.621727000Z"));
    dropoffEtaChangeOutputEvent.setFleetEvent(tripfleetEvent);

    var tripEtaChangeOutputEvent = new EtaRelativeChangeOutputEvent();
    tripEtaChangeOutputEvent.setOriginalEta(
        Timestamp.parseTimestamp("2023-08-02T21:06:00.412009Z"));
    tripEtaChangeOutputEvent.setIdentifier("testTripId1");
    tripEtaChangeOutputEvent.setNewEta(Timestamp.parseTimestamp("2023-08-02T21:12:00.412009Z"));
    tripEtaChangeOutputEvent.setThresholdPercent(0.1);
    tripEtaChangeOutputEvent.setPercentDurationChange(0.36);
    tripEtaChangeOutputEvent.setOriginalDuration(1000000L);
    tripEtaChangeOutputEvent.setIsTripOutputEvent(true);
    tripEtaChangeOutputEvent.setEventTimestamp(
        Timestamp.parseTimestamp("2023-08-02T20:50:34.621727000Z"));
    tripEtaChangeOutputEvent.setFleetEvent(tripfleetEvent);

    EtaRelativeChangeHandler etaRelativeChangeHandler = new EtaRelativeChangeHandler();
    assertTrue(etaRelativeChangeHandler.respondsTo(tripfleetEvent, null, null));

    assertEquals(
        ImmutableList.of(
            pickupEtaChangeOutputEvent, dropoffEtaChangeOutputEvent, tripEtaChangeOutputEvent),
        etaRelativeChangeHandler.handleEvent(tripfleetEvent, null));
    assertEquals(
        ImmutableMap.of(
            EtaRelativeChangeHandler.ORIGINAL_DURATION_MILLISECONDS,
            1000000L,
            EtaRelativeChangeHandler.ORIGINAL_ETA,
            Timestamp.parseTimestamp("2023-08-02T21:06:00.412009Z")),
        newTrip.getEventMetadata().get("relativeEtaPair"));
    assertEquals(
        ImmutableMap.of(
            EtaRelativeChangeHandler.ORIGINAL_DURATION_MILLISECONDS,
            1000000L,
            EtaRelativeChangeHandler.ORIGINAL_ETA,
            Timestamp.parseTimestamp("2023-08-02T21:06:00.412009Z")),
        newDropoffWaypoint.getEventMetadata().get("relativeEtaPair"));
    assertEquals(
        ImmutableMap.of(
            EtaRelativeChangeHandler.ORIGINAL_DURATION_MILLISECONDS,
            1000000L,
            EtaRelativeChangeHandler.ORIGINAL_ETA,
            Timestamp.parseTimestamp("2023-08-02T20:56:00.412009Z")),
        newPickupWaypoint.getEventMetadata().get("relativeEtaPair"));
  }

  @Test
  public void
      etaRelativeChangeHandler_intermediateWaypointAlsoHasPreviousEtaChangeExceedsThreshold() {
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
                Maps.newHashMap(
                    ImmutableMap.of(
                        "relativeEtaPair",
                        ImmutableMap.of(
                            EtaRelativeChangeHandler.ORIGINAL_DURATION_MILLISECONDS,
                            1000000L,
                            EtaRelativeChangeHandler.ORIGINAL_ETA,
                            Timestamp.parseTimestamp("2023-08-02T20:56:00.412009Z")))))
            .build();
    var newPickupWaypoint =
        TripWaypointData.builder()
            .setTripId("testTripId1")
            .setWaypointId("testTripId1-pickup")
            .setEta(Timestamp.parseTimestamp("2023-08-02T21:01:00.412009Z"))
            .setEventMetadata(
                Maps.newHashMap(
                    ImmutableMap.of(
                        "relativeEtaPair",
                        ImmutableMap.of(
                            EtaRelativeChangeHandler.ORIGINAL_DURATION_MILLISECONDS,
                            1000000L,
                            EtaRelativeChangeHandler.ORIGINAL_ETA,
                            Timestamp.parseTimestamp("2023-08-02T20:56:00.412009Z")))))
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
                Maps.newHashMap(
                    ImmutableMap.of(
                        "relativeEtaPair",
                        ImmutableMap.of(
                            EtaRelativeChangeHandler.ORIGINAL_DURATION_MILLISECONDS,
                            1000000L,
                            EtaRelativeChangeHandler.ORIGINAL_ETA,
                            Timestamp.parseTimestamp("2023-08-02T20:57:00.412009Z")))))
            .build();
    var newIntermediateWaypoint =
        TripWaypointData.builder()
            .setTripId("testTripId1")
            .setWaypointId("testTripId1-1")
            .setEta(Timestamp.parseTimestamp("2023-08-02T21:02:00.412009Z"))
            .setEventMetadata(
                Maps.newHashMap(
                    ImmutableMap.of(
                        "relativeEtaPair",
                        ImmutableMap.of(
                            EtaRelativeChangeHandler.ORIGINAL_DURATION_MILLISECONDS,
                            1000000L,
                            EtaRelativeChangeHandler.ORIGINAL_ETA,
                            Timestamp.parseTimestamp("2023-08-02T20:57:00.412009Z")))))
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
                Maps.newHashMap(
                    ImmutableMap.of(
                        "relativeEtaPair",
                        ImmutableMap.of(
                            EtaRelativeChangeHandler.ORIGINAL_DURATION_MILLISECONDS,
                            1000000L,
                            EtaRelativeChangeHandler.ORIGINAL_ETA,
                            Timestamp.parseTimestamp("2023-08-02T21:06:00.412009Z")))))
            .build();
    var newDropoffWaypoint =
        TripWaypointData.builder()
            .setTripId("testTripId1")
            .setWaypointId("testTripId1-dropoff")
            .setEta(Timestamp.parseTimestamp("2023-08-02T21:12:00.412009Z"))
            .setEventMetadata(
                Maps.newHashMap(
                    ImmutableMap.of(
                        "relativeEtaPair",
                        ImmutableMap.of(
                            EtaRelativeChangeHandler.ORIGINAL_DURATION_MILLISECONDS,
                            1000000L,
                            EtaRelativeChangeHandler.ORIGINAL_ETA,
                            Timestamp.parseTimestamp("2023-08-02T21:06:00.412009Z")))))
            .build();
    var dropoffWaypointDifferences =
        ImmutableMap.of(
            "eta",
            new Change(
                Timestamp.parseTimestamp("2023-08-02T21:06:00.412009Z"),
                Timestamp.parseTimestamp("2023-08-02T21:10:00.412009Z")));

    var oldTrip =
        TripData.builder()
            .setVehicleId("testVehicleId1")
            .setTripId("testTripId1")
            .setEta(Timestamp.parseTimestamp("2023-08-02T21:06:00.412009Z"))
            .setWaypoints(
                ImmutableList.of(oldPickupWaypoint, oldIntermediateWaypoint, oldDropoffWaypoint))
            .setEventMetadata(
                Maps.newHashMap(
                    ImmutableMap.of(
                        "relativeEtaPair",
                        ImmutableMap.of(
                            EtaRelativeChangeHandler.ORIGINAL_DURATION_MILLISECONDS,
                            1000000L,
                            EtaRelativeChangeHandler.ORIGINAL_ETA,
                            Timestamp.parseTimestamp("2023-08-02T21:06:00.412009Z")))))
            .build();

    var newTrip =
        TripData.builder()
            .setVehicleId("testVehicleId1")
            .setTripId("testTripId1")
            .setEta(Timestamp.parseTimestamp("2023-08-02T21:12:00.412009Z"))
            .setWaypoints(
                ImmutableList.of(newPickupWaypoint, newIntermediateWaypoint, newDropoffWaypoint))
            .setEventTimestamp(Timestamp.parseTimestamp("2023-08-02T20:50:34.621727000Z"))
            .setEventMetadata(
                Maps.newHashMap(
                    ImmutableMap.of(
                        "relativeEtaPair",
                        ImmutableMap.of(
                            EtaRelativeChangeHandler.ORIGINAL_DURATION_MILLISECONDS,
                            1000000L,
                            EtaRelativeChangeHandler.ORIGINAL_ETA,
                            Timestamp.parseTimestamp("2023-08-02T21:06:00.412009Z")))))
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
    var pickupEtaChangeOutputEvent = new EtaRelativeChangeOutputEvent();
    pickupEtaChangeOutputEvent.setOriginalEta(
        Timestamp.parseTimestamp("2023-08-02T20:56:00.412009Z"));
    pickupEtaChangeOutputEvent.setIdentifier("testTripId1-pickup");
    pickupEtaChangeOutputEvent.setNewEta(Timestamp.parseTimestamp("2023-08-02T21:01:00.412009Z"));
    pickupEtaChangeOutputEvent.setThresholdPercent(0.1);
    pickupEtaChangeOutputEvent.setPercentDurationChange(0.3);
    pickupEtaChangeOutputEvent.setOriginalDuration(1000000L);
    pickupEtaChangeOutputEvent.setEventTimestamp(
        Timestamp.parseTimestamp("2023-08-02T20:50:34.621727000Z"));
    pickupEtaChangeOutputEvent.setFleetEvent(tripfleetEvent);

    var intermediateEtaChangeOutputEvent = new EtaRelativeChangeOutputEvent();
    intermediateEtaChangeOutputEvent.setOriginalEta(
        Timestamp.parseTimestamp("2023-08-02T20:57:00.412009Z"));
    intermediateEtaChangeOutputEvent.setIdentifier("testTripId1-1");
    intermediateEtaChangeOutputEvent.setNewEta(
        Timestamp.parseTimestamp("2023-08-02T21:02:00.412009Z"));
    intermediateEtaChangeOutputEvent.setThresholdPercent(0.1);
    intermediateEtaChangeOutputEvent.setPercentDurationChange(0.3);
    intermediateEtaChangeOutputEvent.setOriginalDuration(1000000L);
    intermediateEtaChangeOutputEvent.setEventTimestamp(
        Timestamp.parseTimestamp("2023-08-02T20:50:34.621727000Z"));
    intermediateEtaChangeOutputEvent.setFleetEvent(tripfleetEvent);

    var dropoffEtaChangeOutputEvent = new EtaRelativeChangeOutputEvent();
    dropoffEtaChangeOutputEvent.setOriginalEta(
        Timestamp.parseTimestamp("2023-08-02T21:06:00.412009Z"));
    dropoffEtaChangeOutputEvent.setIdentifier("testTripId1-dropoff");
    dropoffEtaChangeOutputEvent.setNewEta(Timestamp.parseTimestamp("2023-08-02T21:12:00.412009Z"));
    dropoffEtaChangeOutputEvent.setThresholdPercent(0.1);
    dropoffEtaChangeOutputEvent.setPercentDurationChange(0.36);
    dropoffEtaChangeOutputEvent.setOriginalDuration(1000000L);
    dropoffEtaChangeOutputEvent.setEventTimestamp(
        Timestamp.parseTimestamp("2023-08-02T20:50:34.621727000Z"));
    dropoffEtaChangeOutputEvent.setFleetEvent(tripfleetEvent);

    var tripEtaChangeOutputEvent = new EtaRelativeChangeOutputEvent();
    tripEtaChangeOutputEvent.setOriginalEta(
        Timestamp.parseTimestamp("2023-08-02T21:06:00.412009Z"));
    tripEtaChangeOutputEvent.setIdentifier("testTripId1");
    tripEtaChangeOutputEvent.setNewEta(Timestamp.parseTimestamp("2023-08-02T21:12:00.412009Z"));
    tripEtaChangeOutputEvent.setThresholdPercent(0.1);
    tripEtaChangeOutputEvent.setPercentDurationChange(0.36);
    tripEtaChangeOutputEvent.setOriginalDuration(1000000L);
    tripEtaChangeOutputEvent.setIsTripOutputEvent(true);
    tripEtaChangeOutputEvent.setEventTimestamp(
        Timestamp.parseTimestamp("2023-08-02T20:50:34.621727000Z"));
    tripEtaChangeOutputEvent.setFleetEvent(tripfleetEvent);

    EtaRelativeChangeHandler etaRelativeChangeHandler = new EtaRelativeChangeHandler();
    assertTrue(etaRelativeChangeHandler.respondsTo(tripfleetEvent, null, null));

    assertEquals(
        ImmutableList.of(
            pickupEtaChangeOutputEvent,
            intermediateEtaChangeOutputEvent,
            dropoffEtaChangeOutputEvent,
            tripEtaChangeOutputEvent),
        etaRelativeChangeHandler.handleEvent(tripfleetEvent, null));
    assertEquals(
        ImmutableMap.of(
            EtaRelativeChangeHandler.ORIGINAL_DURATION_MILLISECONDS,
            1000000L,
            EtaRelativeChangeHandler.ORIGINAL_ETA,
            Timestamp.parseTimestamp("2023-08-02T21:06:00.412009Z")),
        newTrip.getEventMetadata().get("relativeEtaPair"));
    assertEquals(
        ImmutableMap.of(
            EtaRelativeChangeHandler.ORIGINAL_DURATION_MILLISECONDS,
            1000000L,
            EtaRelativeChangeHandler.ORIGINAL_ETA,
            Timestamp.parseTimestamp("2023-08-02T21:06:00.412009Z")),
        newDropoffWaypoint.getEventMetadata().get("relativeEtaPair"));
    assertEquals(
        ImmutableMap.of(
            EtaRelativeChangeHandler.ORIGINAL_DURATION_MILLISECONDS,
            1000000L,
            EtaRelativeChangeHandler.ORIGINAL_ETA,
            Timestamp.parseTimestamp("2023-08-02T20:57:00.412009Z")),
        newIntermediateWaypoint.getEventMetadata().get("relativeEtaPair"));
    assertEquals(
        ImmutableMap.of(
            EtaRelativeChangeHandler.ORIGINAL_DURATION_MILLISECONDS,
            1000000L,
            EtaRelativeChangeHandler.ORIGINAL_ETA,
            Timestamp.parseTimestamp("2023-08-02T20:56:00.412009Z")),
        newPickupWaypoint.getEventMetadata().get("relativeEtaPair"));
  }

  @Test
  public void etaRelativeChangeHandler_addIntermediateWaypointRelativeEtaMetadataReset() {
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
                Maps.newHashMap(
                    ImmutableMap.of(
                        "relativeEtaPair",
                        ImmutableMap.of(
                            EtaRelativeChangeHandler.ORIGINAL_DURATION_MILLISECONDS,
                            1000000L,
                            EtaRelativeChangeHandler.ORIGINAL_ETA,
                            Timestamp.parseTimestamp("2023-08-02T20:56:00.412009Z")))))
            .build();
    var newPickupWaypoint =
        TripWaypointData.builder()
            .setTripId("testTripId1")
            .setWaypointId("testTripId1-pickup")
            .setEta(Timestamp.parseTimestamp("2023-08-02T21:01:00.412009Z"))
            .setEventMetadata(
                Maps.newHashMap(
                    ImmutableMap.of(
                        "relativeEtaPair",
                        ImmutableMap.of(
                            EtaRelativeChangeHandler.ORIGINAL_DURATION_MILLISECONDS,
                            1000000L,
                            EtaRelativeChangeHandler.ORIGINAL_ETA,
                            Timestamp.parseTimestamp("2023-08-02T20:56:00.412009Z")))))
            .build();

    var addedIntermediateWaypoint =
        TripWaypointData.builder()
            .setTripId("testTripId1")
            .setWaypointId("testTripId1-1")
            .setEta(Timestamp.parseTimestamp("2023-08-02T21:02:00.412009Z"))
            .setEventMetadata(
                Maps.newHashMap(
                    ImmutableMap.of(
                        "relativeEtaPair",
                        ImmutableMap.of(
                            EtaRelativeChangeHandler.ORIGINAL_DURATION_MILLISECONDS,
                            1000000L,
                            EtaRelativeChangeHandler.ORIGINAL_ETA,
                            Timestamp.parseTimestamp("2023-08-02T20:57:00.412009Z")))))
            .build();
    var firstWaypointDifferences =
        ImmutableMap.of(
            "eta",
            new Change(
                Timestamp.parseTimestamp("2023-08-02T20:57:00.412009Z"),
                Timestamp.parseTimestamp("2023-08-02T21:01:00.412009Z")));
    var secondWaypointDifferences =
        ImmutableMap.of(
            "eta",
            new Change(
                Timestamp.parseTimestamp("2023-08-02T20:58:00.412009Z"),
                Timestamp.parseTimestamp("2023-08-02T21:02:00.412009Z")));
    var thirdWaypointDifferences =
        ImmutableMap.of(
            "eta", new Change(null, Timestamp.parseTimestamp("2023-08-02T21:10:00.412009Z")));

    var oldDropoffWaypoint =
        TripWaypointData.builder()
            .setTripId("testTripId1")
            .setWaypointId("testTripId1-dropoff")
            .setEta(Timestamp.parseTimestamp("2023-08-02T21:06:00.412009Z"))
            .setEventMetadata(
                Maps.newHashMap(
                    ImmutableMap.of(
                        "relativeEtaPair",
                        ImmutableMap.of(
                            EtaRelativeChangeHandler.ORIGINAL_DURATION_MILLISECONDS,
                            1000000L,
                            EtaRelativeChangeHandler.ORIGINAL_ETA,
                            Timestamp.parseTimestamp("2023-08-02T21:06:00.412009Z")))))
            .build();
    var newDropoffWaypoint =
        TripWaypointData.builder()
            .setTripId("testTripId1")
            .setWaypointId("testTripId1-dropoff")
            .setEta(Timestamp.parseTimestamp("2023-08-02T21:12:00.412009Z"))
            .setEventMetadata(
                Maps.newHashMap(
                    ImmutableMap.of(
                        "relativeEtaPair",
                        ImmutableMap.of(
                            EtaRelativeChangeHandler.ORIGINAL_DURATION_MILLISECONDS,
                            1000000L,
                            EtaRelativeChangeHandler.ORIGINAL_ETA,
                            Timestamp.parseTimestamp("2023-08-02T21:06:00.412009Z")))))
            .build();

    var oldTrip =
        TripData.builder()
            .setVehicleId("testVehicleId1")
            .setTripId("testTripId1")
            .setEta(Timestamp.parseTimestamp("2023-08-02T21:06:00.412009Z"))
            .setWaypoints(ImmutableList.of(oldPickupWaypoint, oldDropoffWaypoint))
            .setEventMetadata(
                Maps.newHashMap(
                    ImmutableMap.of(
                        "relativeEtaPair",
                        ImmutableMap.of(
                            EtaRelativeChangeHandler.ORIGINAL_DURATION_MILLISECONDS,
                            1000000L,
                            EtaRelativeChangeHandler.ORIGINAL_ETA,
                            Timestamp.parseTimestamp("2023-08-02T21:06:00.412009Z")))))
            .build();

    var newTrip =
        TripData.builder()
            .setVehicleId("testVehicleId1")
            .setTripId("testTripId1")
            .setEta(Timestamp.parseTimestamp("2023-08-02T21:12:00.412009Z"))
            .setWaypoints(
                ImmutableList.of(newPickupWaypoint, addedIntermediateWaypoint, newDropoffWaypoint))
            .setEventTimestamp(Timestamp.parseTimestamp("2023-08-02T20:50:34.621727000Z"))
            .setEventMetadata(
                Maps.newHashMap(
                    ImmutableMap.of(
                        "relativeEtaPair",
                        ImmutableMap.of(
                            EtaRelativeChangeHandler.ORIGINAL_DURATION_MILLISECONDS,
                            1000000L,
                            EtaRelativeChangeHandler.ORIGINAL_ETA,
                            Timestamp.parseTimestamp("2023-08-02T21:06:00.412009Z")))))
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
                    firstWaypointDifferences, secondWaypointDifferences, thirdWaypointDifferences))
            .setWaypointsChanged(true)
            .build();

    EtaRelativeChangeHandler etaRelativeChangeHandler = new EtaRelativeChangeHandler();
    assertTrue(etaRelativeChangeHandler.respondsTo(tripfleetEvent, null, null));

    assertEquals(ImmutableList.of(), etaRelativeChangeHandler.handleEvent(tripfleetEvent, null));
    assertEquals(
        ImmutableMap.of(
            EtaRelativeChangeHandler.ORIGINAL_DURATION_MILLISECONDS,
            1285791L,
            EtaRelativeChangeHandler.ORIGINAL_ETA,
            Timestamp.parseTimestamp("2023-08-02T21:12:00.412009Z")),
        newTrip.getEventMetadata().get("relativeEtaPair"));
    assertEquals(
        ImmutableMap.of(
            EtaRelativeChangeHandler.ORIGINAL_DURATION_MILLISECONDS,
            1165791L,
            EtaRelativeChangeHandler.ORIGINAL_ETA,
            Timestamp.parseTimestamp("2023-08-02T21:10:00.412009Z")),
        newDropoffWaypoint.getEventMetadata().get("relativeEtaPair"));
    assertEquals(
        ImmutableMap.of(
            EtaRelativeChangeHandler.ORIGINAL_DURATION_MILLISECONDS,
            685791L,
            EtaRelativeChangeHandler.ORIGINAL_ETA,
            Timestamp.parseTimestamp("2023-08-02T21:02:00.412009Z")),
        addedIntermediateWaypoint.getEventMetadata().get("relativeEtaPair"));
    assertEquals(
        ImmutableMap.of(
            EtaRelativeChangeHandler.ORIGINAL_DURATION_MILLISECONDS,
            625791L,
            EtaRelativeChangeHandler.ORIGINAL_ETA,
            Timestamp.parseTimestamp("2023-08-02T21:01:00.412009Z")),
        newPickupWaypoint.getEventMetadata().get("relativeEtaPair"));
  }
}
