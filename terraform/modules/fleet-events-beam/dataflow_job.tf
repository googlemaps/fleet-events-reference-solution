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


# a prefix appended to some resources to make sure the names do not conflict
# this will be regenerated when some of the watched factors have changed
resource "random_id" "jobname_suffix" {
  byte_length = 4

  # change of any of the KVs here will trigger a new jobname suffix to be generated
  keepers = {
    region             = var.GCP_REGION
    topic_input        = data.google_pubsub_topic.topic-fleetevents-input.id
    topic_output       = google_pubsub_topic.topic-fleetevents-output.id
    container_spec_md5 = data.google_storage_bucket_object.template_spec.md5hash
    jar                = terraform_data.script_build_jar.id
    image              = terraform_data.script_build_flex_template.id
  }
}

resource "google_dataflow_flex_template_job" "beam_job" {
  provider  = google-beta
  project   = data.google_project.project_fleetevents.project_id
  name      = format("%s-job-%s", var.PIPELINE_NAME, random_id.jobname_suffix.dec)
  region    = var.GCP_REGION
  on_delete = "cancel"

  ## general pipeline options
  ## although these are not parameters declare directly below the resource type, 
  ## "parameters" take a different style (camel style) for generic parameter names ano also not documented well
  ## therefore using this section which is visible in terraform resource
  container_spec_gcs_path = format("gs://%s/templates/%s.json", google_storage_bucket.bucket_template.name, var.TEMPLATE_NAME)
  service_account_email   = google_service_account.sa_app.email
  temp_location           = format("%s/temp", google_storage_bucket.bucket.url)
  staging_location        = format("%s/staging", google_storage_bucket.bucket.url)
  subnetwork              = format("regions/%s/subnetworks/%s", var.GCP_REGION, google_compute_subnetwork.vpc-subnetwork.name)
  machine_type            = var.DATAFLOW_MACHINE_TYPE
  #max_workers             = var.DATAFLOW_MAX_NUM_WORKERS
  #num_workers             = var.DATAFLOW_NUM_WORKERS

  parameters = {

    ## pipeline specific params defined in the json
    functionName       = var.FLEETEVENTS_FUNCTION_NAME
    gapSize            = var.FLEETEVENTS_GAP_SIZE
    windowSize         = var.FLEETEVENTS_WINDOW_SIZE
    datastoreProjectId = data.google_project.project_fleetevents.project_id
    databaseId         = google_firestore_database.database.name
    inputTopic         = data.google_pubsub_topic.topic-fleetevents-input.id
    outputTopic        = google_pubsub_topic.topic-fleetevents-output.id

    ## additional generic pipeline options
    usePublicIps = var.DATAFLOW_USE_PUBLIC_IPS
    update       = true
  }

  labels = merge(
    local.labels_common,
    {
      topic_input : var.TOPIC_INPUT,
      topic_output : var.TOPIC_OUTPUT
    }
  )

  depends_on = [
    terraform_data.script_build_flex_template,
    google_firestore_database.database,
    google_storage_bucket.bucket
  ]
}



## Cloud Storage

