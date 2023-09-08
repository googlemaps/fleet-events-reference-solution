locals {
  PROJECT_FLEETENGINE  = var.PROJECT_FLEETENGINE
  PROJECT_FLEETEVENTS  = var.PROJECT_FLEETEVENTS
  GCP_REGION           = var.GCP_REGION
  GCP_REGION_FIRESTORE = var.GCP_REGION_FIRESTORE
  PIPELINE_NAME        = var.PIPELINE_NAME
  DATABASE_NAME        = format("%s-db", var.PIPELINE_NAME)
  TOPIC_INPUT          = format("%s-input", var.PIPELINE_NAME)
  TOPIC_OUTPUT         = format("%s-output", var.PIPELINE_NAME)
  ME                   = var.ME
  SETUP_PUBSUB_SUB_BQ  = true
}

module "logging_config" {
  #TODO: replace source with link to git repo so that there is no dependency on this module being locally avail
  source                                = "../../../fleetengine-logging-config/"
  PROJECT_FLEETENGINE                   = local.PROJECT_FLEETENGINE
  PROJECT_LOGGINGSYNC                   = local.PROJECT_FLEETEVENTS
  FLAG_SETUP_LOGGING_LOGGING            = false
  FLAG_SETUP_LOGGING_EXCLUSION          = false
  FLAG_SETUP_LOGGING_PUBSUB             = true
  PUBSUB_TOPIC_NAME                     = local.TOPIC_INPUT
  FLAG_SETUP_LOGGING_PUBSUB_SUB_BQ      = local.SETUP_PUBSUB_SUB_BQ
  FLAG_SETUP_LOGGING_PUBSUB_SUB_DEFAULT = false
  FLAG_SETUP_LOGGING_CLOUDSTORAGE       = false
  FLAG_SETUP_LOGGING_BIGQUERY           = false
  ME                                    = local.ME
  GCP_REGION                            = local.GCP_REGION
}

output "logging_config" {
  value     = module.logging_config
  sensitive = true
}

module "fleetevents-beam" {
  source                    = "../../"
  PROJECT_APP               = local.PROJECT_FLEETEVENTS
  PROJECT_FLEETENGINE       = local.PROJECT_FLEETENGINE
  PROJECT_FLEETENGINE_LOG   = local.PROJECT_FLEETEVENTS
  PIPELINE_NAME             = local.PIPELINE_NAME
  DATABASE_NAME             = local.DATABASE_NAME
  GCP_REGION                = local.GCP_REGION
  GCP_REGION_FIRESTORE      = local.GCP_REGION_FIRESTORE
  TOPIC_INPUT               = local.TOPIC_INPUT
  TOPIC_OUTPUT              = local.TOPIC_OUTPUT
  FLAG_SETUP_PUBSUB_SUB_BQ  = local.SETUP_PUBSUB_SUB_BQ
  ME                        = local.ME
  FLEETEVENTS_FUNCTION_NAME = "TASK_OUTCOME"
  FLEETEVENTS_GAP_SIZE      = 3
  FLEETEVENTS_WINDOW_SIZE   = 3
  SA_APP_ROLES = [
    "roles/datastore.user"
  ]
  depends_on = [module.logging_config]
}

output "fleetevents-beam" {
  value     = module.fleetevents-beam
  sensitive = false
}
