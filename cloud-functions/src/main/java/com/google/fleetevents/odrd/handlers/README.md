## TripStatus Handler

This handler returns changes in the Trip Status per Trip.



* Possible Trip States
    * UNKNOWN_TRIP_STATUS
    * NEW
    * ENROUTE_TO_PICKUP
    * ARRIVED_AT_PICKUP
    * ARRIVED_AT_INTERMEDIATE_DESTINATION
    * ENROUTE_TO_INTERMEDIATE_DESTINATION
    * ENROUTE_TO_DROPOFF
    * UNRECOGNIZED
* Possible Trip Outcomes
    * COMPLETE
    * CANCELED

Here is a scenario that demonstrates when outputs are triggered.



1. Trip T_1 is created. Handler outputs that the status has changed from null -> NEW.
2. The vehicle starts to move. Trip T_1’s status changed to ENROUTE_TO_PICKUP, and the handler outputs a TripStatusOutputEvent with the change in status from NEW -> ENROUTE_TO_PICKUP.
3. The vehicle tries to return the trip status to NEW. This is not an accepted status change by Fleet Engine, so we will log a warning and omit the output.


## ETAChange Handler

This handler returns ETA changes above a configurable threshold. This notifies per-trip and waypoint.



* Types
    * ETA - Measures an ETA change. This triggers when the ETA changes _[threshold]_ above or below the original estimate for a Trip or its Waypoints.
    * RELATIVE_ETA - Measures a relative ETA change. This triggers when the duration changes a percentage _[threshold]_ above or below the original estimate for a Trip or its Waypoints.
* Supported Waypoint types
    * PICKUP - waypoints for picking up riders or items
    * DROPOFF - waypoints for dropping off riders or items
    * INTERMEDIATE_DESTINATION - waypoints for intermediate destinations in a multi-destination trip.

Here is a scenario that demonstrates when outputs are triggered.

**Eta changes above threshold**



1. Vehicle V_1 moves towards the PICKUP at time t1, with an original ETA of t2. Its DROPOFF has an ETA of t3. The Trip is called TRIP_1 with total ETA t4.
2. V_1 is delayed, causing the ETA for PICKUP and DROPOFF to be delayed to [t2 + THRESHOLD + 1] and [t3 + THRESHOLD + 1]. The Trip associated with these waypoints, TRIP_1, is also considered delayed by [t4 + THRESHOLD + 1].
3. 3 EtaAbsoluteChangeOutput events are written for PICKUP, DROPOFF, and TRIP_1. The delta is [THRESHOLD + 1].

**Eta does not recover**



1. V_1 continues to PICKUP, but ETA does not recover from the delay, and remains [THRESHOLD + 1]. The handler continues to notify for all three entities.

**PICKUP completed**



1. V_1 completes the PICKUP and is on its way to DROPOFF. The handler continues to notify for DROPOFF and TRIP_1, because the ETA has not recovered.

**INTERMEDIATE_DESTINATION added**



1. The driver finds a shorter route, and the ETA for DROPOFF has recovered to t3. DROPOFF and TRIP_1 stop alerting.
2. An INTERMEDIATE_DESTINATION is added to TRIP_1. This is considered a delay for the DROPOFF and TRIP_1, so both begin to alert.

**Note: Adding/Removing waypoints during a trip should always be done using the UpdateTrip api request. Adding/removing waypoints solely from UpdateVehicle can result in undefined behavior. When a change is detected in the trips’ waypoints, eta metadata is reset.**


## TimeRemaining Handler

This handler notifies when the time remaining on a trip and its waypoints has passed the configured threshold.

Here is a scenario that demonstrates when outputs are triggered.



1. Vehicle V_1 moves towards the PICKUP with ETA t1. The DROPOFF is scheduled with ETA t2. The entire Trip is called TRIP_1.
2. V_1 is now [THRESHOLD - 1] away from PICKUP. The handler will trigger a TimeRemainingOutput for PICKUP.
3. V_1 is delayed, and is [THRESHOLD + 1] away from PICKUP. The handler does not respond.
4. V_1 is again [THRESHOLD - 1] away from PICKUP. The handler will trigger another output for PICKUP
5. V_1 completes the PICKUP, and is on its way to the DROPOFF. ETA t2 is [THRESHOLD + 100] away. The handler does not respond.
6. V_1 is now [THRESHOLD - 1] away from DROPOFF. The handler will trigger a TimeRemainingOutput for DROPOFF, and TRIP_1.


## DistanceRemaining Handler

This handler notifies when the distance remaining on a trip and its waypoints has passed the configured threshold.

Here is a scenario that demonstrates when outputs are triggered.



1. Vehicle V_1 moves towards the PICKUP that is distance d1 away. The DROPOFF is d2 away. The entire Trip is called TRIP_1.
2. V_1 is now [THRESHOLD - 1] away from PICKUP. The handler will trigger a DistanceRemainingOutput for PICKUP.
3. V_1 is re-routed, and is [THRESHOLD + 1] away from PICKUP. The handler does not respond.
4. V_1 is again [THRESHOLD - 1] away from PICKUP. The handler will trigger another output for PICKUP
5. V_1 completes the PICKUP, and is on its way to the DROPOFF. DROPOFF is [THRESHOLD + 100] away. The handler does not respond.
6. V_1 is now [THRESHOLD - 1] away from DROPOFF. The handler will trigger a DistanceRemainingOutput for DROPOFF, and TRIP_1.


## **Contributors**

Google maintains this article. The following contributors originally wrote it.

Principal authors:



*  | Software Engineer, Google Maps Platform
*  | Software Engineer, Google Maps Platform
*  | Solutions Engineer, Google Maps Platform