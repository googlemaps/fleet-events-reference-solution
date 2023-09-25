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

package com.google.fleetevents.common.sinks;

import com.google.api.core.ApiFuture;
import com.google.api.core.ApiFutureCallback;
import com.google.api.core.ApiFutures;
import com.google.api.gax.rpc.ApiException;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.fleetevents.common.models.OutputEvent;
import com.google.gson.Gson;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;
import com.google.pubsub.v1.TopicName;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/** Writes output events to a PubSub output topic. */
public class PubSubWriter {

  private static final Logger logger = Logger.getLogger(PubSubWriter.class.getName());
  private static final String OUTPUT_ATTRIBUTE_TAG = "output_type";

  public static void publishOutputEvents(
      String projectId, String topicId, List<OutputEvent> outputEvents)
      throws IOException, InterruptedException {
    TopicName topicName = TopicName.of(projectId, topicId);
    Publisher publisher = null;
    try {
      // Create a publisher instance with default settings bound to the topic
      publisher = Publisher.newBuilder(topicName).build();
      for (var outputEvent : outputEvents) {
        publishOutputEvent(publisher, outputEvent);
      }
    } finally {
      if (publisher != null) {
        // When finished with the publisher, shutdown to free up resources.
        publisher.shutdown();
        publisher.awaitTermination(1, TimeUnit.MINUTES);
      }
    }
  }

  public static void publishOutputEvent(Publisher publisher, OutputEvent outputEvent) {

    Gson gson = new Gson();

    var data = gson.toJson(outputEvent);
    PubsubMessage pubsubMessage =
        PubsubMessage.newBuilder()
            .setData(ByteString.copyFromUtf8(data))
            .putAttributes(OUTPUT_ATTRIBUTE_TAG, outputEvent.getClass().getSimpleName())
            .build();

    // Once published, returns a server-assigned message id (unique within the topic)
    ApiFuture<String> future = publisher.publish(pubsubMessage);
    ApiFutures.addCallback(
        future,
        new ApiFutureCallback<>() {
          @Override
          public void onFailure(Throwable throwable) {
            if (throwable instanceof ApiException apiException) {
              // details on the API exception
              logger.info(String.valueOf(apiException.getStatusCode().getCode()));
              logger.info(String.valueOf(apiException.isRetryable()));
            }
            logger.info("Pubsub failed to write data: " + data);
          }

          @Override
          public void onSuccess(String s) {
            logger.info("Pubsub message id: " + s);
          }
        },
        MoreExecutors.directExecutor());
  }
}
