
# Module : fleet-events-beam

This module will setup Apache Beam based Fleet Events pipeline on Cloud Dataflow.

## Features

* Turn on prerequisite services in the project
* Flex template
  * Create Artifact registry repository for images (DOCKER)
  * Create Cloud Storage bucket for template spec json file
  * build uber jar
  * submit container image to repository
  * upload template spec filei to Cloud Storage bucket
* Layout the infrustructure for Dataflow pipline
  * VPC network and subnetwork
    * without public IPs
    * private access enabled
    * Firewall rules that allow communication between worker nodes
  * Cloud Storage bucket for staging and logs
* Dataflow job creation

## Usage examples

examples usage of this module can be found under folder "[examples](./examples/)"

* [with_existing_project](./examples/with_existing_project) : setting up reference solution in a existing project

### provisioning

Follow below to get started with minimum steps.

```shell
# change working directory to the example's folder
cd ./examples/with_existing_project

# copy sample terraform.tfvars
cp terraform.tfvars.sample terraform.tfvars
# TODO: update terraform.tfvars to reflect your environment

# initialization
terraform init 

# let Terraform compare the configuration with your own project and comeup with an execution plan
terraform plan

# deploy the reference solution
terraform apply
```

### un-provisioning

```bash
# reverse the deployment
terraform destroy
```

note:

