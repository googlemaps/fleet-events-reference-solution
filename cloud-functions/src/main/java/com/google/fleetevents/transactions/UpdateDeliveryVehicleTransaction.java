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

package com.google.fleetevents.transactions;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.GeoPoint;
import com.google.cloud.firestore.Transaction;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import com.google.fleetevents.FleetEventCreator;
import com.google.fleetevents.FleetEventHandler;
import com.google.fleetevents.database.FirestoreDatabaseClient;
import com.google.fleetevents.models.Change;
import com.google.fleetevents.models.DeliveryTaskData;
import com.google.fleetevents.models.DeliveryTaskFleetEvent;
import com.google.fleetevents.models.DeliveryVehicleData;
import com.google.fleetevents.models.DeliveryVehicleFleetEvent;
import com.google.fleetevents.models.FleetEvent;
import com.google.fleetevents.models.Pair;
import com.google.fleetevents.models.UpdateAndDifference;
import com.google.fleetevents.models.outputs.OutputEvent;
import com.google.fleetevents.util.NameFormatter;
import com.google.fleetevents.util.ProtoParser;
import com.google.fleetevents.util.TimeUtil;
import com.google.logging.v2.LogEntry;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.type.LatLng;
import google.maps.fleetengine.delivery.v1.DeliveryVehicle;
import google.maps.fleetengine.delivery.v1.UpdateDeliveryVehicleRequest;
import google.maps.fleetengine.delivery.v1.VehicleJourneySegment;
import google.maps.fleetengine.delivery.v1.VehicleStop.TaskInfo;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;
import javax.annotation.Nullable;

/**
 * Creates transaction to update Firestore about an update to an existing delivery vehicle.
 * Generates a DeliveryVehicleFleetEvent for the new vehicle update as well as a
 * DeliveryTaskFleetEvent for any tasks that are moved, removed, or added.
 */
public class UpdateDeliveryVehicleTransaction implements Transaction.Function<List<OutputEvent>> {

  private static final Logger logger =
      Logger.getLogger(UpdateDeliveryVehicleTransaction.class.getName());

  private final List<FleetEventHandler> fleetEventHandlers;
  private final FirestoreDatabaseClient firestoreDatabaseClient;
  private final LogEntry logEntry;
  private final DocumentReference deliveryVehicleRef;
  private final String deliveryVehicleId;

  public UpdateDeliveryVehicleTransaction(
      LogEntry logEntry,
      List<FleetEventHandler> fleetEventHandlers,
      FirestoreDatabaseClient firestoreDatabaseClient)
      throws InvalidProtocolBufferException {
    this.fleetEventHandlers = fleetEventHandlers;
    this.firestoreDatabaseClient = firestoreDatabaseClient;
    this.logEntry = logEntry;
    DeliveryVehicle response =
        ProtoParser.parseLogEntryResponse(logEntry, DeliveryVehicle.getDefaultInstance());

    deliveryVehicleId = NameFormatter.getIdFromName(response.getName());

    deliveryVehicleRef = firestoreDatabaseClient.getVehicleDocument(deliveryVehicleId);
  }

