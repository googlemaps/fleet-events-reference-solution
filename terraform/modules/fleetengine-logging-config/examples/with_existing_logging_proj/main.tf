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


variable "PROJECT_FLEETENGINE" {
  type        = string
  description = "Project with Fleet Engine (ODRD/LMFS) enabled."
  nullable    = false
}
variable "PROJECT_LOGGINGSYNC" {
  type        = string
  description = "Project id of the new project for Fleet Events"
  nullable    = false
}
variable "ME" {
  type        = string
  description = "user running terraform. will be given access rights to resources."
  nullable    = false
}
variable "GCP_REGION" {
  type        = string
  description = "For resources than can be constrained to sit within a GCP region, a region of preference."
  default     = "asia-southeast1"
}

module "logging" {
  source                          = "../../"
  PROJECT_FLEETENGINE             = var.PROJECT_FLEETENGINE
  PROJECT_LOGGINGSYNC             = var.PROJECT_LOGGINGSYNC
  FLAG_SETUP_LOGGING_LOGGING      = false
  FLAG_SETUP_LOGGING_EXCLUSION    = true
  FLAG_SETUP_LOGGING_PUBSUB       = false
  FLAG_SETUP_LOGGING_CLOUDSTORAGE = false
  FLAG_SETUP_LOGGING_BIGQUERY     = true
  GCP_REGION                      = var.GCP_REGION
  ME                              = var.ME
}

output "logrouters" {
  description = "details of log routers"
  value       = module.logging.fleetengine-logrouters
}
output "links" {
  description = "direct link to related pages in Cloud Console"
  value       = module.logging.console-links
}
