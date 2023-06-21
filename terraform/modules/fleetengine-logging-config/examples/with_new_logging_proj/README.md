# Setting up Cloud Logging integration for Fleet Engine

This example setups Cloud Logging integration for Fleet Engine to a new Google Cloud project.

First, create file `terraform.tfvars` with the following :

```yaml
# fleet engine project
PROJECT_FLEETENGINE       = "[Your Mobility(ODRD/LMFS) project]"

# new project
PROJECT_LOGGINGSYNC_ID   = "[Logging Project ID]"
PROJECT_LOGGINGSYNC_NAME = "[Logging Project Name]"
FOLDER_ID                = "[Folder in which project will be created]"
BILLING_ACCOUNT          = "[BIlling account to be associated to project]"

# other params
GCP_REGION                = "[GCP Region to use]"
ME                        = "[Your Google account email address]"
```

Then run :
```Shell
terraform init
terraform plan
terraform apply
```

<!--  terraform-docs markdown table --output-mode insert --show data-sources,inputs,modules,outputs,resources --output-file ./README.md . -->

<!-- BEGIN_TF_DOCS -->
## Modules

| Name | Source | Version |
|------|--------|---------|
| <a name="module_logging"></a> [logging](#module\_logging) | ../../ | n/a |

## Resources

| Name | Type |
|------|------|
| [google_project.project-logging](https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/project) | resource |
| [google_project.project-fleetengine](https://registry.terraform.io/providers/hashicorp/google/latest/docs/data-sources/project) | data source |

## Inputs

| Name | Description | Type | Default | Required |
|------|-------------|------|---------|:--------:|
| <a name="input_BILLING_ACCOUNT"></a> [BILLING\_ACCOUNT](#input\_BILLING\_ACCOUNT) | Billing Account to which the new project will be associated | `string` | n/a | yes |
| <a name="input_FOLDER_ID"></a> [FOLDER\_ID](#input\_FOLDER\_ID) | Folder under which the new project is to be created | `string` | n/a | yes |
| <a name="input_GCP_REGION"></a> [GCP\_REGION](#input\_GCP\_REGION) | For resources than can be constrained to sit within a GCP region, a region of preference. | `string` | `"asia-southeast1"` | no |
| <a name="input_ME"></a> [ME](#input\_ME) | user running terraform. will be given access rights to resources. | `string` | n/a | yes |
| <a name="input_PROJECT_FLEETENGINE"></a> [PROJECT\_FLEETENGINE](#input\_PROJECT\_FLEETENGINE) | Project with Fleet Engine (ODRD/LMFS) enabled. | `string` | n/a | yes |
| <a name="input_PROJECT_LOGGINGSYNC_ID"></a> [PROJECT\_LOGGINGSYNC\_ID](#input\_PROJECT\_LOGGINGSYNC\_ID) | Project id of the new project for Logging | `string` | n/a | yes |
| <a name="input_PROJECT_LOGGINGSYNC_NAME"></a> [PROJECT\_LOGGINGSYNC\_NAME](#input\_PROJECT\_LOGGINGSYNC\_NAME) | Project name (human readable) of the new project for Logging | `string` | `"My Logging Project"` | no |

## Outputs

| Name | Description |
|------|-------------|
| <a name="output_links"></a> [links](#output\_links) | direct link to related pages in Cloud Console |
| <a name="output_logrouters"></a> [logrouters](#output\_logrouters) | details of log routers |
<!-- END_TF_DOCS -->