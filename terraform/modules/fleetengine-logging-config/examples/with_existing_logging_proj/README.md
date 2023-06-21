# Setting up Cloud Logging integration for Fleet Engine

This example setups Cloud Logging integration for Fleet Engine to an existing Google Cloud project.

First, create file `terraform.tfvars` with the following :

```yaml
PROJECT_FLEETENGINE       = "[Your Mobility(ODRD/LMFS) project]"
PROJECT_LOGGINGSYNC       = "[Logging Project]"
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

No resources.

## Inputs

| Name | Description | Type | Default | Required |
|------|-------------|------|---------|:--------:|
| <a name="input_GCP_REGION"></a> [GCP\_REGION](#input\_GCP\_REGION) | For resources than can be constrained to sit within a GCP region, a region of preference. | `string` | `"asia-southeast1"` | no |
| <a name="input_ME"></a> [ME](#input\_ME) | user running terraform. will be given access rights to resources. | `string` | n/a | yes |
| <a name="input_PROJECT_FLEETENGINE"></a> [PROJECT\_FLEETENGINE](#input\_PROJECT\_FLEETENGINE) | Project with Fleet Engine (ODRD/LMFS) enabled. | `string` | n/a | yes |
| <a name="input_PROJECT_LOGGINGSYNC"></a> [PROJECT\_LOGGINGSYNC](#input\_PROJECT\_LOGGINGSYNC) | Project id of the new project for Fleet Events | `string` | n/a | yes |

## Outputs

| Name | Description |
|------|-------------|
| <a name="output_links"></a> [links](#output\_links) | direct link to related pages in Cloud Console |
| <a name="output_logrouters"></a> [logrouters](#output\_logrouters) | details of log routers |
<!-- END_TF_DOCS -->