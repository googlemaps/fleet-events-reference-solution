
## Fleet Events module expects "TOPIC_FLEETEVENTS_OUTPUT" to exist. 
## Here is an example to create the topic ahead of calling the module.
resource "google_pubsub_topic" "output_topic" {
  project = var.PROJECT_FLEETEVENTS
  name    = var.TOPIC_FLEETEVENTS_OUTPUT


  labels = local.labels_common
  #  message_retention_duration = "86600s"
  message_storage_policy {
    allowed_persistence_regions = [
      var.GCP_REGION
    ]
  }
  depends_on = [
    google_project_service.gcp_services
  ]
}

## give Functions service account, permissions to publish to output Pub/Sub topic
resource "google_pubsub_topic_iam_member" "topic_iam" {
  project = google_pubsub_topic.output_topic.project
  topic   = google_pubsub_topic.output_topic.name
  role    = "roles/pubsub.publisher"
  member = format(
    "serviceAccount:%s",
    module.fleet-events.function.serviceaccounts.app.email
  )
  depends_on = [
    google_pubsub_topic.output_topic,
    module.fleet-events
  ]
}



## BigQuery related configurations that runs when FLAG_SETUP_BIGQUERY_SUBSCRIPTION is set to true.

## turn on BigQuery 
resource "google_project_service" "gcp_services_bq" {
  project = data.google_project.project-fleetevents.project_id

  timeouts {
    create = "10m"
    update = "10m"
  }
  service            = "bigquery.googleapis.com"
  disable_on_destroy = false
  depends_on = [

  ]
  count = var.FLAG_SETUP_BIGQUERY_SUBSCRIPTION ? 1 : 0

}

## create BigQuery subscription for the output topic

resource "google_pubsub_subscription" "bqsub" {
  project = var.PROJECT_FLEETEVENTS
  name    = format("%s-sub-bq", var.FUNCTION_NAME)
  topic   = google_pubsub_topic.output_topic.name


  bigquery_config {
    table = format(
      "%s.%s.%s",
      var.PROJECT_FLEETEVENTS,
      google_bigquery_table.bqsub_table[0].dataset_id,
      google_bigquery_table.bqsub_table[0].table_id
    )
    write_metadata = true
  }
  labels = local.labels_common
  depends_on = [
  ]
  count = var.FLAG_SETUP_BIGQUERY_SUBSCRIPTION ? 1 : 0

}

## create BigQuery dataset 
resource "google_bigquery_dataset" "bqsub_dataset" {
  project                    = var.PROJECT_FLEETEVENTS
  dataset_id                 = replace(format("%s-sub", var.FUNCTION_NAME), "-", "_")
  location                   = var.GCP_REGION
  description                = format("BigQuery subscription to Fleet Events function \"%s\"'s output topic", var.FUNCTION_NAME)
  labels                     = local.labels_common
  delete_contents_on_destroy = true
  depends_on = [
    google_project_service.gcp_services_bq
  ]
  count = var.FLAG_SETUP_BIGQUERY_SUBSCRIPTION ? 1 : 0

}

## create BigQuery table where the events will be stored 

resource "google_bigquery_table" "bqsub_table" {
  project             = var.PROJECT_FLEETEVENTS
  deletion_protection = false
  table_id            = "subscription"
  dataset_id          = google_bigquery_dataset.bqsub_dataset[0].dataset_id
  labels              = local.labels_common
  description         = format("BigQuery subscription to Fleet Events function \"%s\"'s output topic", var.FUNCTION_NAME)
  count               = var.FLAG_SETUP_BIGQUERY_SUBSCRIPTION ? 1 : 0

  # https://cloud.google.com/pubsub/docs/bigquery
  schema = <<EOF
[
{
    "name": "subscription_name",
    "type": "STRING",
    "description":"Name of a subscription."
},
{
    "name": "message_id",
    "type": "STRING",
    "description":"ID of a message"
},
{
    "name": "publish_time",
    "type": "TIMESTAMP",
    "description":"The time of publishing a message."
},
{
    "name": "data",
    "type": "JSON",
    "mode": "NULLABLE",
    "description": "The message body."
},
{
    "name": "attributes",
    "type": "STRING",
    "description":"A JSON object containing all message attributes. It also contains additional fields that are part of the Pub/Sub message including the ordering key, if present."
}
]
EOF
}


## give Pub/Sub service account, permissions to write to BigQuery
resource "google_bigquery_dataset_iam_member" "editor" {
  project    = google_bigquery_dataset.bqsub_dataset[0].project
  dataset_id = google_bigquery_dataset.bqsub_dataset[0].dataset_id
  role       = "roles/bigquery.dataEditor"
  member = format(
    "serviceAccount:service-%s@gcp-sa-pubsub.iam.gserviceaccount.com",
    data.google_project.project-fleetevents.number
  )
  count = var.FLAG_SETUP_BIGQUERY_SUBSCRIPTION ? 1 : 0

}
