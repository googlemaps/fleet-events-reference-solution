
## Getting started

“Fleet Events” can be used to generate events using logs produced by Fleet Engine. This document explains how to get started on the “Fleet Events” reference solution using Dataflow. It provides details on the handlers that are supported, and how to deploy the handlers to your Google Cloud project. This repository includes code for creating fleet events, which can be customized to individual business needs.


## Use cases covered:


### [Delivery Vehicle Not Updating](./src/main/java/com/google/fleetevents/beam/VehicleNotUpdating.java)

This handler will monitor each Vehicle session received from Cloud Logs, and output a notification when it is no longer sending updates to Fleet Engine.

Example workflow:



1. VehicleNotUpdating handler is configured with a_ GAP_ of 3 minutes.
2. DeliveryVehicle _v1_ begins driving at time _t1_.
3. _v1 _outputs events to FleetEngine every 10 seconds, from _t1 _to _t3_, where _t3-t1 = _2 minutes
4. _v1 _stops outputting events to FleetEngine from _t3-t6_. A notification is output by VehicleNotUpdating with context information for _v1_.


### [Task Outcome Changes](./src/main/java/com/google/fleetevents/beam/TaskOutcomeChange.java)

Outputs a notification when a Task Outcome for a Task has changed.

Example workflow:

1. Task _t1_ is created with no outcome. TaskOutcome handler outputs an update with a new Task outcome_ TASK_OUTCOME_UNSPECIFIED, _with context information_._
2. _t1_ is completed with outcome _SUCCEEDED_. TaskOutcome handler outputs an update with a new outcome _SUCCEEDED_, with context information.


## Prerequisites

#### Information about your environment

Note down the following information before getting started.

* **Fleet Engine project (project id)**: Your active project that is enabled for “[On Demand Rides and Deliveries Solution](https://developers.google.com/maps/documentation/transportation-logistics/on-demand-rides-deliveries-solution)”(ODRD) or “[Last Mile Fleet Solution](https://developers.google.com/maps/documentation/transportation-logistics/last-mile-fleet-solution)”(LMFS) is the event source. When handling events, you will often need to access Fleet Engine's APIs to retrieve information. Therefore, your code needs to know which project to access.
    * example) `my-fleetengine-project`
* **Fleet Events project (project id)**: The project in which this reference solution will be deployed. This project can be pre-created or be generated as part of the automated provisioning. A clean developer sandbox project is highly recommended for safe experiments.
    * example) `my-fleetevents-project`
* **Billing Account for Google Cloud**: This reference solution will make use of several Google Cloud Platform services. Therefore, the Billing Account for GCP usage is required. You may have different billing arrangements between Google Maps Platform (GMP) and Google Cloud Platform (GCP), in which case the Fleet Events project above should be associated with the latter. If you are not sure, please reach out to your Google account manager or Google Maps Platform reseller.
    * example) `XXXXXX-XXXXXX-XXXXXX` (alphanumeric)
* **Project Folder**: If your organization is adopting a folder structure to manage multiple Google Cloud projects, identify the folder and its id (number) under which the Fleet Events projects should reside.
    * example) `XXXXXXXXXXXX` (all digits)
* **Developer google account** : for simplicity, the deployment automation will run under a developer’s account and its privilege. The developer will be given permission to resources created.
    * example) `XXX@gmail.com`

#### Tools and setup