* Terraform does not delete Firestore database instance. Manual removal also not possible in the Cloud console. `gcloud alpha firestore databases delete` command does allow deletion. However, the database is not immediately deleted after this command execution and is rather hidden and put in queue. For that reason, the same database instance name cannot be reused for a while. (30 days)  Therefore, when re-provisioning, suggest using a fresh database name.

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
| [google_compute_network.vpc-network](https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/compute_network) | resource |
| [google_compute_region_network_firewall_policy.firewall_policy_dataflow](https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/compute_region_network_firewall_policy) | resource |
| [google_compute_region_network_firewall_policy_association.primary](https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/compute_region_network_firewall_policy_association) | resource |
| [google_compute_region_network_firewall_policy_rule.firewall-egress](https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/compute_region_network_firewall_policy_rule) | resource |
| [google_compute_region_network_firewall_policy_rule.firewall-ingress](https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/compute_region_network_firewall_policy_rule) | resource |
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
| [google_storage_bucket_iam_member.bucket_template_iam_me](https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/storage_bucket_iam_member) | resource |
| [google_storage_bucket_iam_member.bucket_template_iam_sa](https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/storage_bucket_iam_member) | resource |
| [google_storage_bucket_iam_member.bucket_template_iam_sa_build](https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/storage_bucket_iam_member) | resource |
| [random_id.jobname_suffix](https://registry.terraform.io/providers/hashicorp/random/latest/docs/resources/id) | resource |
| [random_id.template_suffix](https://registry.terraform.io/providers/hashicorp/random/latest/docs/resources/id) | resource |
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
| <a name="input_DATAFLOW_MACHINE_TYPE"></a> [DATAFLOW\_MACHINE\_TYPE](#input\_DATAFLOW\_MACHINE\_TYPE) | The Compute Engine machine type that Dataflow uses when starting worker VMs. You can use any available Compute Engine machine type families or custom machine types. | `string` | `"n1-standard-4"` | no |
| <a name="input_DATAFLOW_MAX_NUM_WORKERS"></a> [DATAFLOW\_MAX\_NUM\_WORKERS](#input\_DATAFLOW\_MAX\_NUM\_WORKERS) | The maximum number of Compute Engine instances to be made available to your pipeline during execution. This value can be higher than the initial number of workers (specified by numWorkers) to allow your job to scale up, automatically or otherwise. | `number` | `3` | no |
| <a name="input_DATAFLOW_NUM_WORKERS"></a> [DATAFLOW\_NUM\_WORKERS](#input\_DATAFLOW\_NUM\_WORKERS) | The initial number of Compute Engine instances to use when executing your pipeline. This option determines how many workers the Dataflow service starts up when your job begins. | `number` | `1` | no |
| <a name="input_DATAFLOW_USE_PUBLIC_IPS"></a> [DATAFLOW\_USE\_PUBLIC\_IPS](#input\_DATAFLOW\_USE\_PUBLIC\_IPS) | Specifies whether Dataflow workers use external IP addresses. If the value is set to false, Dataflow workers use internal IP addresses for all communication. | `bool` | `false` | no |
| <a name="input_FLAG_SETUP_PUBSUB_SUB_BQ"></a> [FLAG\_SETUP\_PUBSUB\_SUB\_BQ](#input\_FLAG\_SETUP\_PUBSUB\_SUB\_BQ) | whether to setup a subscription for the Pub/Sub topic | `bool` | `true` | no |
| <a name="input_FLEETEVENTS_FUNCTION_NAME"></a> [FLEETEVENTS\_FUNCTION\_NAME](#input\_FLEETEVENTS\_FUNCTION\_NAME) | sample function to run. | `string` | `"TASK_OUTCOME_CHANGE"` | no |
| <a name="input_FLEETEVENTS_GAP_SIZE"></a> [FLEETEVENTS\_GAP\_SIZE](#input\_FLEETEVENTS\_GAP\_SIZE) | How long to wait (in minutes) before considering a Vehicle to be offline. | `number` | `3` | no |
| <a name="input_FLEETEVENTS_WINDOW_SIZE"></a> [FLEETEVENTS\_WINDOW\_SIZE](#input\_FLEETEVENTS\_WINDOW\_SIZE) | Window size to use to process events, in minutes. This parameter does not apply to VEHICLE\_NOT\_UPDATING jobs. | `number` | `3` | no |
| <a name="input_GCP_REGION"></a> [GCP\_REGION](#input\_GCP\_REGION) | For resources than can be constrained run or store data within a GCP region, the default region of preference. | `string` | `"us-central1"` | no |
| <a name="input_GCP_REGION_FIRESTORE"></a> [GCP\_REGION\_FIRESTORE](#input\_GCP\_REGION\_FIRESTORE) | Refer to https://cloud.google.com/firestore/docs/locations to choose GCP Region for Firestore. If not set, will use value of GCP\_REGION. | `string` | n/a | yes |
| <a name="input_ME"></a> [ME](#input\_ME) | Account ID (in the form of email address) of the developer running terraform. | `string` | n/a | yes |
| <a name="input_PIPELINE_CLASS"></a> [PIPELINE\_CLASS](#input\_PIPELINE\_CLASS) | The Java class of the Beam pipeline | `string` | `"com.google.fleetevents.beam.FleetEventRunner"` | no |
| <a name="input_PIPELINE_NAME"></a> [PIPELINE\_NAME](#input\_PIPELINE\_NAME) | Name of the Beam Pipeline. This will become the prefix of the Dataflow/Beam pipeline Job. | `string` | `"fleetevents-pipeline"` | no |
| <a name="input_PROJECT_APP"></a> [PROJECT\_APP](#input\_PROJECT\_APP) | The project to setup resources | `string` | n/a | yes |
| <a name="input_PROJECT_FLEETENGINE"></a> [PROJECT\_FLEETENGINE](#input\_PROJECT\_FLEETENGINE) | Project ID of the project where Fleet Engine (ODRD/LMFS) is enabled. | `string` | n/a | yes |
| <a name="input_PROJECT_FLEETENGINE_LOG"></a> [PROJECT\_FLEETENGINE\_LOG](#input\_PROJECT\_FLEETENGINE\_LOG) | Project ID of the project where Fleet Engine logs are persisted. (LogSink of Cloud Logging settings.) | `string` | n/a | yes |
| <a name="input_SA_APP_ROLES"></a> [SA\_APP\_ROLES](#input\_SA\_APP\_ROLES) | Project level IAM Roles the pipeline runtime Service Account requires. For example, it might require roles/datastore.user to use Datastore. | `list(string)` | `[]` | no |
| <a name="input_SA_FLEETENGINE_ROLES"></a> [SA\_FLEETENGINE\_ROLES](#input\_SA\_FLEETENGINE\_ROLES) | Project level IAM Roles the pipeline's FleetEngine Service Account requires. If read only, roles such as roles/fleetengine.deliveryFleetReader can be sufficient | `list(string)` | <pre>[<br>  "roles/fleetengine.deliveryFleetReader"<br>]</pre> | no |
| <a name="input_TEMPLATE_NAME"></a> [TEMPLATE\_NAME](#input\_TEMPLATE\_NAME) | Dataflow Flex template name | `string` | `"fleetevents-beam"` | no |
| <a name="input_TOPIC_INPUT"></a> [TOPIC\_INPUT](#input\_TOPIC\_INPUT) | Pub/Sub Topic to which Fleet Engine logs are published following Cloud Logging setup. | `string` | n/a | yes |
| <a name="input_TOPIC_OUTPUT"></a> [TOPIC\_OUTPUT](#input\_TOPIC\_OUTPUT) | Pub/Sub Topic to which FleetEvents pipeline will publish events. | `string` | n/a | yes |

## Outputs

| Name | Description |
|------|-------------|
| <a name="output_network"></a> [network](#output\_network) | n/a |
| <a name="output_pipeline"></a> [pipeline](#output\_pipeline) | n/a |
<!-- END_TF_DOCS -->