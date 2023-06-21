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
data "google_project" "project" {
  project_id = var.PROJECT_APP
}
data "google_project" "project_fleetengine" {
  project_id = var.PROJECT_FLEETENGINE
}
data "google_project" "project_fleetengine_logs" {
  project_id = var.PROJECT_FLEETENGINE_LOG
}

locals {
  labels_common = {
    "created_by"               = "terraform"
    "fleetevents_function"     = lower(var.FUNCTION_NAME)
    "project_fleetevents"      = var.PROJECT_APP
    "project_fleetengine"      = var.PROJECT_FLEETENGINE
    "project_fleetengine_logs" = var.PROJECT_FLEETENGINE_LOG
  }
}

# enable prereq gcp services
resource "google_project_service" "gcp_services" {
  project = data.google_project.project.project_id
  for_each = toset([
    "pubsub.googleapis.com",
    "cloudbuild.googleapis.com",
    "cloudfunctions.googleapis.com",
    "artifactregistry.googleapis.com",
    "run.googleapis.com",
    "eventarc.googleapis.com",
    "eventarcpublishing.googleapis.com"
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



# reference Pub/Sub topic where FleetEngine logs will be published
data "google_pubsub_topic" "fleetengine-logging-topic" {
  project = data.google_project.project_fleetengine_logs.project_id
  name    = var.TOPIC_FLEETENGINE_LOG
}

# allow SA for Pub/Sub Trigger to subscribe to the FleetEngine logs topic
resource "google_pubsub_topic_iam_member" "member" {
  project = data.google_pubsub_topic.fleetengine-logging-topic.project
  topic   = data.google_pubsub_topic.fleetengine-logging-topic.name
  role    = "roles/pubsub.subscriber"
  member  = format("serviceAccount:%s", google_service_account.sa_trigger.email)
}

# deployment of the main function
# most paramteres are externalized as module variables
resource "google_cloudfunctions2_function" "function" {
  project = var.PROJECT_APP
  name    = var.FUNCTION_NAME

  description = var.FUNCTION_DESCRIPTION
  location    = var.GCP_REGION_FUNCTIONS

  build_config {
    runtime     = var.FUNCTION_RUNTIME
    entry_point = var.FUNCTION_ENTRYPOINT
    source {
      # source code will be staged in Cloud Storage
      storage_source {
        bucket = google_storage_bucket.bucket.name
        object = google_storage_bucket_object.function_source.name
      }
    }
  }

  service_config {
    max_instance_count               = var.FUNCTION_MAX_INSTANCE_COUNT
    available_memory                 = var.FUNCTION_AVAILABLE_MEMORY
    timeout_seconds                  = 60
    max_instance_request_concurrency = var.FUNCTION_MAX_REQUEST_CONCURRENCY
    service_account_email            = google_service_account.sa_app.email
    environment_variables = merge(
      # sets these two by default, which are required for the function to act on events and call FleetEngine APIs
      {
        // required in calling FleetEngine APIs
        PROJECT_FLEETENGINE = var.PROJECT_FLEETENGINE
        // project where the services are deployed
        PROJECT_APP = var.PROJECT_APP
        // required to be able impersonate the FleetEngine SA
        SA_FLEETENGINE = google_service_account.sa_fleetengine.email
        // topic to which messages can be published
        TOPIC_OUTPUT = var.TOPIC_FLEETEVENTS_OUTPUT
        // project in which TOPIC_OUTPUT exists
        PROJECT_OUTPUT = var.PROJECT_APP
      },
      # and any additional given via module variables
      var.FUNCTION_ADDITIONAL_ENV_VARS
    )
    #max_instance_request_concurrency = 5
  }

  # FleetEvents functions will react to FleetEngine logs published to Pub/Sub
  event_trigger {
    trigger_region        = var.GCP_REGION_FUNCTIONS
    event_type            = "google.cloud.pubsub.topic.v1.messagePublished"
    pubsub_topic          = data.google_pubsub_topic.fleetengine-logging-topic.id
    retry_policy          = "RETRY_POLICY_RETRY"
    service_account_email = google_service_account.sa_trigger.email
  }

  labels = merge(
    local.labels_common,
    {
      "source_zip_md5" = data.archive_file.zipfile.output_md5
    }
  )
  depends_on = [
    #time_sleep.wait_60_seconds
    google_project_service.gcp_services,
    google_project_iam_member.project_iam_sa_app
  ]
}

# reference the run service underlying the function
data "google_cloud_run_service" "function_run" {
  project  = google_cloudfunctions2_function.function.project
  location = google_cloudfunctions2_function.function.location
  name     = google_cloudfunctions2_function.function.name
}



