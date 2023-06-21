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


# fleet engine projects
variable "PROJECT_FLEETENGINE_US" {
  type        = string
  description = "Project with Fleet Engine (ODRD/LMFS) enabled."
  nullable    = false
}
variable "PROJECT_FLEETENGINE_EU" {
  type        = string
  description = "Project with Fleet Engine (ODRD/LMFS) enabled."
  nullable    = false
}
variable "PROJECT_FLEETENGINE_AP" {
  type        = string
  description = "Project with Fleet Engine (ODRD/LMFS) enabled."
  nullable    = false
}

# new project
variable "PROJECT_LOGGINGSYNC_ID" {
  type        = string
  description = "Project id of the new project for Logging"
  nullable    = false
}
variable "PROJECT_LOGGINGSYNC_NAME" {
  type        = string
  description = "Project name (human readable) of the new project for Logging"
  nullable    = false
  default     = "My Logging Project"
}
variable "FOLDER_ID" {
  type        = string
  description = "Folder under which the new project is to be created"
  nullable    = true
}
variable "BILLING_ACCOUNT" {
  type        = string
  description = "Billing Account to which the new project will be associated"
  nullable    = false
}


# other params
variable "ME" {
  type        = string
  description = "user running terraform. will be given access rights to resources."
  nullable    = false
}
variable "GCP_REGION_LOGGING" {
  type        = string
  description = "a region in which logs are to be consolidated."
  default     = "europe-west3"
}