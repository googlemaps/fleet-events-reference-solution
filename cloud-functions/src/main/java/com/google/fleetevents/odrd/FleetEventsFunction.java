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

package com.google.fleetevents.odrd;

import com.google.fleetengine.auth.token.factory.signer.SignerInitializationException;
import com.google.fleetevents.FleetEventsFunctionBase;
import com.google.fleetevents.odrd.handlers.DistanceRemainingHandler;
import com.google.fleetevents.odrd.handlers.EtaAbsoluteChangeHandler;
import com.google.fleetevents.odrd.handlers.EtaRelativeChangeHandler;
import com.google.fleetevents.odrd.handlers.TimeRemainingHandler;
import com.google.fleetevents.odrd.handlers.TripStatusHandler;
import java.io.IOException;

/**
 * Default fleet events function implementation for ODRD. Modify for custom logic. Register Fleet
 * Event handlers here. Entrypoint class for Cloud Functions.
 */
public class FleetEventsFunction extends FleetEventsFunctionBase {

  public FleetEventsFunction() throws IOException, SignerInitializationException {
    super(new FleetEventCreator());
    registerFleetEventHandler(new DistanceRemainingHandler());
    registerFleetEventHandler(new TimeRemainingHandler());
    registerFleetEventHandler(new TripStatusHandler());
    registerFleetEventHandler(new EtaAbsoluteChangeHandler());
    registerFleetEventHandler(new EtaRelativeChangeHandler());
  }
}
