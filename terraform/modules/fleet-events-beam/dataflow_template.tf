


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


## Cloud Storage for flex-template

resource "google_storage_bucket" "bucket_template" {
  project                     = data.google_project.project_fleetevents.project_id
  name                        = format("%s-flextemplate-%s", data.google_project.project_fleetevents.project_id, var.TEMPLATE_NAME)
  location                    = var.GCP_REGION
  uniform_bucket_level_access = true
  public_access_prevention    = "enforced"
  force_destroy               = true
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
    "roles/storage.admin"
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


data "google_storage_bucket_object" "template_spec" {
  name   = format("templates/%s.json", var.TEMPLATE_NAME)
  bucket = google_storage_bucket.bucket_template.id
  depends_on = [
    terraform_data.script_build_flex_template
  ]
}



## build - jar

resource "terraform_data" "script_build_jar" {

  # when any of these values change, the uber jar will be rebuilt
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



## build - flex-template image

resource "terraform_data" "script_build_flex_template" {

  # when any of these values change, the container image will be rebuilt
  triggers_replace = [
    "aaa",
    fileexists(abspath(local.PATH_JAR)),
    #data.local_file.jar.content_md5
  ]
  provisioner "local-exec" {
    command = format(
      "pwd; echo ${path.module};  %s/scripts/scripts.sh tf_buildTemplate %s %s %s %s %s %s %s %s",
      path.module,                                        # prepend the script path
      data.google_project.project_fleetevents.project_id, # project_id
      var.TEMPLATE_NAME,                                  # template 
      format("gs://%s/templates/%s.json", google_storage_bucket.bucket_template.name, var.TEMPLATE_NAME),
      abspath("../../../../../beam/fleetevents-beam.json"),
      abspath(local.PATH_JAR),
      google_artifact_registry_repository.repo.name,
      var.GCP_REGION,
      google_storage_bucket.bucket_template.name
      #format("%s-flextemplate-%s", data.google_project.project_fleetevents.project_id, var.TEMPLATE_NAME)
    )
    interpreter = ["bash", "-c"]
  }
  depends_on = [terraform_data.script_build_jar]
}
