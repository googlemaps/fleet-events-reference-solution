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


## BigQuery
resource "google_project_service" "gcp_services_bigquery" {
  project            = local.logging_sink_project_id
  service            = "bigquery.googleapis.com"
  disable_on_destroy = false
  count              = var.FLAG_SETUP_LOGGING_BIGQUERY ? 1 : 0
}

resource "google_bigquery_dataset" "fleetengine-logging-sink-bq" {
  project                         = local.logging_sink_project_id
  dataset_id                      = var.BQ_DATASET
  friendly_name                   = "FleetEngine Logs"
  description                     = format("Logging(FleetEngine) -> BigQuery (src proj : %s)", local.logging_src_project_id)
  location                        = var.GCP_REGION
  labels                          = local.labels_common
  max_time_travel_hours           = "168"
  default_table_expiration_ms     = tonumber(var.RETENTION) * 24 * 3600 * 1000
  default_partition_expiration_ms = tonumber(var.RETENTION) * 24 * 3600 * 1000
  count                           = var.FLAG_SETUP_LOGGING_BIGQUERY ? 1 : 0
  delete_contents_on_destroy      = true
  depends_on = [
    google_project_service.gcp_services_bigquery
  ]
}

resource "google_logging_project_sink" "fleetengine-logrouter-bigquery" {
  project                = local.logging_src_project_id
  name                   = format("fleetengine-logrouter-to-bigquery-%s", local.logging_sink_project_number)
  description            = format("Logging -> BigQuery (dest proj : %s, %s)", local.logging_sink_project_id, local.logging_sink_project_number)
  destination            = format("bigquery.googleapis.com/%s", google_bigquery_dataset.fleetengine-logging-sink-bq[0].id)
  filter                 = local.logging_filter
  unique_writer_identity = true
  bigquery_options {
    use_partitioned_tables = true
  }
  count = var.FLAG_SETUP_LOGGING_BIGQUERY ? 1 : 0
}

resource "google_bigquery_dataset_iam_member" "fleetengine-logging-sink-bq-editor" {
  project    = local.logging_sink_project_id
  dataset_id = google_bigquery_dataset.fleetengine-logging-sink-bq[0].dataset_id
  role       = "roles/bigquery.dataEditor"
  member     = google_logging_project_sink.fleetengine-logrouter-bigquery[0].writer_identity
  count      = var.FLAG_SETUP_LOGGING_BIGQUERY ? 1 : 0
}



