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
  BUCKET                 = format("%s-%s", data.google_project.project_fleetevents.project_id, local.TEMPLATE_NAME)
  #BUCKET                 = format("%s-%s", data.google_project.project_fleetevents.project_id, var.PIPELINE_NAME)
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
