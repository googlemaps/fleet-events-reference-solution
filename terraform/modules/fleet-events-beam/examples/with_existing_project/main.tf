locals {
  # existing project
  PROJECT_FLEETENGINE = "crystal-gcp-project"
  # existing project
  PROJECT_FLEETEVENTS  = "crystal-gcp-project"
  GCP_REGION           = "us-central1"
  GCP_REGION_FIRESTORE = "nam5"
  PIPELINE_NAME        = "fleetevents-beam-moritani01"
  DATABASE_NAME        = "fleetevents-beam-moritani01"
  TOPIC_INPUT          = format("%s-input", local.PIPELINE_NAME)
  TOPIC_OUTPUT         = format("%s-output", local.PIPELINE_NAME)
  ME                   = "moritani@google.com"
  SETUP_PUBSUB_SUB_BQ  = true
}
# locals {
#   # existing project
#   PROJECT_FLEETENGINE = "moritani-sandbox-mobility"
#   # existing project
#   PROJECT_FLEETEVENTS = "moritani-sandbox-fleetevents"
#   GCP_REGION          = "asia-southeast1"
#   PIPELINE_NAME       = "fleetevents-beam"
#   DATABASE_NAME       = "fleetevents-db-2"
#   TOPIC_LOGGING       = format("%s-input", local.PIPELINE_NAME)
#   ME                  = "moritani@google.com"
#   SETUP_PUBSUB_SUB_BQ = true
# }

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
  source                   = "../../"
  PROJECT_APP              = local.PROJECT_FLEETEVENTS
  PROJECT_FLEETENGINE      = local.PROJECT_FLEETENGINE
  PROJECT_FLEETENGINE_LOG  = local.PROJECT_FLEETEVENTS
  PIPELINE_NAME            = local.PIPELINE_NAME
  DATABASE_NAME            = local.DATABASE_NAME
  GCP_REGION               = local.GCP_REGION
  GCP_REGION_FIRESTORE     = local.GCP_REGION_FIRESTORE
  TOPIC_INPUT              = local.TOPIC_INPUT
  TOPIC_OUTPUT             = local.TOPIC_OUTPUT
  FLAG_SETUP_PUBSUB_SUB_BQ = local.SETUP_PUBSUB_SUB_BQ
  ME                       = local.ME
  SA_APP_ROLES = [
    "roles/datastore.user"
  ]
  depends_on = [module.logging_config]
}

output "fleetevents-beam" {
  value     = module.fleetevents-beam
  sensitive = true
}
