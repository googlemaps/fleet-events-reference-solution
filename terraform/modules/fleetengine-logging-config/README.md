# Module : Fleet Engine logging config

This module will setup Cloud Logging integration for Fleet Engine.

## Features

* Turn on prerequisite services in the project
* Create Cloud Router for each logging sink service (Logging Bucket, BigQuery, Pub/Sub, Cloud Storage) of choice.
* Configure IAM permissions for cross project logging (source: Fleet Engine project, destination: Logging project)
* Configure Logging exclusion for _Default sink of Fleet Engine project

## Usage examples

examples usage of this module can be found under folder "[examples](./examples/)"

* with_existing_logging_project : setting up reference solution in a existing project
* with_new_logging_project : creates a new project for the reference solution prior to setup
* multiregion : setting up a shared logging project for multiple regional Fleet Engine projects


<!-- terraform-docs markdown table --output-mode insert --output-file ./README.md . -->

<!-- BEGIN_TF_DOCS -->
## Modules

No modules.

## Resources

| Name | Type |
|------|------|
| [google_bigquery_dataset.fleetengine-logging-sink-bq](https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/bigquery_dataset) | resource |
| [google_bigquery_dataset.fleetengine-logging-sink-pubsub-bq](https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/bigquery_dataset) | resource |
| [google_bigquery_dataset_iam_member.fleetengine-logging-sink-bq-editor](https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/bigquery_dataset_iam_member) | resource |
| [google_bigquery_table.fleetengine-logging-sink-pubsub-bq-table](https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/bigquery_table) | resource |
| [google_logging_project_bucket_config.fleetengine-logging-logbucket](https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/logging_project_bucket_config) | resource |
| [google_logging_project_exclusion.log-exclusion-fleetengine](https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/logging_project_exclusion) | resource |
| [google_logging_project_sink.fleetengine-logrouter-bigquery](https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/logging_project_sink) | resource |
| [google_logging_project_sink.fleetengine-logrouter-logging](https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/logging_project_sink) | resource |
| [google_logging_project_sink.fleetengine-logrouter-pubsub](https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/logging_project_sink) | resource |
| [google_logging_project_sink.fleetengine-logrouter-storage](https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/logging_project_sink) | resource |
| [google_project_iam_member.bq_data_editor](https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/project_iam_member) | resource |
| [google_project_iam_member.bq_metadata_viewer](https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/project_iam_member) | resource |
| [google_project_iam_member.iam_member_me](https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/project_iam_member) | resource |
| [google_project_iam_member.project_iam_loggingadmin](https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/project_iam_member) | resource |
| [google_project_iam_member.project_iam_loggingbucket_writer](https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/project_iam_member) | resource |
| [google_project_service.gcp_services](https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/project_service) | resource |
| [google_project_service.gcp_services_bigquery](https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/project_service) | resource |
| [google_project_service.gcp_services_logging](https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/project_service) | resource |
| [google_project_service.gcp_services_pubsub](https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/project_service) | resource |
| [google_project_service.gcp_services_storage](https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/project_service) | resource |
| [google_pubsub_subscription.fleetengine-logging-subscription-bigquery](https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/pubsub_subscription) | resource |
| [google_pubsub_subscription.fleetengine-logging-subscription-default](https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/pubsub_subscription) | resource |
| [google_pubsub_topic.fleetengine-logging-sink-pubsub](https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/pubsub_topic) | resource |
| [google_pubsub_topic_iam_member.fleetengine-logging-sink-topic-publisher](https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/pubsub_topic_iam_member) | resource |
| [google_storage_bucket.fleetengine-logging-sink-storage](https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/storage_bucket) | resource |
| [google_storage_bucket_iam_member.fleetengine-logging-sink-storage-editor](https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/storage_bucket_iam_member) | resource |
| [google_project.project-fleetengine](https://registry.terraform.io/providers/hashicorp/google/latest/docs/data-sources/project) | data source |
| [google_project.project-loggingsync](https://registry.terraform.io/providers/hashicorp/google/latest/docs/data-sources/project) | data source |

## Inputs

| Name | Description | Type | Default | Required |
|------|-------------|------|---------|:--------:|
| <a name="input_BQ_DATASET"></a> [BQ\_DATASET](#input\_BQ\_DATASET) | Name of BigQuery dataset to store logs | `string` | `"fleetengine_logging"` | no |
| <a name="input_FLAG_SETUP_LOGGING_BIGQUERY"></a> [FLAG\_SETUP\_LOGGING\_BIGQUERY](#input\_FLAG\_SETUP\_LOGGING\_BIGQUERY) | whether to setup Logging or not for BigQuery | `bool` | `true` | no |
| <a name="input_FLAG_SETUP_LOGGING_CLOUDSTORAGE"></a> [FLAG\_SETUP\_LOGGING\_CLOUDSTORAGE](#input\_FLAG\_SETUP\_LOGGING\_CLOUDSTORAGE) | whether to setup Logging or not for Cloud Storage Buckets | `bool` | `false` | no |
| <a name="input_FLAG_SETUP_LOGGING_EXCLUSION"></a> [FLAG\_SETUP\_LOGGING\_EXCLUSION](#input\_FLAG\_SETUP\_LOGGING\_EXCLUSION) | whether to setup Logging exclusion for \_Default | `bool` | `false` | no |
| <a name="input_FLAG_SETUP_LOGGING_LOGGING"></a> [FLAG\_SETUP\_LOGGING\_LOGGING](#input\_FLAG\_SETUP\_LOGGING\_LOGGING) | whether to setup Logging or not for Logging Buckets | `bool` | `false` | no |
| <a name="input_FLAG_SETUP_LOGGING_PUBSUB"></a> [FLAG\_SETUP\_LOGGING\_PUBSUB](#input\_FLAG\_SETUP\_LOGGING\_PUBSUB) | whether to setup Logging or not for Pub/Sub | `bool` | `false` | no |
| <a name="input_FLAG_SETUP_LOGGING_PUBSUB_SUB_BQ"></a> [FLAG\_SETUP\_LOGGING\_PUBSUB\_SUB\_BQ](#input\_FLAG\_SETUP\_LOGGING\_PUBSUB\_SUB\_BQ) | whether to setup a push BigQuery subscription for the Pub/Sub topic | `bool` | `false` | no |
| <a name="input_FLAG_SETUP_LOGGING_PUBSUB_SUB_DEFAULT"></a> [FLAG\_SETUP\_LOGGING\_PUBSUB\_SUB\_DEFAULT](#input\_FLAG\_SETUP\_LOGGING\_PUBSUB\_SUB\_DEFAULT) | whether to setup a subscription for the Pub/Sub topic | `bool` | `false` | no |
| <a name="input_GCP_REGION"></a> [GCP\_REGION](#input\_GCP\_REGION) | For resources than can be constrained to sit within a GCP region, a region of preference. | `string` | `"asia-southeast1"` | no |
| <a name="input_GCP_ZONE"></a> [GCP\_ZONE](#input\_GCP\_ZONE) | For resources than can be constrained to sit within a GCP zone, a zone of preference. This should be one of the zones in the selected GCP\_REGION | `string` | `"asia-southeast1-b"` | no |
| <a name="input_LOG_FILTER"></a> [LOG\_FILTER](#input\_LOG\_FILTER) | Filter applied to capture log events and send to sinks | `string` | `"(\n resource.type=\"audited_resource\"\n resource.labels.service=\"fleetengine.googleapis.com\"\n resource.labels.method=\"maps.fleetengine.v1.TripService.ReportBillableTrip\"\n) \nOR resource.type=\"fleetengine.googleapis.com/Fleet\"\nOR resource.type=\"fleetengine.googleapis.com/DeliveryFleet\"\n"` | no |
| <a name="input_ME"></a> [ME](#input\_ME) | user account running terraform | `string` | n/a | yes |
| <a name="input_PROJECT_FLEETENGINE"></a> [PROJECT\_FLEETENGINE](#input\_PROJECT\_FLEETENGINE) | The project with Fleet Engine (ODRD/LMFS) enabled. | `string` | n/a | yes |
| <a name="input_PROJECT_LOGGINGSYNC"></a> [PROJECT\_LOGGINGSYNC](#input\_PROJECT\_LOGGINGSYNC) | The project where the Logging data will be persisted | `string` | n/a | yes |
| <a name="input_PUBSUB_TOPIC_NAME"></a> [PUBSUB\_TOPIC\_NAME](#input\_PUBSUB\_TOPIC\_NAME) | Name of Pub/Sub topic to publish log events | `string` | `"fleetengine-logging-topic"` | no |
| <a name="input_RETENTION"></a> [RETENTION](#input\_RETENTION) | Data retention period in days | `number` | `30` | no |

## Outputs

| Name | Description |
|------|-------------|
| <a name="output_console-links"></a> [console-links](#output\_console-links) | direct link to related pages in Cloud Console |
| <a name="output_fleetengine-log-sinks"></a> [fleetengine-log-sinks](#output\_fleetengine-log-sinks) | Log Sink details |
| <a name="output_fleetengine-logrouters"></a> [fleetengine-logrouters](#output\_fleetengine-logrouters) | Log router details |
<!-- END_TF_DOCS -->
