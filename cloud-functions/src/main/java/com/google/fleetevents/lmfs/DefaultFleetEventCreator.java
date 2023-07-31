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

package com.google.fleetevents.lmfs;

import com.google.fleetengine.auth.token.factory.signer.SignerInitializationException;
import com.google.fleetevents.FleetEventCreator;
import com.google.fleetevents.common.database.FirestoreDatabaseClient;
import com.google.fleetevents.common.util.FleetEngineClient;
import java.io.IOException;

/** Default implementation of the fleet event creator class. Modify for custom logic. */
public class DefaultFleetEventCreator extends FleetEventCreator {

  private static FirestoreDatabaseClient db;
  private static FleetEngineClient fleetEngineClient;

  public DefaultFleetEventCreator() throws IOException, SignerInitializationException {
    super();
    db = new FirestoreDatabaseClient();
    fleetEngineClient = new FleetEngineClient();
  }

  @Override
  public FirestoreDatabaseClient getDatabase() {
    return db;
  }

  @Override
  public FleetEngineClient getFleetEngineClient() {
    return fleetEngineClient;
  }
}