  static UpdateAndDifference<DeliveryVehicleData> getDeliveryVehicleAndDiff(
      LogEntry logEntry, @Nullable DeliveryVehicleData oldDeliveryVehicleData)
      throws InvalidProtocolBufferException {

    UpdateDeliveryVehicleRequest request =
        ProtoParser.parseLogEntryRequest(
            logEntry, UpdateDeliveryVehicleRequest.getDefaultInstance());
    DeliveryVehicle deliveryVehicle =
        ProtoParser.parseLogEntryResponse(logEntry, DeliveryVehicle.getDefaultInstance());
    List<String> updateMask = request.getUpdateMask().getPathsList();

    if (oldDeliveryVehicleData == null) {
      oldDeliveryVehicleData = DeliveryVehicleData.builder().build();
    }

    var updatedFields = new HashSet<>(updateMask);

    Map<String, Change> vehicleDifferences = new HashMap<>();
    DeliveryVehicleData.Builder deliveryVehicleBuilder =
        oldDeliveryVehicleData.toBuilder()
            .setEventTimestamp(TimeUtil.protobufToLong(logEntry.getTimestamp()))
            .setExpireAt(TimeUtil.offsetFromNow(TimeUtil.ONE_HOUR_IN_SECONDS));

    if (updatedFields.contains("last_location")
        || !Objects.equals(
            oldDeliveryVehicleData.getLastLocation(),
            latLngToGeoPoint(deliveryVehicle.getLastLocation().getLocation()))) {
      if (deliveryVehicle.hasLastLocation() && deliveryVehicle.getLastLocation().hasLocation()) {
        LatLng lastLocation = deliveryVehicle.getLastLocation().getLocation();
        deliveryVehicleBuilder.setLastLocation(latLngToGeoPoint(lastLocation));
        vehicleDifferences.put(
            "lastLocation", new Change<>(oldDeliveryVehicleData.getLastLocation(), lastLocation));
      }
      updatedFields.remove("last_location");
    }
    if (updatedFields.contains("navigation_status")
        || !Objects.equals(
            oldDeliveryVehicleData.getNavigationStatus(),
            deliveryVehicle.getNavigationStatus().name())) {
      String status = deliveryVehicle.getNavigationStatus().name();
      deliveryVehicleBuilder.setNavigationStatus(status);
      vehicleDifferences.put(
          "navigationStatus", new Change<>(oldDeliveryVehicleData.getNavigationStatus(), status));
      updatedFields.remove("navigation_status");
    }
    if (updatedFields.contains("remaining_duration")
        || !Objects.equals(
            oldDeliveryVehicleData.getRemainingDuration(),
            deliveryVehicle.getRemainingDuration().getSeconds())) {
      if (deliveryVehicle.hasRemainingDuration()) {
        com.google.protobuf.Duration duration = deliveryVehicle.getRemainingDuration();
        var newDuration = TimeUtil.protobufToLong(duration);
        deliveryVehicleBuilder.setRemainingDuration(newDuration);
        vehicleDifferences.put(
            "remainingDuration",
            new Change<>(oldDeliveryVehicleData.getRemainingDuration(), newDuration));
      }
      updatedFields.remove("remaining_duration");
    }
    if (updatedFields.contains("remaining_distance_meters")
        || !Objects.equals(
            oldDeliveryVehicleData.getRemainingDistanceMeters(),
            (long) deliveryVehicle.getRemainingDistanceMeters().getValue())) {
      if (deliveryVehicle.hasRemainingDistanceMeters()) {
        int remainingDistanceMeters = deliveryVehicle.getRemainingDistanceMeters().getValue();
        deliveryVehicleBuilder.setRemainingDistanceMeters(remainingDistanceMeters);
        vehicleDifferences.put(
            "remainingDistanceMeters",
            new Change<>(
                oldDeliveryVehicleData.getRemainingDistanceMeters(), remainingDistanceMeters));
      }
      updatedFields.remove("remaining_distance_meters");
    }
    List<VehicleJourneySegment> segments = deliveryVehicle.getRemainingVehicleJourneySegmentsList();
    var newRemainingVehicleJourneySegments =
        segments.stream()
            .map(
                com.google.fleetevents.models.VehicleJourneySegment::fromVehicleJourneySegmentProto)
            .toList();

    if (updatedFields.contains("remaining_vehicle_journey_segments")
        || Objects.equals(
            oldDeliveryVehicleData.getRemainingVehicleJourneySegments(),
            newRemainingVehicleJourneySegments)) {
      List<String> currentDeliveryTaskIds = new ArrayList<>();
      List<String> plannedDeliveryTaskIds = new ArrayList<>();

      deliveryVehicleBuilder.setRemainingVehicleJourneySegments(newRemainingVehicleJourneySegments);

      for (int i = 0; i < segments.size(); i++) {
        VehicleJourneySegment segment = segments.get(i);
        if (i == 0) {
          segment.getStop().getTasksList().stream()
              .map(TaskInfo::getTaskId)
              .forEach(currentDeliveryTaskIds::add);
        } else {
          segment.getStop().getTasksList().stream()
              .map(TaskInfo::getTaskId)
              .forEach(plannedDeliveryTaskIds::add);
        }
      }
      deliveryVehicleBuilder.setCurrentDeliveryTaskIds(currentDeliveryTaskIds);
      deliveryVehicleBuilder.setPlannedDeliveryTaskIds(plannedDeliveryTaskIds);
      if (!currentDeliveryTaskIds.equals(oldDeliveryVehicleData.getCurrentDeliveryTaskIds())) {
        vehicleDifferences.put(
            "currentDeliveryTaskIds",
            new Change<>(
                oldDeliveryVehicleData.getCurrentDeliveryTaskIds(), currentDeliveryTaskIds));
      }
      if (!plannedDeliveryTaskIds.equals(oldDeliveryVehicleData.getCurrentDeliveryTaskIds())) {
        vehicleDifferences.put(
            "plannedDeliveryTaskIds",
            new Change<>(
                oldDeliveryVehicleData.getCurrentDeliveryTaskIds(), plannedDeliveryTaskIds));
      }
      updatedFields.remove("remaining_vehicle_journey_segments");
    }
    for (var nonHandledUpdateField : updatedFields) {
      logger.info(String.format("Not currently handling %s updates\n", nonHandledUpdateField));
    }
    return new UpdateAndDifference<>(deliveryVehicleBuilder.build(), vehicleDifferences);
  }

