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


# firestore database instance cannot be easily deleted
# and even if deleted, the database name is not immediately reusable
# therefore, it is recommended to use a more dynamic name


locals {
  REGION_FIRESTORE = (var.GCP_REGION_FIRESTORE != "" && var.GCP_REGION_FIRESTORE != null) ? var.GCP_REGION_FIRESTORE : var.GCP_REGION
}

resource "google_firestore_database" "database" {
  project                     = data.google_project.project_fleetevents.project_id
  name                        = format("%s-%s", var.DATABASE_NAME, random_id.jobname_suffix.dec)
  location_id                 = local.REGION_FIRESTORE
  type                        = "FIRESTORE_NATIVE"
  concurrency_mode            = "PESSIMISTIC"
  app_engine_integration_mode = "DISABLED"

  depends_on = [
    google_project_service.gcp_services["firestore.googleapis.com"]
  ]

}
