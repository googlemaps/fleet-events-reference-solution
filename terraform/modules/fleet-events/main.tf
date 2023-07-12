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

# reference existing Fleet Engine project
data "google_project" "project-fleetengine" {
  project_id = var.PROJECT_FLEETENGINE
}

# reference existing Fleet Events project
data "google_project" "project-fleetevents" {
  project_id = var.PROJECT_FLEETEVENTS
}

# reference project where Fleet Engine logs sinks are
data "google_project" "project-fleetengine-logs" {
  project_id = var.PROJECT_FLEETENGINE_LOG
}

# reference existing Topic where FleetEngine logs are published 
data "google_pubsub_topic" "logging_topic" {
  project = data.google_project.project-fleetengine-logs.project_id
  name    = var.TOPIC_FLEETENGINE_LOG
  depends_on = [
    module.logging_config
  ]
}

data "google_pubsub_topic" "events_output_topic" {
  project = data.google_project.project-fleetevents.project_id
  name    = var.TOPIC_FLEETEVENTS_OUTPUT
}

## if there is a service specfic GCP Region of choice, use it. Otherwise, default to GCP_REGION
locals {
  REGION_FUNCTIONS = (var.GCP_REGION_FUNCTIONS != "" && var.GCP_REGION_FUNCTIONS != null) ? var.GCP_REGION_FUNCTIONS : var.GCP_REGION
  REGION_STORAGE   = (var.GCP_REGION_STORAGE != "" && var.GCP_REGION_STORAGE != null) ? var.GCP_REGION_STORAGE : var.GCP_REGION
  REGION_FIRESTORE = (var.GCP_REGION_FIRESTORE != "" && var.GCP_REGION_FIRESTORE != null) ? var.GCP_REGION_FIRESTORE : var.GCP_REGION

}

resource "google_project_service" "firestore" {
  project = data.google_project.project-fleetevents.project_id
  service = "firestore.googleapis.com"

  # Needed for CI tests for permissions to propagate, should not be needed for actual usage
  #depends_on = [time_sleep.wait_60_seconds]
}
resource "google_firestore_database" "database" {
  project     = data.google_project.project-fleetevents.project_id
  name        = "(default)"
  location_id = local.REGION_FIRESTORE
  type        = "FIRESTORE_NATIVE"
  # read this for concurrency modes : https://firebase.google.com/docs/firestore/transaction-data-contention
  concurrency_mode            = "PESSIMISTIC"
  app_engine_integration_mode = "DISABLED"

  depends_on = [
    google_project_service.firestore
  ]
}

# enable prereq gcp services other than firestore
resource "google_project_service" "gcp_services" {
  project = data.google_project.project-fleetevents.project_id
  for_each = toset([

  ])
  service            = each.key
  disable_on_destroy = false
  depends_on = [

  ]
}

## setup logging with "fleetenting-logging-config" module
module "logging_config" {
  #TODO: replace source with link to git repo so that there is no dependency on this module being locally avail
  source                                = "../fleetengine-logging-config/"
  PROJECT_FLEETENGINE                   = data.google_project.project-fleetengine.project_id
  PROJECT_LOGGINGSYNC                   = data.google_project.project-fleetevents.project_id
  FLAG_SETUP_LOGGING_LOGGING            = false
  FLAG_SETUP_LOGGING_EXCLUSION          = false
  FLAG_SETUP_LOGGING_PUBSUB             = true
  PUBSUB_TOPIC_NAME                     = var.TOPIC_FLEETENGINE_LOG
  FLAG_SETUP_LOGGING_PUBSUB_SUB_BQ      = true
  FLAG_SETUP_LOGGING_PUBSUB_SUB_DEFAULT = false
  FLAG_SETUP_LOGGING_CLOUDSTORAGE       = false
  FLAG_SETUP_LOGGING_BIGQUERY           = false
  ME                                    = var.ME
  GCP_REGION                            = var.GCP_REGION
}

## setup fleet events function with "fleet-events-function" module
## configuration will be specific to the bundled "DefaultFleetEventsFunction"
module "func-fleetevents" {
  source                    = "../fleet-events-function/"
  PROJECT_APP               = var.PROJECT_FLEETEVENTS
  GCP_REGION                = var.GCP_REGION
  GCP_REGION_FUNCTIONS      = local.REGION_FUNCTIONS
  GCP_REGION_STORAGE        = local.REGION_STORAGE
  GCP_REGION_FIRESTORE      = local.REGION_FIRESTORE
  FUNCTION_SRC_DIR          = var.FUNCTION_SRC_DIR
  FUNCTION_NAME             = var.FUNCTION_NAME
  FUNCTION_RUNTIME          = "java17"
  FUNCTION_DESCRIPTION      = "Function deployed as Fleet Events Reference Solution"
  FUNCTION_ENTRYPOINT       = "com.google.fleetevents.lmfs.DefaultFleetEventsFunction"
  FUNCTION_AVAILABLE_MEMORY = "256M"
  PROJECT_FLEETENGINE       = data.google_project.project-fleetengine.project_id
  PROJECT_FLEETENGINE_LOG   = data.google_project.project-fleetevents.project_id
  TOPIC_FLEETENGINE_LOG     = data.google_pubsub_topic.logging_topic.name
  TOPIC_FLEETEVENTS_OUTPUT  = data.google_pubsub_topic.events_output_topic.name
  FUNCTION_ADDITIONAL_ENV_VARS = {
    # The env vars are read by FleetEventConfig class 
    # name of the firestore database (this will always be "(default)")
    DATABASE_NAME = google_firestore_database.database.name
    # name of the firestore collection for the delivery vehicle data
    DELIVERY_VEHICLE_COLLECTION_NAME = "deliveryVehicles"
    # name of the firestore collection for the delivery task data
    DELIVERY_TASK_COLLECTION_NAME = "deliveryTasks"
    # project in which the output Topic exists
    FUNCTION_OUTPUT_PROJECT_ID = data.google_pubsub_topic.events_output_topic.project
    # name of output Topic
    FUNCTION_OUTPUT_TOPIC_ID   = data.google_pubsub_topic.events_output_topic.name
  }

  SA_APP_ROLES = [
    "roles/datastore.user",
    "roles/artifactregistry.reader"
  ]
  SA_FLEETENGINE_ROLES = [
    "roles/fleetengine.deliveryFleetReader"
  ]
  FUNCTION_SRC_EXCLUDE_FILES = [
    "README.md",
    "pom.xml.versionsBackup",
    ".DS_Store"
  ]
  FUNCTION_SRC_EXCLUDE_PATTERNS = [
    ".*",
    ".apt_generated_tests/**",
    ".idea/**",
    ".git/**",
    ".terraform/**",
    ".vscode/**",
    "src/test/**",
    "target/**",
    "terraform/**"
  ]
  depends_on = [
    module.logging_config,
    data.google_pubsub_topic.logging_topic
  ]
}

