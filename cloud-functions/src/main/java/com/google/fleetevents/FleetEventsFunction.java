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

package com.google.fleetevents;

import com.google.cloud.functions.CloudEventsFunction;
import com.google.fleetevents.common.sinks.PubSubWriter;
import com.google.fleetevents.common.util.ProtoParser;
import com.google.fleetevents.lmfs.config.FleetEventConfig;
import com.google.fleetevents.lmfs.models.outputs.OutputEvent;
import com.google.logging.v2.LogEntry;
import com.google.protobuf.InvalidProtocolBufferException;
import io.cloudevents.CloudEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Fleet Events function that handles incoming logs from Cloud Logging and outputs events handled by
 * fleet event handlers.
 */
public class FleetEventsFunction implements CloudEventsFunction {

  private static final Logger logger = Logger.getLogger(FleetEventsFunction.class.getName());

  FleetEventCreator fleetEventCreator;
  List<FleetEventHandler> fleetEventHandlers;

  public FleetEventsFunction(FleetEventCreator fleetEventCreator) {
    this.fleetEventCreator = fleetEventCreator;
    fleetEventHandlers = new ArrayList<>();
  }

  public void registerFleetEventHandler(FleetEventHandler fleetEventHandler) {
    fleetEventHandlers.add(fleetEventHandler);
  }

  @Override
  public void accept(CloudEvent cloudEvent) {
    if (cloudEvent.getData() != null) {
      List<OutputEvent> outputEvents;
      try {
        LogEntry logEntry = ProtoParser.cloudEventDataToLogEntry(cloudEvent.getData());
        outputEvents = fleetEventCreator.processCloudLog(logEntry, fleetEventHandlers);
        fleetEventCreator.addExtraInfo(outputEvents);
        var outputTopicId = FleetEventConfig.getOutputTopicId();
        if (outputTopicId != null) {
          PubSubWriter.publishOutputEvents(
              FleetEventConfig.getOutputProjectId(), outputTopicId, outputEvents);
        }
      } catch (InvalidProtocolBufferException e) {
        logger.warning(String.format("Received protobug parse error %s " + e));
        logger.warning(
            String.format("Failed to parse message: %s", cloudEvent.getData().toString()));
      } catch (Exception e) {
        // do not throw exception to reduce cold start
        // https://cloud.google.com/functions/docs/bestpractices/tips#error_reporting
        logger.warning("Encountered error while processing: " + e);
      }
    }
  }
}
