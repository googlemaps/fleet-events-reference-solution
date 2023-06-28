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

package com.google.fleetevents.handlers;

import com.google.cloud.firestore.Transaction;
import com.google.fleetevents.FleetEventHandler;
import com.google.fleetevents.database.FirestoreDatabaseClient;
import com.google.fleetevents.models.DeliveryVehicleData;
import com.google.fleetevents.models.DeliveryVehicleFleetEvent;
import com.google.fleetevents.models.FleetEvent;
import com.google.fleetevents.models.outputs.OutputEvent;
import com.google.fleetevents.models.outputs.TimeRemainingOutputEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * FleetEventHandler that alerts when a task has crossed a threshold for remaining duration until
 * delivery. Default threshold is 5 minutes in milliseconds.
 */
public class TimeRemainingHandler implements FleetEventHandler {

  private static final long DEFAULT_THRESHOLD_MILLISECONDS = 5 * 60 * 1000L;
  private static final String TIME_REMAINING_KEY_NAME = "timeRemaining";
  private static final Logger logger = Logger.getLogger(TimeRemainingHandler.class.getName());

  @Override
  public List<OutputEvent> handleEvent(FleetEvent fleetEvent, Transaction transaction) {
    DeliveryVehicleFleetEvent deliveryVehicleFleetEvent = (DeliveryVehicleFleetEvent) fleetEvent;
    DeliveryVehicleData oldDeliveryVehicleData = deliveryVehicleFleetEvent.oldDeliveryVehicle();
    DeliveryVehicleData newDeliveryVehicleData = deliveryVehicleFleetEvent.newDeliveryVehicle();
    var remainingVehicleJourneySegments =
        newDeliveryVehicleData.getRemainingVehicleJourneySegments();
    var timeRemainingUpdates = new HashMap<String, Long>();
    var cumulativeDuration = 0L;

    for (var i = 0; i < remainingVehicleJourneySegments.size(); i++) {
      if (i == 0) {
        cumulativeDuration = newDeliveryVehicleData.getRemainingDuration();
      } else {
        cumulativeDuration += remainingVehicleJourneySegments.get(i).getDuration();
      }
      for (var deliveryTask :
          remainingVehicleJourneySegments.get(i).getVehicleStop().getTaskInfos()) {
        cumulativeDuration += deliveryTask.getTaskDuration();
        timeRemainingUpdates.put(deliveryTask.getTaskId(), cumulativeDuration);
      }
    }

    List<OutputEvent> outputEvents = new ArrayList<>();
    for (var deliveryTaskId : timeRemainingUpdates.keySet()) {
      boolean hasOriginalTime = hasOriginalTimeRemaining(deliveryTaskId, oldDeliveryVehicleData);
      /* Report if we have seen the time remaining before, it was over the threshold and we are
       * crossing that threshold now or we haven't seen it before and it's crossing the threshold.
       * */
      if ((hasOriginalTime
          && getOriginalTimeRemaining(deliveryTaskId, oldDeliveryVehicleData)
          > DEFAULT_THRESHOLD_MILLISECONDS
          && timeRemainingUpdates.get(deliveryTaskId) < DEFAULT_THRESHOLD_MILLISECONDS)
          || (!hasOriginalTime
          && timeRemainingUpdates.get(deliveryTaskId) < DEFAULT_THRESHOLD_MILLISECONDS)) {

        TimeRemainingOutputEvent timeRemainingOutputEvent = new TimeRemainingOutputEvent();
        timeRemainingOutputEvent.setTaskId(deliveryTaskId);
        timeRemainingOutputEvent.setFleetEvent(deliveryVehicleFleetEvent);
        if (hasOriginalTime) {
          timeRemainingOutputEvent.setOldTimeRemainingSeconds(
              getOriginalTimeRemaining(deliveryTaskId, oldDeliveryVehicleData));
        }
        timeRemainingOutputEvent.setNewTimeRemainingSeconds(
            timeRemainingUpdates.get(deliveryTaskId));
        timeRemainingOutputEvent.setEventTimestamp(
            deliveryVehicleFleetEvent.newDeliveryVehicle().getEventTimestamp());
        logger.info(String.format("Time Remaining Event for task: %s", deliveryTaskId));
        outputEvents.add(timeRemainingOutputEvent);
      }
    }
    newDeliveryVehicleData.getEventMetadata().put(TIME_REMAINING_KEY_NAME, timeRemainingUpdates);
    return outputEvents;
  }

  @Override
  public boolean respondsTo(
      FleetEvent fleetEvent,
      Transaction transaction,
      FirestoreDatabaseClient firestoreDatabaseClient) {
    if (fleetEvent.getEventType() != FleetEvent.Type.DELIVERY_VEHICLE_FLEET_EVENT) {
      return false;
    }
    DeliveryVehicleFleetEvent deliveryVehicleFleetEvent = (DeliveryVehicleFleetEvent) fleetEvent;
    return deliveryVehicleFleetEvent.vehicleDifferences().containsKey("remainingDuration");
  }

  @Override
  public boolean verifyOutput(OutputEvent outputEvent) {
    if (!(outputEvent instanceof TimeRemainingOutputEvent)) {
      return false;
    }
    return outputEvent.getType() == OutputEvent.Type.TIME_REMAINING;
  }

  public boolean hasOriginalTimeRemaining(
      String deliveryTaskId, DeliveryVehicleData oldDeliveryVehicleData) {
    return oldDeliveryVehicleData != null
        && oldDeliveryVehicleData.getEventMetadata() != null
        && oldDeliveryVehicleData.getEventMetadata().containsKey(TIME_REMAINING_KEY_NAME)
        && ((Map<String, Long>)
        oldDeliveryVehicleData.getEventMetadata().get(TIME_REMAINING_KEY_NAME))
        .containsKey(deliveryTaskId);
  }

  public long getOriginalTimeRemaining(
      String deliveryTaskId, DeliveryVehicleData oldDeliveryVehicleData) {
    Map<String, Long> timeRemaining =
        (Map<String, Long>) oldDeliveryVehicleData.getEventMetadata().get(TIME_REMAINING_KEY_NAME);
    return timeRemaining.get(deliveryTaskId);
  }
}
