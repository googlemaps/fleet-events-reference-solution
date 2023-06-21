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


## this is an example for a global deployment, where there probably is a fleetengine project per tennant
## this example assumes you want one single analytics project to centrlize logs
## logs will be routed to BigQuery with one dataset for each corresponding fleetengine region

## reference the existing Fleet Engine enabled projects
data "google_project" "project-fleetengine-us" {
  project_id = var.PROJECT_FLEETENGINE_US
}
data "google_project" "project-fleetengine-eu" {
  project_id = var.PROJECT_FLEETENGINE_EU
}
data "google_project" "project-fleetengine-ap" {
  project_id = var.PROJECT_FLEETENGINE_AP
}

locals {

  BQ_DATASET_PREFIX = "fleetengine_logging"

  regional_projects = {
    us = data.google_project.project-fleetengine-us
    eu = data.google_project.project-fleetengine-eu
    ap = data.google_project.project-fleetengine-ap
  }
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
    created_by                    = "terraform"
    fleetengine_us_project_number = data.google_project.project-fleetengine-us.number
    fleetengine_us_project_id     = data.google_project.project-fleetengine-us.project_id
    fleetengine_eu_project_number = data.google_project.project-fleetengine-eu.number
    fleetengine_eu_project_id     = data.google_project.project-fleetengine-eu.project_id
    fleetengine_ap_project_number = data.google_project.project-fleetengine-ap.number
    fleetengine_ap_project_id     = data.google_project.project-fleetengine-ap.project_id
  }
}

## setup logging

module "logging" {
  source = "../../"

  for_each                        = toset(["us", "eu", "ap"])
  PROJECT_FLEETENGINE             = local.regional_projects[each.key].project_id
  PROJECT_LOGGINGSYNC             = google_project.project-logging.project_id
  FLAG_SETUP_LOGGING_LOGGING      = var.FLAG_SETUP_LOGGING_LOGGING
  FLAG_SETUP_LOGGING_EXCLUSION    = var.FLAG_SETUP_LOGGING_EXCLUSION
  FLAG_SETUP_LOGGING_PUBSUB       = var.FLAG_SETUP_LOGGING_PUBSUB
  FLAG_SETUP_LOGGING_CLOUDSTORAGE = var.FLAG_SETUP_LOGGING_CLOUDSTORAGE
  FLAG_SETUP_LOGGING_BIGQUERY     = var.FLAG_SETUP_LOGGING_BIGQUERY
  BQ_DATASET                      = format("%s_%s", local.BQ_DATASET_PREFIX, each.key)
  GCP_REGION                      = var.GCP_REGION_LOGGING
  ME                              = var.ME
  depends_on = [
    google_project.project-logging
  ]
}

output "logrouters-us" {
  value = module.logging["us"].fleetengine-logrouters
}
output "logrouters-eu" {
  value = module.logging["eu"].fleetengine-logrouters
}
output "logrouters-ap" {
  value = module.logging["ap"].fleetengine-logrouters
}
output "links-us" {
  value = module.logging["us"].console-links
}
output "links-eu" {
  value = module.logging["eu"].console-links
}
output "links-ap" {
  value = module.logging["ap"].console-links
}
