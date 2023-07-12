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

package com.google.fleetevents.lmfs.handlers;

import com.google.cloud.firestore.Transaction;
import com.google.fleetevents.FleetEventHandler;
import com.google.fleetevents.common.database.FirestoreDatabaseClient;
import com.google.fleetevents.common.models.FleetEvent;
import com.google.fleetevents.lmfs.models.DeliveryVehicleData;
import com.google.fleetevents.lmfs.models.DeliveryVehicleFleetEvent;
import com.google.fleetevents.lmfs.models.outputs.DistanceRemainingOutputEvent;
import com.google.fleetevents.lmfs.models.outputs.OutputEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * FleetEventHandler that alerts when a task has crossed a threshold for remaining distance until
 * delivery. Default threshold is 1000m.
 */
public class DistanceRemainingHandler implements FleetEventHandler {

  private static final long DEFAULT_THRESHOLD_METERS = 1000L;
  private static final String DISTANCE_REMAINING_KEY_NAME = "distanceRemaining";
  private static final Logger logger = Logger.getLogger(DistanceRemainingHandler.class.getName());

  @Override
  public List<OutputEvent> handleEvent(FleetEvent fleetEvent, Transaction transaction) {
    DeliveryVehicleFleetEvent deliveryVehicleFleetEvent = (DeliveryVehicleFleetEvent) fleetEvent;
    DeliveryVehicleData oldDeliveryVehicleData = deliveryVehicleFleetEvent.oldDeliveryVehicle();
    DeliveryVehicleData newDeliveryVehicleData = deliveryVehicleFleetEvent.newDeliveryVehicle();
    var remainingVehicleJourneySegments =
        newDeliveryVehicleData.getRemainingVehicleJourneySegments();
    var distanceRemainingUpdates = new HashMap<String, Long>();
    var cumulativeDistance = 0L;

    for (var i = 0; i < remainingVehicleJourneySegments.size(); i++) {
      if (i == 0) {
        cumulativeDistance = newDeliveryVehicleData.getRemainingDistanceMeters();
      } else {
        cumulativeDistance += remainingVehicleJourneySegments.get(i).getDistance();
      }
      for (var deliveryTask :
          remainingVehicleJourneySegments.get(i).getVehicleStop().getTaskInfos()) {
        distanceRemainingUpdates.put(deliveryTask.getTaskId(), cumulativeDistance);
      }
    }

    List<OutputEvent> outputEvents = new ArrayList<>();
    for (var deliveryTaskId : distanceRemainingUpdates.keySet()) {
      boolean hasOriginalDistance =
          hasOriginalDistanceRemaining(deliveryTaskId, oldDeliveryVehicleData);
      /* Report if we have seen the distance remaining before, it was over the threshold and we are
       * crossing that threshold now or we haven't seen it before and it's crossing the threshold.
       * */
      if ((hasOriginalDistance
              && getOriginalDistanceRemaining(deliveryTaskId, oldDeliveryVehicleData)
                  > DEFAULT_THRESHOLD_METERS
              && distanceRemainingUpdates.get(deliveryTaskId) < DEFAULT_THRESHOLD_METERS)
          || (!hasOriginalDistance
              && distanceRemainingUpdates.get(deliveryTaskId) < DEFAULT_THRESHOLD_METERS)) {

        DistanceRemainingOutputEvent distanceRemainingOutputEvent =
            new DistanceRemainingOutputEvent();
        distanceRemainingOutputEvent.setTaskId(deliveryTaskId);
        distanceRemainingOutputEvent.setFleetEvent(deliveryVehicleFleetEvent);
        if (hasOriginalDistance) {
          distanceRemainingOutputEvent.setOldDistanceRemainingMeters(
              getOriginalDistanceRemaining(deliveryTaskId, oldDeliveryVehicleData));
        }
        distanceRemainingOutputEvent.setNewDistanceRemainingMeters(
            distanceRemainingUpdates.get(deliveryTaskId));
        distanceRemainingOutputEvent.setEventTimestamp(
            deliveryVehicleFleetEvent.newDeliveryVehicle().getEventTimestamp());
        logger.info(
            String.format("Distance Remaining Event triggered for task: %s", deliveryTaskId));
        outputEvents.add(distanceRemainingOutputEvent);
      }
    }
    newDeliveryVehicleData
        .getEventMetadata()
        .put(DISTANCE_REMAINING_KEY_NAME, distanceRemainingUpdates);
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
    return deliveryVehicleFleetEvent.vehicleDifferences().containsKey("remainingDistanceMeters");
  }

  @Override
  public boolean verifyOutput(OutputEvent outputEvent) {
    if (!(outputEvent instanceof DistanceRemainingOutputEvent)) {
      return false;
    }
    return outputEvent.getType() == OutputEvent.Type.DISTANCE_REMAINING;
  }

  public boolean hasOriginalDistanceRemaining(
      String deliveryTaskId, DeliveryVehicleData oldDeliveryVehicleData) {
    return oldDeliveryVehicleData != null
        && oldDeliveryVehicleData.getEventMetadata() != null
        && oldDeliveryVehicleData.getEventMetadata().containsKey(DISTANCE_REMAINING_KEY_NAME)
        && ((Map<String, Long>)
                oldDeliveryVehicleData.getEventMetadata().get(DISTANCE_REMAINING_KEY_NAME))
            .containsKey(deliveryTaskId);
  }

  public long getOriginalDistanceRemaining(
      String deliveryTaskId, DeliveryVehicleData oldDeliveryVehicleData) {
    Map<String, Long> distanceRemaining =
        (Map<String, Long>)
            oldDeliveryVehicleData.getEventMetadata().get(DISTANCE_REMAINING_KEY_NAME);
    return distanceRemaining.get(deliveryTaskId);
  }
}
