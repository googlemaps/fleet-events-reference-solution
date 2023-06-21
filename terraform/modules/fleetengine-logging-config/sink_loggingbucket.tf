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


## Cloud Logging Bucket
resource "google_project_service" "gcp_services_logging" {
  project            = local.logging_sink_project_id
  service            = "logging.googleapis.com"
  disable_on_destroy = false
  count              = var.FLAG_SETUP_LOGGING_LOGGING ? 1 : 0
}
resource "google_logging_project_bucket_config" "fleetengine-logging-logbucket" {
  project  = local.logging_sink_project_id
  location = var.GCP_REGION
  #retention_days = tonumber(var.RETENTION)
  description = format("Logging -> Logging Bucket (dest proj : %s, %s)", local.logging_sink_project_id, local.logging_sink_project_number)
  bucket_id   = "fleetengine_logging_logbucket"
  count       = var.FLAG_SETUP_LOGGING_LOGGING ? 1 : 0
}

resource "google_logging_project_sink" "fleetengine-logrouter-logging" {
  project                = local.logging_src_project_id
  name                   = format("fleetengine-logrouter-to-logging-%s", local.logging_sink_project_number)
  description            = format("Logging -> Logging Bucket (src proj : %s)", local.logging_src_project_id)
  destination            = format("logging.googleapis.com/%s", google_logging_project_bucket_config.fleetengine-logging-logbucket[0].id)
  filter                 = local.logging_filter
  unique_writer_identity = true
  count                  = var.FLAG_SETUP_LOGGING_LOGGING ? 1 : 0
}

resource "google_project_iam_member" "project_iam_loggingbucket_writer" {
  project = local.logging_sink_project_id
  role    = "roles/logging.bucketWriter"
  member  = format("%s", google_logging_project_sink.fleetengine-logrouter-logging[0].writer_identity != null ? google_logging_project_sink.fleetengine-logrouter-logging[0].writer_identity : "serviceAccount:cloud-logs@system.gserviceaccount.com")
  #member  = format("serviceAccount:%s", "cloud-logs@system.gserviceaccount.com")
  count = var.FLAG_SETUP_LOGGING_LOGGING ? 1 : 0
}
resource "google_project_iam_member" "project_iam_loggingadmin" {
  project = local.logging_sink_project_id
  role    = "roles/logging.admin"
  member  = "user:${var.ME}"
  #member  = format("serviceAccount:%s", "cloud-logs@system.gserviceaccount.com")
  count = var.FLAG_SETUP_LOGGING_LOGGING ? 1 : 0
}

