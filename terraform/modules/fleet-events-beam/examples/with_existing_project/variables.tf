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

variable "PROJECT_FLEETEVENTS" {
  type        = string
  description = "The project to setup resources"
  nullable    = false
}
variable "PROJECT_FLEETENGINE" {
  type        = string
  description = "Project ID of the project where Fleet Engine (ODRD/LMFS) is enabled."
  nullable    = false
}
variable "GCP_REGION" {
  type        = string
  description = "For resources than can be constrained run or store data within a GCP region, the default region of preference."
  nullable    = false
  default     = "us-central1"
}
variable "GCP_REGION_FIRESTORE" {
  type        = string
  description = "Refer to https://cloud.google.com/firestore/docs/locations to choose GCP Region for Firestore. If not set, will use value of GCP_REGION."
  #default     = "nam5"
  nullable = true
}

variable "FLEETEVENTS_FUNCTION_NAME" {
  type = string
  description = "Fleet Event function that you'd like to run. Refer to beam/README.md for possible values."
  default = "TASK_OUTCOME_CHANGE"
  nullable = false
}


variable "PIPELINE_NAME" {
  type        = string
  description = "Name of the Beam Pipeline. This will become the prefix of the Dataflow/Beam pipeline Job."
  nullable    = false
  default     = "fleetevents-pipeline"
}

variable "ME" {
  type        = string
  description = "Account ID (in the form of email address) of the developer running terraform."
  nullable    = false
}
