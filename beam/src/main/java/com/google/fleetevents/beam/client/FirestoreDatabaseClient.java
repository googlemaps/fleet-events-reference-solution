package com.google.fleetevents.beam.client;

import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import com.google.cloud.firestore.Transaction;
import com.google.cloud.firestore.WriteResult;
import com.google.common.base.Preconditions;
import com.google.fleetevents.beam.model.TaskMetadata;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Client for firestore with convenience methods for accessing a vehicle or task object through the
 * Firestore connection.
 */
public class FirestoreDatabaseClient implements Serializable {
  private static final Logger logger = Logger.getLogger(FirestoreDatabaseClient.class.getName());

  protected final String TASK_COLLECTION_NAME = "dataflowTaskMetadata";
  protected Firestore firestore;

  public FirestoreDatabaseClient() throws IOException {}

  public Firestore initFirestore(String projectId, String databaseId, String appName)
      throws IOException {

    try {
      if (this.firestore != null) return firestore;
      GoogleCredentials credentials = GoogleCredentials.getApplicationDefault();
      FirestoreOptions firestoreOptions =
          FirestoreOptions.newBuilder()
              .setCredentials(credentials)
              .setProjectId(projectId)
              .setDatabaseId(databaseId)
              .build();
      firestore = firestoreOptions.getService();
      logger.log(Level.INFO, "firestore initialized");

      return firestore;
    } catch (Exception e) {
      logger.log(Level.WARNING, e.getMessage());
      e.printStackTrace();
      throw e;
    }
  }

  public <T> ApiFuture<T> runTransaction(Transaction.Function<T> result) {
    checkFirestoreInitialized();
    return firestore.runTransaction(result);
  }

  public TaskMetadata getTask(DocumentReference documentReference)
      throws ExecutionException, InterruptedException {
    ApiFuture<DocumentSnapshot> doc = documentReference.get();
    if (!doc.get().exists()) return null;
    TaskMetadata result = doc.get().get(TASK_COLLECTION_NAME, TaskMetadata.class);
    return result;
  }

  public DocumentReference getTaskReference(String taskName) {
    checkFirestoreInitialized();
    String id = getDocumentId(taskName);
    DocumentReference documentReference = firestore.collection(TASK_COLLECTION_NAME).document(id);
    return documentReference;
  }

  public WriteResult updateTask(DocumentReference taskReference, TaskMetadata taskMetadata)
      throws ExecutionException, InterruptedException {
    Map<String, Object> data = new HashMap<>();
    data.put(TASK_COLLECTION_NAME, taskMetadata);
    ApiFuture<WriteResult> result = taskReference.set(data);
    WriteResult writeResult = result.get();
    return writeResult;
  }

  public void shutdown() {
    if (firestore != null) {
      firestore.shutdown();
    }
  }

  private void checkFirestoreInitialized() {
    Preconditions.checkNotNull(
        firestore, "Firestore is null! Please call FirestoreDatabaseClient.initFirestore");
  }

  private String getDocumentId(String taskName) {
    String[] idString = taskName.split("/");
    return idString[idString.length - 1];
  }
}
