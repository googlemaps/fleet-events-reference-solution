
locals {
  FUNCTION_NAME = "fn-echo-standalone"
  PROJECT       = "moritani-sandbox-stolos2"
}

data "google_project" "project" {
  project_id = local.PROJECT
}
module "function" {
  source                    = "../../"
  PROJECT_APP               = local.PROJECT
  FUNCTION_SRC_DIR          = "./src"
  FUNCTION_NAME             = local.FUNCTION_NAME
  FUNCTION_RUNTIME          = "java17"
  FUNCTION_DESCRIPTION      = "FleetEvents : sample echo function"
  FUNCTION_ENTRYPOINT       = "samples.EchoFunction"
  FUNCTION_AVAILABLE_MEMORY = "256M"
  PROJECT_FLEETENGINE       = "cabrio-1501793433270"
  PROJECT_FLEETENGINE_LOG   = local.PROJECT
  TOPIC_FLEETENGINE_LOG     = "logging_cabrio"
  TOPIC_FLEETEVENTS_OUTPUT  = google_pubsub_topic.topic.name
  FUNCTION_ADDITIONAL_ENV_VARS = {
    OUTPUT_TOPIC_ID         = google_pubsub_topic.topic.name
    OUTPUT_PROJECT_ID       = google_pubsub_topic.topic.project
    VEHICLE_COLLECTION_NAME = "deliveryVehicles"
    TASK_COLLECTION_NAME    = "deliveryTasks"
  }
  SA_APP_ROLES = [
    "roles/datastore.user"
  ]
  SA_FLEETENGINE_ROLES = [
    "roles/fleetengine.deliveryFleetReader"
  ]
  FUNCTION_SRC_EXCLUDE_FILES = [
    "pom.xml.versionsBackup",
  ]
  FUNCTION_SRC_EXCLUDE_PATTERNS = [
    ".*",
    ".vscode/**",
    "target/**",
    "**/.gitkeep"
  ]
  depends_on = [
    google_pubsub_topic.topic
  ]
}

resource "google_pubsub_topic" "topic" {
  project = local.PROJECT
  name    = format("%s-output", local.FUNCTION_NAME)

  labels = {
    "created_via"          = "terraform"
    "fleetevents_function" = lower(local.FUNCTION_NAME)
    "project_fleetevents"  = local.PROJECT
  }
  #  message_retention_duration = "86600s"
}

resource "google_pubsub_subscription" "bqsub" {
  project = local.PROJECT
  name    = format("%s-sub-bq", local.FUNCTION_NAME)
  topic   = google_pubsub_topic.topic.name

  bigquery_config {
    table = format(
      "%s.%s.%s",
      local.PROJECT,
      google_bigquery_table.bqsub_table.dataset_id,
      google_bigquery_table.bqsub_table.table_id
    )
    write_metadata = true
  }

  depends_on = [
    #google_project_iam_member.viewer,
    #google_bigquery_dataset_iam_member.editor
  ]
}

resource "google_bigquery_dataset" "bqsub_dataset" {
  project    = local.PROJECT
  dataset_id = replace(format("%s-sub", local.FUNCTION_NAME), "-", "_")
}

resource "google_bigquery_table" "bqsub_table" {
  project             = local.PROJECT
  deletion_protection = false
  table_id            = "subscription"
  dataset_id          = google_bigquery_dataset.bqsub_dataset.dataset_id

  schema = <<EOF
[
    {
        "name": "subscription_name",
        "type": "STRING"
    },
    {
        "name": "message_id",
        "type": "STRING"
    },
    {
        "name": "publish_time",
        "type": "TIMESTAMP"
    },
    {
        "name": "data",
        "type": "JSON",
        "mode": "NULLABLE",
        "description": "The data"
    },
    {
        "name": "attributes",
        "type": "STRING"
    }
]
EOF
}

resource "google_bigquery_dataset_iam_member" "editor" {
  project    = google_bigquery_dataset.bqsub_dataset.project
  dataset_id = google_bigquery_dataset.bqsub_dataset.dataset_id
  role       = "roles/bigquery.dataEditor"
  member = format(
    "serviceAccount:service-%s@gcp-sa-pubsub.iam.gserviceaccount.com",
    data.google_project.project.number
  )
}

resource "google_pubsub_topic_iam_member" "topic_iam" {
  project = google_pubsub_topic.topic.project
  topic   = google_pubsub_topic.topic.name
  role    = "roles/pubsub.publisher"
  member = format(
    "serviceAccount:%s",
    module.function.serviceaccounts.app.email
  )
  depends_on = [
    google_pubsub_topic.topic,
    module.function
  ]
}

output "function" {
  value = module.function
}

# output "topic" {
#   value = google_pubsub_topic.topic
# }
# output "topic_iam" {
#   value = google_pubsub_topic_iam_member.topic_iam
# }
