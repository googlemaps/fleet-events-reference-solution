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
from datetime import datetime
import concurrent.futures
import threading

"""
Load tests with simulated data.
"""

PLAN = "./data/2_stop_success.json"
LOAD_TEST_UUID = UUID
TASK_OUTCOME_SUBSCRIPTION_NAME = "load_test_" + UUID
TASK_OUTCOME_OUTPUT_TAG = "LoadTestEvent"
SLEEP_TIME = 5

UPDATE_DELIVERY_VEHICLE_LOG = "projects/fake-gcp-project/logs/fleetengine.googleapis.com%2Fupdate_delivery_vehicle"
UPDATE_TASK_LOG = "projects/fake-gcp-project/logs/fleetengine.googleapis.com%2Fupdate_task"
CREATE_DELIVERY_VEHICLE_LOG = "projects/fake-gcp-project/logs/fleetengine.googleapis.com%2Fcreate_delivery_vehicle"
CREATE_TASK_LOG = "projects/fake-gcp-project/logs/fleetengine.googleapis.com%2Fcreate_task"

# vehicle and task ids in 2_stop_success.json
REPLACE_ID = "d0fba8"


def load_gen(input_data: str, insert_id: str) -> int:
    curr_uuid = str(uuid.uuid4())
    replaced_data = input_data.replace(REPLACE_ID, curr_uuid)
    data = json.loads(replaced_data)
    count = 0
    for log_entry in data:
        count = count + 1
        log_entry["insertId"] = insert_id + ":" + curr_uuid + ":" + log_entry["insertId"]
        print(f'thread {threading.current_thread().name}: {log_entry}')
    publish_messages(project_id, input_topic, tag=TASK_OUTCOME_SUBSCRIPTION_NAME, fleet_logs=data, sleep_time=SLEEP_TIME)
    return count


if __name__ == '__main__':
    parser = argparse.ArgumentParser(
        description=__doc__,
        formatter_class=argparse.RawDescriptionHelpFormatter,
    )
    parser.add_argument("--project_id", help="Your Google Cloud project ID")
    parser.add_argument("--input_topic_id", help="PubSub topic to publish to")

    args = parser.parse_args()
    project_id = args.project_id
    input_topic = args.input_topic_id

    try:
        now = datetime.timestamp(datetime.now())
        print("Starting load_gen at ", now)
        f = open(PLAN)
        file_string = f.read()
        num_threads = 15
        num_calls = 1
        futures = []
        log_id = str(now).split(".")[0] + ":" + str(num_calls)
        print(log_id)

        with concurrent.futures.ThreadPoolExecutor(max_workers=num_threads) as executor:
            for i in range(num_calls):
                futures.append(executor.submit(load_gen, file_string, log_id))
            results = [future.result(timeout=900) for future in futures]
    finally:
        executor.shutdown(wait=True)
        print(f"completed load test with {sum(results)} logs at "
              f"{datetime.now()}, with logId prefix {str(now).split('.')[0]}")