* **install terraform**: follow the steps for your environment
    * [https://developer.hashicorp.com/terraform/tutorials/gcp-get-started/install-cli](https://developer.hashicorp.com/terraform/tutorials/gcp-get-started/install-cli)
* **install cloud cli**: follow the steps for your environment
    * [https://cloud.google.com/sdk/docs/install](https://cloud.google.com/sdk/docs/install)
* **initialize cloud cli**: run the following which will allow terraform to run under your Google Account
    * `gcloud auth application-default login`
    * [https://registry.terraform.io/providers/hashicorp/google/latest/docs/guides/getting_started](https://registry.terraform.io/providers/hashicorp/google/latest/docs/guides/getting_started)

#### Check permission to configure projects

Terraform will run under your Google account’s permissions. (you can also run terraform under service accounts. Resources to learn about such configuration will be linked later in this doc)

For testing, give your developer user account IAM role `roles/editor` in both your **Fleet Engine project** and **Fleet Event project**. This gives you most of the permissions required to quickly go through this solution. Additionally, there are a few additionally required IAM roles.

**Fleet Engine** project

* `roles/logging.configWriter`: Contains permissions required to create logging sinks that defines where the log messages will be delivered.

**Fleet Events** project

* `roles/iam.securityAdmin`: Includes permission to set IAM policies for various resources such as Pub/Sub Topics or Subscriptions, BigQuery datasets, etc.  Required to apply least privilege principles.
* `roles/datastore.owner`: Includes permission required to create Firestore database instance.

Here is an example to give an user account the required IAM roles with Cloud CLI. The same can be achieved through [manual steps](https://cloud.google.com/iam/docs/granting-changing-revoking-access#single-role) in the Cloud Console UI.

```shell
# set your email to variable
USER_EMAIL=xxxx@gmail.com


# set your Fleet Engine project 
FLEETENGINE_PROJECT_ID=XXXXXXXX

# give your account, a broad set of permissons by applying roles/editor role
gcloud projects add-iam-policy-binding $FLEETENGINE_PROJECT_ID \ 
    --member=user:$USER_EMAIL --roles=roles/editor

# give your account, the permissions required to configure Logging
gcloud projects add-iam-policy-binding $FLEETENGINE_PROJECT_ID \
    --member=user:$USER_EMAIL -r-oles=roles/logging.configWriter


# set your Fleet Events project
FLEETEVENTS_PROJECT_ID=XXXXXXXX

# give your account, a broad set of permissons by applying roles/editor role
gcloud projects add-iam-policy-binding $FLEETEVENTS_PROJECT_ID \
    --member=user:$USER_EMAIL --roles=roles/editor

# give your account, the permissions required to configure IAM for various resources
gcloud projects add-iam-policy-binding $FLEETEVENTS_PROJECT_ID \
    --member=user:$USER_EMAIL --roles=roles/iam.securityAdmin

# give your account, the permissions required to configure firestore database instances
gcloud projects add-iam-policy-binding $FLEETEVENTS_PROJECT_ID \
    --member=user:$USER_EMAIL --roles=roles/datastore.owner
```

For production deployment, consider tightened access control, including adoption of Service Accounts to detach dependency on certain individuals. Also use custom roles to give the least set of permissions required for setup.

## Access the code

Clone this repo.

```shell
git clone https://github.com/googlemaps/fleet-events-reference-solutions

# change working directory to the cloned repo
cd fleet-events-reference-solutions
```

## Deploy the references solution

Follow the steps below to deploy a Dataflow handler.

#### STEP 1 : identify the example to start with

Change the Terraform working directory to one of the examples.

```shell
# choosing "with_existing_project" example here,
# which assumes you already have a project for Fleet Events.

cd terraform/modules/fleet-events-beam/examples/with_existing_project
```

#### STEP 2 : create a `terraform.tfvars` file from example

This example is also a terraform module with input variables defined. These variables can be set in a configuration file `terraform.tfvars`. Make a copy of file `terraform.tfvars.sample` and edit it to match your environment. Your text editor of choice can be used. Example below is using `vi`` as the editor.

```shell
# copy sample file to create your own config file.
cp terraform.tfvars.sample terraform.tfvars

# edit
vi terraform.tfvars
```

`terraform.tfvars` can look like this. Adjust the values to match your own environment

```hcl
PROJECT_FLEETEVENTS         = "<YOUR FLEETEVENTS PROJECT ID>"
PROJECT_FLEETENGINE         = "<YOUR FLEETENGINE PROJECT ID>"
ME                          = "<YOUR EMAIL>"
GCP_REGION                  = "us-central1"
# check https://firebase.google.com/docs/firestore/locations for valid Firestore regions
GCP_REGION_FIRESTORE        = "nam5"
PIPELINE_NAME               = "fleetevents-beam"
# the full set of available values will be in the FleetEventRunner.SampleFunction enum.
FLEETEVENTS_FUNCTION_NAME   = "TASK_OUTCOME_CHANGE|VEHICLE_NOT_UPDATING"
```

The full set of variables that can be set can be referenced in the module’s [README.md](../terraform/modules/fleet-events-beam/examples/with_existing_project/README.md).


#### STEP 3 : Initialize terraform

Initialize terraform before first run. Dependencies (modules, etc.) will be fetched to the working directory.

```shell
terraform init
```

#### STEP 4 : terraform plan

Let terraform compare current and ideal state and come up with a plan.

```shell
terraform plan
```

#### STEP 5 : Apply changes

If the plan is good to go, apply changes so that terraform can create and configure resources in your project accordingly.

```shell
terraform apply
```

Learn more about each terraform commands [here](https://cloud.google.com/docs/terraform/basic-commands).

#### STEP 6 : Verify your deployment

Use the publisher tool to test the TaskOutcomeChange handler.

```shell
## install prerequisite python libraries
pip3 install -r ./tools/python/requirements.txt

## run the tool
python3 task_outcome_test.py \
    --project_id "<gcp_project_id>" \
    --input_topic_id "<input_topic_id>" \
    --output_topic_id "<output_topic_id>"
``````

Note: `_input_topic_id_` is the topic that your Cloud Function reads from. `_output_topic_id_` should point to the PubSub topic where your deployed cloud function writes its notifications. Ensure the user running the script can read and write from all topics specified.

This publisher tool can also be used to publish test events to Cloud Functions. Sample itineraries that can be published are in folder `./tools/python/data/`. You can follow the steps below:

```shell
## install prerequisite python libraries
pip3 install -r ./tools/python/requirements.txt

## run the tool
python3 main.py --project_id "<gcp_project_id>" \
    --plan "<path_to_json>" \
    --topic_id "<input_topic_id>"
```


### Updating a Dataflow Job

To update your dataflow job with new code or parameters, refer to ([https://cloud.google.com/dataflow/docs/guides/updating-a-pipeline](https://cloud.google.com/dataflow/docs/guides/updating-a-pipeline))


### Starting over

When you need to change configuration, update the `terraform.tfvar` file and rerun terraform from the same folder. In case you want to start over or clean up the environment, de-provisioning can be done by simply running the following command.

```shell
terraform destroy
```


## Running Unit Tests

Some unit tests in this package interact with Firestore. To run these tests successfully, you must set up and run the Firstore emulator.

Follow the instructions for installing the local emulator suite ([https://firebase.google.com/docs/emulator-suite/install_and_configure#install_the_local_emulator_suite](https://firebase.google.com/docs/emulator-suite/install_and_configure#install_the_local_emulator_suite)) and configure the Firestore emulator at port 8080 (the default selection) to run these tests.
