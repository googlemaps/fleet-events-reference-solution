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

from main import *

"""
Tests the TaskOutcome handler with simulated data.
"""

PLAN = "./data/lmfs/task_outcome_change.json"
TASK_OUTCOME_SUBSCRIPTION_NAME = "task_outcome_test_" + UUID
TASK_OUTCOME_OUTPUT_TAG = "TaskOutcomeOutputEvent"

if __name__ == '__main__':
    parser = argparse.ArgumentParser(
        description=__doc__,
        formatter_class=argparse.RawDescriptionHelpFormatter,
    )
    parser.add_argument("--project_id", help="Your Google Cloud project ID")
    parser.add_argument("--input_topic_id", help="PubSub topic to publish to")
    parser.add_argument("--output_topic_id", help="PubSub topic to listen on for results")

    args = parser.parse_args()
    project_id = args.project_id
    input_topic = args.input_topic_id
    output_topic = args.output_topic_id
    print(f"writing to project_id:{project_id}, topic_id:{input_topic}")

    create_subscription_with_filtering(project_id, output_topic, TASK_OUTCOME_SUBSCRIPTION_NAME,
                                       f"attributes.output_type=\"{TASK_OUTCOME_OUTPUT_TAG}\"")
    results = []
    try:
        f = open(PLAN)
        data = json.load(f)
        publish_messages(project_id, input_topic, tag=TASK_OUTCOME_SUBSCRIPTION_NAME, fleet_logs=data, sleep_time=1)
        results = receive_messages(project_id, TASK_OUTCOME_SUBSCRIPTION_NAME)
        assert len(results) == 1, "Expected 1 result"
        result = results[0]
        print(f"Found output result {result.data.decode('utf-8')}")
        log_entry = json.loads(result.data.decode('utf-8').replace("'", '"'))
        expected_task_id = data[0]["jsonPayload"]["request"]["taskId"]
        assert log_entry["taskId"] == expected_task_id, "Unexpected taskId found in output"
        assert log_entry["newTaskOutcome"] == "SUCCEEDED", "Expecting output field newTaskOutcome to be SUCCEEDED"
    finally:
        delete_subscription(project_id, TASK_OUTCOME_SUBSCRIPTION_NAME)
