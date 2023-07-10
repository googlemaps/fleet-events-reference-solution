/*
 * Copyright 2023 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use thi  xs file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package samples;

import com.google.api.core.ApiFuture;
import com.google.cloud.audit.AuditLog;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.gson.Gson;
import com.google.logging.v2.LogEntry;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Timestamp;
import com.google.protobuf.util.JsonFormat;
import io.cloudevents.CloudEvent;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

/** A utility class to externalize utility methods for better readability and reusability */
public class Util {

  record CloudEventPubsubBody(CloudEventPubsubMessage message) {}

  record CloudEventPubsubMessage(
      String data, Map<String, String> attributes, String messageId, String publishTime) {}

  static LogEntry cloudevent2logentry(CloudEvent event) {
    // log(LOGGER,event);

    LogEntry logentry = null;

    // The Pub/Sub message is passed as the CloudEvent's data payload.

    if (event.getData() != null) {

      // Extract Cloud Event data and convert to PubSubBody
      String cloudEventData = new String(event.getData().toBytes(), StandardCharsets.UTF_8);

      CloudEventPubsubBody body = new Gson().fromJson(cloudEventData, CloudEventPubsubBody.class);
      // Retrieve and decode PubSub message data
      CloudEventPubsubMessage msg = body.message();
      // log(LOGGER,msg);
      // base64 encoded message
      String encodedData = msg.data();

      // decoded message (json string)
      String decodedData =
          new String(Base64.getDecoder().decode(encodedData), StandardCharsets.UTF_8);

      logentry = jsonstring2logentry(decodedData);
    }
    return logentry;
  }

  static LogEntry jsonstring2logentry(String jsonstring) {
    LogEntry logentry = null;

    try {
      // structure a LogEntry from the json string
      LogEntry.Builder builder = LogEntry.newBuilder();
      JsonFormat.parser().ignoringUnknownFields().merge(jsonstring, builder);
      logentry = builder.build();
      // log(LOGGER,log);

    } catch (InvalidProtocolBufferException e) {
      e.printStackTrace();
    }
    return logentry;
  }

  static AuditLog jsonstring2auditlog(String jsonstring) {
    AuditLog auditlog = null;

    try {
      // structure a LogEntry from the json string
      AuditLog.Builder builder = AuditLog.newBuilder();
      JsonFormat.parser().ignoringUnknownFields().merge(jsonstring, builder);
      auditlog = builder.build();
      // this.log(log);

    } catch (InvalidProtocolBufferException e) {
      e.printStackTrace();
    }
    return auditlog;
  }

  static void log(Logger LOGGER, CloudEventPubsubMessage msg) {
    LOGGER.info(String.format("msg.messageId : %s", msg.messageId()));
    LOGGER.info(String.format("msg.publishTime : %s", msg.publishTime()));
    msg.attributes().keySet().stream()
        .sorted()
        .forEach(
            (key) -> {
              LOGGER.info(String.format("msg.attributes[%s] : %s", key, msg.attributes().get(key)));
            });
    String encodedData = msg.data();
    LOGGER.info(String.format("msg.data : %s", encodedData));

    String decodedData =
        new String(Base64.getDecoder().decode(encodedData), StandardCharsets.UTF_8);
    LOGGER.info("msg.data.decoded : " + decodedData);
  }

  static void log(Logger LOGGER, CloudEvent event) {
    LOGGER.info(String.format("event.id : %s", event.getId()));
    LOGGER.info(String.format("event.subject : %s", event.getSubject()));
    LOGGER.info(String.format("event.type : %s", event.getType()));
    LOGGER.info(String.format("event.dataContentType : %s", event.getDataContentType()));
    LOGGER.info(String.format("event.dataSchema : %s", event.getDataSchema()));
  }

  static void log(Logger LOGGER, LogEntry logentry) {

    final String PREFIX = "logentry";
    LOGGER.info(String.format("%s.logName : %s", PREFIX, logentry.getLogName()));
    LOGGER.info(String.format("%s.insertId : %s", PREFIX, logentry.getInsertId()));
    LOGGER.info(String.format("%s.receiveTimestamp : %s", PREFIX, logentry.getReceiveTimestamp()));
    logentry
        .getLabelsMap()
        .keySet()
        .forEach(
            (key) -> {
              LOGGER.info(
                  String.format(
                      "%s.labels[%s] : %s", PREFIX, key, logentry.getLabelsMap().get(key)));
            });
    LOGGER.info(String.format("%s.resource.type : %s", PREFIX, logentry.getResource().getType()));

    logentry
        .getResource()
        .getLabelsMap()
        .keySet()
        .forEach(
            (key) -> {
              LOGGER.info(
                  String.format(
                      "%s.resource.labels[%s] : %s",
                      PREFIX, key, logentry.getResource().getLabelsMap().get(key)));
            });
  }

