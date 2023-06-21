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


# fleet engine project
variable "PROJECT_FLEETENGINE" {
  type        = string
  description = "Project with Fleet Engine (ODRD/LMFS) enabled."
  nullable    = false
}


# new project
variable "PROJECT_LOGGINGSYNC_ID" {
  type        = string
  description = "Project id of the new project for Logging"
  nullable    = false
}
variable "PROJECT_LOGGINGSYNC_NAME" {
  type        = string
  description = "Project name (human readable) of the new project for Logging"
  nullable    = false
  default     = "My Logging Project"
}
variable "FOLDER_ID" {
  type        = string
  description = "Folder under which the new project is to be created"
  nullable    = true
}
variable "BILLING_ACCOUNT" {
  type        = string
  description = "Billing Account to which the new project will be associated"
  nullable    = false
}

# other params
variable "ME" {
  type        = string
  description = "user running terraform. will be given access rights to resources."
  nullable    = false
}
variable "GCP_REGION" {
  type        = string
  description = "For resources than can be constrained to sit within a GCP region, a region of preference."
  default     = "asia-southeast1"
}


## reference the existing Fleet Engine enabled project
data "google_project" "project-fleetengine" {
  project_id = var.PROJECT_FLEETENGINE
}


## create new project for logging
resource "google_project" "project-logging" {
  auto_create_network = false
  folder_id           = var.FOLDER_ID
  name                = var.PROJECT_LOGGINGSYNC_NAME
  project_id          = var.PROJECT_LOGGINGSYNC_ID
  billing_account     = var.BILLING_ACCOUNT
  skip_delete         = false
  labels = {
    created_by                  = "terraform"
    fleetengine_project_number = data.google_project.project-fleetengine.number
    fleetengine_project_id     = data.google_project.project-fleetengine.project_id
  }
}

## setup logging
module "logging" {
   source                          = "../../"
  PROJECT_FLEETENGINE             = data.google_project.project-fleetengine.project_id
  PROJECT_LOGGINGSYNC             = google_project.project-logging.project_id
  FLAG_SETUP_LOGGING_LOGGING      = false
  FLAG_SETUP_LOGGING_EXCLUSION    = false
  FLAG_SETUP_LOGGING_PUBSUB       = false
  FLAG_SETUP_LOGGING_CLOUDSTORAGE = false
  FLAG_SETUP_LOGGING_BIGQUERY     = true
  GCP_REGION                      = var.GCP_REGION
  ME                              = var.ME
  depends_on = [
    google_project.project-logging
  ]
}

output "logrouters" {
  description = "details of log routers"
  value       = module.logging.fleetengine-logrouters
}
output "links" {
  description = "direct link to related pages in Cloud Console"
  value       = module.logging.console-links
}