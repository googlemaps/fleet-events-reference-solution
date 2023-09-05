locals {
  # existing project
  PROJECT_FLEETENGINE = "moritani-sandbox-mobility"
  # existing project
  PROJECT_FLEETEVENTS = "moritani-sandbox-fleetevents"
  GCP_REGION          = "asia-southeast1"
  PIPELINE_NAME       = "fleetevents-beam"
  TOPIC_LOGGING       = format("%s-input", local.PIPELINE_NAME)
  ME                  = "moritani@google.com"
  SETUP_PUBSUB_SUB_BQ = true
}

module "logging_config" {
  #TODO: replace source with link to git repo so that there is no dependency on this module being locally avail
  source                                = "../../../fleetengine-logging-config/"
  PROJECT_FLEETENGINE                   = local.PROJECT_FLEETENGINE
  PROJECT_LOGGINGSYNC                   = local.PROJECT_FLEETEVENTS
  FLAG_SETUP_LOGGING_LOGGING            = false
  FLAG_SETUP_LOGGING_EXCLUSION          = false
  FLAG_SETUP_LOGGING_PUBSUB             = true
  PUBSUB_TOPIC_NAME                     = local.TOPIC_LOGGING
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
  source                           = "../../"
  PROJECT_APP                      = local.PROJECT_FLEETEVENTS
  PROJECT_FLEETENGINE              = local.PROJECT_FLEETENGINE
  PROJECT_FLEETENGINE_LOG          = local.PROJECT_FLEETEVENTS
  PIPELINE_NAME                    = local.PIPELINE_NAME
  GCP_REGION                       = local.GCP_REGION
  TOPIC_FLEETENGINE_LOG            = local.TOPIC_LOGGING
  FLAG_SETUP_LOGGING_PUBSUB_SUB_BQ = local.SETUP_PUBSUB_SUB_BQ
  ME                               = local.ME
  SA_APP_ROLES = [
    "roles/datastore.user"
  ]
  depends_on = [module.logging_config]
}

output "fleetevents-beam" {
  value     = module.fleetevents-beam
  sensitive = true
}
