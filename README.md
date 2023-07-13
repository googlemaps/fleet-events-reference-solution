
# Getting started with “Fleet Events" reference solution 

This document explains how to get started with Fleet Events. It provides step-by-step instructions and technical details to help you understand the components and deploy a working reference solution to your Google Cloud project. This solution can be beneficial for your near-real-time use cases.

Fleet Events uses near-real-time events produced by Fleet Engine as a basis for generating custom events and handlers for your specific requirements. The reference solution in this repository includes code for creating fleet events and automated deployment using Terraform.

You can learn more about real time event processing concepts by referencing [this article](https://docs.google.com/document/d/15zeL34K2SfkHHVMpRD0tYdrDiYDy_WzvHEjlwgDI4X8/edit).

Use cases the example covers :



* Task Outcome
* ETA Updates


## Overview

To adjust the configuration parameters, you need to have at least a high-level understanding of the building blocks before starting the deployment.

The end state :


* There will be two projects, one for Fleet Engine, and one for the Fleet Events reference solution. Projects are separated so that each can be tied to respective billing accounts. (Example: Fleet Events: your GCP billing account.  Fleet Engine: your Google Maps Platform Mobility billing account)
* Logs from Fleet Engine project will be setup to flow into a Cloud Pub/Sub Topic “Fleet Engine Events” in “Fleet Events” project
* A Cloud Function that subscribes to the “Fleet Engine Events” Topic will be deployed. This function will have the implementation unique to the use case.
* A Pub/Sub topic “Custom Events” where generated events will be published.

## Deploy the reference solution


### Prerequisites


#### Information about your environment.

Note down the following information before getting started.



* **Fleet Engine project (project id) **: Your active project that is enabled for  “[On Demand Rides and Deliveries Solution](https://developers.google.com/maps/documentation/transportation-logistics/on-demand-rides-deliveries-solution)” (ODRD) or “[Last Mile Fleet Solution](https://developers.google.com/maps/documentation/transportation-logistics/last-mile-fleet-solution)” (LMFS) is the event source. When handling events, you will often need to access Fleet Engine's APIs to retrieve information. Therefore, your code needs to know which project to access.
    * example) my-fleetengine-project
* **Fleet Events project (project id)** : The project in which this reference solution will be deployed. This project can be pre-created or be generated as part of the automated provisioning. A clean developer sandbox project is highly recommended for safe experiments.
    * example) my-fleetevents-project
* **Billing Account for Google Cloud** : This reference solution will make use of several Google Cloud services. Given you may have different billing arrangements between Google Maps Platform (GMP) and Google Cloud Platform (GCP), we highly recommend a separate billing account for GCP. The Fleet Events project above should be associated with this billing account.
    * example) XXXXXX-XXXXXX-XXXXXX (alpha numeric)
* **Project Folder : **If your organization is adopting a folder structure to manage multiple Google Cloud projects, identify the folder and its id (number) under which the Fleet Events projects should reside.
    * example) XXXXXXXXXXXX (all digit)
* **Developer google account** : for simplicity, the deployment automation will run under a developer’s account and its privilege. The developer will be given permission to resources created.
    * example) XXX@gmail.com


#### Tools and setup



