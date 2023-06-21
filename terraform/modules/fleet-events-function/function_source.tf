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


# Cloud Storage bucket to stage Functions code
resource "google_storage_bucket" "bucket" {
  project                     = var.PROJECT_APP
  name                        = format("gcf-src-%s-%s-%s", data.google_project.project.number, var.GCP_REGION, var.FUNCTION_NAME)
  location                    = var.GCP_REGION_STORAGE
  uniform_bucket_level_access = true
  force_destroy               = true
  versioning {
    enabled = true
  }
  labels = local.labels_common
}


## zip functions source code into a zip file that will be deployed to Functions
data "archive_file" "zipfile" {
  type        = "zip"
  output_path = "${path.root}/tmp/function_src.zip"
  source_dir  = var.FUNCTION_SRC_DIR
  excludes = concat(
    var.FUNCTION_SRC_EXCLUDE_FILES,
    flatten([
      for i in var.FUNCTION_SRC_EXCLUDE_PATTERNS : tolist(fileset(var.FUNCTION_SRC_DIR, i))
    ])
  )
}


# place zip file onto the code staging bucket
# injecting md5 hash into the file name, so that every code change triggers a redeployment of the function
resource "google_storage_bucket_object" "function_source" {
  name             = format("source-%s-%s.zip", var.FUNCTION_NAME, data.archive_file.zipfile.output_md5)
  bucket           = google_storage_bucket.bucket.name
  source           = data.archive_file.zipfile.output_path
  temporary_hold   = false
  event_based_hold = false
  metadata         = {}


}
