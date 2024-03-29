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

package com.google.fleetevents.beam.client;

import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import com.google.cloud.firestore.FirestoreOptions.EmulatorCredentials;
import java.io.IOException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FirestoreEmulatorDatabaseClient extends FirestoreDatabaseClient {

  private static final Logger logger =
      Logger.getLogger(FirestoreEmulatorDatabaseClient.class.getName());

  public FirestoreEmulatorDatabaseClient() throws IOException {}

  @Override
  public Firestore initFirestore(String projectId, String databaseId, String appName)
      throws IOException {
    try {
      logger.log(Level.INFO, "Using a firestore emulator");
      if (this.firestore != null) return firestore;
      FirestoreOptions firestoreOptions =
          FirestoreOptions.newBuilder()
              .setEmulatorHost("localhost:8080")
              .setCredentials(new EmulatorCredentials())
              .setProjectId(projectId)
              .setDatabaseId(databaseId)
              .build();

      logger.log(Level.INFO, "Test firestore initialized");

      firestore = firestoreOptions.getService();

      return this.firestore;
    } catch (Exception e) {
      logger.log(Level.WARNING, e.getMessage());
      e.printStackTrace();
      throw e;
    }
  }

  // test method only
  public void cleanupTest(String projectId) throws IOException {
    Firestore testFirestore = initFirestore(projectId, "(default)", "test" + UUID.randomUUID());

    CollectionReference collection = testFirestore.collection(this.TASK_COLLECTION_NAME);
    for (DocumentReference ref : collection.listDocuments()) {
      logger.log(Level.INFO, "deleting " + ref.getId());
      ref.delete();
    }
  }
}
