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


## Cloud Storage Bucket
resource "google_project_service" "gcp_services_storage" {
  project            = local.logging_sink_project_id
  service            = "storage.googleapis.com"
  disable_on_destroy = false
  count              = var.FLAG_SETUP_LOGGING_CLOUDSTORAGE ? 1 : 0
}

resource "google_storage_bucket" "fleetengine-logging-sink-storage" {
  project = local.logging_sink_project_id

  # Bucket names must contain 3-63 characters
  # https://cloud.google.com/storage/docs/naming-buckets
  name                        = format("%s-logging-fleetengine-%s", local.logging_sink_project_id, local.logging_src_project_number)
  force_destroy               = false
  location                    = var.GCP_REGION
  storage_class               = "STANDARD"
  uniform_bucket_level_access = true
  versioning {
    enabled = false
  }
  lifecycle_rule {
    action {
      type = "Delete"
    }
    condition {
      age        = tonumber(var.RETENTION)
      with_state = "ANY"
    }
  }
  labels = local.labels_common
  count = var.FLAG_SETUP_LOGGING_CLOUDSTORAGE ? 1 : 0
}

resource "google_storage_bucket_iam_member" "fleetengine-logging-sink-storage-editor" {
  bucket = google_storage_bucket.fleetengine-logging-sink-storage[0].name
  role   = "roles/storage.admin"
  member = google_logging_project_sink.fleetengine-logrouter-storage[0].writer_identity
  count  = var.FLAG_SETUP_LOGGING_CLOUDSTORAGE ? 1 : 0
}

resource "google_logging_project_sink" "fleetengine-logrouter-storage" {
  project                = local.logging_src_project_id
  name                   = format("fleetengine-logrouter-to-cloudstorage-%s", local.logging_sink_project_number)
  description            = format("Logging -> Cloud Storage (dest proj : %s, %s)", local.logging_sink_project_id, local.logging_sink_project_number)
  destination            = format("storage.googleapis.com/%s", google_storage_bucket.fleetengine-logging-sink-storage[0].id)
  filter                 = local.logging_filter
  unique_writer_identity = true
  count                  = var.FLAG_SETUP_LOGGING_CLOUDSTORAGE ? 1 : 0
}