* install terraform : follow the steps for your environment
    * [https://developer.hashicorp.com/terraform/tutorials/gcp-get-started/install-cli](https://developer.hashicorp.com/terraform/tutorials/gcp-get-started/install-cli)
* install cloud cli : follow the steps for your environment
    * [https://cloud.google.com/sdk/docs/install](https://cloud.google.com/sdk/docs/install)
* initialize cloud cli : run the following which will allow terraform to run under your Google Account
    * [https://registry.terraform.io/providers/hashicorp/google/latest/docs/guides/getting_started](https://registry.terraform.io/providers/hashicorp/google/latest/docs/guides/getting_started)


#### Check permission to your configure projects

Terraform will run under your Google account’s permissions. (you can also run terraform under service accounts. Links to resources for such setup will be linked later in this doc)

For testing, give your developer user account IAM role “roles/editor” in both your **Fleet Engine project **and **Fleet Event project. **This gives you most of the permissions required to quickly go through this solution. Additionally, there are a few additional IAM roles to add on

Fleet Engine project



* roles/logging.configWriter : Contains permissions required to create logging sinks that defines where the log messages will be delivered.

Fleet Events project



* roles/iam.securityAdmin : Includes permission to set IAM policies for various resources such as Pub/Sub Topics or Subscriptions, BigQuery datasets, etc.  Required to apply least privilege principles.
* roles/datastore.owner: Includes permission required to create Firestore database instance.

Below is an example to give an user account the required IAM roles with Cloud CLI. The same can be achieved through [manual steps](https://cloud.google.com/iam/docs/granting-changing-revoking-access#single-role) in the Cloud Console UI.

For production deployment, consider tightened access control, including adoption of Service Accounts to detach dependency on certain individuals. Also use custom roles to give the least permission required for setup.


### Access the code

Clone this repo.


#### Repo structure

This git repo is structured as follows.



* README.md 					: this readme doc
* cloud-functions/				: reference functions source code
* terraform/ 					: deployment automation with terraform
    * modules/				: folder holding multiple terraform modules
        * &lt;Module A>			:
            * README.md		: README for the module
            * [main.tf](https://github.com/googlemaps/fleet-events-reference-solution/blob/main/terraform/modules/fleet-events/main.tf)			: main code for the module
            * [variables.tf](https://github.com/googlemaps/fleet-events-reference-solution/blob/main/terraform/modules/fleet-events/variables.tf)		: defines input parameters of the module
            * [outputs.tf](https://github.com/googlemaps/fleet-events-reference-solution/blob/main/terraform/modules/fleet-events/outputs.tf)		: defines outputs of the module
            * *.tf			:
            * Examples		: one or more example use of the module
                * &lt;Example 1>
                    * [README.md](https://github.com/googlemaps/fleet-events-reference-solution/blob/main/terraform/modules/fleet-events/examples/with_existing_project/README.md)			: readme for example
                    * [main.tf](https://github.com/googlemaps/fleet-events-reference-solution/blob/main/terraform/modules/fleet-events/examples/with_existing_project/main.tf)				: code with module usage
                    * *.tf
                    * terraform.tfvars.sample		: sample config file
                * &lt;Example 2>
                * …
* diagrams/ 					: diagrams used in articles
* *.md						: additional articles


### Deploy the references solution

Follow the steps below to deploy the reference solution.


#### STEP 1 : identify the example to start with

Change the Terraform working directory to one of the examples.


#### STEP 2 : create a terraform.tfvars file from example

This example is also a terraform module with input variables defined. These variables can be set in a configuration file “terraform.tfvars”. Copy the sample .tfvars file and edit to match your environment. Your text editor of choice can be used. Example below is using "vi" as the editor.

“terraform.tfvars” can look like this. Adjust the values to match your own environment

The full set of variables that can be set can be referenced in the module’s README (link).


##### Naming rules

Google Cloud applies different naming restrictions depending on the service and resource. The “Fleet Events” deployment script will generate identifiers or labels so that they are easily recognizable by concatenating given resource names. However, concatenation can sometimes hit naming rule limits. Therefore, a general guidance is to not make identifiers too long. Or, if such errors are observed, reconsider the name.

Example :



* Service Accounts: ([ref](https://cloud.google.com/iam/docs/service-accounts-create#:~:text=The%20ID%20must%20be%20between,you%20cannot%20change%20its%20name.))
* Labels: ([ref](https://cloud.google.com/compute/docs/labeling-resources#requirements))
* Cloud Storage:  ([ref](https://cloud.google.com/storage/docs/buckets#naming))
* Cloud Pub/Sub: ([ref](https://cloud.google.com/pubsub/docs/create-topic#resource_names))
* BigQuery: Datasets ([ref](https://cloud.google.com/bigquery/docs/datasets#dataset-naming)), Tables ([ref](https://cloud.google.com/bigquery/docs/tables#table_naming))


#### STEP 3 : Initialize terraform

Initialize terraform before first run. Dependencies (modules, etc.) will be fetched.


#### STEP 4 : terraform plan

Let terraform compare current and idea state and come up with a plan.


#### STEP 5 : Apply changes

If the plan is good to go, apply changes so that terraform can create and configure resources in your project accordingly.

You can learn more about these terraform commands here

[https://cloud.google.com/docs/terraform/basic-commands](https://cloud.google.com/docs/terraform/basic-commands)


#### STEP 6 : Verify your deployment

Use the publisher tool to test the TaskOutcome handler.

Note: `_input_topic_id_` is the topic that your Cloud Function reads from. `_output_topic_id_` should point to the PubSub topic where your deployed cloud function writes its notifications. Ensure the user running the script can read and write from all topics specified.

You can also use the publisher tool to publish test events to Cloud Functions. Sample itineraries that can be published are in folder “./tools/python/data/”. You can follow the steps below:


#### Starting over

When you need to change configuration, update the terraform.tfvar file and rerun terraform from the same folder.

In case you want to start over or clean up the environment, de-provisioning can be done by simply running the following command.


#### Recovering from errors

There are situations where deployment may fail mid-way. This can be due to asynchronous tasks such as service enablement or function deployment timing out and terraform returning after initiation but without waiting long enough for actual availability.


##### Service not available

If the dependent service was not fully available, you may see error messages like this :

Run the following after a few minutes to confirm the state of the service.

Once confirmed, rerun terraform


##### If you don’t see any logs

Check to see if the `_default` log stream is disabled. Because Cloud Functions writes logs to resource `cloud_run_revision` in Cloud Logs, it’s possible that the logs aren’t being routed at all.


##### Handling inconsistency

Inconsistency between the actual project state and the state terraform locally caches can be caused by failures. In such a case, a re-run can cause terraform to try to create a resource that already exists and fail again with a message like this.

This requires the current project state to be imported into terraform by running “terraform import” command. The error above is about Firestore Database resource documented here :



* [https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/firestore_database](https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/firestore_database)

As in the [bottom of this page](https://registry.terraform.io/providers/hashicorp/google/latest/docs/resources/firestore_database#import), “terraform import” can be run in one of the supported forms.



* terraform import google_firestore_database.default projects/{{project}}/databases/{{name}}
* terraform import google_firestore_database.default {{project}}/{{name}}
* terraform import google_firestore_database.default {{name}}

In  the case of the error above:



* Database resources identifier within Terraform (as in the error message): “module.fleet-events-with-existing-project.module.fleet-events.google_firestore_database.database”
* Database resource path: “&lt;PROJECT_FLEETEVENTS>/(default)”. Note that Firestore database instance name is always “(default)”

Therefore, the import command can be run as follows

If both identifiers are recognized, you will see a message like this.

After successful  import, rerun the deployment.

For resources other than the Firestore database example above, commands to  import different GCP resources’s state is well documented in each resource type’s documentation.



* [https://registry.terraform.io/providers/hashicorp/google/latest/docs](https://registry.terraform.io/providers/hashicorp/google/latest/docs)

Also reference this guide from Google Cloud.



* [https://cloud.google.com/docs/terraform/resource-management/import#import_the_modules_into_the_terraform_state](https://cloud.google.com/docs/terraform/resource-management/import#import_the_modules_into_the_terraform_state)


### Advanced use of terraform

More advanced use of terraform is not the focus of this document, but here are some pointers that will help you in case it is required for your environment.


#### Run terraform under dedicated Service Account

This is recommended, especially when setting up CI/CD processes.



* [https://registry.terraform.io/providers/hashicorp/google/latest/docs/guides/provider_reference#impersonating-service-accounts](https://registry.terraform.io/providers/hashicorp/google/latest/docs/guides/provider_reference#impersonating-service-accounts) 


#### Externalizing state to Cloud Storage

In case there are multiple users responsible for deployment,  you need to share terraform state by externalizing it to a Cloud Storage backend.



* [https://cloud.google.com/docs/terraform/resource-management/store-state](https://cloud.google.com/docs/terraform/resource-management/store-state)

For developers considering wider adoption of terraform, below is a recommended read that will help you plan ahead.



* [https://cloud.google.com/docs/terraform/best-practices-for-terraform](https://cloud.google.com/docs/terraform/best-practices-for-terraform)


## Limitations
[PubSub Triggers](https://firebase.google.com/docs/functions/pubsub-events?gen=2nd) delivers fleet logs
to Cloud Functions. These triggers do not guarantee ordering. Out of order eventing can originate from:
- Logs delivered out of order by Cloud Logging (if used)
- Logs delivered out of order by PubSub (see [Cloud PubSub: Ordering Messages](https://cloud.google.com/pubsub/docs/ordering))
- Logs processed out of order by Cloud Functions

The volume of out of order logs increases when a function does not have enough compute infrastructure. For
example, if memory consumption is reaching capacity (see
[Monitoring Cloud Functions](https://cloud.google.com/functions/docs/monitoring)) for the deployed Cloud Function,
we recommend re-deploying the function with more memory.

By default, Fleet Events does not enable retries. This may cause events to be dropped if logs fail to be delivered
or processed by Cloud Functions.

## Conclusion

You now have a working environment of “Fleet Events” reference solution that takes near-real time events from Fleet Engine and turn them into actionable custom events along with handlers that act on them.