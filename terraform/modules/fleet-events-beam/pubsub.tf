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

# reference Pub/Sub topic where FleetEngine logs will be published
data "google_pubsub_topic" "topic-fleetevents-input" {
  project = data.google_project.project_fleetengine_logs.project_id
  name    = var.TOPIC_FLEETENGINE_LOG
}
output "var_TOPIC_FLEETENGINE_LOG" {
  value = var.TOPIC_FLEETENGINE_LOG
}

# allow SA for Pub/Sub Trigger to subscribe to the FleetEngine logs topic
resource "google_pubsub_topic_iam_member" "input-subscriber-sa" {
  project = data.google_project.project_fleetevents.project_id
  topic   = data.google_pubsub_topic.topic-fleetevents-input.name
  for_each = toset([
    "roles/pubsub.subscriber"
  ])
  role   = each.value
  member = format("serviceAccount:%s", google_service_account.sa_app.email)
}

resource "google_pubsub_topic" "topic-fleetevents-output" {
  project = data.google_project.project_fleetevents.project_id

  name   = format("%s-output", var.PIPELINE_NAME)
  labels = local.labels_common
}

resource "google_pubsub_topic_iam_member" "output-publisher-sa" {
  project = data.google_project.project_fleetevents.project_id
  topic   = google_pubsub_topic.topic-fleetevents-output.name
  for_each = toset([
    "roles/pubsub.publisher"
  ])
  role   = each.value
  member = format("serviceAccount:%s", google_service_account.sa_app.email)
}
