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
variable "PROJECT_FLEETENGINE_LOG" {
  type        = string
  description = "Project ID of the project where Fleet Engine logs are persisted. (LogSink of Cloud Logging settings.)"
  nullable    = false
}
variable "TOPIC_INPUT" {
  type        = string
  description = "Pub/Sub Topic to which Fleet Engine logs are published following Cloud Logging setup."
  nullable    = false
}
variable "TOPIC_OUTPUT" {
  type        = string
  description = "Pub/Sub Topic to which FleetEvents pipeline will publish events."
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
variable "SA_APP_ROLES" {
  type        = list(string)
  description = "Project level IAM Roles the pipeline runtime Service Account requires. For example, it might require roles/datastore.user to use Datastore."
  default     = []
}
variable "SA_FLEETENGINE_ROLES" {
  type        = list(string)
  description = "Project level IAM Roles the pipeline's FleetEngine Service Account requires. If read only, roles such as roles/fleetengine.deliveryFleetReader can be sufficient"
  default = [
    "roles/fleetengine.deliveryFleetReader"
  ]
}

variable "PIPELINE_NAME" {
  type        = string
  description = "Name of the Beam Pipeline. This will become the prefix of the Dataflow/Beam pipeline Job."
  nullable    = false
  default     = "fleetevents-pipeline"
}
variable "PIPELINE_CLASS" {
  type        = string
  nullable    = false
  description = "The Java class of the Beam pipeline"
  default     = "com.google.fleetevents.beam.FleetEventRunner"
}
variable "TEMPLATE_NAME" {
  type        = string
  description = "Dataflow Flex template name"
  nullable    = false
  default     = "fleetevents-beam"
}

variable "DATABASE_NAME" {
  type        = string
  description = "Firestore database instance name"
  nullable    = false
  default     = "fleetevents-db"
}

variable "ME" {
  type        = string
  description = "Account ID (in the form of email address) of the developer running terraform."
  nullable    = false
}

variable "FLAG_SETUP_PUBSUB_SUB_BQ" {
  type        = bool
  description = "whether to setup a subscription for the Pub/Sub topic"
  nullable    = false
  default     = true
}

# details about various dataflow level pipeline options can be referenced here
# https://cloud.google.com/dataflow/docs/reference/pipeline-options

variable "DATAFLOW_MACHINE_TYPE" {
  type        = string
  description = "The Compute Engine machine type that Dataflow uses when starting worker VMs. You can use any available Compute Engine machine type families or custom machine types."
  default     = "n1-standard-4"
}

variable "DATAFLOW_USE_PUBLIC_IPS" {
  type        = bool
  description = "Specifies whether Dataflow workers use external IP addresses. If the value is set to false, Dataflow workers use internal IP addresses for all communication."
  default     = false
}

variable "DATAFLOW_NUM_WORKERS" {
  type        = number
  description = "The initial number of Compute Engine instances to use when executing your pipeline. This option determines how many workers the Dataflow service starts up when your job begins."
  default     = 1
}

variable "DATAFLOW_MAX_NUM_WORKERS" {
  type        = number
  description = "The maximum number of Compute Engine instances to be made available to your pipeline during execution. This value can be higher than the initial number of workers (specified by numWorkers) to allow your job to scale up, automatically or otherwise."
  default     = 3
}


## Fleetevents pipeline specific options

variable "FLEETEVENTS_FUNCTION_NAME" {
  type        = string
  description = "sample function to run."
  default     = "TASK_OUTCOME"
}

variable "FLEETEVENTS_GAP_SIZE" {
  type        = number
  description = "How long to wait (in minutes) before considering a Vehicle to be offline."
  default     = 3
}

variable "FLEETEVENTS_WINDOW_SIZE" {
  type        = number
  description = "Window size to use to process events, in minutes. This parameter does not apply to VEHICLE_OFFLINE jobs."
  default     = 3
}
