Transactions inherit from the Firebase Transaction class. Each class stores only
necessary information about the firestore references it will need in the
transaction to avoid having to create them again in the updateCallback method.

Logs routed to the FleetEventCreator decide which transactions to execute based
on the type of the log, for example `UpdateDeliveryVehicle` logs will be routed
to the `UpdateDeliveryVehicleTransaction`.

**Transactions**:

- `BatchCreateDeliveryTasksTransaction`
- `CreateDeliveryTaskTransaction`
- `CreateDeliveryVehicleTransaction`
- `UpdateDeliveryTaskTransaction`
- `UpdateDeliveryVehicleTransaction`

Each transaction passes the transaction object that contains the database
operations to perform as a parameter to each Fleet Event Handler. Therefore, if
you have data in Firestore that you would like to get or update in the
transaction you can add to it from within the handlers.

## Contributors

Google maintains this article. The following contributors originally wrote it.

Principal authors:

- Ethel Bao | Software Engineer, Google Maps Platform
- Mohanad Almiski | Software Engineer, Google Maps Platform
- Naoya Moritani | Solutions Engineer, Google Maps Platform