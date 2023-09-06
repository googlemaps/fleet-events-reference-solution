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



## Cloud Storage

resource "google_storage_bucket" "bucket" {
  project                     = data.google_project.project_fleetevents.project_id
  name                        = local.BUCKET
  location                    = var.GCP_REGION
  uniform_bucket_level_access = true
  public_access_prevention    = "enforced"
  force_destroy               = true
}

resource "google_storage_bucket_iam_member" "bucket_iam_me" {
  bucket = google_storage_bucket.bucket.name
  for_each = toset([
    "roles/storage.admin"
  ])
  role   = each.key
  member = format("user:%s", var.ME)
}
resource "google_storage_bucket_iam_member" "bucket_iam_sa" {
  bucket = google_storage_bucket.bucket.name
  for_each = toset([
    "roles/storage.admin"
  ])
  role   = each.key
  member = format("serviceAccount:%s", google_service_account.sa_app.email)
}
resource "google_storage_bucket_iam_member" "bucket_iam_sa_build" {
  bucket = google_storage_bucket.bucket.name
  for_each = toset([
    "roles/storage.admin"
  ])
  role   = each.key
  member = format("serviceAccount:%s@cloudbuild.gserviceaccount.com", data.google_project.project_fleetevents.number)
}


## setup the VPC network and subnetwork dedicated for dataflow worker nodes

resource "google_compute_network" "vpc-network" {
  project                 = data.google_project.project_fleetevents.project_id
  name                    = "network-fleetevents"
  routing_mode            = "REGIONAL"
  auto_create_subnetworks = false
  description             = "VPC to run dataflow worker nodes"
}

resource "google_compute_subnetwork" "vpc-subnetwork" {
  project                  = data.google_project.project_fleetevents.project_id
  name                     = format("subnet-fleetevents-%s", var.GCP_REGION)
  ip_cidr_range            = "10.2.0.0/16"
  region                   = var.GCP_REGION
  network                  = google_compute_network.vpc-network.id
  private_ip_google_access = true

  log_config {
    aggregation_interval = "INTERVAL_5_SEC"
    flow_sampling        = 0.5
    metadata             = "INCLUDE_ALL_METADATA"
    metadata_fields      = []
  }
}

## setup firewalls that allow worker nodes to talk to each other

resource "google_compute_firewall" "firewall_rule_ingress" {
  project     = data.google_project.project_fleetevents.project_id
  name        = "dataflow-worker-firewall-rule-ingress"
  network     = google_compute_network.vpc-network.id
  description = "Firewall rule for dataflow worker notes (ingress)"
  direction   = "INGRESS"
  priority    = 101
  allow {
    protocol = "tcp"
    ports    = ["12345", "12346"]
  }
  log_config {
    metadata = "INCLUDE_ALL_METADATA"
  }
  source_tags = ["dataflow"]
}
resource "google_compute_firewall" "firewall_rule_egress" {
  project     = data.google_project.project_fleetevents.project_id
  name        = "dataflow-worker-firewall-rule-egress"
  network     = google_compute_network.vpc-network.id
  description = "Firewall rule for dataflow worker notes (egress)"
  direction   = "EGRESS"
  priority    = 102
  allow {
    protocol = "tcp"
    ports    = ["12345", "12346"]
  }
  log_config {
    metadata = "INCLUDE_ALL_METADATA"
  }
  target_tags = ["dataflow"]
}

## setup firewalls that allow worker nodes to talk to each other

# resource "google_compute_region_network_firewall_policy" "firewall_policy_dataflow" {
#   project     = data.google_project.project_fleetevents.project_id
#   name        = format("fleetevents-firewall-policy-%s", var.GCP_REGION)
#   description = format("Regional network firewall policy for FleetEvents/Dataflow (%s)", var.GCP_REGION)
#   region      = var.GCP_REGION
# }
# resource "google_compute_region_network_firewall_policy_association" "primary" {
#   name              = "association"
#   attachment_target = google_compute_network.vpc-network.id
#   firewall_policy   = google_compute_region_network_firewall_policy.firewall_policy_dataflow.name
#   project           = data.google_project.project_fleetevents.project_id
#   region            = var.GCP_REGION
# }
# resource "google_compute_region_network_firewall_policy_rule" "firewall-ingress" {
#   project         = data.google_project.project_fleetevents.project_id
#   action          = "allow"
#   description     = "Ingress firewall rule for FleetEvents/Dataflow"
#   direction       = "INGRESS"
#   disabled        = false
#   enable_logging  = true
#   firewall_policy = google_compute_region_network_firewall_policy.firewall_policy_dataflow.name
#   priority        = 101
#   region          = var.GCP_REGION
#   rule_name       = "dataflow-rule-ingress"
#   #target_service_accounts = ["my@service-account.com"]

#   match {
#     src_ip_ranges  = ["0.0.0.0/0"]
#     dest_ip_ranges = ["0.0.0.0/0"]

#     layer4_configs {
#       ip_protocol = "tcp"
#       ports       = ["12345", "12346"]
#     }
#   }
# }
# resource "google_compute_region_network_firewall_policy_rule" "firewall-egress" {
#   project         = data.google_project.project_fleetevents.project_id
#   action          = "allow"
#   description     = "Egress firewall rule for FleetEvents/Dataflow"
#   direction       = "EGRESS"
#   disabled        = false
#   enable_logging  = true
#   firewall_policy = google_compute_region_network_firewall_policy.firewall_policy_dataflow.name
#   priority        = google_compute_region_network_firewall_policy_rule.firewall-ingress.priority + 1
#   region          = var.GCP_REGION
#   rule_name       = "dataflow-rule-egress"
#   #target_service_accounts = ["my@service-account.com"]

#   match {
#     src_ip_ranges = ["0.0.0.0/0"]
#     dest_ip_ranges = [
#       google_compute_subnetwork.vpc-subnetwork.ip_cidr_range
#     ]

#     layer4_configs {
#       ip_protocol = "tcp"
#       ports       = ["12345", "12346"]
#     }
#   }
# }


## setting up a repository for containerized pipeline images

resource "google_artifact_registry_repository" "repo" {
  project       = data.google_project.project_fleetevents.project_id
  location      = var.GCP_REGION
  repository_id = var.TEMPLATE_NAME
  description   = format("Repository for FleetEvents flex-template images for template \"%s\"", var.TEMPLATE_NAME)
  format        = "DOCKER"
  labels        = local.labels_common
}
