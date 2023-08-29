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




# # reference Pub/Sub topic where FleetEngine logs will be published
# data "google_pubsub_topic" "fleetengine-logging-topic" {
#   project = data.google_project.project_fleetengine_logs.project_id
#   name    = var.TOPIC_FLEETENGINE_LOG
# }

# # allow SA for Pub/Sub Trigger to subscribe to the FleetEngine logs topic
# resource "google_pubsub_topic_iam_member" "member" {
#   project = data.google_pubsub_topic.fleetengine-logging-topic.project
#   topic   = data.google_pubsub_topic.fleetengine-logging-topic.name
#   role    = "roles/pubsub.subscriber"
#   member  = format("serviceAccount:%s", google_service_account.sa_trigger.email)
# }


data "google_pubsub_topic" "topic-fleetevents-input" {
  project = data.google_project.project_fleetengine_logs.project_id
  name    = var.TOPIC_FLEETENGINE_LOG
}
output "var_TOPIC_FLEETENGINE_LOG" {
  value = var.TOPIC_FLEETENGINE_LOG
}
resource "google_pubsub_topic_iam_member" "input-subscriber-sa" {
  project = data.google_project.project_fleetevents.project_id
  topic   = data.google_pubsub_topic.topic-fleetevents-input.name
  for_each = toset([
    "roles/pubsub.subscriber"
  ])
  role   = each.value
  member = format("serviceAccount:%s", google_service_account.sa_app.email)
}

resource "google_pubsub_topic" "topic-fleetevents-output" {
  project = data.google_project.project_fleetevents.project_id

  name   = format("fleetevents-output-%s", var.PIPELINE_NAME)
  labels = local.labels_common
}

resource "google_pubsub_topic_iam_member" "output-publisher-sa" {
  project = data.google_project.project_fleetevents.project_id
  topic   = google_pubsub_topic.topic-fleetevents-output.name
  for_each = toset([
    "roles/pubsub.publisher"
  ])
  role   = each.value
  member = format("serviceAccount:%s", google_service_account.sa_app.email)
}

resource "google_storage_bucket" "bucket" {
  project                     = data.google_project.project_fleetevents.project_id
  name                        = local.BUCKET
  location                    = var.GCP_REGION
  uniform_bucket_level_access = true
  public_access_prevention    = "enforced"
}

