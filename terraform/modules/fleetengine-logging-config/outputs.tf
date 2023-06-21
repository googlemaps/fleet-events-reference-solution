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


output "fleetengine-logrouters" {
  description = "Log router details"

  value = {
    logging  = google_logging_project_sink.fleetengine-logrouter-logging != null && length(google_logging_project_sink.fleetengine-logrouter-logging) > 0 ? google_logging_project_sink.fleetengine-logrouter-logging[0] : null
    bigquery = google_logging_project_sink.fleetengine-logrouter-bigquery != null && length(google_logging_project_sink.fleetengine-logrouter-bigquery) > 0 ? google_logging_project_sink.fleetengine-logrouter-bigquery[0] : null
    storage  = google_logging_project_sink.fleetengine-logrouter-storage != null && length(google_logging_project_sink.fleetengine-logrouter-storage) > 0 ? google_logging_project_sink.fleetengine-logrouter-storage[0] : null
    pubsub   = google_logging_project_sink.fleetengine-logrouter-pubsub != null && length(google_logging_project_sink.fleetengine-logrouter-pubsub) > 0 ? google_logging_project_sink.fleetengine-logrouter-pubsub[0] : null
  }
}

output "console-links" {
  description = "direct link to related pages in Cloud Console"
  value = {
    console_src_project_router = format("https://console.cloud.google.com/logs/router?project=%s", local.logging_src_project_id)
    console_sink_project_bq    = format("https://console.cloud.google.com/bigquery?project=%s&ws=!1m4!1m3!3m2!1s%s!2s%s", local.logging_sink_project_id, local.logging_sink_project_id, var.BQ_DATASET)
  }
}


output "fleetengine-log-sinks" {
  description = "Log Sink details"

  value = {
    logging = var.FLAG_SETUP_LOGGING_LOGGING ? {
      bucket = google_logging_project_bucket_config.fleetengine-logging-logbucket[0]
    } : null
    bigquery = var.FLAG_SETUP_LOGGING_BIGQUERY ? {
      dataset = google_bigquery_dataset.fleetengine-logging-sink-bq[0]
    } : null
    storage = var.FLAG_SETUP_LOGGING_CLOUDSTORAGE ? {
      bucket = google_storage_bucket.fleetengine-logging-sink-storage[0]
    } : null
    pubsub = var.FLAG_SETUP_LOGGING_PUBSUB ? {
      topic                = google_pubsub_topic.fleetengine-logging-sink-pubsub[0]
      subscription_default = var.FLAG_SETUP_LOGGING_PUBSUB_SUB_DEFAULT ? google_pubsub_subscription.fleetengine-logging-subscription-default[0] : null
      bigquery = var.FLAG_SETUP_LOGGING_PUBSUB_SUB_BQ ? {
        dataset = google_bigquery_dataset.fleetengine-logging-sink-pubsub-bq[0]
        table   = google_bigquery_table.fleetengine-logging-sink-pubsub-bq-table[0]
      } : null
    } : null
  }
}
