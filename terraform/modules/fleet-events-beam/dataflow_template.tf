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


resource "random_id" "template_suffix" {
  byte_length = 4

}


## Cloud Storage for flex-template

resource "google_storage_bucket" "bucket_template" {
  project                     = data.google_project.project_fleetevents.project_id
  name                        = format("%s-flextemplate-%s", data.google_project.project_fleetevents.project_id, var.TEMPLATE_NAME)
  location                    = var.GCP_REGION
  uniform_bucket_level_access = true
  public_access_prevention    = "enforced"
  force_destroy               = true
  labels                      = local.labels_common
}

resource "google_storage_bucket_iam_member" "bucket_template_iam_me" {
  bucket = google_storage_bucket.bucket_template.name
  for_each = toset([
    "roles/storage.admin"
  ])
  role   = each.key
  member = format("user:%s", var.ME)
}
resource "google_storage_bucket_iam_member" "bucket_template_iam_sa" {
  bucket = google_storage_bucket.bucket_template.name
  for_each = toset([
    "roles/storage.objectViewer"
  ])
  role   = each.key
  member = format("serviceAccount:%s", google_service_account.sa_app.email)
}
resource "google_storage_bucket_iam_member" "bucket_template_iam_sa_build" {
  bucket = google_storage_bucket.bucket_template.name
  for_each = toset([
    "roles/storage.admin"
  ])
  role   = each.key
  member = format("serviceAccount:%s@cloudbuild.gserviceaccount.com", data.google_project.project_fleetevents.number)
}

# json file on GCS that holds the template spec

data "google_storage_bucket_object" "template_spec" {
  name   = format("templates/%s.json", var.TEMPLATE_NAME)
  bucket = google_storage_bucket.bucket_template.id
  depends_on = [
    terraform_data.script_build_flex_template
  ]
}





## build - jar

data "local_file" "pom_xml" {
  filename = format("%s/../../../beam/pom.xml", path.module)
}
data "local_file" "src_pipeline" {
  filename = format("%s/../../../beam/src/main/java/com/google/fleetevents/beam/FleetEventRunner.java", path.module)
}

resource "terraform_data" "script_build_jar" {

  # when any of these values change, the uber jar will be rebuilt
  # TODO: add more flags to correctly detect when to rebuild the jar
  triggers_replace = [
    "aaaa",
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



## build - flex-template image

## using the gcloud cli for template building until alternative is ready
## dataflow flex-templates build process takes the pipeline code and builds a container image
## it also takes a template spec json, and stores a altered version on GCS.
## the whole process is taken care behind "gcloud dataflow flex-template build" command, 
## but there is no equivalent terraform. 

resource "terraform_data" "script_build_flex_template" {

  # when any of these values change, the container image will be rebuilt
  # TODO: add more flags to correctly detect when to rebuild an image
  triggers_replace = [
    "aaat",
    fileexists(abspath("../../../../../beam/target/fleetevents-beam-bundled-1.0-SNAPSHOT.jar")),
    #data.local_file.jar.content_md5
  ]
  provisioner "local-exec" {
    command = format(
      "%s/scripts/scripts.sh tf_buildTemplate %s",
      path.module,
      join(" ",
        [
          format(" --project %s", data.google_project.project_fleetevents.project_id),
          format(" --template_name %s", var.TEMPLATE_NAME),
          format(" --template_json_gcs %s/templates/%s.json", google_storage_bucket.bucket_template.url, var.TEMPLATE_NAME),
          format(" --template_json_local %s", abspath("../../../../../beam/fleetevents-beam.json")),
          format(" --jar %s", abspath("../../../../../beam/target/fleetevents-beam-bundled-1.0-SNAPSHOT.jar")),
          format(" --repository %s", google_artifact_registry_repository.repo.name),
          format(" --region %s", var.GCP_REGION),
          format(" --bucket %s", google_storage_bucket.bucket_template.name),
          format(" --image-gcr-path %s-docker.pkg.dev/%s/%s/%s:latest",
            var.GCP_REGION,
            data.google_project.project_fleetevents.project_id,
            google_artifact_registry_repository.repo.name,
            var.TEMPLATE_NAME
          ),
          format(" --template_mainclass %s", "com.google.fleetevents.beam.FleetEventRunner"),
          format(" --template_baseimage %s", "gcr.io/dataflow-templates-base/java11-template-launcher-base-distroless"),
        ]
      )
    )
    interpreter = ["bash", "-c"]
  }
  depends_on = [
    terraform_data.script_build_jar
  ]
}


## setting up a repository for containerized pipeline images

resource "google_artifact_registry_repository" "repo" {
  project       = data.google_project.project_fleetevents.project_id
  location      = var.GCP_REGION
  repository_id = format("repo-%s-%s", var.TEMPLATE_NAME, random_id.template_suffix.dec)
  description   = format("Repository for FleetEvents flex-template images for template \"%s\"", var.TEMPLATE_NAME)
  format        = "DOCKER"
  labels        = local.labels_common
}
