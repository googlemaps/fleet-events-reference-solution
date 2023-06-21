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

output "projects" {
  description = "Cloud Projects in use"
  value = {
    app              = data.google_project.project
    fleetengine      = data.google_project.project_fleetengine
    fleetengine_logs = data.google_project.project_fleetengine_logs
  }
}

output "serviceaccounts" {
  description = "Service accounts in use"
  value = {
    app         = google_service_account.sa_app
    trigger     = google_service_account.sa_trigger
    fleetengine = google_service_account.sa_fleetengine
  }
}
output "function" {
  description = "Deployed Cloud Function"
  value       = google_cloudfunctions2_function.function
}
# output "run" {
#  description = "Cloud Run beneath Function v2 "
#   value = data.google_cloud_run_service.function_run
# }


output "source" {
  description = "Zipped source files"
  value       = data.archive_file.zipfile
}
