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

output "topic-fleetevents-input" {
  value = data.google_pubsub_topic.topic-fleetevents-input
}
output "topic-fleetevents-output" {
  value = google_pubsub_topic.topic-fleetevents-output
}
# output "subscription_fleetenginelogs" {
#   value = google_pubsub_subscription.subscription-fleetenginelogs
# }
output "vpc_network" {
    value=google_compute_network.vpc-network
}
output "vpc-subnetwork"{
    value=google_compute_subnetwork.vpc-subnetwork
}

output "bucket_jobs" {
    value=google_storage_bucket.bucket
}
output "bucket_template" {
    value=google_storage_bucket.bucket_template
}
output "container_spec" {
    value=data.google_storage_bucket_object.template_spec
}
output "script_build_jar" {
  value = terraform_data.script_build_jar
}
