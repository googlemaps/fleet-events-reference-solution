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

import com.google.fleetevents.helpers.FleetEventsTestHelper;
import com.google.fleetevents.models.pubsub.PubSubBody;
import com.google.gson.Gson;
import com.google.protobuf.util.JsonFormat;
import io.cloudevents.CloudEvent;
import io.cloudevents.core.builder.CloudEventBuilder;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Data used in testing proto parser.
 */
public class CloudEventTestData {

  public static CloudEvent getUpdateDeliveryVehicleCloudEvent() throws IOException {
    PubSubBody pubSubBody = new PubSubBody();
    pubSubBody
        .getMessage()
        .setData(
            Base64.getEncoder()
                .encodeToString(
                    JsonFormat.printer()
                        .print(FleetEventsTestHelper.updateDeliveryVehicleLog1())
                        .getBytes(StandardCharsets.UTF_8)));
    Gson gson = new Gson();
    return CloudEventBuilder.v1()
        .withSource(URI.create("/pubsub"))
        .withType("PubSub")
        .withData(gson.toJson(pubSubBody).getBytes(StandardCharsets.UTF_8))
        .withId("create_delivery_vehicle_1")
        .build();
  }
}
