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



resource "google_dataflow_flex_template_job" "beam_job" {
  provider = google-beta
  project  = data.google_project.project_fleetevents.project_id
  name     = "fleetevents-job-${random_id.jobname_suffix.dec}"
  region   = var.GCP_REGION
  #container_spec_gcs_path = local.TEMPLATE_FILE_GCS_PATH
  container_spec_gcs_path = format("gs://%s/templates/%s.json",google_storage_bucket.bucket_template.name,var.TEMPLATE_NAME)
  service_account_email   = google_service_account.sa_app.email
  temp_location           = format("%s/temp", google_storage_bucket.bucket.url)
  staging_location        = format("%s/staging", google_storage_bucket.bucket.url)
  on_delete               = "cancel"
  subnetwork              = format("regions/%s/subnetworks/%s", var.GCP_REGION, google_compute_subnetwork.vpc-subnetwork.name)

  # no longer specifying machine spec as it cannot co-exist with Dataflow prime (autoscaling)
  # machine_type = "e2-standard-2"

  parameters = {

    ## pipeline specific params defined in the json
    functionName       = "TASK_OUTCOME"
    gapSize            = 3
    windowSize         = 3
    datastoreProjectId = var.PROJECT_APP
    databaseId         = google_firestore_database.database.name
    inputTopic         = data.google_pubsub_topic.topic-fleetevents-input.id
    outputTopic        = google_pubsub_topic.topic-fleetevents-output.id


    ## generic pipeline options
    # https://beam.apache.org/releases/javadoc/current/index.html?org/apache/beam/sdk/options/PipelineOptions.html
    # https://cloud.google.com/dataflow/docs/reference/pipeline-options
    #workerMachineType = "e2-standard-2"
    # workerRegion      = var.GCP_REGION
    maxNumWorkers = 2
    usePublicIps  = false
    #update            = true
  }
  labels = local.labels_common
  depends_on = [
    terraform_data.script_build_flex_template,
    google_firestore_database.database, 
    google_storage_bucket.bucket
  ]
}
