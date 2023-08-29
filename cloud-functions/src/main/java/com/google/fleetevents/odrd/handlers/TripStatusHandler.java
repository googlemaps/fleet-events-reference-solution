package com.google.fleetevents.odrd.handlers;

import com.google.cloud.firestore.Transaction;
import com.google.common.collect.ImmutableList;
import com.google.fleetevents.FleetEventHandler;
import com.google.fleetevents.common.database.FirestoreDatabaseClient;
import com.google.fleetevents.common.models.Change;
import com.google.fleetevents.common.models.FleetEvent;
import com.google.fleetevents.common.models.OutputEvent;
import com.google.fleetevents.odrd.models.TripFleetEvent;
import com.google.fleetevents.odrd.models.outputs.TripStatusOutputEvent;
import google.maps.fleetengine.v1.TripStatus;
import java.util.List;
import java.util.Objects;

public class TripStatusHandler implements FleetEventHandler {

  @Override
  public List<OutputEvent> handleEvent(FleetEvent fleetEvent, Transaction transaction) {
    var tripFleetEvent = (TripFleetEvent) fleetEvent;
    Change<String> tripStatusChange = tripFleetEvent.tripDifferences().get("tripStatus");
    var tripStatusOutputEvent = new TripStatusOutputEvent();
    tripStatusOutputEvent.setTripId(tripFleetEvent.tripId());
    tripStatusOutputEvent.setOldTripStatus(tripStatusChange.oldValue);
    tripStatusOutputEvent.setNewTripStatus(tripStatusChange.newValue);
    tripStatusOutputEvent.setEventTimestamp(
        Objects.requireNonNull(tripFleetEvent.newTrip()).getEventTimestamp());
    tripStatusOutputEvent.setFleetEvent(tripFleetEvent);
    return ImmutableList.of(tripStatusOutputEvent);
  }

  @Override
  public boolean respondsTo(
      FleetEvent fleetEvent,
      Transaction transaction,
      FirestoreDatabaseClient firestoreDatabaseClient) {
    if (fleetEvent.getEventType() != FleetEvent.Type.TRIP_FLEET_EVENT) {
      return false;
    }
    var tripFleetEvent = (TripFleetEvent) fleetEvent;
    return tripFleetEvent.tripDifferences().containsKey("tripStatus");
  }

  @Override
  public boolean verifyOutput(OutputEvent outputEvent) {
    if (!(outputEvent instanceof TripStatusOutputEvent tripStatusOutputEvent)) {
      return false;
    }
    TripStatus oldTripStatus = TripStatus.UNKNOWN_TRIP_STATUS;
    TripStatus newTripStatus = TripStatus.UNKNOWN_TRIP_STATUS;
    if (tripStatusOutputEvent.getOldTripStatus() != null) {
      oldTripStatus = TripStatus.valueOf(tripStatusOutputEvent.getOldTripStatus());
    }
    if (tripStatusOutputEvent.getNewTripStatus() != null) {
      newTripStatus = TripStatus.valueOf(tripStatusOutputEvent.getNewTripStatus());
    }

    return outputEvent.getType() == OutputEvent.Type.TRIP_STATUS_CHANGED
        && isStatusChangeValid(oldTripStatus, newTripStatus);
  }

  boolean isStatusChangeValid(TripStatus oldTripStatus, TripStatus newTripStatus) {
    switch (oldTripStatus) {
      case NEW, UNKNOWN_TRIP_STATUS, UNRECOGNIZED -> {
        return newTripStatus != TripStatus.UNKNOWN_TRIP_STATUS
            && newTripStatus != TripStatus.UNRECOGNIZED
            && newTripStatus != TripStatus.NEW;
      }
        /* The next state after enroute to pickup can't be new, unrecognized, unknown or still
         * enroute to pickup. */
      case ENROUTE_TO_PICKUP -> {
        return newTripStatus != TripStatus.NEW
            && newTripStatus != TripStatus.UNKNOWN_TRIP_STATUS
            && newTripStatus != TripStatus.UNRECOGNIZED
            && newTripStatus != TripStatus.ENROUTE_TO_PICKUP;
      }
      case ENROUTE_TO_INTERMEDIATE_DESTINATION -> {
        return newTripStatus == TripStatus.ARRIVED_AT_INTERMEDIATE_DESTINATION
            || newTripStatus == TripStatus.CANCELED;
      }
      case ENROUTE_TO_DROPOFF -> {
        return newTripStatus == TripStatus.COMPLETE || newTripStatus == TripStatus.CANCELED;
      }
      case ARRIVED_AT_PICKUP, ARRIVED_AT_INTERMEDIATE_DESTINATION -> {
        return newTripStatus == TripStatus.CANCELED
            || newTripStatus == TripStatus.ENROUTE_TO_INTERMEDIATE_DESTINATION
            || newTripStatus == TripStatus.ENROUTE_TO_DROPOFF;
      }
      case COMPLETE, CANCELED -> {
        return false;
      }
    }
    return false;
  }
}
