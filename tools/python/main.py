#!/usr/bin/env python
# Copyright 2023 Google Inc. All Rights Reserved.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

"""
Publishes fleet logs to Google PubSub topic to simulate a delivery.
"""

import argparse
import time
from concurrent import futures
from concurrent.futures import TimeoutError
from google.cloud import pubsub_v1
import json
import uuid
from typing import Any
from typing import Callable

UUID = str(uuid.uuid4())
SUBSCRIBER_PREFIX = "fleet_events_publish_tool"
PUBLISHER_SUBSCRIPTION_NAME = SUBSCRIBER_PREFIX + "_" + UUID
# amount of time to wait for messages
READ_TIMEOUT_SECONDS = 15


def publish_messages(project_id: str, topic_id: str, tag: str, fleet_logs: [str], sleep_time: int) -> None:
    """Publishes multiple messages to a Pub/Sub topic with an error handler."""
    publisher = pubsub_v1.PublisherClient()
    topic_path = publisher.topic_path(project_id, topic_id)
    publish_futures = []

    def get_callback(
            publish_future: pubsub_v1.publisher.futures.Future, fleet_log: str
    ) -> Callable[[pubsub_v1.publisher.futures.Future], None]:
        def callback(publish_future: pubsub_v1.publisher.futures.Future) -> None:
            try:
                print(f"Successfully wrote log. Result id is {publish_future.result(timeout=60)}")
            except futures.TimeoutError:
                print(f"Publishing {fleet_log} timed out.")
        return callback

    for i in fleet_logs:
        fleet_log = str(i)
        publish_future = publisher.publish(topic_path, fleet_log.encode("utf-8"), source=f"{tag}")
        publish_future.add_done_callback(get_callback(publish_future, fleet_log))
        publish_futures.append(publish_future)
        # introduce a wait between publishes for better ordering
        time.sleep(sleep_time)
    futures.wait(publish_futures, return_when=futures.ALL_COMPLETED)
    print(f"Published messages to {topic_path}.")


def create_subscription_with_filtering(
        project_id: str,
        topic_id: str,
        subscription_id: str,
        filter: str,
) -> None:
    """Create a subscription with filtering enabled."""
    publisher = pubsub_v1.PublisherClient()
    subscriber = pubsub_v1.SubscriberClient()
    topic_path = publisher.topic_path(project_id, topic_id)
    subscription_path = subscriber.subscription_path(project_id, subscription_id)

    with subscriber:
        subscription = subscriber.create_subscription(
            request={"name": subscription_path, "topic": topic_path, "filter": filter}
        )
        print(f"Created subscription: {subscription}")


def receive_messages(
        project_id: str, subscription_id: str
) -> Any:
    """Receives messages from a pull subscription."""
    subscriber = pubsub_v1.SubscriberClient()
    subscription_path = subscriber.subscription_path(project_id, subscription_id)
    result = []
    def callback(message: pubsub_v1.subscriber.message.Message) -> pubsub_v1.subscriber.message.Message:
        print(f"Received {message}.")
        message.ack()
        result.append(message)
    streaming_pull_future = subscriber.subscribe(subscription_path, callback=callback)
    print(f"Listening for messages on {subscription_path}..\n")
    with subscriber:
        try:
            result.append(streaming_pull_future.result(timeout=READ_TIMEOUT_SECONDS))
        except TimeoutError:
            streaming_pull_future.cancel()
            streaming_pull_future.result()
    return result


def delete_subscription(project_id: str, subscription_id: str) -> None:
    """Deletes an existing Pub/Sub topic."""
    subscriber = pubsub_v1.SubscriberClient()
    subscription_path = subscriber.subscription_path(project_id, subscription_id)
    with subscriber:
        subscriber.delete_subscription(request={"subscription": subscription_path})
    print(f"Subscription deleted: {subscription_path}.")


if __name__ == '__main__':
    parser = argparse.ArgumentParser(
        description=__doc__,
        formatter_class=argparse.RawDescriptionHelpFormatter,
    )
    parser.add_argument("--project_id", help="Your Google Cloud project ID")
    parser.add_argument("--plan", help="Filename of itinerary")
    parser.add_argument("--topic_id", help="PubSub topic to publish to")
    args = parser.parse_args()

    print("loading " + args.plan)
    print("writing to project_id:{project_id}, topic_id:{topic_id}"
          .format(project_id=args.project_id, topic_id=args.topic_id))
    f = open(args.plan)
    data = json.load(f)
    publish_messages(args.project_id, args.topic_id, tag=PUBLISHER_SUBSCRIPTION_NAME, fleet_logs=data, sleep_time=2)