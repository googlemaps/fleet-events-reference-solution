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

package com.google.fleetevents.helpers;

import com.google.cloud.firestore.GeoPoint;
import com.google.fleetevents.common.util.ProtoParser;
import com.google.fleetevents.lmfs.models.DeliveryVehicleData;
import com.google.logging.v2.LogEntry;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import com.google.type.LatLng;
import google.maps.fleetengine.delivery.v1.CreateDeliveryVehicleRequest;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

/** Helper for loading sample logs for the fleet events tests. */
public final class FleetEventsTestHelper {

  public static LogEntry getLogEntry(String logEntryJson) throws IOException {
    LogEntry.Builder logEntryBuilder = LogEntry.newBuilder();
    JsonFormat.parser().ignoringUnknownFields().merge(logEntryJson, logEntryBuilder);
    return logEntryBuilder.build();
  }

  public static LogEntry createDeliveryVehicleLog1() throws IOException {
    String fileContent =
        Files.readString(
            Paths.get("src/test/resources/logs/createDeliveryVehicleLog_1.json"),
            Charset.defaultCharset());
    return getLogEntry(fileContent);
  }

  public static LogEntry updateDeliveryVehicleLog1() throws IOException {
    String fileContent =
        Files.readString(
            Paths.get("src/test/resources/logs/updateDeliveryVehicleLog_1.json"),
            Charset.defaultCharset());
    return getLogEntry(fileContent);
  }

  public static LogEntry updateDeliveryVehicleLog2() throws IOException {
    String fileContent =
        Files.readString(
            Paths.get("src/test/resources/logs/updateDeliveryVehicleLog_2.json"),
            Charset.defaultCharset());
    return getLogEntry(fileContent);
  }

  public static LogEntry createTaskLog1() throws IOException {
    String fileContent =
        Files.readString(
            Paths.get("src/test/resources/logs/createTaskLog_1.json"), Charset.defaultCharset());
    return getLogEntry(fileContent);
  }

  public static LogEntry batchCreateTasksLog1() throws IOException {
    String fileContent =
        Files.readString(
            Paths.get("src/test/resources/logs/batchCreateTasksLog_1.json"),
            Charset.defaultCharset());
    return getLogEntry(fileContent);
  }

  public static LogEntry updateTaskLog1() throws IOException {
    String fileContent =
        Files.readString(
            Paths.get("src/test/resources/logs/updateTaskLog_1.json"), Charset.defaultCharset());
    return getLogEntry(fileContent);
  }

  public static LogEntry updateTaskLog2() throws IOException {
    String fileContent =
        Files.readString(
            Paths.get("src/test/resources/logs/updateTaskLog_2.json"), Charset.defaultCharset());
    return getLogEntry(fileContent);
  }

  public static DeliveryVehicleData updateDeliveryVehicleData1() throws IOException {
    return logEntryToDeliveryVehicleData(updateDeliveryVehicleLog1());
  }

  public static DeliveryVehicleData createDeliveryVehicleData1() throws IOException {
    return logEntryToDeliveryVehicleData(createDeliveryVehicleLog1());
  }

  public static DeliveryVehicleData logEntryToDeliveryVehicleData(LogEntry logEntry)
      throws InvalidProtocolBufferException {
    CreateDeliveryVehicleRequest request =
        ProtoParser.parseLogEntryRequest(
            logEntry, CreateDeliveryVehicleRequest.getDefaultInstance());
    google.maps.fleetengine.delivery.v1.DeliveryVehicle response =
        ProtoParser.parseLogEntryResponse(
            logEntry, google.maps.fleetengine.delivery.v1.DeliveryVehicle.getDefaultInstance());
    String deliveryVehicleId = request.getDeliveryVehicleId();
    String name = response.getName();
    LatLng lastLocation = response.getLastLocation().getLocation();
    DeliveryVehicleData.Builder newDeliveryVehicleBuilder =
        DeliveryVehicleData.builder()
            .setDeliveryVehicleId(deliveryVehicleId)
            .setName(name)
            .setLastLocation(new GeoPoint(lastLocation.getLatitude(), lastLocation.getLongitude()));
    return newDeliveryVehicleBuilder.build();
  }
}
