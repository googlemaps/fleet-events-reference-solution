<!-- BEGIN_TF_DOCS -->
## Modules

| Name | Source | Version |
|------|--------|---------|
| <a name="module_logging"></a> [logging](#module\_logging) | ../../ | n/a |

## Resources

| Name | Type |
|------|------|
| [google_project.project-logging](https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/project) | resource |
| [google_project.project-fleetengine-ap](https://registry.terraform.io/providers/hashicorp/google/latest/docs/data-sources/project) | data source |
| [google_project.project-fleetengine-eu](https://registry.terraform.io/providers/hashicorp/google/latest/docs/data-sources/project) | data source |
| [google_project.project-fleetengine-us](https://registry.terraform.io/providers/hashicorp/google/latest/docs/data-sources/project) | data source |

## Inputs

| Name | Description | Type | Default | Required |
|------|-------------|------|---------|:--------:|
| <a name="input_BILLING_ACCOUNT"></a> [BILLING\_ACCOUNT](#input\_BILLING\_ACCOUNT) | Billing Account to which the new project will be associated | `string` | n/a | yes |
| <a name="input_FOLDER_ID"></a> [FOLDER\_ID](#input\_FOLDER\_ID) | Folder under which the new project is to be created | `string` | n/a | yes |
| <a name="input_GCP_REGION_LOGGING"></a> [GCP\_REGION\_LOGGING](#input\_GCP\_REGION\_LOGGING) | a region in which logs are to be consolidated. | `string` | `"europe-west3"` | no |
| <a name="input_ME"></a> [ME](#input\_ME) | user running terraform. will be given access rights to resources. | `string` | n/a | yes |
| <a name="input_PROJECT_FLEETENGINE_AP"></a> [PROJECT\_FLEETENGINE\_AP](#input\_PROJECT\_FLEETENGINE\_AP) | Project with Fleet Engine (ODRD/LMFS) enabled. | `string` | n/a | yes |
| <a name="input_PROJECT_FLEETENGINE_EU"></a> [PROJECT\_FLEETENGINE\_EU](#input\_PROJECT\_FLEETENGINE\_EU) | Project with Fleet Engine (ODRD/LMFS) enabled. | `string` | n/a | yes |
| <a name="input_PROJECT_FLEETENGINE_US"></a> [PROJECT\_FLEETENGINE\_US](#input\_PROJECT\_FLEETENGINE\_US) | Project with Fleet Engine (ODRD/LMFS) enabled. | `string` | n/a | yes |
| <a name="input_PROJECT_LOGGINGSYNC_ID"></a> [PROJECT\_LOGGINGSYNC\_ID](#input\_PROJECT\_LOGGINGSYNC\_ID) | Project id of the new project for Logging | `string` | n/a | yes |
| <a name="input_PROJECT_LOGGINGSYNC_NAME"></a> [PROJECT\_LOGGINGSYNC\_NAME](#input\_PROJECT\_LOGGINGSYNC\_NAME) | Project name (human readable) of the new project for Logging | `string` | `"My Logging Project"` | no |

## Outputs

| Name | Description |
|------|-------------|
| <a name="output_links-ap"></a> [links-ap](#output\_links-ap) | n/a |
| <a name="output_links-eu"></a> [links-eu](#output\_links-eu) | n/a |
| <a name="output_links-us"></a> [links-us](#output\_links-us) | n/a |
| <a name="output_logrouters-ap"></a> [logrouters-ap](#output\_logrouters-ap) | n/a |
| <a name="output_logrouters-eu"></a> [logrouters-eu](#output\_logrouters-eu) | n/a |
| <a name="output_logrouters-us"></a> [logrouters-us](#output\_logrouters-us) | n/a |
<!-- END_TF_DOCS -->