  private static GeoPoint latLngToGeoPoint(LatLng latLng) {
    if (latLng == null) {
      return null;
    }
    return new GeoPoint(latLng.getLatitude(), latLng.getLongitude());
  }

  @Override
  public List<OutputEvent> updateCallback(Transaction transaction)
      throws InvalidProtocolBufferException, ExecutionException, InterruptedException {
    DeliveryVehicleData oldDeliveryVehicleData =
        transaction.get(deliveryVehicleRef).get().toObject(DeliveryVehicleData.class);
    if (oldDeliveryVehicleData == null) {
      oldDeliveryVehicleData = DeliveryVehicleData.builder().build();
    }
    UpdateAndDifference<DeliveryVehicleData> newDeliveryVehicleAndDifferences =
        getDeliveryVehicleAndDiff(logEntry, oldDeliveryVehicleData);

    DeliveryVehicleData newDeliveryVehicleData = newDeliveryVehicleAndDifferences.updated;
    Map<String, Change> deliveryVehicleChanges = newDeliveryVehicleAndDifferences.differences;

    DeliveryVehicleFleetEvent deliveryVehicleFleetEvent =
        DeliveryVehicleFleetEvent.builder()
            .setDeliveryVehicleId(deliveryVehicleId)
            .setOldDeliveryVehicle(oldDeliveryVehicleData)
            .setNewDeliveryVehicle(newDeliveryVehicleData)
            .setVehicleDifferences(deliveryVehicleChanges)
            .build();

    ImmutableList.Builder<FleetEvent> updateDeliveryFleetEventsBuilder = ImmutableList.builder();
    updateDeliveryFleetEventsBuilder.add(deliveryVehicleFleetEvent);

    /* Create any necessary Task events from the update log depending on what was updated in the
     * vehicle. */
    var oldCurrentDeliveryTaskIds =
        new HashSet<>(oldDeliveryVehicleData.getCurrentDeliveryTaskIds());
    var newCurrentDeliveryTaskIds =
        new HashSet<>(newDeliveryVehicleData.getCurrentDeliveryTaskIds());
    var newPlannedDeliveryTaskIds =
        new HashSet<>(newDeliveryVehicleData.getPlannedDeliveryTaskIds());

    var removedTaskIds = Sets.difference(oldCurrentDeliveryTaskIds, newCurrentDeliveryTaskIds);
    var taskUpdates = new ArrayList<DeliveryTaskData>();
    for (String removedTaskId : removedTaskIds) {
      /* If the task is still planned it means the task was moved */
      var deliveryTaskRef = firestoreDatabaseClient.getTaskDocument(removedTaskId);
      DeliveryTaskData oldDeliveryTaskData =
          transaction.get(deliveryTaskRef).get().toObject(DeliveryTaskData.class);
      if (newPlannedDeliveryTaskIds.contains(removedTaskId)) {
        /* For every task that is moved from the current stop to a planned stop,
         * add an event to signal what happened. */
        updateDeliveryFleetEventsBuilder.add(
            DeliveryTaskFleetEvent.builder()
                .setDeliveryTaskId(removedTaskId)
                .setOldDeliveryTask(oldDeliveryTaskData)
                .setOldDeliveryVehicle(oldDeliveryVehicleData)
                .setTaskMovedFromCurrentToPlanned(true)
                .setVehicleDifferences(deliveryVehicleChanges)
                .build());
      } else {
        var taskDifferences = new HashMap<String, Change>();
        if (oldDeliveryTaskData == null) {
          oldDeliveryTaskData = DeliveryTaskData.builder().setDeliveryTaskId(removedTaskId).build();
        }
        taskDifferences.put("state", new Change(oldDeliveryTaskData.getState(), "CLOSED"));
        var newDeliveryTaskData =
            oldDeliveryTaskData.toBuilder()
                .setState("CLOSED")
                .setExpireAt(TimeUtil.offsetFromNow(TimeUtil.ONE_HOUR_IN_SECONDS))
                .build();
        /* For every removed task, add an event to signal the closure of that task. */
        updateDeliveryFleetEventsBuilder.add(
            DeliveryTaskFleetEvent.builder()
                .setDeliveryTaskId(removedTaskId)
                .setOldDeliveryTask(oldDeliveryTaskData)
                .setNewDeliveryTask(newDeliveryTaskData)
                .setOldDeliveryVehicle(oldDeliveryVehicleData)
                .setNewDeliveryVehicle(newDeliveryVehicleData)
                .setTaskDifferences(taskDifferences)
                .setVehicleDifferences(deliveryVehicleChanges)
                .build());
        /* Record the delivery task id to update its state, no more updates will come for the task
         * except for potentially a task outcome update. The expireAt ttl will eventually delete the
         * task after a certain period. */
        taskUpdates.add(newDeliveryTaskData);
      }
    }
    var newTaskIds = Sets.difference(newCurrentDeliveryTaskIds, oldCurrentDeliveryTaskIds);
    for (String newTaskId : newTaskIds) {
      var deliveryTaskRef = firestoreDatabaseClient.getTaskDocument(newTaskId);
      DeliveryTaskData newDeliveryTask =
          transaction.get(deliveryTaskRef).get().toObject(DeliveryTaskData.class);
      if (newDeliveryTask == null) {
        newDeliveryTask =
            DeliveryTaskData.builder()
                .setDeliveryVehicleId(deliveryVehicleId)
                .setDeliveryTaskId(newTaskId)
                .setEventTimestamp(TimeUtil.protobufToLong(logEntry.getTimestamp()))
                .setExpireAt(TimeUtil.offsetFromNow(TimeUtil.ONE_HOUR_IN_SECONDS))
                .build();
        logger.warning(
            String.format(
                "Task id %s referenced by Vehicle, but has no stored state. Creating new task.",
                newTaskId));
      }
      /* Since this task was assigned to this vehicle, set the delivery vehicle id if it isn't set.
       */
      newDeliveryTask = newDeliveryTask.toBuilder().setDeliveryVehicleId(deliveryVehicleId).build();
      /* For every added task, add an event to signal the task has been added. */
      updateDeliveryFleetEventsBuilder.add(
          DeliveryTaskFleetEvent.builder()
              .setDeliveryTaskId(newTaskId)
              .setNewDeliveryTask(newDeliveryTask)
              .setOldDeliveryVehicle(oldDeliveryVehicleData)
              .setTaskMovedFromCurrentToPlanned(false)
              .setVehicleDifferences(deliveryVehicleChanges)
              .build());
      taskUpdates.add(newDeliveryTask);
    }
    var outputEvents =
        FleetEventCreator.callFleetEventHandlers(
            updateDeliveryFleetEventsBuilder.build(),
            fleetEventHandlers,
            transaction,
            firestoreDatabaseClient);

    List<Pair<DeliveryTaskData, DocumentReference>> taskDataToDocReferences = new ArrayList<>();
    // In firestore transactions, all reads must come before writes
    for (var taskUpdate : taskUpdates) {
      taskDataToDocReferences.add(
          new Pair(
              taskUpdate, firestoreDatabaseClient.getTaskDocument(taskUpdate.getDeliveryTaskId())));
    }
    transaction.set(deliveryVehicleRef, newDeliveryVehicleData);
    for (var newTaskUpdate : taskDataToDocReferences) {
      transaction.set(newTaskUpdate.getValue(), newTaskUpdate.getKey());
    }
    return outputEvents;
  }
}
