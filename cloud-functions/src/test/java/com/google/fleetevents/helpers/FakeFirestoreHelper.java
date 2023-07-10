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

package com.google.fleetevents.helpers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;

import com.google.api.core.ApiFutures;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Transaction;
import com.google.fleetevents.database.FirestoreDatabaseClient;
import java.util.HashMap;
import org.mockito.Mockito;

/** Helper for creating a fake firestore used in testing transactions. */
public class FakeFirestoreHelper {

  public static FirestoreDatabaseClient getFakeFirestoreDatabaseClient() {
    Firestore mockFirestore = Mockito.mock(Firestore.class);
    FirestoreDatabaseClient firestoreDatabaseClient = new FirestoreDatabaseClient(mockFirestore);
    doAnswer(
            invocationOnMock -> {
              var collectionName = (String) invocationOnMock.getArguments()[0];
              var mock = Mockito.mock(CollectionReference.class);
              doAnswer(
                      iOM -> {
                        var args = iOM.getArguments();
                        var document = (String) args[0];
                        var docRef = Mockito.mock(DocumentReference.class);
                        doReturn(String.format("%s/%s", collectionName, document))
                            .when(docRef)
                            .getId();
                        return docRef;
                      })
                  .when(mock)
                  .document(any(String.class));
              return mock;
            })
        .when(mockFirestore)
        .collection(any(String.class));
    return firestoreDatabaseClient;
  }

  public static Transaction getFakeTransaction(HashMap<String, Object> fakeBackend) {
    Transaction transaction = Mockito.mock(Transaction.class);
    doAnswer(
            invocationMock -> {
              var args = invocationMock.getArguments();
              var docRef = (DocumentReference) args[0];
              var object = args[1];
              fakeBackend.put(docRef.getId(), object);
              return null;
            })
        .when(transaction)
        .set(any(DocumentReference.class), any(Object.class));

    doAnswer(
            invocationMock -> {
              var args = invocationMock.getArguments();
              var docRef = (DocumentReference) args[0];
              var data = fakeBackend.get(docRef.getId());
              var docSnapshot = Mockito.mock(DocumentSnapshot.class);
              doReturn(data).when(docSnapshot).toObject(any(Class.class));
              return ApiFutures.immediateFuture(docSnapshot);
            })
        .when(transaction)
        .get(any(DocumentReference.class));
    return transaction;
  }
}