data "google_storage_bucket_object" "template_spec" {
  name   = format("samples/dataflow/templates/%s.json", local.TEMPLATE_NAME)
  bucket = google_storage_bucket.bucket.id
  #source = "../../../../../beam/fleetevents-beam.json"
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


resource "random_id" "jobname_suffix" {
  byte_length = 4
  keepers = {
    region             = var.GCP_REGION
    topic_id           = data.google_pubsub_topic.topic-fleetevents-input.id
    container_spec_md5 = data.google_storage_bucket_object.template_spec.md5hash

    #subscription_id = google_pubsub_subscription.subscription-fleetenginelogs.id
  }
}

locals {
  TEMPLATE_NAME          = "fleetevents-beam"
  BUCKET                 = format("%s-%s", data.google_project.project_fleetevents.project_id, var.PIPELINE_NAME) #"moritani-sandbox-fleetevents-beam-sg"
  TEMPLATE_FILE_GCS_PATH = format("gs://%s/templates/%s.json", local.BUCKET, local.TEMPLATE_NAME)
  REPOSITORY_NAME        = "fleetevents-docker"
  IMAGE_GCR_PATH = format(
    "%s-docker.pkg.dev/%s/%s/fleetevents/%s:latest",
    var.GCP_REGION,
    data.google_project.project_fleetevents.project_id,
    local.REPOSITORY_NAME,
    local.TEMPLATE_NAME
  )
  PATH_JAR = format("%s/../../../beam/target/fleetevents-beam-bundled-1.0-SNAPSHOT.jar", path.module)

}

resource "google_dataflow_flex_template_job" "beam_job" {
  provider                     = google-beta
  project                      = data.google_project.project_fleetevents.project_id
  name                         = "fleetevents-job-${random_id.jobname_suffix.dec}"
  region                       = var.GCP_REGION
  container_spec_gcs_path      = local.TEMPLATE_FILE_GCS_PATH
  service_account_email        = google_service_account.sa_app.email
  temp_location                = format("%s/temp", google_storage_bucket.bucket.url)
  staging_location             = format("%s/staging", google_storage_bucket.bucket.url)
  skip_wait_on_job_termination = false

  parameters = {

    ## pipeline specific params defined in the json
    functionName       = "TASK_OUTCOME"
    gapSize            = 3
    windowSize         = 3
    datastoreProjectId = var.PROJECT_APP
    inputTopic         = data.google_pubsub_topic.topic-fleetevents-input.id
    outputTopic        = google_pubsub_topic.topic-fleetevents-output.id


    ## generic pipeline options
    # https://beam.apache.org/releases/javadoc/current/index.html?org/apache/beam/sdk/options/PipelineOptions.html
    subnetwork        = format("regions/%s/subnetworks/%s", var.GCP_REGION, google_compute_subnetwork.vpc-subnetwork.name)
    workerMachineType = "e2-standard-2"
    numWorkers        = 1
    maxNumWorkers     = 2
    usePublicIps      = false
  }
  labels = local.labels_common
}




## using the gcloud module for template building until alternative is ready
## dataflow flex-templates build process takes the pipeline code and builds a container image
## it also takes a template spec json, and stores a altered version on GCS.
## the whole process is taken care behind "gcloud dataflow flex-template build" command, 
## but there is no equivalent terraform. 


data "local_file" "pom_xml" {
  filename = format("%s/../../../beam/pom.xml", path.module)
}
data "local_file" "src_pipeline" {
  filename = format("%s/../../../beam/src/main/java/com/google/fleetevents/beam/FleetEventRunner.java", path.module)
}

# data "local_file" "jar" {
#   filename = local.PATH_JAR
#   depends_on = [
#     terraform_data.script_build_jar
#   ]
# }


resource "terraform_data" "script_build_jar" {
  triggers_replace = [
    data.local_file.pom_xml.content_md5,
    data.local_file.src_pipeline.content_md5,
    fileexists(format("%s/../../../beam/target/fleetevents-beam-1.0-SNAPSHOT-shaded.jar", path.module))
  ]
  provisioner "local-exec" {
    #    command     = format("pwd ; echo %s/scripts/scripts.sh tf_buildJar | tee /tmp/out.txt", path.module)
    command     = format("%s/scripts/scripts.sh tf_buildJar %s/../../../beam/pom.xml", path.module, path.module)
    interpreter = ["bash", "-c"]
  }
}
output "script_build_jar" {
  value = terraform_data.script_build_jar
}

resource "terraform_data" "script_build_flex_template" {
  triggers_replace = [
    "aaaa",
    #fileexists(format("%s/../../../beam/target/fleetevents-beam-1.0-SNAPSHOT-shaded.jar", path.module)),
    fileexists(abspath(local.PATH_JAR)),
    #data.local_file.jar.content_md5
  ]
  provisioner "local-exec" {
    #    command     = format("pwd ; echo %s/scripts/scripts.sh tf_buildJar | tee /tmp/out.txt", path.module)
    command = format(
      "pwd; echo ${path.module};  %s/scripts/scripts.sh tf_buildTemplate %s %s %s %s %s",
      path.module,                                        # prepend the script path
      data.google_project.project_fleetevents.project_id, # project_id
      local.TEMPLATE_NAME,                                #template 
      local.TEMPLATE_FILE_GCS_PATH,
      abspath("../../../../../beam/fleetevents-beam.json"),
      abspath(local.PATH_JAR)

    )
    interpreter = ["bash", "-c"]
  }
}


# module "gcloud" {
#   source  = "terraform-google-modules/gcloud/google"
#   version = "~> 2.0"

#   platform              = "darwin"
#   additional_components = ["beta"]
#   skip_download         = true
#   #create_cmd_entrypoint  = "gcloud"
#   create_cmd_body = join(" ", [
#     format("--project=%s", data.google_project.project_fleetevents.project_id),
#     "version"
#   ])
#   #destroy_cmd_entrypoint = "gcloud"
#   #destroy_cmd_body       = "version"
# }

