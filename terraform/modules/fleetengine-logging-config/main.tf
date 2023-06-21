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


data "google_project" "project-loggingsync" {
  project_id = var.PROJECT_LOGGINGSYNC
}
data "google_project" "project-fleetengine" {
  project_id = var.PROJECT_FLEETENGINE
}

locals {
  logging_project             = data.google_project.project-loggingsync
  logging_src_project_id      = data.google_project.project-fleetengine.project_id
  logging_src_project_number  = data.google_project.project-fleetengine.number
  logging_sink_project_id     = data.google_project.project-loggingsync.project_id
  logging_sink_project_number = data.google_project.project-loggingsync.number
  logging_filter              = var.LOG_FILTER
  labels_common = {
    created_by                 = "terraform"
    project_fleetengine_number = local.logging_src_project_number
    project_fleetengine_id     = local.logging_src_project_id
  }
}



resource "google_project_service" "gcp_services" {
  project = local.logging_src_project_id
  for_each = toset([
    "iam.googleapis.com",
    # required for setting google_logging_project_sink
    # https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/logging_project_sink
    "cloudresourcemanager.googleapis.com"
  ])
  service            = each.key
  disable_on_destroy = false
}



resource "google_project_iam_member" "iam_member_me" {
  project = local.logging_src_project_id
  for_each = toset([
    # required for setting log configurations
    # https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/logging_project_sink
    "roles/logging.configWriter"
  ])
  role   = each.key
  member = "user:${var.ME}"
}

## Exclusion filter
## https://developers.google.com/maps/documentation/transportation-logistics/last-mile-fleet-solution/fleet-performance/fleet-engine/logging#reducing_logging_usage
## https://cloud.google.com/logging/docs/routing/overview#exclusions
resource "google_logging_project_exclusion" "log-exclusion-fleetengine" {
  name        = "log-exclusion-fleetengine"
  project     = local.logging_src_project_id
  description = "Exclude Fleet Engine logs from _Default sink"
  filter      = "resource.type = fleetengine.googleapis.com"
  count       = var.FLAG_SETUP_LOGGING_EXCLUSION ? 1 : 0
}
