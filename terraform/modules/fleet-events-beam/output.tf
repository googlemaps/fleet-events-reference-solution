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

output "network" {
  value = {
    network    = google_compute_network.vpc-network
    subnetwork = google_compute_subnetwork.vpc-subnetwork
    # ingress    = google_compute_firewall.firewall_rule_ingress
    # egress     = google_compute_firewall.firewall_rule_egress
  
  }
}
output "pipeline" {
  value = {
    bucket_jobs      = google_storage_bucket.bucket
    bucket_template  = google_storage_bucket.bucket_template
    container_spec   = data.google_storage_bucket_object.template_spec
    script_build_jar = terraform_data.script_build_jar
    topic_input      = data.google_pubsub_topic.topic-fleetevents-input
    topic_output     = google_pubsub_topic.topic-fleetevents-output
  }
}
