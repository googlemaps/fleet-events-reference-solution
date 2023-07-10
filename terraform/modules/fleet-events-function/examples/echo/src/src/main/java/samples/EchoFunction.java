/*
 * Copyright 2023 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

import com.google.cloud.functions.CloudEventsFunction;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.gson.Gson;
import com.google.logging.v2.LogEntry;
import com.google.pubsub.v1.TopicName;
import io.cloudevents.CloudEvent;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

// follows examples from
// https://cloud.google.com/functions/docs/writing/write-event-driven-functions
// https://cloud.google.com/functions/docs/writing/specifying-dependencies-java

/** A sample Cloud Function that publishes detected Fleet Engine events to a Pub/Sub Topic */
public class EchoFunction implements CloudEventsFunction {

  static Logger LOGGER = Logger.getLogger(EchoFunction.class.getName());

  static String SA_FLEETENGINE = null;
  static String PROJECT_FLEETENGINE = null;
  static String PROJECT_APP = null;
  static String TOPIC_OUTPUT = null;

  static TopicName topicName = null;
  static Publisher publisher = null;

  static Level LOG_LEVEL = Level.INFO;

  static enum PARAMS {
    PROJECT_APP,
    PROJECT_FLEETENGINE,
    SA_FLEETENGINE,
    TOPIC_OUTPUT
  }

  static {
    assert System.getenv(PARAMS.PROJECT_APP.toString()) != null;
    assert System.getenv(PARAMS.PROJECT_FLEETENGINE.toString()) != null;
    assert System.getenv(PARAMS.SA_FLEETENGINE.toString()) != null;
    assert System.getenv(PARAMS.TOPIC_OUTPUT.toString()) != null;

    System.getenv().keySet().stream()
        .sorted()
        .forEach(
            (key) -> {
              LOGGER.log(LOG_LEVEL, String.format("System.env.%s : %s", key, System.getenv(key)));
            });

    SA_FLEETENGINE = System.getenv(PARAMS.SA_FLEETENGINE.toString());
    PROJECT_FLEETENGINE = System.getenv(PARAMS.PROJECT_FLEETENGINE.toString());
    PROJECT_APP = System.getenv(PARAMS.PROJECT_APP.toString());
    TOPIC_OUTPUT = System.getenv(PARAMS.TOPIC_OUTPUT.toString());

    topicName = TopicName.of(PROJECT_APP, TOPIC_OUTPUT);
    try {
      publisher = Publisher.newBuilder(topicName).build();
    } catch (IOException e) {
      e.printStackTrace();
    }

    if (System.getenv().containsKey("LOG_LEVEL")) {
      LOG_LEVEL = Level.parse(System.getenv("LOG_LEVEL"));
      LOGGER.setLevel(LOG_LEVEL);
    } else {
      LOG_LEVEL = Level.INFO;
      LOGGER.setLevel(LOG_LEVEL);
    }
  }

  /** publishes extracted ODRD/LMFS specific event info to Pub/Sub topic. */
  @Override
  public void accept(CloudEvent event) throws Exception {
    LOGGER.log(LOG_LEVEL, "#accept");

    LogEntry logentry = Util.cloudevent2logentry(event);
    if (logentry != null) {

      FleetEvents.FleetEventMessage msg = Util.logentry2fleeteventmsg(logentry);
      String msgjsonstring = new Gson().toJson(msg, FleetEvents.FleetEventMessage.class);
      LOGGER.log(LOG_LEVEL, msgjsonstring);
      try {
        Util.publishFleetEventMessage(publisher, msg, LOGGER);
      } catch (InterruptedException | ExecutionException e) {
        e.printStackTrace();
      }
    }
  }
}
