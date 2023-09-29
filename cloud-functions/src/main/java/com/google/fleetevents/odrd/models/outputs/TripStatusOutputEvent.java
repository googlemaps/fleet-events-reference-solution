/*
 * Copyright 2023 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.fleetevents.odrd.models.outputs;

import com.google.cloud.Timestamp;
import com.google.fleetevents.common.models.OutputEvent;
import java.util.Objects;

/** OutputEvent for trip status changes. */
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
