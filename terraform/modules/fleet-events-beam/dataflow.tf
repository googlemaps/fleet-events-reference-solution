# Copyright 2023 Google LLC
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
# https://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.


resource "random_id" "jobname_suffix" {
  byte_length = 4

  # change of any of the KVs here will trigger a new jobname to be generated
  keepers = {
    region             = var.GCP_REGION
    topic_id           = data.google_pubsub_topic.topic-fleetevents-input.id
    container_spec_md5 = data.google_storage_bucket_object.template_spec.md5hash
    jar                = terraform_data.script_build_jar.id
    image              = terraform_data.script_build_flex_template.id
    #subscription_id = google_pubsub_subscription.subscription-fleetenginelogs.id
  }
}




