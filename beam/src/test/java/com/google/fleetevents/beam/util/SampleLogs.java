package com.google.fleetevents.beam.util;

import com.google.logging.v2.LogEntry;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import com.google.protobuf.util.JsonFormat;
import google.maps.fleetengine.delivery.v1.Task;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class SampleLogs {

  static HashMap<String, Integer> enumStringsMap = new HashMap<>();

  static {
    enumStringsMap.put("TASK_OUTCOME_LOG_UNSPECIFIED", 0);
    enumStringsMap.put("TASK_OUTCOME_LOG_SUCCEEDED", 1);
    enumStringsMap.put("TASK_OUTCOME_LOG_FAILED", 2);

    enumStringsMap.put("TASK_STATE_LOG_UNSPECIFIED", 0);
    enumStringsMap.put("TASK_STATE_LOG_OPEN", 1);
    enumStringsMap.put("TASK_STATE_LOG_CLOSED", 2);

    enumStringsMap.put("VEHICLE_STOP_STATE_LOG_UNSPECIFIED", 0);
    enumStringsMap.put("VEHICLE_STOP_STATE_LOG_NEW", 1);
    enumStringsMap.put("VEHICLE_STOP_STATE_LOG_ENROUTE", 2);
    enumStringsMap.put("VEHICLE_STOP_STATE_LOG_ARRIVED", 3);

    enumStringsMap.put("NAVIGATION_STATUS_LOG_UNSPECIFIED", 0);
    enumStringsMap.put("NAVIGATION_STATUS_NO_GUIDANCE", 1);
    enumStringsMap.put("NAVIGATION_STATUS_ENROUTE_TO_DESTINATION", 2);
    enumStringsMap.put("NAVIGATION_STATUS_OFF_ROUTE", 3);
    enumStringsMap.put("NAVIGATION_STATUS_ARRIVED_AT_DESTINATION", 4);
  }

  public static Task getCreateTask1() throws IOException {
    Task task = parseLogEntryResponse(getCreateTaskLogEntry1(), Task.getDefaultInstance());
    return task;
  }

  public static Task getUpdateTask1() throws IOException {
    Task task = parseLogEntryResponse(getUpdateTaskLogEntry1(), Task.getDefaultInstance());
    return task;
  }

  public static LogEntry getCreateTaskLogEntry1() throws IOException {
    String fileContent =
        Files.readString(
            Paths.get("src/test/resources/create_task_logentry1.json"), Charset.defaultCharset());
    return getLogEntry(fileContent);
  }

  public static LogEntry getUpdateTaskLogEntry1() throws IOException {
    String fileContent =
        Files.readString(
            Paths.get("src/test/resources/update_task_logentry1.json"), Charset.defaultCharset());
    return getLogEntry(fileContent);
  }

  public static LogEntry getUpdateDeliveryVehicleLogEntry1() throws IOException {
    String fileContent =
        Files.readString(
            Paths.get("src/test/resources/update_delivery_vehicle_logentry1.json"),
            Charset.defaultCharset());
    return getLogEntry(fileContent);
  }

  public static LogEntry getUpdateDeliveryVehicleLogEntry2() throws IOException {
    String fileContent =
        Files.readString(
            Paths.get("src/test/resources/update_delivery_vehicle_logentry2.json"),
            Charset.defaultCharset());
    return getLogEntry(fileContent);
  }

  public static <T extends Message> T parseLogEntryResponse(LogEntry logEntry, T message)
      throws InvalidProtocolBufferException {
    Message.Builder builder = message.toBuilder();
    Map<String, Value> jsonStruct = logEntry.getJsonPayload().getFieldsMap();
    if (!jsonStruct.containsKey("response")) {
      throw new IllegalArgumentException(
          "Received log entry with empty response: " + logEntry.getLogName());
    }
    Struct response = jsonStruct.get("response").getStructValue();
    String json = JsonFormat.printer().print(response);
    parseJson(json, builder);
    return (T) builder.build();
  }

  public static LogEntry getLogEntry(String logEntryJson) throws IOException {
    LogEntry.Builder logEntryBuilder = LogEntry.newBuilder();
    JsonFormat.parser().ignoringUnknownFields().merge(logEntryJson, logEntryBuilder);
    return logEntryBuilder.build();
  }

  public static <T extends Message.Builder> void parseJson(String json, T messageType)
      throws InvalidProtocolBufferException {
    for (String enumKey : enumStringsMap.keySet()) {
      json = json.replace(enumKey, String.valueOf(enumStringsMap.get(enumKey)));
    }
    JsonFormat.parser().ignoringUnknownFields().merge(json, messageType);
  }
}
