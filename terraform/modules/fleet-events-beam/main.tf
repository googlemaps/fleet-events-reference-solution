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
  TEMPLATE_NAME          = "fleetevents-beam"
  BUCKET                 = format("%s-%s", data.google_project.project_fleetevents.project_id, var.PIPELINE_NAME)
  TEMPLATE_FILE_GCS_PATH = format("gs://%s/templates/%s.json", local.BUCKET, local.TEMPLATE_NAME)
  REPOSITORY_NAME        = local.TEMPLATE_NAME
  PATH_JAR               = format("%s/../../../beam/target/fleetevents-beam-bundled-1.0-SNAPSHOT.jar", path.module)
  IMAGE_GCR_PATH = format(
    "%s-docker.pkg.dev/%s/%s/fleetevents/%s:latest",
    var.GCP_REGION,
    data.google_project.project_fleetevents.project_id,
    local.REPOSITORY_NAME,
    local.TEMPLATE_NAME
  )
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


resource "google_service_account" "sa_app" {
  project      = var.PROJECT_APP
  account_id   = format("sa-%s", var.PIPELINE_NAME)
  display_name = format("'%s' SA - Runtime", var.PIPELINE_NAME)
  description  = format("Service Account used by FleetEvents pipline \"%s\" runtime", var.PIPELINE_NAME)
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
  for_each = toset(
    concat(
      [
        "roles/storage.objectAdmin",
        "roles/viewer",
        "roles/dataflow.worker",
        "roles/dataflow.serviceAgent"
      ],
    var.SA_APP_ROLES)
  )
  role   = each.key
  member = format("serviceAccount:%s", google_service_account.sa_app.email)
}



resource "google_firestore_database" "database" {
  project                     = data.google_project.project_fleetevents.project_id
  name                        = format("%s-%s", var.PIPELINE_NAME, "db")
  location_id                 = var.GCP_REGION
  type                        = "FIRESTORE_NATIVE"
  concurrency_mode            = "PESSIMISTIC"
  app_engine_integration_mode = "DISABLED"
  depends_on = [
    google_project_service.gcp_services["firestore.googleapis.com"]
  ]
}
