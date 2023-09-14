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

package com.google.fleetevents.odrd.handlers;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

import com.google.cloud.Timestamp;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.fleetevents.common.models.Change;
import com.google.fleetevents.common.models.OutputEvent;
import com.google.fleetevents.odrd.models.TripData;
import com.google.fleetevents.odrd.models.TripFleetEvent;
import com.google.fleetevents.odrd.models.outputs.TripStatusOutputEvent;
import java.util.List;
import org.junit.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/** Tests for the trip status fleet event handler. */
public class TripStatusHandlerTest {

  @Test
  public void updateTripNoTripStatus_doesNotRespond() {
    TripFleetEvent tripFleetEvent =
        TripFleetEvent.builder()
            .setVehicleId("testVehicleId1")
            .setTripId("testTripId1")
            .setNewTrip(TripData.builder().setTripId("testTripId1").build())
            .build();
    TripStatusHandler taskStateHandler = new TripStatusHandler();
    assertFalse(taskStateHandler.respondsTo(tripFleetEvent, null, null));
  }

  public static final List<String> STATES =
      ImmutableList.of(
          "NEW",
          "ENROUTE_TO_PICKUP",
          "ARRIVED_AT_PICKUP",
          "ENROUTE_TO_INTERMEDIATE_DESTINATION",
          "ARRIVED_AT_INTERMEDIATE_DESTINATION",
          "ENROUTE_TO_DROP_OFF",
          "COMPLETE");

  @ParameterizedTest
  @ValueSource(
      strings = {
        "NEW",
        "ENROUTE_TO_PICKUP",
        "ARRIVED_AT_PICKUP",
        "ENROUTE_TO_INTERMEDIATE_DESTINATION",
        "ARRIVED_AT_INTERMEDIATE_DESTINATION",
        "ENROUTE_TO_DROP_OFF",
        "COMPLETE"
      })
  public void updateTripTripStatus_respondsReturnsOutputEvent(String tripStatus) {
    var oldTripStatusIndex = STATES.indexOf(tripStatus) - 1;
    var oldTripStatus = oldTripStatusIndex > 0 ? STATES.get(oldTripStatusIndex) : null;
    TripFleetEvent tripFleetEvent =
        TripFleetEvent.builder()
            .setVehicleId("testVehicleId1")
            .setTripId("testTripId1")
            .setOldTrip(
                TripData.builder()
                    .setTripId("testTripId1")
                    .setTripStatus(oldTripStatus)
                    .setEventTimestamp(Timestamp.parseTimestamp("2023-08-02T20:51:34.621727000Z"))
                    .build())
            .setNewTrip(
                TripData.builder()
                    .setTripId("testTripId1")
                    .setTripStatus(tripStatus)
                    .setEventTimestamp(Timestamp.parseTimestamp("2023-08-02T20:56:34.621727000Z"))
                    .build())
            .setTripDifferences(
                ImmutableMap.of("tripStatus", new Change(oldTripStatus, tripStatus)))
            .build();
    TripStatusHandler tripStatusHandler = new TripStatusHandler();
    assertTrue(tripStatusHandler.respondsTo(tripFleetEvent, null, null));
    TripStatusOutputEvent expectedOutputEvent = new TripStatusOutputEvent();
    expectedOutputEvent.setFleetEvent(tripFleetEvent);
    expectedOutputEvent.setOldTripStatus(oldTripStatus);
    expectedOutputEvent.setNewTripStatus(tripStatus);
    expectedOutputEvent.setTripId("testTripId1");
    expectedOutputEvent.setEventTimestamp(
        Timestamp.parseTimestamp("2023-08-02T20:51:34.621727000Z"));

    List<OutputEvent> expected = ImmutableList.of(expectedOutputEvent);
    assertIterableEquals(expected, tripStatusHandler.handleEvent(tripFleetEvent, null));
  }
}
