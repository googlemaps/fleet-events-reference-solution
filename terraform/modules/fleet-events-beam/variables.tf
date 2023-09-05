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

variable "PROJECT_APP" {
  type        = string
  description = "The project to setup resources"
  nullable    = false
}
variable "PROJECT_FLEETENGINE" {
  type        = string
  description = "Project ID of the project where Fleet Engine (ODRD/LMFS) is enabled."
  nullable    = false
}
variable "PROJECT_FLEETENGINE_LOG" {
  type        = string
  description = "Project ID of the project where Fleet Engine logs are persisted. (LogSink of Cloud Logging settings.)"
  nullable    = false
}
variable "TOPIC_FLEETENGINE_LOG" {
  type        = string
  description = "Pub/Sub Topic to which Fleet Engine logs are published following Cloud Logging setup."
  nullable    = false
}
variable "GCP_REGION" {
  type        = string
  description = "For resources than can be constrained run or store data within a GCP region, the default region of preference."
  nullable    = false
  default     = "us-central1"
}
variable "SA_APP_ROLES" {
  type        = list(string)
  description = "Project level IAM Roles the Function's runtime Service Account requires. For example, it might require roles/datastore.user to use Datastore."
  default = [

  ]
}
# variable "SA_FLEETENGINE_ROLES" {
#   type        = list(string)
#   description = "Project level IAM Roles the Function's FleetEngine Service Account requires. If read only, roles such as roles/fleetengine.deliveryFleetReader can be sufficient"
#   default = [
#     "roles/fleetengine.serviceSuperUser"
#   ]
# }

variable "PIPELINE_NAME" {
  type        = string
  description = "Name of the Beam Pipeline."
  nullable    = false
}

variable "PIPELINE_CLASS" {
  type     = string
  nullable = false
  default  = "com.google.fleetevents.beam.FleetEventRunner"

}

variable "ME" {
  type        = string
  description = "Account ID (in the form of email address) of the developer running terraform."
  nullable    = false
}

variable "FLAG_SETUP_LOGGING_PUBSUB_SUB_BQ" {
  type        = bool
  description = "whether to setup a subscription for the Pub/Sub topic"
  nullable    = false
  default     = true
}
