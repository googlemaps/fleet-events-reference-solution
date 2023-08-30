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


## Pub/Sub
resource "google_project_service" "gcp_services_pubsub" {
  project            = local.logging_sink_project_id
  service            = "pubsub.googleapis.com"
  disable_on_destroy = false
  count              = var.FLAG_SETUP_LOGGING_PUBSUB ? 1 : 0
}

resource "google_pubsub_topic" "fleetengine-logging-sink-pubsub" {
  project = local.logging_sink_project_id
  name    = var.PUBSUB_TOPIC_NAME
  message_storage_policy {
    allowed_persistence_regions = [
      var.GCP_REGION
    ]
  }
  labels = {
    createdby                = "terraform"
    logsource_project_number = local.logging_src_project_number
    logsource_project_id     = local.logging_src_project_id
  }
  count = var.FLAG_SETUP_LOGGING_PUBSUB ? 1 : 0
}

resource "google_pubsub_topic_iam_member" "fleetengine-logging-sink-topic-publisher" {
  project = google_pubsub_topic.fleetengine-logging-sink-pubsub[0].project
  topic   = google_pubsub_topic.fleetengine-logging-sink-pubsub[0].name
  role    = "roles/pubsub.publisher"
  member  = google_logging_project_sink.fleetengine-logrouter-pubsub[0].writer_identity
  count   = var.FLAG_SETUP_LOGGING_PUBSUB ? 1 : 0

}



### default subscription for topic so that data since creation of the topic can be captured.
resource "google_pubsub_subscription" "fleetengine-logging-subscription-default" {
  project = local.logging_sink_project_id
  #name    = "fleetengine-logging-subscription-default"
  name   = format("%s-subscription-default", google_pubsub_topic.fleetengine-logging-sink-pubsub[0].name)
  topic  = google_pubsub_topic.fleetengine-logging-sink-pubsub[0].name
  labels = local.labels_common

  # 20 minutes
  message_retention_duration = "1200s"
  retain_acked_messages      = true

  ack_deadline_seconds = 20

  expiration_policy {
    ttl = "300000.5s"
  }
  retry_policy {
    minimum_backoff = "10s"
  }

  enable_message_ordering = true
  count                   = (var.FLAG_SETUP_LOGGING_PUBSUB && var.FLAG_SETUP_LOGGING_PUBSUB_SUB_DEFAULT) ? 1 : 0

}


resource "google_project_service" "services_bqsub_prereq_bigquery" {
  project            = local.logging_sink_project_id
  service            = "bigquery.googleapis.com"
  disable_on_destroy = false
  count              = var.FLAG_SETUP_LOGGING_PUBSUB && var.FLAG_SETUP_LOGGING_PUBSUB_SUB_BQ ? 1 : 0

}

## pubsub subscription to bq
## https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/pubsub_subscription

resource "google_pubsub_subscription" "fleetengine-logging-subscription-bigquery" {

  project = local.logging_sink_project_id
  #name    = "fleetengine-logging-subscription-bigquery"
  name   = format("%s-subscription-bigquery", google_pubsub_topic.fleetengine-logging-sink-pubsub[0].name)
  topic  = google_pubsub_topic.fleetengine-logging-sink-pubsub[0].name
  labels = local.labels_common
  bigquery_config {
    table = format(
      "%s:%s.%s",
      google_bigquery_table.fleetengine-logging-sink-pubsub-bq-table[0].project,
      google_bigquery_table.fleetengine-logging-sink-pubsub-bq-table[0].dataset_id,
      google_bigquery_table.fleetengine-logging-sink-pubsub-bq-table[0].table_id
    )
    write_metadata = true

  }
  count = var.FLAG_SETUP_LOGGING_PUBSUB && var.FLAG_SETUP_LOGGING_PUBSUB_SUB_BQ ? 1 : 0

  depends_on = [google_project_iam_member.bq_metadata_viewer, google_project_iam_member.bq_data_editor]
}

resource "google_project_iam_member" "bq_metadata_viewer" {
  project = local.logging_sink_project_id
  role    = "roles/bigquery.metadataViewer"
  member  = "serviceAccount:service-${local.logging_sink_project_number}@gcp-sa-pubsub.iam.gserviceaccount.com"
}

resource "google_project_iam_member" "bq_data_editor" {
  project = local.logging_sink_project_id
  role    = "roles/bigquery.dataEditor"
  member  = "serviceAccount:service-${local.logging_sink_project_number}@gcp-sa-pubsub.iam.gserviceaccount.com"
}

resource "google_bigquery_dataset" "fleetengine-logging-sink-pubsub-bq" {
  project               = local.logging_sink_project_id
  dataset_id            = format("%s_pubsub", replace(var.PUBSUB_TOPIC_NAME, "-", "_"))
  description           = format("BigQuery subscription to Fleet Engine log stream into Pub/Sub")
  max_time_travel_hours = "168"
  count                 = var.FLAG_SETUP_LOGGING_PUBSUB && var.FLAG_SETUP_LOGGING_PUBSUB_SUB_BQ ? 1 : 0
  labels                = local.labels_common
}

resource "google_bigquery_table" "fleetengine-logging-sink-pubsub-bq-table" {
  deletion_protection = false
  project             = local.logging_sink_project_id
  count               = var.FLAG_SETUP_LOGGING_PUBSUB && var.FLAG_SETUP_LOGGING_PUBSUB_SUB_BQ ? 1 : 0
  labels              = local.labels_common
  table_id            = "pubsub_subscription"
  dataset_id          = google_bigquery_dataset.fleetengine-logging-sink-pubsub-bq[0].dataset_id

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


resource "google_logging_project_sink" "fleetengine-logrouter-pubsub" {
  project                = local.logging_src_project_id
  name                   = format("fleetengine-logrouter-to-pubsub-%s-%s", local.logging_sink_project_number, google_pubsub_topic.fleetengine-logging-sink-pubsub[0].name)
  description            = format("Logging -> Pub/Sub (dest proj : %s, %s, topic : %s)", local.logging_sink_project_id, local.logging_sink_project_number, google_pubsub_topic.fleetengine-logging-sink-pubsub[0].name)
  destination            = format("pubsub.googleapis.com/%s", google_pubsub_topic.fleetengine-logging-sink-pubsub[0].id)
  filter                 = local.logging_filter
  unique_writer_identity = true
  count                  = var.FLAG_SETUP_LOGGING_PUBSUB ? 1 : 0

}
