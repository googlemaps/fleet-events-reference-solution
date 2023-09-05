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



resource "random_id" "jobname_suffix" {
  byte_length = 4
  keepers = {
    region             = var.GCP_REGION
    topic_id           = data.google_pubsub_topic.topic-fleetevents-input.id
    container_spec_md5 = data.google_storage_bucket_object.template_spec.md5hash
    jar                = terraform_data.script_build_jar.id
    image              = terraform_data.script_build_flex_template.id
    #subscription_id = google_pubsub_subscription.subscription-fleetenginelogs.id
  }
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
    "aaa",
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
    "aaa",
    #fileexists(format("%s/../../../beam/target/fleetevents-beam-1.0-SNAPSHOT-shaded.jar", path.module)),
    fileexists(abspath(local.PATH_JAR)),
    #data.local_file.jar.content_md5
  ]
  provisioner "local-exec" {
    #    command     = format("pwd ; echo %s/scripts/scripts.sh tf_buildJar | tee /tmp/out.txt", path.module)
    command = format(
      "pwd; echo ${path.module};  %s/scripts/scripts.sh tf_buildTemplate %s %s %s %s %s %s",
      path.module,                                        # prepend the script path
      data.google_project.project_fleetevents.project_id, # project_id
      local.TEMPLATE_NAME,                                #template 
      local.TEMPLATE_FILE_GCS_PATH,
      abspath("../../../../../beam/fleetevents-beam.json"),
      abspath(local.PATH_JAR),
      google_artifact_registry_repository.repo.name
    )
    interpreter = ["bash", "-c"]
  }
  depends_on = [terraform_data.script_build_jar]
}



resource "google_dataflow_flex_template_job" "beam_job" {
  provider                = google-beta
  project                 = data.google_project.project_fleetevents.project_id
  name                    = "fleetevents-job-${random_id.jobname_suffix.dec}"
  region                  = var.GCP_REGION
  container_spec_gcs_path = local.TEMPLATE_FILE_GCS_PATH
  service_account_email   = google_service_account.sa_app.email
  temp_location           = format("%s/temp", google_storage_bucket.bucket.url)
  staging_location        = format("%s/staging", google_storage_bucket.bucket.url)
  #skip_wait_on_job_termination = true
  on_delete = "cancel"
  # max_workers = 2
  subnetwork = format("regions/%s/subnetworks/%s", var.GCP_REGION, google_compute_subnetwork.vpc-subnetwork.name)
  # no longer specifying machine spec as it cannot co-exist with Dataflow prime (autoscaling)
  # machine_type = "e2-standard-2"
  parameters = {

    ## pipeline specific params defined in the json
    functionName       = "TASK_OUTCOME"
    gapSize            = 3
    windowSize         = 3
    datastoreProjectId = var.PROJECT_APP
    databaseId         = google_firestore_database.database.name
    inputTopic         = data.google_pubsub_topic.topic-fleetevents-input.id
    outputTopic        = google_pubsub_topic.topic-fleetevents-output.id


    ## generic pipeline options
    # https://beam.apache.org/releases/javadoc/current/index.html?org/apache/beam/sdk/options/PipelineOptions.html
    # https://cloud.google.com/dataflow/docs/reference/pipeline-options
    #workerMachineType = "e2-standard-2"
    # workerRegion      = var.GCP_REGION
    maxNumWorkers = 2
    usePublicIps  = false
    #update            = true
  }
  labels = local.labels_common
  depends_on = [
    terraform_data.script_build_flex_template
  ]
}
