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
    "created_by"               = "terraform"
    "fleetevents_pipeline"     = lower(var.PIPELINE_NAME)
    "project_fleetevents"      = var.PROJECT_APP
    "project_fleetengine"      = var.PROJECT_FLEETENGINE
    "project_fleetengine_logs" = var.PROJECT_FLEETENGINE_LOG
  }
}


# Reference existing projects
data "google_project" "project" {
  project_id = var.PROJECT_APP
}
data "google_project" "project_fleetengine" {
  project_id = var.PROJECT_FLEETENGINE
}
data "google_project" "project_fleetengine_logs" {
  project_id = var.PROJECT_FLEETENGINE_LOG
}
data "google_project" "project_fleetevents" {
  project_id = var.PROJECT_APP
}


# enable prereq gcp services
resource "google_project_service" "gcp_services" {
  project = data.google_project.project.project_id
  for_each = toset([
    "dataflow.googleapis.com",
  ])
  service = each.key
  timeouts {
    create = "10m"
    update = "10m"
  }
  disable_on_destroy = false
  depends_on = [

  ]
}


resource "google_service_account" "sa_app" {
  project      = var.PROJECT_APP
  account_id   = format("sa-%s", var.PIPELINE_NAME)
  display_name = format("'%s' SA - Runtime", var.PIPELINE_NAME)
  description  = format("Service Account used by FleetEvents pipline %s's Runtime", var.PIPELINE_NAME)
  #  display_name = "Functions Service Account"
}

resource "google_service_account_iam_binding" "sa_app_iam" {
  service_account_id = google_service_account.sa_app.id

  for_each = toset([
    "roles/iam.serviceAccountUser",
    "roles/iam.serviceAccountTokenCreator"
  ])
  role = each.key

  members = [
    format("user:%s", var.ME),
    #format("serviceAccount:%s", google_service_account.sa_app.email),
  ]
}

resource "google_project_iam_member" "project_iam_sa_app" {
  project = var.PROJECT_APP
  for_each = toset([
    "roles/storage.objectAdmin",
    "roles/viewer",
    "roles/dataflow.worker",
    "roles/dataflow.serviceAgent"
  ])
  role   = each.key
  member = format("serviceAccount:%s", google_service_account.sa_app.email)
}

