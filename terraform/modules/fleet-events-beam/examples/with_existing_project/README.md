<!-- BEGIN_TF_DOCS -->
## Requirements

No requirements.

## Providers

No providers.

## Modules

| Name | Source | Version |
|------|--------|---------|
| <a name="module_fleetevents-beam"></a> [fleetevents-beam](#module\_fleetevents-beam) | ../../ | n/a |
| <a name="module_logging_config"></a> [logging\_config](#module\_logging\_config) | ../../../fleetengine-logging-config/ | n/a |

## Resources

No resources.

## Inputs

| Name | Description | Type | Default | Required |
|------|-------------|------|---------|:--------:|
| <a name="input_FLEETEVENTS_FUNCTION_NAME"></a> [FLEETEVENTS\_FUNCTION\_NAME](#input\_FLEETEVENTS\_FUNCTION\_NAME) | Fleet Event function that you'd like to run. Refer to beam/README.md for possible values. | `string` | `"TASK_OUTCOME_CHANGE"` | no |
| <a name="input_GCP_REGION"></a> [GCP\_REGION](#input\_GCP\_REGION) | For resources than can be constrained run or store data within a GCP region, the default region of preference. | `string` | `"us-central1"` | no |
| <a name="input_GCP_REGION_FIRESTORE"></a> [GCP\_REGION\_FIRESTORE](#input\_GCP\_REGION\_FIRESTORE) | Refer to https://cloud.google.com/firestore/docs/locations to choose GCP Region for Firestore. If not set, will use value of GCP\_REGION. | `string` | `"nam5"` | no |
| <a name="input_ME"></a> [ME](#input\_ME) | Account ID (in the form of email address) of the developer running terraform. | `string` | n/a | yes |
| <a name="input_PIPELINE_NAME"></a> [PIPELINE\_NAME](#input\_PIPELINE\_NAME) | Name of the Beam Pipeline. This will become the prefix of the Dataflow/Beam pipeline Job. | `string` | `"fleetevents-pipeline"` | no |
| <a name="input_PROJECT_FLEETENGINE"></a> [PROJECT\_FLEETENGINE](#input\_PROJECT\_FLEETENGINE) | Project ID of the project where Fleet Engine (ODRD/LMFS) is enabled. | `string` | n/a | yes |
| <a name="input_PROJECT_FLEETEVENTS"></a> [PROJECT\_FLEETEVENTS](#input\_PROJECT\_FLEETEVENTS) | The project to setup resources | `string` | n/a | yes |

## Outputs

| Name | Description |
|------|-------------|
| <a name="output_fleetevents-beam"></a> [fleetevents-beam](#output\_fleetevents-beam) | n/a |
| <a name="output_logging_config"></a> [logging\_config](#output\_logging\_config) | n/a |
<!-- END_TF_DOCS -->