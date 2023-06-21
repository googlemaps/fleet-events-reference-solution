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


# create new Fleet Events project
resource "google_project" "project-fleetevents" {
  auto_create_network = false
  folder_id           = var.FOLDER_ID
  project_id          = var.PROJECT_FLEETEVENTS
  name                = var.PROJECT_FLEETEVENTS_NAME
  billing_account     = var.BILLING_ACCOUNT
  skip_delete         = false
  labels = {
    createdby                  = "terraform"
    fleetengine_project_number = data.google_project.project-fleetengine.number
    fleetengine_project_id     = data.google_project.project-fleetengine.project_id
  }
}

# referencing existing project with Fleeet Engine enabled
data "google_project" "project-fleetengine" {
  project_id = var.PROJECT_FLEETENGINE
}


# Using "with-existing-project' example as a module, passing information for new project. 

module "fleet-events-with-existing-project" {
  source                           = "../with_existing_project/"
  PROJECT_FLEETENGINE              = data.google_project.project-fleetengine.project_id
  PROJECT_FLEETEVENTS              = var.PROJECT_FLEETEVENTS
  PROJECT_FLEETENGINE_LOG          = var.PROJECT_FLEETENGINE_LOG
  TOPIC_FLEETENGINE_LOG            = var.TOPIC_FLEETENGINE_LOG
  GCP_REGION                       = var.GCP_REGION
  GCP_REGION_FIRESTORE             = var.GCP_REGION_FIRESTORE
  ME                               = var.ME
  FUNCTION_SRC_DIR                 = abspath(var.FUNCTION_SRC_DIR)
  FUNCTION_NAME                    = var.FUNCTION_NAME
  TOPIC_FLEETEVENTS_OUTPUT         = format("%s-output", lower(var.FUNCTION_NAME))
  FLAG_SETUP_BIGQUERY_SUBSCRIPTION = var.FLAG_SETUP_BIGQUERY_SUBSCRIPTION
  depends_on = [
    google_project.project-fleetevents
  ]
}



output "logging_config" {
  description = "details of logging config"
  value       = module.fleet-events-with-existing-project.logging_config
}
output "function" {
  description = "defails of function deployed"
  value       = module.fleet-events-with-existing-project.function
}
