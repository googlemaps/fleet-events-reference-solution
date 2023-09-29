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

package com.google.fleetevents.util;

import static org.junit.Assert.assertEquals;

import com.google.fleetevents.CloudEventTestData;
import com.google.fleetevents.common.util.ProtoParser;
import com.google.logging.v2.LogEntry;
import com.google.protobuf.Int32Value;
import google.maps.fleetengine.delivery.v1.DeliveryVehicle;
import google.maps.fleetengine.delivery.v1.UpdateDeliveryVehicleRequest;
import io.cloudevents.CloudEvent;
import java.io.IOException;
import java.util.Arrays;
import org.junit.Test;

/** Tests for proto parser. */
public class ProtoParserTest {

  private static final String UPDATE_DELIVERY_VEHICLE_LOG_NAME =
      "projects/test-123/logs/fleetengine.googleapis.com%2Fupdate_delivery_vehicle";

  @Test
  public void testCloudEventToLogEntry() throws IOException {
    CloudEvent cloudEvent = CloudEventTestData.getUpdateDeliveryVehicleCloudEvent();
    LogEntry logEntry = ProtoParser.cloudEventDataToLogEntry(cloudEvent.getData());
    assertEquals(UPDATE_DELIVERY_VEHICLE_LOG_NAME, logEntry.getLogName());
  }

  @Test
  public void testParseLogEntryRequest() throws IOException {
    CloudEvent cloudEvent = CloudEventTestData.getUpdateDeliveryVehicleCloudEvent();
    LogEntry logEntry = ProtoParser.cloudEventDataToLogEntry(cloudEvent.getData());
    assertEquals(logEntry.getLogName(), UPDATE_DELIVERY_VEHICLE_LOG_NAME);
    UpdateDeliveryVehicleRequest updateDeliveryVehicleRequest =
        ProtoParser.parseLogEntryRequest(
            logEntry, UpdateDeliveryVehicleRequest.getDefaultInstance());
    assertEquals(
        "providers/test-123/deliveryVehicles/testDeliveryVehicleId1",
        updateDeliveryVehicleRequest.getDeliveryVehicle().getName());
    assertEquals(
        Arrays.asList("remaining_duration", "remaining_distance_meters", "last_location"),
        updateDeliveryVehicleRequest.getUpdateMask().getPathsList());
  }

  @Test
  public void testParseLogEntryResponse() throws IOException {
    CloudEvent cloudEvent = CloudEventTestData.getUpdateDeliveryVehicleCloudEvent();
    LogEntry logEntry = ProtoParser.cloudEventDataToLogEntry(cloudEvent.getData());
    assertEquals(logEntry.getLogName(), UPDATE_DELIVERY_VEHICLE_LOG_NAME);
    DeliveryVehicle deliveryVehicle =
        ProtoParser.parseLogEntryResponse(logEntry, DeliveryVehicle.getDefaultInstance());
    assertEquals(
        "providers/test-123/deliveryVehicles/testDeliveryVehicleId1", deliveryVehicle.getName());
    assertEquals(Int32Value.of(2804), deliveryVehicle.getRemainingDistanceMeters());
  }
}