  static void log(Logger LOGGER, AuditLog log) {

    final String PREFIX = "auditlog";

    LOGGER.info(String.format("%s.resourceName : %s", PREFIX, log.getResourceName()));
    LOGGER.info(String.format("%s.serviceName : %s", PREFIX, log.getServiceName()));
    LOGGER.info(
        String.format(
            "%s.authenticationInfo.principalEmail : %s",
            PREFIX, log.getAuthenticationInfo().getPrincipalEmail()));
  }

  // building on top of sample here
  // https://cloud.google.com/pubsub/docs/samples/pubsub-publish-custom-attributes
  static void publishFleetEventMessage(
      Publisher publisher, FleetEvents.FleetEventMessage msg, Logger LOGGER)
      throws InterruptedException, ExecutionException {
    assert publisher != null;

    String jsonstring = new Gson().toJson(msg, FleetEvents.FleetEventMessage.class);
    if (LOGGER != null) {
      LOGGER.info("message: " + jsonstring);
    }
    ByteString data = ByteString.copyFromUtf8(jsonstring);
    com.google.pubsub.v1.PubsubMessage pubsubMessage =
        com.google.pubsub.v1.PubsubMessage.newBuilder()
            .setData(data)
            .putAllAttributes(msg.attributes())
            .build();

    // Once published, returns a server-assigned message id (unique within the
    // topic)
    ApiFuture<String> messageIdFuture = publisher.publish(pubsubMessage);
    String messageId = messageIdFuture.get();
    if (LOGGER != null) {
      LOGGER.info("Published a message with custom attributes: " + messageId);
    }
  }

  static FleetEvents.FleetEventMessage logentry2fleeteventmsg(LogEntry logentry) {

    String name = logentry.getLogName();
    String[] split = name.split("/");
    String projectid = split[1];
    String eventname = split[3].split("%2F")[1];

    Timestamp eventtime = logentry.getTimestamp();
    Timestamp logtime = logentry.getReceiveTimestamp();

    Map<String, String> msg_attributes = new HashMap<>();
    msg_attributes.put("project", projectid);

    FleetEvents.Usecase usecase = null;

    switch (eventname) {
      case "create_vehicle":
      case "get_vehicle":
      case "update_vehicle":
        msg_attributes.put(
            FleetEvents.LABELS.vehicle_id.toString(),
            logentry.getLabelsOrThrow(FleetEvents.LABELS.vehicle_id.toString()));
        usecase = FleetEvents.Usecase.ODRD;
        break;
      case "create_trip":
      case "get_trip":
      case "update_trip":
        msg_attributes.put(
            FleetEvents.LABELS.trip_id.toString(),
            logentry.getLabelsOrThrow(FleetEvents.LABELS.trip_id.toString()));
        usecase = FleetEvents.Usecase.ODRD;
        break;
      case "create_task":
      case "get_task":
      case "get_task_tracking_info":
      case "update_task":
        msg_attributes.put(
            FleetEvents.LABELS.task_id.toString(),
            logentry.getLabelsOrThrow(FleetEvents.LABELS.task_id.toString()));
        usecase = FleetEvents.Usecase.LMFS;
        break;
      case "create_delivery_vehicle":
      case "get_delivery_vehicle":
      case "update_delivery_vehicle":
        msg_attributes.put(
            FleetEvents.LABELS.delivery_vehicle_id.toString(),
            logentry.getLabelsOrThrow(FleetEvents.LABELS.delivery_vehicle_id.toString()));
        usecase = FleetEvents.Usecase.LMFS;
        break;
      case "search_vehicles":
        usecase = FleetEvents.Usecase.ODRD;
        break;
      case "list_delivery_vehicles":
        usecase = FleetEvents.Usecase.LMFS;
        break;
      default:
    }

    FleetEvents.FleetEventMessage msg =
        new FleetEvents.FleetEventMessage(eventname, usecase, msg_attributes, eventtime, logtime);

    return msg;
  }
}
