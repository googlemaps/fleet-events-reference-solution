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



# Reference existing projects

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
  project = data.google_project.project_fleetevents.project_id
  for_each = toset([
    "dataflow.googleapis.com",
    "artifactregistry.googleapis.com",
    "bigquery.googleapis.com",
    "cloudbuild.googleapis.com",
    "cloudresourcemanager.googleapis.com",
    "logging.googleapis.com",
    "pubsub.googleapis.com",
    "storage-api.googleapis.com",
    "autoscaling.googleapis.com",
    "firestore.googleapis.com"
  ])
  service = each.key
  # timeouts {
  #   create = "10m"
  #   update = "10m"
  # }
  disable_on_destroy = false
  depends_on = [

  ]
}


# service account under which the pipeline will run
resource "google_service_account" "sa_app" {
  project      = var.PROJECT_APP
  account_id   = format("sa-%s", var.PIPELINE_NAME)
  display_name = format("FleetEvents '%s' runtime SA", var.PIPELINE_NAME)
  description  = format("Service Account under which FleetEvents pipline \"%s\" runs", var.PIPELINE_NAME)
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

# project level roles applied to pipeline SA
resource "google_project_iam_member" "project_iam_sa_app" {
  project = var.PROJECT_APP

  for_each = toset(
    concat(
      # default roles that is required for dataflow pipline to run
      [
        "roles/storage.objectAdmin",
        "roles/viewer",
        "roles/dataflow.worker",
        "roles/dataflow.serviceAgent"
      ],
      # combined with addtional roles that can be specified to reflect any other requirements for pipleine implementation
      var.SA_APP_ROLES
    )
  )
  role   = each.key
  member = format("serviceAccount:%s", google_service_account.sa_app.email)
}



