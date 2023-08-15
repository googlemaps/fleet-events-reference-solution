package com.google.fleetevents.beam.client;

import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import com.google.firebase.internal.EmulatorCredentials;
import java.io.IOException;
import java.io.Serializable;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FirestoreEmulatorDatabaseClient extends FirestoreDatabaseClient
    implements Serializable {
  private static final Logger logger =
      Logger.getLogger(FirestoreEmulatorDatabaseClient.class.getName());

  public FirestoreEmulatorDatabaseClient() throws IOException {}

  @Override
  public Firestore initFirestore(String projectId, String appName) throws IOException {
    try {
      logger.log(Level.INFO, "Using a firestore emulator");
      if (this.firestore != null) return firestore;
      FirestoreOptions firestoreOptions =
          FirestoreOptions.newBuilder().setEmulatorHost("localhost:8080").build();
      FirebaseOptions options =
          new FirebaseOptions.Builder()
              .setCredentials(new EmulatorCredentials())
              .setProjectId(projectId)
              .setFirestoreOptions(firestoreOptions)
              .build();
      logger.log(Level.INFO, "Test firestore initialized");
      FirebaseApp app = FirebaseApp.initializeApp(options, appName);
      this.firestore = FirestoreClient.getFirestore(app);
      return this.firestore;
    } catch (Exception e) {
      System.out.println(e.getMessage());
      e.printStackTrace();
      throw e;
    }
  }

  // test method only
  public void cleanupTest(String projectId) throws IOException {
    Firestore testFirestore = initFirestore(projectId, "test" + UUID.randomUUID());

    CollectionReference collection = testFirestore.collection(this.TASK_COLLECTION_NAME);
    for (DocumentReference ref : collection.listDocuments()) {
      logger.log(Level.INFO, "deleting " + ref.getId());
      ref.delete();
    }
  }
}
