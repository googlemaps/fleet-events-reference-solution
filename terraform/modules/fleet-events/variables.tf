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
  description = "Project ID of the project where Fleet Engine (ODRD/LMFS) is enabled."
  nullable    = false
}
variable "PROJECT_FLEETEVENTS" {
  type        = string
  description = "Project ID of the project in which to setup Fleet Events reference solution."
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
  description = "Default GCP region for Cloud resources"
  nullable    = false
  default     = "us-central1"
}

variable "GCP_REGION_FUNCTIONS" {
  type        = string
  description = "Refer to https://cloud.google.com/functions/docs/locations to choose GCP Region for Function deployment. If not set, the value of GCP_REGION will be applied."
  #default     = "us-central1"
  nullable = true
}
variable "GCP_REGION_STORAGE" {
  type        = string
  description = "Refer to https://cloud.google.com/storage/docs/locations to choose GCP Region for Cloud Storage Buckets. If not set, the value of GCP_REGION will be applied."
  #default     = "us-central1"
  nullable = true
}
variable "GCP_REGION_FIRESTORE" {
  type        = string
  description = "Refer to https://cloud.google.com/firestore/docs/locations to choose GCP Region for Firestore. If not set, will use value of GCP_REGION."
  #default     = "nam5"
  nullable = true
}
variable "ME" {
  type        = string
  description = "Account ID (in the form of email address) of the developer running terraform."
  nullable    = false
}

variable "FUNCTION_NAME" {
  type        = string
  description = "Name of the Cloud Function. This will used as the indentifier of the Cloud Function (v2), and has to follow naming rules. Only lower case alphanumeric and '-' allowed."
  default     = "fleetevents-fn"
}
variable "FUNCTION_SRC_DIR" {
  type        = string
  description = "Path to the directory where the Cloud Functions source code is located."
  nullable    = false
}

variable "TOPIC_FLEETEVENTS_OUTPUT" {
  type        = string
  description = "An \"Existing\" Pub/Sub Topic to which the deployed function will be publishing events."
  nullable    = false
  default     = "fleetevents-fn-output"
}
