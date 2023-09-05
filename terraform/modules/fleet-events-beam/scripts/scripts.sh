#!/bin/bash

PROJECT_ID=moritani-sandbox-fleetevents
REGION=asia-southeast1
TEMPLATE_NAME=fleetevents-beam

BUCKET=${PROJECT_ID}-${TEMPLATE_NAME}

MAINCLASS=com.google.fleetevents.beam.FleetEventRunner

DIR_SRC=../../../../beam
PATH_JAR=${DIR_SRC}/target/${TEMPLATE_NAME}-bundled-1.0-SNAPSHOT.jar
TEMPLATE_FILE_GCS_PATH=gs://${BUCKET}/templates/${TEMPLATE_NAME}.json
METADATA_FILE="${DIR_SRC}/${TEMPLATE_NAME}.json"

SA_EMAIL=sa-fleetevents-beam@${PROJECT_ID}.iam.gserviceaccount.com


function tf_buildJar {
    local PATH_POM=$1
    echo "pom.xml path : $PATH_POM"
    mvn -f $PATH_POM clean package -Dmaven.test.skip=true
    
}

function tf_buildTemplate {
    local PROJECT_ID=$1
    local TEMPLATE_NAME=$2
    local TEMPLATE_FILE_GCS_PATH=$3
    local METADATA_FILE=$4
    local PATH_JAR=$5
    local REPOSITORY=$6
    FLEX_TEMPLATE_BASE_IMAGE=gcr.io/dataflow-templates-base/java11-template-launcher-base-distroless
    #BASEIMAGE=JAVA11
    IMAGE_GCR_PATH="${REGION}-docker.pkg.dev/${PROJECT_ID}/${REPOSITORY}/dataflow/${TEMPLATE_NAME}:latest"
    
    pwd
    
    gcloud --project ${PROJECT_ID} dataflow flex-template build ${TEMPLATE_FILE_GCS_PATH} \
    --image-gcr-path $IMAGE_GCR_PATH \
    --sdk-language "JAVA" \
    --flex-template-base-image ${FLEX_TEMPLATE_BASE_IMAGE}  \
    --metadata-file ${METADATA_FILE} \
    --jar ${PATH_JAR} \
    --gcs-log-dir gs://${BUCKET}/logs \
    --additional-experiments=enable_prime \
    --env FLEX_TEMPLATE_JAVA_MAIN_CLASS=${MAINCLASS} 

    # --disable-public-ips \
    # --num-workers=1 \
    # --worker-machine-type=e2-standard-2 \
    # --service-account-email=${SA_EMAIL}
    
}


## Terraform does not support deletion of databases and nor does Cloud Console
## The only way to cleanly delete a firestore database instance is by using this alpha command with Cloud CLI

function deleteFirestoreDatabase {
    local DATABASE=$1
    gcloud --project ${PROJECT_ID} alpha firestore databases delete --database=${DATABASE}
}
"$@"