There currently is only a single sink to write out events as serialized json to
an output Pub/Sub topic specified in the terraform. Additional sinks can be
added here and then used in the FleetEventsFunction class to route output events
to alternative locations such as SMS, Slack, etc.

## Contributors

Google maintains this article. The following contributors originally wrote it.

Principal authors:

- Ethel Bao | Software Engineer, Google Maps Platform
- Mohanad Almiski | Software Engineer, Google Maps Platform
- Naoya Moritani | Solutions Engineer, Google Maps Platform