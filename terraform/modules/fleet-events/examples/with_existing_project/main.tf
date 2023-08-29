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


locals {
  labels_common = {
    "created_by"             = "terraform"
    "function_fleetevents"   = lower(var.FUNCTION_NAME)
    "project_fleetevents_id" = var.PROJECT_FLEETEVENTS
    "project_fleetengine_id" = var.PROJECT_FLEETENGINE

  }
}

# enable prerequisite services
resource "google_project_service" "gcp_services" {
  project = data.google_project.project-fleetevents.project_id
  for_each = toset([
    "pubsub.googleapis.com"
  ])
  timeouts {
    create = "10m"
    update = "10m"
  }
  service            = each.key
  disable_on_destroy = false
  depends_on = [

  ]
}


# reference existing project for Fleet Events
data "google_project" "project-fleetevents" {
  project_id = var.PROJECT_FLEETEVENTS
}

# reference existing project for Fleet Engine (ODRD/LMFS)
data "google_project" "project-fleetengine" {
  project_id = var.PROJECT_FLEETENGINE
}

# utilize Fleet Events module to set up reference solution
module "fleet-events" {
  source                   = "../../"
  PROJECT_FLEETENGINE      = data.google_project.project-fleetengine.project_id
  PROJECT_FLEETEVENTS      = data.google_project.project-fleetevents.project_id
  PROJECT_FLEETENGINE_LOG  = data.google_project.project-fleetevents.project_id
  GCP_REGION               = var.GCP_REGION
  GCP_REGION_FIRESTORE     = var.GCP_REGION_FIRESTORE
  GCP_REGION_STORAGE       = null
  GCP_REGION_FUNCTIONS     = null
  ME                       = var.ME
  FUNCTION_SRC_DIR         = abspath(var.FUNCTION_SRC_DIR)
  FUNCTION_NAME            = var.FUNCTION_NAME
  TOPIC_FLEETENGINE_LOG    = var.TOPIC_FLEETENGINE_LOG
  TOPIC_FLEETEVENTS_OUTPUT = google_pubsub_topic.output_topic.name
  MOBILITY_SOLUTION        = var.MOBILITY_SOLUTION
}




output "logging_config" {
  description = "details of logging config"
  value       = module.fleet-events.logging_config

}
output "function" {
  description = "defails of function deployed"
  value       = module.fleet-events.function
}
