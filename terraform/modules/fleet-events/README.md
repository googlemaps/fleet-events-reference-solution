# Module : Fleet Events

This module will setup Fleet Events reference solution in an existing Google Cloud project.

## Features

* Turn on prerequisite services in the project
* Setup Firestore to be available in the Google Cloud region of choice
* Setup Fleet Engine's logging integration so that log events flow into Fleet Event project's Pub/Sub topic. (This part uses another module `fleetengine-logging-config`)

## Usage examples

examples usage of this module can be found under folder "[examples](./examples/)"

* with_existing_project : setting up reference solution in a existing project
* with_new_project : creates a new project for the reference solution prior to setup

<!--  terraform-docs markdown table --output-mode insert --show data-sources,inputs,modules,outputs,resources --output-file ./README.md . -->

<!-- BEGIN_TF_DOCS -->
## Modules

| Name | Source | Version |
|------|--------|---------|
| <a name="module_func-fleetevents"></a> [func-fleetevents](#module\_func-fleetevents) | ../fleet-events-function/ | n/a |
| <a name="module_logging_config"></a> [logging\_config](#module\_logging\_config) | ../fleetengine-logging-config/ | n/a |

## Resources

| Name | Type |
|------|------|
| [google_firestore_database.database](https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/firestore_database) | resource |
| [google_project_service.firestore](https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/project_service) | resource |
| [google_project_service.gcp_services](https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/project_service) | resource |
| [google_project.project-fleetengine](https://registry.terraform.io/providers/hashicorp/google/latest/docs/data-sources/project) | data source |
| [google_project.project-fleetengine-logs](https://registry.terraform.io/providers/hashicorp/google/latest/docs/data-sources/project) | data source |
| [google_project.project-fleetevents](https://registry.terraform.io/providers/hashicorp/google/latest/docs/data-sources/project) | data source |
| [google_pubsub_topic.events_output_topic](https://registry.terraform.io/providers/hashicorp/google/latest/docs/data-sources/pubsub_topic) | data source |
| [google_pubsub_topic.logging_topic](https://registry.terraform.io/providers/hashicorp/google/latest/docs/data-sources/pubsub_topic) | data source |

## Inputs

| Name | Description | Type | Default | Required |
|------|-------------|------|---------|:--------:|
| <a name="input_FUNCTION_NAME"></a> [FUNCTION\_NAME](#input\_FUNCTION\_NAME) | Name of the Cloud Function. This will used as the indentifier of the Cloud Function (v2), and has to follow naming rules. Only lower case alphanumeric and '-' allowed. | `string` | `"fleetevents-fn"` | no |
| <a name="input_FUNCTION_SRC_DIR"></a> [FUNCTION\_SRC\_DIR](#input\_FUNCTION\_SRC\_DIR) | Path to the directory where the Cloud Functions source code is located. | `string` | n/a | yes |
| <a name="input_GCP_REGION"></a> [GCP\_REGION](#input\_GCP\_REGION) | Default GCP region for Cloud resources | `string` | `"us-central1"` | no |
| <a name="input_GCP_REGION_FIRESTORE"></a> [GCP\_REGION\_FIRESTORE](#input\_GCP\_REGION\_FIRESTORE) | Refer to https://cloud.google.com/firestore/docs/locations to choose GCP Region for Firestore. If not set, will use value of GCP\_REGION. | `string` | n/a | yes |
| <a name="input_GCP_REGION_FUNCTIONS"></a> [GCP\_REGION\_FUNCTIONS](#input\_GCP\_REGION\_FUNCTIONS) | Refer to https://cloud.google.com/functions/docs/locations to choose GCP Region for Function deployment. If not set, the value of GCP\_REGION will be applied. | `string` | n/a | yes |
| <a name="input_GCP_REGION_STORAGE"></a> [GCP\_REGION\_STORAGE](#input\_GCP\_REGION\_STORAGE) | Refer to https://cloud.google.com/storage/docs/locations to choose GCP Region for Cloud Storage Buckets. If not set, the value of GCP\_REGION will be applied. | `string` | n/a | yes |
| <a name="input_ME"></a> [ME](#input\_ME) | Account ID (in the form of email address) of the developer running terraform. | `string` | n/a | yes |
| <a name="input_PROJECT_FLEETENGINE"></a> [PROJECT\_FLEETENGINE](#input\_PROJECT\_FLEETENGINE) | Project ID of the project where Fleet Engine (ODRD/LMFS) is enabled. | `string` | n/a | yes |
| <a name="input_PROJECT_FLEETENGINE_LOG"></a> [PROJECT\_FLEETENGINE\_LOG](#input\_PROJECT\_FLEETENGINE\_LOG) | Project ID of the project where Fleet Engine logs are persisted. (LogSink of Cloud Logging settings.) | `string` | n/a | yes |
| <a name="input_PROJECT_FLEETEVENTS"></a> [PROJECT\_FLEETEVENTS](#input\_PROJECT\_FLEETEVENTS) | Project ID of the project in which to setup Fleet Events reference solution. | `string` | n/a | yes |
| <a name="input_TOPIC_FLEETENGINE_LOG"></a> [TOPIC\_FLEETENGINE\_LOG](#input\_TOPIC\_FLEETENGINE\_LOG) | Pub/Sub Topic to which Fleet Engine logs are published following Cloud Logging setup. | `string` | n/a | yes |
| <a name="input_TOPIC_FLEETEVENTS_OUTPUT"></a> [TOPIC\_FLEETEVENTS\_OUTPUT](#input\_TOPIC\_FLEETEVENTS\_OUTPUT) | An "Existing" Pub/Sub Topic to which the deployed function will be publishing events. | `string` | `"fleetevents-fn-output"` | no |

## Outputs

| Name | Description |
|------|-------------|
| <a name="output_function"></a> [function](#output\_function) | defails of function deployed |
| <a name="output_logging_config"></a> [logging\_config](#output\_logging\_config) | details of logging config |
<!-- END_TF_DOCS -->
