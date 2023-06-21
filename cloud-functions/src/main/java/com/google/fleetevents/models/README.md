Every class for every data model in the project lives in this folder. This
includes:

- Output events - whenever a new handler is added a corresponding output event
  model can be added under the outputs folder.
- Data serialized into Firestore - `DeliveryTaskData` and `DeliveryVehicleData`
  contain information about the task and vehicles from the logs and updates that
  are sent to them.
- Internal data types - `DeliveryTaskFleetEvent` and `DeliveryVehicleFleetEvent`
  are
  the intermediate events that carry information about state changes propagating
  through to the handlers.
- Fleet Engine type wrappers - other models like the `TaskInfo` and
  `VehicleJourneySegment` are copies of Fleet Engine delivery proto objects.

## Contributors

Google maintains this article. The following contributors originally wrote it.

Principal authors:

- Ethel Bao | Software Engineer, Google Maps Platform
- Mohanad Almiski | Software Engineer, Google Maps Platform
- Naoya Moritani | Solutions Engineer, Google Maps Platform
