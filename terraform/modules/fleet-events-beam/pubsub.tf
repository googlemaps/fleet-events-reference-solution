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

locals {
  FLAG_SETUP_LOGGING_PUBSUB_SUB_BQ = true
}


## pubsub subscription to bq
## https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/pubsub_subscription

resource "google_pubsub_subscription" "topic-output-subscription-bigquery" {

  project = data.google_project.project_fleetevents.project_id
  #name    = "fleetengine-logging-subscription-bigquery"
  name   = format("%s-subscription-bigquery", google_pubsub_topic.topic-fleetevents-output.name)
  topic  = google_pubsub_topic.topic-fleetevents-output.name
  labels = local.labels_common
  bigquery_config {
    table = format(
      "%s:%s.%s",
      google_bigquery_table.topic-output-bq-subscription-table[0].project,
      google_bigquery_table.topic-output-bq-subscription-table[0].dataset_id,
      google_bigquery_table.topic-output-bq-subscription-table[0].table_id
    )
    write_metadata = true

  }
  count = var.FLAG_SETUP_LOGGING_PUBSUB_SUB_BQ ? 1 : 0

  depends_on = [
    google_project_iam_member.bq_metadata_viewer,
    google_project_iam_member.bq_data_editor
  ]
}

resource "google_project_iam_member" "bq_metadata_viewer" {
  project = data.google_project.project_fleetevents.project_id
  role    = "roles/bigquery.metadataViewer"
  member  = format("serviceAccount:%s", google_service_account.sa_app.email)
}

resource "google_project_iam_member" "bq_data_editor" {
  project = data.google_project.project_fleetevents.project_id
  role    = "roles/bigquery.dataEditor"
  member  = format("serviceAccount:%s", google_service_account.sa_app.email)
}

resource "google_bigquery_dataset" "topic-output-bq-subscription-dataset" {
  project               = data.google_project.project_fleetevents.project_id
  dataset_id            = format("%s_pubsub", replace(google_pubsub_topic.topic-fleetevents-output.name, "-", "_"))
  description           = format("BigQuery subscription to FleetEvents output Pub/Sub topic")
  max_time_travel_hours = "168"
  count                 = var.FLAG_SETUP_LOGGING_PUBSUB_SUB_BQ ? 1 : 0
  labels                = local.labels_common
}

resource "google_bigquery_table" "topic-output-bq-subscription-table" {
  deletion_protection = false
  project             = data.google_project.project_fleetevents.project_id
  count               = var.FLAG_SETUP_LOGGING_PUBSUB_SUB_BQ ? 1 : 0
  labels              = local.labels_common
  table_id            = "pubsub_subscription"
  dataset_id          = google_bigquery_dataset.topic-output-bq-subscription-dataset[0].dataset_id

  schema = <<EOF
[
  {
    "name": "subscription_name",
    "type": "STRING",
    "mode": "NULLABLE",
    "description": "Name of a subscription."
  },
  {
    "name": "message_id",
    "type": "STRING",
    "mode": "NULLABLE",
    "description": "ID of a message"
  },
  {
    "name": "publish_time",
    "type": "TIMESTAMP",
    "mode": "NULLABLE",
    "description": "The time of publishing a message."
  },
  {
    "name": "data",
    "type": "STRING",
    "mode": "NULLABLE",
    "description": "The message body."
  },
  {
    "name": "attributes",
    "type": "STRING",
    "mode": "NULLABLE",
    "description": "A JSON object containing all message attributes. It also contains additional fields that are part of the Pub/Sub message including the ordering key, if present."
  }
]
EOF
}
