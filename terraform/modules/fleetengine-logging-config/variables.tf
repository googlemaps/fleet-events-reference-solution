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
  description = "The project with Fleet Engine (ODRD/LMFS) enabled."
  nullable    = false
}
variable "PROJECT_LOGGINGSYNC" {
  type        = string
  description = "The project where the Logging data will be persisted"
  nullable    = false
}
variable "RETENTION" {
  type        = number
  description = "Data retention period in days. Note that retention <= 30 days is required for storage of Maps data"
  default     = 30
  nullable    = false
}

variable "GCP_REGION" {
  type        = string
  description = "For resources than can be constrained to sit within a GCP region, a region of preference."
  nullable    = false
  default     = "asia-southeast1"
}
variable "GCP_ZONE" {
  type        = string
  description = "For resources than can be constrained to sit within a GCP zone, a zone of preference. This should be one of the zones in the selected GCP_REGION"
  nullable    = false
  default     = "asia-southeast1-b"
}

variable "LOG_FILTER" {
  type        = string
  description = "Filter applied to capture log events and send to sinks"
  nullable    = false
  default     = <<EOF
(
 resource.type="audited_resource"
 resource.labels.service="fleetengine.googleapis.com"
 resource.labels.method="maps.fleetengine.v1.TripService.ReportBillableTrip"
) 
OR resource.type="fleetengine.googleapis.com/Fleet"
OR resource.type="fleetengine.googleapis.com/DeliveryFleet"
EOF
}

variable "BQ_DATASET" {
  type        = string
  description = "Name of BigQuery dataset to store logs"
  nullable    = false
  default     = "fleetengine_logging"
}

variable "PUBSUB_TOPIC_NAME" {
  type        = string
  description = "Name of Pub/Sub topic to publish log events"
  default     = "fleetengine-logging-topic"
}
variable "FLAG_SETUP_LOGGING_LOGGING" {
  type        = bool
  description = "whether to setup Logging or not for Logging Buckets"
  nullable    = false
  default     = false
}
variable "FLAG_SETUP_LOGGING_BIGQUERY" {
  type        = bool
  description = "whether to setup Logging or not for BigQuery"
  nullable    = false
  default     = true
}
variable "FLAG_SETUP_LOGGING_PUBSUB" {
  type        = bool
  description = "whether to setup Logging or not for Pub/Sub"
  nullable    = false
  default     = false
}
variable "FLAG_SETUP_LOGGING_PUBSUB_SUB_DEFAULT" {
  type        = bool
  description = "whether to setup a subscription for the Pub/Sub topic"
  nullable    = false
  default     = false
}
variable "FLAG_SETUP_LOGGING_PUBSUB_SUB_BQ" {
  type        = bool
  description = "whether to setup a push BigQuery subscription for the Pub/Sub topic"
  nullable    = false
  default     = false
}
variable "FLAG_SETUP_LOGGING_CLOUDSTORAGE" {
  type        = bool
  description = "whether to setup Logging or not for Cloud Storage Buckets"
  nullable    = false
  default     = false
}

variable "FLAG_SETUP_LOGGING_EXCLUSION" {
  type        = bool
  description = "whether to setup Logging exclusion for _Default"
  nullable    = false
  default     = false
}

variable "ME" {
  type        = string
  description = "user account running terraform"
  nullable    = false
}
