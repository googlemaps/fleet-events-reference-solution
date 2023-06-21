<!-- terraform-docs markdown table --output-mode insert --output-file ./README.md . -->

<!-- BEGIN_TF_DOCS -->
## Modules

No modules.

## Resources

| Name | Type |
|------|------|
| [google_cloud_run_service_iam_member.run_iam](https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/cloud_run_service_iam_member) | resource |
| [google_cloudfunctions2_function.function](https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/cloudfunctions2_function) | resource |
| [google_cloudfunctions2_function_iam_member.function_iam](https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/cloudfunctions2_function_iam_member) | resource |
| [google_project_iam_member.project_fleetengine_iam](https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/project_iam_member) | resource |
| [google_project_iam_member.project_iam_sa_app](https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/project_iam_member) | resource |
| [google_project_service.gcp_services](https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/project_service) | resource |
| [google_pubsub_topic_iam_member.member](https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/pubsub_topic_iam_member) | resource |
| [google_service_account.sa_app](https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/service_account) | resource |
| [google_service_account.sa_fleetengine](https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/service_account) | resource |
| [google_service_account.sa_trigger](https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/service_account) | resource |
| [google_service_account_iam_binding.sa_fleetengine_iam](https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/service_account_iam_binding) | resource |
| [google_storage_bucket.bucket](https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/storage_bucket) | resource |
| [google_storage_bucket_object.function_source](https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/storage_bucket_object) | resource |
| [archive_file.zipfile](https://registry.terraform.io/providers/hashicorp/archive/latest/docs/data-sources/file) | data source |
| [google_cloud_run_service.function_run](https://registry.terraform.io/providers/hashicorp/google/latest/docs/data-sources/cloud_run_service) | data source |
| [google_project.project](https://registry.terraform.io/providers/hashicorp/google/latest/docs/data-sources/project) | data source |
| [google_project.project_fleetengine](https://registry.terraform.io/providers/hashicorp/google/latest/docs/data-sources/project) | data source |
| [google_project.project_fleetengine_logs](https://registry.terraform.io/providers/hashicorp/google/latest/docs/data-sources/project) | data source |
| [google_pubsub_topic.fleetengine-logging-topic](https://registry.terraform.io/providers/hashicorp/google/latest/docs/data-sources/pubsub_topic) | data source |

## Inputs

| Name | Description | Type | Default | Required |
|------|-------------|------|---------|:--------:|
| <a name="input_FUNCTION_ADDITIONAL_ENV_VARS"></a> [FUNCTION\_ADDITIONAL\_ENV\_VARS](#input\_FUNCTION\_ADDITIONAL\_ENV\_VARS) | parameters passed to function as environmental variables. | `map(string)` | n/a | yes |
| <a name="input_FUNCTION_AVAILABLE_MEMORY"></a> [FUNCTION\_AVAILABLE\_MEMORY](#input\_FUNCTION\_AVAILABLE\_MEMORY) | The amount of memory available for a function. Supported units are k, M, G, Mi, Gi. If no unit is supplied the value is interpreted as bytes. | `string` | `"256M"` | no |
| <a name="input_FUNCTION_DESCRIPTION"></a> [FUNCTION\_DESCRIPTION](#input\_FUNCTION\_DESCRIPTION) | Description of the Cloud Function. | `string` | n/a | yes |
| <a name="input_FUNCTION_ENTRYPOINT"></a> [FUNCTION\_ENTRYPOINT](#input\_FUNCTION\_ENTRYPOINT) | Entry Point of the Cloud Function | `string` | n/a | yes |
| <a name="input_FUNCTION_MAX_INSTANCE_COUNT"></a> [FUNCTION\_MAX\_INSTANCE\_COUNT](#input\_FUNCTION\_MAX\_INSTANCE\_COUNT) | The limit on the maximum number of function instances that may coexist at a given time. | `number` | `3` | no |
| <a name="input_FUNCTION_MAX_REQUEST_CONCURRENCY"></a> [FUNCTION\_MAX\_REQUEST\_CONCURRENCY](#input\_FUNCTION\_MAX\_REQUEST\_CONCURRENCY) | Sets the maximum number of concurrent requests that each instance can receive. only set value >1 when using larger instances (above 1vCPU, equivalent to 2GiB+ memory) https://cloud.google.com/functions/docs/configuring/memory | `number` | `1` | no |
| <a name="input_FUNCTION_NAME"></a> [FUNCTION\_NAME](#input\_FUNCTION\_NAME) | Name of the Cloud Function. This will used as the indentifier of the Cloud Function (v2), and has to follow naming rules. Only lower case alphanumeric and '-' allowed. | `string` | n/a | yes |
| <a name="input_FUNCTION_RUNTIME"></a> [FUNCTION\_RUNTIME](#input\_FUNCTION\_RUNTIME) | Function runtime (java11, java17, etc.) | `string` | `"java11"` | no |
| <a name="input_FUNCTION_SRC_DIR"></a> [FUNCTION\_SRC\_DIR](#input\_FUNCTION\_SRC\_DIR) | Path to the directory where the Cloud Functions source code is located. | `string` | n/a | yes |
| <a name="input_FUNCTION_SRC_EXCLUDE_FILES"></a> [FUNCTION\_SRC\_EXCLUDE\_FILES](#input\_FUNCTION\_SRC\_EXCLUDE\_FILES) | list of files under source folder to be excluded from source zip. | `list(string)` | `[]` | no |
| <a name="input_FUNCTION_SRC_EXCLUDE_PATTERNS"></a> [FUNCTION\_SRC\_EXCLUDE\_PATTERNS](#input\_FUNCTION\_SRC\_EXCLUDE\_PATTERNS) | list of file patterns under source folder to be excluded from source zip. | `list(string)` | `[]` | no |
| <a name="input_GCP_REGION"></a> [GCP\_REGION](#input\_GCP\_REGION) | For resources than can be constrained run or store data within a GCP region, the default region of preference. | `string` | `"us-central1"` | no |
| <a name="input_GCP_REGION_FIRESTORE"></a> [GCP\_REGION\_FIRESTORE](#input\_GCP\_REGION\_FIRESTORE) | GCP Region for Firestore.  If not set, will use value of GCP\_REGION. Reference and choose the right one for your deployement https://cloud.google.com/firestore/docs/locations | `string` | `"nam5"` | no |
| <a name="input_GCP_REGION_FUNCTIONS"></a> [GCP\_REGION\_FUNCTIONS](#input\_GCP\_REGION\_FUNCTIONS) | GCP Region for Function deployment. If not set, the value of GCP\_REGION will be applied. Reference and choose the right one for your deployment https://cloud.google.com/functions/docs/locations | `string` | `"us-central1"` | no |
| <a name="input_GCP_REGION_STORAGE"></a> [GCP\_REGION\_STORAGE](#input\_GCP\_REGION\_STORAGE) | GCP Region for Cloud Storage Buckets.  If not set, the value of GCP\_REGION will be applied. Reference and choose the right one for your deployment https://cloud.google.com/storage/docs/locations | `string` | `"us-central1"` | no |
| <a name="input_PROJECT_APP"></a> [PROJECT\_APP](#input\_PROJECT\_APP) | The project to setup resources | `string` | n/a | yes |
| <a name="input_PROJECT_FLEETENGINE"></a> [PROJECT\_FLEETENGINE](#input\_PROJECT\_FLEETENGINE) | Project ID of the project where Fleet Engine (ODRD/LMFS) is enabled. | `string` | n/a | yes |
| <a name="input_PROJECT_FLEETENGINE_LOG"></a> [PROJECT\_FLEETENGINE\_LOG](#input\_PROJECT\_FLEETENGINE\_LOG) | Project ID of the project where Fleet Engine logs are persisted. (LogSink of Cloud Logging settings.) | `string` | n/a | yes |
| <a name="input_SA_APP_ROLES"></a> [SA\_APP\_ROLES](#input\_SA\_APP\_ROLES) | Project level IAM Roles the Function's runtime Service Account requires. For example, it might require roles/datastore.user to use Datastore. | `list(string)` | `[]` | no |
| <a name="input_SA_FLEETENGINE_ROLES"></a> [SA\_FLEETENGINE\_ROLES](#input\_SA\_FLEETENGINE\_ROLES) | Project level IAM Roles the Function's FleetEngine Service Account requires. If read only, roles such as roles/fleetengine.deliveryFleetReader can be sufficient | `list(string)` | <pre>[<br>  "roles/fleetengine.serviceSuperUser"<br>]</pre> | no |
| <a name="input_SUBSCRIPTION_FILTER"></a> [SUBSCRIPTION\_FILTER](#input\_SUBSCRIPTION\_FILTER) | filter to be applied to limit messages that reach the function. | `string` | `""` | no |
| <a name="input_TOPIC_FLEETENGINE_LOG"></a> [TOPIC\_FLEETENGINE\_LOG](#input\_TOPIC\_FLEETENGINE\_LOG) | Pub/Sub Topic to which Fleet Engine logs are published following Cloud Logging setup. | `string` | n/a | yes |
| <a name="input_TOPIC_FLEETEVENTS_OUTPUT"></a> [TOPIC\_FLEETEVENTS\_OUTPUT](#input\_TOPIC\_FLEETEVENTS\_OUTPUT) | Pub/Sub Topic to which the deployed function will be publishing events. | `string` | n/a | yes |

## Outputs

| Name | Description |
|------|-------------|
| <a name="output_function"></a> [function](#output\_function) | Deployed Cloud Function |
| <a name="output_projects"></a> [projects](#output\_projects) | Cloud Projects in use |
| <a name="output_serviceaccounts"></a> [serviceaccounts](#output\_serviceaccounts) | Service accounts in use |
| <a name="output_source"></a> [source](#output\_source) | Zipped source files |
<!-- END_TF_DOCS -->