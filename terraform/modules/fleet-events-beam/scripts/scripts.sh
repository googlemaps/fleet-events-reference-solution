#!/bin/bash


function tf_buildJar {
    local PATH_POM=$1
    echo "pom.xml path : $PATH_POM"
    mvn -f $PATH_POM clean package -Dmaven.test.skip=true
}

function tf_buildTemplate {
    
    local PROJECT_ID
    local TEMPLATE_NAME
    local TEMPLATE_JSON_GCS
    local TEMPLATE_JSON_LOCAL
    local PATH_JAR
    local REPOSITORY
    local REGION
    local BUCKET
    local IMAGE_GCR_PATH
    local TEMPLATE_MAINCLASS
    local TEMPLATE_BASEIMAGE
    while [[ "$#" -gt 0 ]]
    do
        case $1 in
            -p|--project)          PROJECT_ID="$2";           shift;;
            --template_name)       TEMPLATE_NAME="$2";        shift;;
            --template_json_gcs)   TEMPLATE_JSON_GCS="$2";    shift;;
            --template_json_local) TEMPLATE_JSON_LOCAL="$2";  shift;;
            --jar)                 PATH_JAR="$2";             shift;;
            --repository)          REPOSITORY="$2";           shift;;
            --region)              REGION="$2";               shift;;
            --bucket)              BUCKET="$2";               shift;;
            --image-gcr-path)      IMAGE_GCR_PATH="$2";       shift;;
            --template_mainclass)  TEMPLATE_MAINCLASS="$2";   shift;;
            --template_baseimage)  TEMPLATE_BASEIMAGE="$2";   shift;;
            *) echo "Unknown parameter passed: $1"; exit 1;;
            
        esac
        shift
    done
    
    #local MAINCLASS=com.google.fleetevents.beam.FleetEventRunner
    #local FLEX_TEMPLATE_BASE_IMAGE=gcr.io/dataflow-templates-base/java11-template-launcher-base-distroless
    #local IMAGE_GCR_PATH="${REGION}-docker.pkg.dev/${PROJECT_ID}/${REPOSITORY}/${TEMPLATE_NAME}:latest"
    
    gcloud --project ${PROJECT_ID} --verbosity info \
    dataflow flex-template build ${TEMPLATE_JSON_GCS} \
    --image-gcr-path           $IMAGE_GCR_PATH \
    --sdk-language             "JAVA" \
    --flex-template-base-image ${TEMPLATE_BASEIMAGE}  \
    --metadata-file            ${TEMPLATE_JSON_LOCAL} \
    --jar                      ${PATH_JAR} \
    --gcs-log-dir              gs://${BUCKET}/logs \
    --env                      FLEX_TEMPLATE_JAVA_MAIN_CLASS=${TEMPLATE_MAINCLASS}
    #    --additional-experiments=enable_prime \
    
}
function test_tf_buildTemplate2 {
    tf_buildTemplate2 \
    --project              crystal-gcp-project \
    --template_name        fleetevents-beam \
    --template_json_gcs    gs://crystal-gcp-project-flextemplate-fleetevents-beam/templates/fleetevents-beam.json \
    --template_json_local  /Users/moritani/work_geo/incubation/fleetevents/github/gmalmiski/fleet-events-reference-solution/beam/fleetevents-beam.json \
    --jar                  /Users/moritani/work_geo/incubation/fleetevents/github/gmalmiski/fleet-events-reference-solution/beam/target/fleetevents-beam-bundled-1.0-SNAPSHOT.jar \
    --repository           repo-fleetevents-beam \
    --region               us-central1 \
    --bucket               crystal-gcp-project-flextemplate-fleetevents-beam \
    --image-gcr-path       us-central1-docker.pkg.dev/crystal-gcp-project/repo-fleetevents-beam/fleetevents-beam:latest \
    --template_mainclass   com.google.fleetevents.beam.FleetEventRunner \
    --template_baseimage   gcr.io/dataflow-templates-base/java11-template-launcher-base-distroless
}




## Terraform does not support deletion of databases and nor does Cloud Console
## The only way to cleanly delete a firestore database instance is by using this alpha command with Cloud CLI
## However, the observed behaviour is that the database is hidden rather than immediately removed, thus the same name cannot be immediately reused.

function deleteFirestoreDatabase {
    local DATABASE=$1
    gcloud --project ${PROJECT_ID} alpha firestore databases delete --database=${DATABASE}
}



# in deleting vpc networks, if there are firewall rules that were added outside of terraform, it fails
# this script is used to clean up firewall rules for a network before deletion

function deleteFirewallRules {
    local PROJECT_ID=$1
    local NETWORK=$2
    
    #PROJECT_ID=crystal-gcp-project
    #NETWORK=network-fleetevents
    
    for fw in $(gcloud --project $PROJECT_ID compute firewall-rules list --filter="(network=${NETWORK})" --format="value(name)")
    do
        gcloud --quiet --project $PROJECT_ID compute firewall-rules delete $fw
    done
}

"$@"