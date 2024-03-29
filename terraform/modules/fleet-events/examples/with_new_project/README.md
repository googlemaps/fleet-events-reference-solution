# Setting up Fleet Events reference solution

This example creates a new Google Cloud project and will setup Fleet Events reference solution.

First, create file `terraform.tfvars` with the following :

```yaml
PROJECT_FLEETENGINE      = "[Your Mobility(ODRD/LMFS) project]"
PROJECT_FLEETEVENTS      = "[Your Fleet Events project(pre-created)]"
FOLDER_ID                = "[folder(id) under which the new project should be created]"
BILLING_ACCOUNT          = "[Billing Account(id) the new project should be associated with]"
PROJECT_FLEETEVENTS_NAME = "[Human readable name for the new project]"
GCP_REGION               = "[GCP Region to use]"
ME                       = "[Your Google account email address]"
```

Then run :
```Shell
terraform init
terraform plan
```

<!--  terraform-docs markdown table --output-mode insert --show data-sources,inputs,modules,outputs,resources --output-file ./README.md . -->

<!-- BEGIN_TF_DOCS -->
## Modules

| Name | Source | Version |
|------|--------|---------|
| <a name="module_fleet-events-with-existing-project"></a> [fleet-events-with-existing-project](#module\_fleet-events-with-existing-project) | ../with_existing_project/ | n/a |

## Resources

| Name | Type |
|------|------|
| [google_project.project-fleetevents](https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/project) | resource |
| [google_project.project-fleetengine](https://registry.terraform.io/providers/hashicorp/google/latest/docs/data-sources/project) | data source |

## Inputs

| Name | Description | Type | Default | Required |
|------|-------------|------|---------|:--------:|
| <a name="input_BILLING_ACCOUNT"></a> [BILLING\_ACCOUNT](#input\_BILLING\_ACCOUNT) | ID of Billing Account to be associated with the new project. | `string` | n/a | yes |
| <a name="input_FLAG_SETUP_BIGQUERY_SUBSCRIPTION"></a> [FLAG\_SETUP\_BIGQUERY\_SUBSCRIPTION](#input\_FLAG\_SETUP\_BIGQUERY\_SUBSCRIPTION) | whether to setup a push BigQuery subscription for the Pub/Sub topic | `bool` | `false` | no |
| <a name="input_FOLDER_ID"></a> [FOLDER\_ID](#input\_FOLDER\_ID) | Folder ID of the folder under which the new project will be created. | `string` | n/a | yes |
| <a name="input_FUNCTION_NAME"></a> [FUNCTION\_NAME](#input\_FUNCTION\_NAME) | Name of the Cloud Function. This will used as the indentifier of the Cloud Function (v2), and has to follow naming rules. Only lower case alphanumeric and '-' allowed. | `string` | `"fleetevents-fn"` | no |
| <a name="input_FUNCTION_SRC_DIR"></a> [FUNCTION\_SRC\_DIR](#input\_FUNCTION\_SRC\_DIR) | Path to the directory where the Cloud Functions source code is located. | `string` | `"../../../../../cloud-functions/"` | no |
| <a name="input_GCP_REGION"></a> [GCP\_REGION](#input\_GCP\_REGION) | For resources than can be constrained run or store data within a GCP region, the default region of preference. | `string` | `"us-central1"` | no |
| <a name="input_GCP_REGION_FIRESTORE"></a> [GCP\_REGION\_FIRESTORE](#input\_GCP\_REGION\_FIRESTORE) | GCP Region for Firestore.  If not set, will use value of GCP\_REGION. Reference and choose the right one for your deployement https://cloud.google.com/firestore/docs/locations | `string` | `"nam5"` | no |
| <a name="input_ME"></a> [ME](#input\_ME) | Account ID (in the form of email address) of the developer running terraform. | `string` | n/a | yes |
| <a name="input_PROJECT_FLEETENGINE"></a> [PROJECT\_FLEETENGINE](#input\_PROJECT\_FLEETENGINE) | Project ID of the project where Fleet Engine (ODRD/LMFS) is enabled | `string` | n/a | yes |
| <a name="input_PROJECT_FLEETENGINE_LOG"></a> [PROJECT\_FLEETENGINE\_LOG](#input\_PROJECT\_FLEETENGINE\_LOG) | Project ID of the project where Fleet Engine logs are persisted. (LogSink of Cloud Logging settings.) | `string` | n/a | yes |
| <a name="input_PROJECT_FLEETEVENTS"></a> [PROJECT\_FLEETEVENTS](#input\_PROJECT\_FLEETEVENTS) | Project ID of the project in which to setup Fleet Events reference solution. | `string` | n/a | yes |
| <a name="input_PROJECT_FLEETEVENTS_NAME"></a> [PROJECT\_FLEETEVENTS\_NAME](#input\_PROJECT\_FLEETEVENTS\_NAME) | Human-readable name of the project in which to setup Fleet Events reference solution | `string` | `"Fleet Events"` | no |
| <a name="input_TOPIC_FLEETENGINE_LOG"></a> [TOPIC\_FLEETENGINE\_LOG](#input\_TOPIC\_FLEETENGINE\_LOG) | Pub/Sub Topic to which Fleet Engine logs are published following Cloud Logging setup. | `string` | n/a | yes |

## Outputs

| Name | Description |
|------|-------------|
| <a name="output_function"></a> [function](#output\_function) | defails of function deployed |
| <a name="output_logging_config"></a> [logging\_config](#output\_logging\_config) | details of logging config |
<!-- END_TF_DOCS -->