package com.google.fleetevents.odrd.models.outputs;

import com.google.cloud.Timestamp;
import com.google.fleetevents.common.models.OutputEvent;
import java.util.Objects;

public class TripStatusOutputEvent extends OutputEvent {
  private String tripId;
  private String oldTripStatus;
  private String newTripStatus;
  private Timestamp eventTimestamp;

  public TripStatusOutputEvent() {
    this.setType(Type.TRIP_STATUS_CHANGED);
  }

  public String getTripId() {
    return tripId;
  }

  public void setTripId(String tripId) {
    this.tripId = tripId;
  }

  public String getOldTripStatus() {
    return oldTripStatus;
  }

  public void setOldTripStatus(String oldTripStatus) {
    this.oldTripStatus = oldTripStatus;
  }

  public String getNewTripStatus() {
    return newTripStatus;
  }

  public void setNewTripStatus(String newTripStatus) {
    this.newTripStatus = newTripStatus;
  }

  public Timestamp getEventTimestamp() {
    return eventTimestamp;
  }

  public void setEventTimestamp(Timestamp eventTimestamp) {
    this.eventTimestamp = eventTimestamp;
  }

  public String toString() {
    return String.format(
        """
            TripStatusOutputEvent{
            \ttripId: %s
            \toldTripStatus: %s
            \tnewTripStatus: %s
            \teventTimestamp: %s
            \tfleetEvent:%s
            }""",
        tripId, oldTripStatus, newTripStatus, eventTimestamp, getFleetEvent());
  }

  @Override
  public boolean equals(Object object) {
    if (object instanceof TripStatusOutputEvent that) {
      return Objects.equals(that.tripId, this.tripId)
          && Objects.equals(that.oldTripStatus, this.oldTripStatus)
          && Objects.equals(that.newTripStatus, this.newTripStatus)
          && Objects.equals(that.getFleetEvent(), this.getFleetEvent());
    }
    return false;
  }
}
