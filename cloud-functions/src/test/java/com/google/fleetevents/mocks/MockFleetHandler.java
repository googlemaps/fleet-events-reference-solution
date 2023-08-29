package com.google.fleetevents.mocks;

import autovalue.shaded.com.google.common.collect.ImmutableList;
import com.google.cloud.firestore.Transaction;
import com.google.fleetevents.FleetEventHandler;
import com.google.fleetevents.common.database.FirestoreDatabaseClient;
import com.google.fleetevents.common.models.FleetEvent;
import com.google.fleetevents.common.models.OutputEvent;
import java.util.List;

public class MockFleetHandler implements FleetEventHandler {

  @Override
  public List<OutputEvent> handleEvent(FleetEvent fleetEvent, Transaction transaction) {
    var outputEvent = new OutputEvent();
    outputEvent.setFleetEvent(fleetEvent);
    return ImmutableList.of(outputEvent);
  }

  @Override
  public boolean respondsTo(
      FleetEvent fleetEvent,
      Transaction transaction,
      FirestoreDatabaseClient firestoreDatabaseClient) {
    return true;
  }

  @Override
  public boolean verifyOutput(OutputEvent output) {
    return true;
  }
}
