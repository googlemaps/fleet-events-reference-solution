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
variable "TOPIC_FLEETEVENTS_OUTPUT" {
  type        = string
  description = "Pub/Sub Topic to which the deployed function will be publishing events."
  nullable    = true
}
variable "FUNCTION_SRC_DIR" {
  type        = string
  description = "Path to the directory where the Cloud Functions source code is located."
  nullable    = false
}
variable "GCP_REGION" {
  type        = string
  description = "For resources than can be constrained run or store data within a GCP region, the default region of preference."
  nullable    = false
  default     = "us-central1"
}
variable "GCP_REGION_FUNCTIONS" {
  type        = string
  description = "GCP Region for Function deployment. If not set, the value of GCP_REGION will be applied. Reference and choose the right one for your deployment https://cloud.google.com/functions/docs/locations"
  default     = "us-central1"
  nullable    = true
}
variable "GCP_REGION_STORAGE" {
  type        = string
  description = "GCP Region for Cloud Storage Buckets.  If not set, the value of GCP_REGION will be applied. Reference and choose the right one for your deployment https://cloud.google.com/storage/docs/locations"
  default     = "us-central1"
  nullable    = true

}
variable "GCP_REGION_FIRESTORE" {
  type        = string
  description = "GCP Region for Firestore.  If not set, will use value of GCP_REGION. Reference and choose the right one for your deployement https://cloud.google.com/firestore/docs/locations"
  default     = "nam5"
  nullable    = true
}
variable "SA_APP_ROLES" {
  type        = list(string)
  description = "Project level IAM Roles the Function's runtime Service Account requires. For example, it might require roles/datastore.user to use Datastore."
  default = [

  ]
}
variable "SA_FLEETENGINE_ROLES" {
  type        = list(string)
  description = "Project level IAM Roles the Function's FleetEngine Service Account requires. If read only, roles such as roles/fleetengine.deliveryFleetReader can be sufficient"
  default = [
    "roles/fleetengine.serviceSuperUser"
  ]
}

variable "FUNCTION_NAME" {
  type        = string
  description = "Name of the Cloud Function. This will used as the indentifier of the Cloud Function (v2), and has to follow naming rules. Only lower case alphanumeric and '-' allowed."
  nullable    = false
}
variable "FUNCTION_DESCRIPTION" {
  type        = string
  description = "Description of the Cloud Function."
  nullable    = true
}
variable "FUNCTION_RUNTIME" {
  type        = string
  description = "Function runtime (java11, java17, etc.)"
  default     = "java11"
}
variable "FUNCTION_ENTRYPOINT" {
  type        = string
  description = "Entry Point of the Cloud Function"
  nullable    = false
}
variable "FUNCTION_ADDITIONAL_ENV_VARS" {
  type        = map(string)
  description = "parameters passed to function as environmental variables."
  nullable    = true
}
variable "FUNCTION_SRC_EXCLUDE_FILES" {
  type        = list(string)
  description = "list of files under source folder to be excluded from source zip."
  default     = []
}
variable "FUNCTION_SRC_EXCLUDE_PATTERNS" {
  type        = list(string)
  description = "list of file patterns under source folder to be excluded from source zip."
  default     = []
}
variable "SUBSCRIPTION_FILTER" {
  type        = string
  description = "filter to be applied to limit messages that reach the function."
  default     = ""
}
variable "FUNCTION_AVAILABLE_MEMORY" {
  type        = string
  description = "The amount of memory available for a function. Supported units are k, M, G, Mi, Gi. If no unit is supplied the value is interpreted as bytes."
  default     = "256M" #128M is too small so setting 128Mi as the least default.
}
variable "FUNCTION_MAX_INSTANCE_COUNT" {
  type        = number
  description = "The limit on the maximum number of function instances that may coexist at a given time."
  default     = 3
}

variable "FUNCTION_MAX_REQUEST_CONCURRENCY" {
  type        = number
  description = "Sets the maximum number of concurrent requests that each instance can receive. only set value >1 when using larger instances (above 1vCPU, equivalent to 2GiB+ memory) https://cloud.google.com/functions/docs/configuring/memory"
  default     = 1
}
