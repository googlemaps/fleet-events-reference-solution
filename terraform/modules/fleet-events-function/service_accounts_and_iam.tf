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


resource "google_service_account" "sa_app" {
  project = var.PROJECT_APP
  #ccount_id   = var.SA_APP_PREFIX
  account_id   = format("sa-app-%s", var.FUNCTION_NAME)
  display_name = format("FleetEvents '%s' SA - App Runtime", var.FUNCTION_NAME)
  description  = format("Service Account used by FleetEvents function %s's App Runtime", var.FUNCTION_NAME)
  #  display_name = "Functions Service Account"
}

resource "google_project_iam_member" "project_iam_sa_app" {
  project  = var.PROJECT_APP
  for_each = toset(var.SA_APP_ROLES)
  role     = each.key
  member   = format("serviceAccount:%s", google_service_account.sa_app.email)
}


resource "google_service_account" "sa_trigger" {
  project      = var.PROJECT_APP
  account_id   = format("sa-trigger-%s", var.FUNCTION_NAME)
  display_name = format("FleetEvents '%s' SA - Trigger", var.FUNCTION_NAME)
  description  = format("Service Account used by FleetEvents function %s's Trigger", var.FUNCTION_NAME)
}

resource "google_cloudfunctions2_function_iam_member" "function_iam" {
  project        = google_cloudfunctions2_function.function.project
  location       = google_cloudfunctions2_function.function.location
  cloud_function = google_cloudfunctions2_function.function.name
  role           = "roles/cloudfunctions.invoker"
  member         = "serviceAccount:${google_service_account.sa_trigger.email}"
}
resource "google_cloud_run_service_iam_member" "run_iam" {
  project  = data.google_cloud_run_service.function_run.project
  location = data.google_cloud_run_service.function_run.location
  service  = data.google_cloud_run_service.function_run.name
  role     = "roles/run.invoker"
  member   = "serviceAccount:${google_service_account.sa_trigger.email}"
}



resource "google_service_account" "sa_fleetengine" {
  project      = var.PROJECT_FLEETENGINE
  account_id   = format("sa-fe-%s", var.FUNCTION_NAME)
  display_name = format("FleetEvents '%s' SA - FleetEngine (%s)", var.FUNCTION_NAME, data.google_project.project.number)
  description  = format("Service Account used by FleetEvents function '%s' (Events project : %s)", var.FUNCTION_NAME, data.google_project.project.number)
}

resource "google_project_iam_member" "project_fleetengine_iam" {
  project  = var.PROJECT_FLEETENGINE
  for_each = toset(var.SA_FLEETENGINE_ROLES)
  role     = each.key
  member   = format("serviceAccount:%s", google_service_account.sa_fleetengine.email)
}


# to allow the Function runtime SA to impersonate the Function FleetEngine SA
resource "google_service_account_iam_binding" "sa_fleetengine_iam" {
  service_account_id = google_service_account.sa_fleetengine.name

  for_each = toset([
    "roles/iam.serviceAccountUser",
    "roles/iam.serviceAccountTokenCreator"
  ])
  role = each.key

  members = [
    #format("user:%s", var.ME),
    format("serviceAccount:%s", google_service_account.sa_app.email),
  ]
}