resource "google_storage_bucket" "bucket" {
  project                     = data.google_project.project_fleetevents.project_id
  name                        = format("%s-jobs-%s", data.google_project.project_fleetevents.project_id, var.TEMPLATE_NAME)
  location                    = var.GCP_REGION
  uniform_bucket_level_access = true
  public_access_prevention    = "enforced"
  force_destroy               = true
  labels                      = local.labels_common

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


## setup the VPC network and subnetwork dedicated for dataflow worker nodes

resource "google_compute_network" "vpc-network" {
  project                 = data.google_project.project_fleetevents.project_id
  name                    = format("network-fleetevents-%s", random_id.jobname_suffix.dec)
  routing_mode            = "REGIONAL"
  auto_create_subnetworks = false
  description             = "The Compute Engine network for launching Compute Engine instances to run your pipeline. "

  provisioner "local-exec" {
    when        = destroy
    interpreter = ["bash", "-c"]
    command = format(
      "%s/scripts/scripts.sh deleteFirewallRules %s %s",
      path.module,
      self.project,
      self.name
    )
  }
}

resource "google_compute_subnetwork" "vpc-subnetwork" {
  project                  = data.google_project.project_fleetevents.project_id
  name                     = format("subnet-fleetevents-%s-%s", random_id.jobname_suffix.dec, var.GCP_REGION)
  ip_cidr_range            = "10.2.0.0/16"
  region                   = var.GCP_REGION
  network                  = google_compute_network.vpc-network.id
  private_ip_google_access = true
  description              = "The Compute Engine subnetwork for launching Compute Engine instances to run your pipeline."

  log_config {
    aggregation_interval = "INTERVAL_5_SEC"
    flow_sampling        = 0.5
    metadata             = "INCLUDE_ALL_METADATA"
    metadata_fields      = []
  }
}

## setup firewalls that allow worker nodes to talk to each other

# resource "google_compute_firewall" "firewall_rule_ingress" {
#   project     = data.google_project.project_fleetevents.project_id
#   name        = "dataflow-worker-firewall-rule-ingress"
#   network     = google_compute_network.vpc-network.id
#   description = "Firewall rule for dataflow worker nodes (ingress)"
#   direction   = "INGRESS"
#   priority    = 101
#   allow {
#     protocol = "tcp"
#     ports    = ["12345", "12346"]
#   }
#   log_config {
#     metadata = "INCLUDE_ALL_METADATA"
#   }
#   source_tags = ["dataflow"]
# }
# resource "google_compute_firewall" "firewall_rule_egress" {
#   project     = data.google_project.project_fleetevents.project_id
#   name        = "dataflow-worker-firewall-rule-egress"
#   network     = google_compute_network.vpc-network.id
#   description = "Firewall rule for dataflow worker nodes (egress)"
#   direction   = "EGRESS"
#   priority    = 102
#   allow {
#     protocol = "tcp"
#     ports    = ["12345", "12346"]
#   }
#   log_config {
#     metadata = "INCLUDE_ALL_METADATA"
#   }
#   target_tags = ["dataflow"]
# }

## setup firewalls that allow worker nodes to talk to each other

resource "google_compute_region_network_firewall_policy" "firewall_policy_dataflow" {
  project     = data.google_project.project_fleetevents.project_id
  name        = format("fleetevents-firewall-policy-%s-%s", var.GCP_REGION, random_id.jobname_suffix.dec)
  description = format("Regional network firewall policy for FleetEvents/Dataflow (%s)", var.GCP_REGION)
  region      = var.GCP_REGION
}
resource "google_compute_region_network_firewall_policy_association" "primary" {
  name              = "association"
  attachment_target = google_compute_network.vpc-network.id
  firewall_policy   = google_compute_region_network_firewall_policy.firewall_policy_dataflow.name
  project           = data.google_project.project_fleetevents.project_id
  region            = var.GCP_REGION
}
resource "google_compute_region_network_firewall_policy_rule" "firewall-ingress" {
  project         = data.google_project.project_fleetevents.project_id
  action          = "allow"
  description     = "Ingress firewall rule for FleetEvents/Dataflow"
  direction       = "INGRESS"
  disabled        = false
  enable_logging  = true
  firewall_policy = google_compute_region_network_firewall_policy.firewall_policy_dataflow.name
  priority        = 101
  region          = var.GCP_REGION
  rule_name       = "dataflow-rule-ingress"
  #target_service_accounts = ["my@service-account.com"]

  match {
    src_ip_ranges  = ["0.0.0.0/0"]
    dest_ip_ranges = ["0.0.0.0/0"]

    layer4_configs {
      ip_protocol = "tcp"
      ports       = ["12345", "12346"]
    }
  }
}
resource "google_compute_region_network_firewall_policy_rule" "firewall-egress" {
  project         = data.google_project.project_fleetevents.project_id
  action          = "allow"
  description     = "Egress firewall rule for FleetEvents/Dataflow"
  direction       = "EGRESS"
  disabled        = false
  enable_logging  = true
  firewall_policy = google_compute_region_network_firewall_policy.firewall_policy_dataflow.name
  priority        = google_compute_region_network_firewall_policy_rule.firewall-ingress.priority + 1
  region          = var.GCP_REGION
  rule_name       = "dataflow-rule-egress"
  #target_service_accounts = ["my@service-account.com"]

  match {
    src_ip_ranges = ["0.0.0.0/0"]
    dest_ip_ranges = [
      google_compute_subnetwork.vpc-subnetwork.ip_cidr_range
    ]

    layer4_configs {
      ip_protocol = "tcp"
      ports       = ["12345", "12346"]
    }
  }
}
