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

package com.google.fleetevents.common.database;

import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import com.google.cloud.firestore.Transaction.Function;
import com.google.fleetevents.lmfs.config.FleetEventConfig;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * Client for firestore with convenience methods for accessing a vehicle or task object through the
 * Firestore connection.
 */
public class FirestoreDatabaseClient {

  private final String VEHICLE_COLLECTION_NAME;
  private final String TASK_COLLECTION_NAME;
  private final String WATERMARK_COLLECTION_NAME = "watermark";
  private final Firestore firestore;

  public FirestoreDatabaseClient() throws IOException {
    GoogleCredentials credentials = GoogleCredentials.getApplicationDefault();
    var projectId = FleetEventConfig.getProjectId();

    FirestoreOptions firestoreOptions =
        FirestoreOptions.getDefaultInstance().toBuilder()
            .setCredentials(credentials)
            .setProjectId(projectId)
            .build();
    firestore = firestoreOptions.getService();

    VEHICLE_COLLECTION_NAME = FleetEventConfig.getDeliveryVehicleCollectionName();
    TASK_COLLECTION_NAME = FleetEventConfig.getTaskCollectionName();
  }

  public FirestoreDatabaseClient(Firestore firestore) {
    this.firestore = firestore;
    VEHICLE_COLLECTION_NAME = FleetEventConfig.getDeliveryVehicleCollectionName();
    TASK_COLLECTION_NAME = FleetEventConfig.getTaskCollectionName();
  }

  protected Firestore getFirestore() {
    return this.firestore;
  }

  public <T> ApiFuture<T> runTransaction(Function<T> function) {
    return getFirestore().runTransaction(function);
  }

  public DocumentReference getTaskDocument(String deliveryTaskId) {
    return getDocument(TASK_COLLECTION_NAME, deliveryTaskId);
  }

  public DocumentReference getVehicleDocument(String deliveryVehicleId) {
    return getDocument(VEHICLE_COLLECTION_NAME, deliveryVehicleId);
  }

  public DocumentReference getWaterMarkReference(String id)
      throws ExecutionException, InterruptedException {
    DocumentReference ref = getFirestore().collection(WATERMARK_COLLECTION_NAME).document(id);
    return ref;
  }

  private DocumentReference getDocument(String collectionName, String id) {
    return getFirestore().collection(collectionName).document(id);
  }
}
