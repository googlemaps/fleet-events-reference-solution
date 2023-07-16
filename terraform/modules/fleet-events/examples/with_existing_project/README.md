# Setting up Fleet Events reference solution

This example setups Fleet Events reference solution in an existing Google Cloud project.

First, create file `terraform.tfvars` with the following :

```hcl
PROJECT_FLEETENGINE = "[Your Mobility(ODRD/LMFS) project]"
PROJECT_FLEETEVENTS = "[Your Fleet Events project(pre-created)]"
GCP_REGION          = "[GCP Region to use]"
ME                  = "[Your Google account email address]"
```

Then run :
```shell
terraform init
terraform plan
terraform apply
```

<!-- terraform-docs markdown table --output-mode insert --show data-sources,inputs,modules,outputs,resources --output-file ./README.md . -->

<!-- BEGIN_TF_DOCS -->
## Modules

| Name | Source | Version |
|------|--------|---------|
| <a name="module_fleet-events"></a> [fleet-events](#module\_fleet-events) | ../../ | n/a |

## Resources

| Name | Type |
|------|------|
| [google_bigquery_dataset.bqsub_dataset](https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/bigquery_dataset) | resource |
| [google_bigquery_dataset_iam_member.editor](https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/bigquery_dataset_iam_member) | resource |
| [google_bigquery_table.bqsub_table](https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/bigquery_table) | resource |
| [google_project_service.gcp_services](https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/project_service) | resource |
| [google_project_service.gcp_services_bq](https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/project_service) | resource |
| [google_pubsub_subscription.bqsub](https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/pubsub_subscription) | resource |
| [google_pubsub_topic.output_topic](https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/pubsub_topic) | resource |
| [google_pubsub_topic_iam_member.topic_iam](https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/pubsub_topic_iam_member) | resource |
| [google_project.project-fleetengine](https://registry.terraform.io/providers/hashicorp/google/latest/docs/data-sources/project) | data source |
| [google_project.project-fleetevents](https://registry.terraform.io/providers/hashicorp/google/latest/docs/data-sources/project) | data source |

## Inputs

| Name | Description | Type | Default | Required |
|------|-------------|------|---------|:--------:|
| <a name="input_FLAG_SETUP_BIGQUERY_SUBSCRIPTION"></a> [FLAG\_SETUP\_BIGQUERY\_SUBSCRIPTION](#input\_FLAG\_SETUP\_BIGQUERY\_SUBSCRIPTION) | whether to setup a push BigQuery subscription for the Pub/Sub topic | `bool` | `false` | no |
| <a name="input_FUNCTION_NAME"></a> [FUNCTION\_NAME](#input\_FUNCTION\_NAME) | Name of the Cloud Function. This will used as the indentifier of the Cloud Function (v2), and has to follow naming rules. Only lower case alphanumeric and '-' allowed. | `string` | `"fleetevents-fn"` | no |
| <a name="input_FUNCTION_SRC_DIR"></a> [FUNCTION\_SRC\_DIR](#input\_FUNCTION\_SRC\_DIR) | Path to the directory where the Cloud Functions source code is located. | `string` | `"../../../../../cloud-functions/"` | no |
| <a name="input_GCP_REGION"></a> [GCP\_REGION](#input\_GCP\_REGION) | For resources than can be constrained run or store data within a GCP region, the default region of preference. | `string` | `"us-central1"` | no |
| <a name="input_GCP_REGION_FIRESTORE"></a> [GCP\_REGION\_FIRESTORE](#input\_GCP\_REGION\_FIRESTORE) | GCP Region for Firestore.  If not set, will use value of GCP\_REGION. Reference and choose the right one for your deployement https://cloud.google.com/firestore/docs/locations | `string` | `"nam5"` | no |
| <a name="input_ME"></a> [ME](#input\_ME) | Account ID (in the form of email address) of the developer running terraform. | `string` | n/a | yes |
| <a name="input_PROJECT_FLEETENGINE"></a> [PROJECT\_FLEETENGINE](#input\_PROJECT\_FLEETENGINE) | Project ID of the project where Fleet Engine (ODRD/LMFS) is enabled | `string` | n/a | yes |
| <a name="input_PROJECT_FLEETENGINE_LOG"></a> [PROJECT\_FLEETENGINE\_LOG](#input\_PROJECT\_FLEETENGINE\_LOG) | Project ID of the project where Fleet Engine logs are persisted. (LogSink of Cloud Logging settings.) | `string` | n/a | yes |
| <a name="input_PROJECT_FLEETEVENTS"></a> [PROJECT\_FLEETEVENTS](#input\_PROJECT\_FLEETEVENTS) | Project ID of the project in which to setup Fleet Events reference solution. | `string` | n/a | yes |
| <a name="input_TOPIC_FLEETENGINE_LOG"></a> [TOPIC\_FLEETENGINE\_LOG](#input\_TOPIC\_FLEETENGINE\_LOG) | Pub/Sub Topic to which Fleet Engine logs are published following Cloud Logging setup. | `string` | n/a | yes |
| <a name="input_TOPIC_FLEETEVENTS_OUTPUT"></a> [TOPIC\_FLEETEVENTS\_OUTPUT](#input\_TOPIC\_FLEETEVENTS\_OUTPUT) | Pub/Sub Topic what will be created and to which the deployed function will be publishing events. | `string` | `"fleetevents-fn-output"` | no |

## Outputs

| Name | Description |
|------|-------------|
| <a name="output_function"></a> [function](#output\_function) | defails of function deployed |
| <a name="output_logging_config"></a> [logging\_config](#output\_logging\_config) | details of logging config |
<!-- END_TF_DOCS -->
