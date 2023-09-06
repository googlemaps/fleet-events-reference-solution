<!-- terraform-docs markdown table --output-mode insert --output-file ./README.md . -->

<!-- BEGIN_TF_DOCS -->
## Requirements

No requirements.

## Providers

| Name | Version |
|------|---------|
| <a name="provider_google"></a> [google](#provider\_google) | n/a |
| <a name="provider_google-beta"></a> [google-beta](#provider\_google-beta) | n/a |
| <a name="provider_local"></a> [local](#provider\_local) | n/a |
| <a name="provider_random"></a> [random](#provider\_random) | n/a |
| <a name="provider_terraform"></a> [terraform](#provider\_terraform) | n/a |

## Modules

No modules.

## Resources

| Name | Type |
|------|------|
| [google-beta_google_dataflow_flex_template_job.beam_job](https://registry.terraform.io/providers/hashicorp/google-beta/latest/docs/resources/google_dataflow_flex_template_job) | resource |
| [google_artifact_registry_repository.repo](https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/artifact_registry_repository) | resource |
| [google_bigquery_dataset.topic-output-bq-subscription-dataset](https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/bigquery_dataset) | resource |
| [google_bigquery_table.topic-output-bq-subscription-table](https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/bigquery_table) | resource |
| [google_compute_firewall.firewall_rule_egress](https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/compute_firewall) | resource |
| [google_compute_firewall.firewall_rule_ingress](https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/compute_firewall) | resource |
| [google_compute_network.vpc-network](https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/compute_network) | resource |
| [google_compute_subnetwork.vpc-subnetwork](https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/compute_subnetwork) | resource |
| [google_firestore_database.database](https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/firestore_database) | resource |
| [google_project_iam_member.bq_data_editor](https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/project_iam_member) | resource |
| [google_project_iam_member.bq_metadata_viewer](https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/project_iam_member) | resource |
| [google_project_iam_member.project_iam_sa_app](https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/project_iam_member) | resource |
| [google_project_service.gcp_services](https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/project_service) | resource |
| [google_pubsub_subscription.topic-output-subscription-bigquery](https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/pubsub_subscription) | resource |
| [google_pubsub_topic.topic-fleetevents-output](https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/pubsub_topic) | resource |
| [google_pubsub_topic_iam_member.input-subscriber-sa](https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/pubsub_topic_iam_member) | resource |
| [google_pubsub_topic_iam_member.output-publisher-sa](https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/pubsub_topic_iam_member) | resource |
| [google_service_account.sa_app](https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/service_account) | resource |
| [google_service_account_iam_binding.sa_app_iam](https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/service_account_iam_binding) | resource |
| [google_storage_bucket.bucket](https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/storage_bucket) | resource |
| [google_storage_bucket.bucket_template](https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/storage_bucket) | resource |
| [google_storage_bucket_iam_member.bucket_iam_me](https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/storage_bucket_iam_member) | resource |
| [google_storage_bucket_iam_member.bucket_iam_sa](https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/storage_bucket_iam_member) | resource |
| [google_storage_bucket_iam_member.bucket_iam_sa_build](https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/storage_bucket_iam_member) | resource |
| [google_storage_bucket_iam_member.bucket_template_iam_me](https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/storage_bucket_iam_member) | resource |
| [google_storage_bucket_iam_member.bucket_template_iam_sa](https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/storage_bucket_iam_member) | resource |
| [google_storage_bucket_iam_member.bucket_template_iam_sa_build](https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/storage_bucket_iam_member) | resource |
| [random_id.jobname_suffix](https://registry.terraform.io/providers/hashicorp/random/latest/docs/resources/id) | resource |
| [terraform_data.script_build_flex_template](https://registry.terraform.io/providers/hashicorp/terraform/latest/docs/resources/data) | resource |
| [terraform_data.script_build_jar](https://registry.terraform.io/providers/hashicorp/terraform/latest/docs/resources/data) | resource |
| [google_project.project_fleetengine](https://registry.terraform.io/providers/hashicorp/google/latest/docs/data-sources/project) | data source |
| [google_project.project_fleetengine_logs](https://registry.terraform.io/providers/hashicorp/google/latest/docs/data-sources/project) | data source |
| [google_project.project_fleetevents](https://registry.terraform.io/providers/hashicorp/google/latest/docs/data-sources/project) | data source |
| [google_pubsub_topic.topic-fleetevents-input](https://registry.terraform.io/providers/hashicorp/google/latest/docs/data-sources/pubsub_topic) | data source |
| [google_storage_bucket_object.template_spec](https://registry.terraform.io/providers/hashicorp/google/latest/docs/data-sources/storage_bucket_object) | data source |
| [local_file.pom_xml](https://registry.terraform.io/providers/hashicorp/local/latest/docs/data-sources/file) | data source |
| [local_file.src_pipeline](https://registry.terraform.io/providers/hashicorp/local/latest/docs/data-sources/file) | data source |

## Inputs

| Name | Description | Type | Default | Required |
|------|-------------|------|---------|:--------:|
| <a name="input_DATABASE_NAME"></a> [DATABASE\_NAME](#input\_DATABASE\_NAME) | Firestore database instance name | `string` | `"fleetevents-db"` | no |
| <a name="input_FLAG_SETUP_PUBSUB_SUB_BQ"></a> [FLAG\_SETUP\_PUBSUB\_SUB\_BQ](#input\_FLAG\_SETUP\_PUBSUB\_SUB\_BQ) | whether to setup a subscription for the Pub/Sub topic | `bool` | `true` | no |
| <a name="input_GCP_REGION"></a> [GCP\_REGION](#input\_GCP\_REGION) | For resources than can be constrained run or store data within a GCP region, the default region of preference. | `string` | `"us-central1"` | no |
| <a name="input_GCP_REGION_FIRESTORE"></a> [GCP\_REGION\_FIRESTORE](#input\_GCP\_REGION\_FIRESTORE) | Refer to https://cloud.google.com/firestore/docs/locations to choose GCP Region for Firestore. If not set, will use value of GCP\_REGION. | `string` | n/a | yes |
| <a name="input_ME"></a> [ME](#input\_ME) | Account ID (in the form of email address) of the developer running terraform. | `string` | n/a | yes |
| <a name="input_PIPELINE_CLASS"></a> [PIPELINE\_CLASS](#input\_PIPELINE\_CLASS) | n/a | `string` | `"com.google.fleetevents.beam.FleetEventRunner"` | no |
| <a name="input_PIPELINE_NAME"></a> [PIPELINE\_NAME](#input\_PIPELINE\_NAME) | Name of the Beam Pipeline. | `string` | n/a | yes |
| <a name="input_PROJECT_APP"></a> [PROJECT\_APP](#input\_PROJECT\_APP) | The project to setup resources | `string` | n/a | yes |
| <a name="input_PROJECT_FLEETENGINE"></a> [PROJECT\_FLEETENGINE](#input\_PROJECT\_FLEETENGINE) | Project ID of the project where Fleet Engine (ODRD/LMFS) is enabled. | `string` | n/a | yes |
| <a name="input_PROJECT_FLEETENGINE_LOG"></a> [PROJECT\_FLEETENGINE\_LOG](#input\_PROJECT\_FLEETENGINE\_LOG) | Project ID of the project where Fleet Engine logs are persisted. (LogSink of Cloud Logging settings.) | `string` | n/a | yes |
| <a name="input_SA_APP_ROLES"></a> [SA\_APP\_ROLES](#input\_SA\_APP\_ROLES) | Project level IAM Roles the Function's runtime Service Account requires. For example, it might require roles/datastore.user to use Datastore. | `list(string)` | `[]` | no |
| <a name="input_TEMPLATE_NAME"></a> [TEMPLATE\_NAME](#input\_TEMPLATE\_NAME) | Dataflow Flex template name | `string` | `"fleetevents-beam"` | no |
| <a name="input_TOPIC_INPUT"></a> [TOPIC\_INPUT](#input\_TOPIC\_INPUT) | Pub/Sub Topic to which Fleet Engine logs are published following Cloud Logging setup. | `string` | n/a | yes |
| <a name="input_TOPIC_OUTPUT"></a> [TOPIC\_OUTPUT](#input\_TOPIC\_OUTPUT) | Pub/Sub Topic to which FleetEvents pipeline will publish events. | `string` | n/a | yes |

## Outputs

| Name | Description |
|------|-------------|
| <a name="output_bucket"></a> [bucket](#output\_bucket) | n/a |
| <a name="output_container_spec"></a> [container\_spec](#output\_container\_spec) | n/a |
| <a name="output_script_build_jar"></a> [script\_build\_jar](#output\_script\_build\_jar) | n/a |
| <a name="output_topic-fleetevents-input"></a> [topic-fleetevents-input](#output\_topic-fleetevents-input) | n/a |
| <a name="output_topic-fleetevents-output"></a> [topic-fleetevents-output](#output\_topic-fleetevents-output) | n/a |
| <a name="output_vpc-subnetwork"></a> [vpc-subnetwork](#output\_vpc-subnetwork) | n/a |
| <a name="output_vpc_network"></a> [vpc\_network](#output\_vpc\_network) | output "subscription\_fleetenginelogs" { value = google\_pubsub\_subscription.subscription-fleetenginelogs } |
<!-- END_TF_DOCS -->