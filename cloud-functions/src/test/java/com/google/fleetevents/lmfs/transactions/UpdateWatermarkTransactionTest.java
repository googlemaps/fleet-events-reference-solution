package com.google.fleetevents.lmfs.transactions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.fleetevents.common.util.TimeUtil;
import com.google.fleetevents.helpers.FakeFirestoreHelper;
import com.google.fleetevents.helpers.FleetEventsTestHelper;
import com.google.logging.v2.LogEntry;
import com.google.protobuf.Timestamp;
import java.util.HashMap;
import org.junit.Test;

public class UpdateWatermarkTransactionTest {
  private static final String DOCUMENT_KEY = UpdateWatermarkTransaction.DOCUMENT_KEY;

  @Test
  public void updateWatermark_createAddsWatermark() throws Exception {
    HashMap<String, Object> fakeBackend = new HashMap<>();

    var firestoreDatabaseClient = FakeFirestoreHelper.getFakeFirestoreDatabaseClient();
    var transaction = FakeFirestoreHelper.getFakeTransaction(fakeBackend);

    LogEntry logEntry = FleetEventsTestHelper.createTaskLog1();
    UpdateWatermarkTransaction watermarkTransaction =
        new UpdateWatermarkTransaction(
            "providers/test-project/tasks/task1", logEntry, firestoreDatabaseClient);
    watermarkTransaction.updateCallback(transaction);

    assertTrue(fakeBackend.containsKey("watermark/task1"));
    assertEquals(
        TimeUtil.protobufToLong(logEntry.getTimestamp()),
        ((HashMap<String, Long>) fakeBackend.get("watermark/task1")).get(DOCUMENT_KEY));
  }

  @Test
  public void updateWatermark_updateAddsWatermark() throws Exception {

    HashMap<String, Object> fakeBackend = new HashMap<>();
    var firestoreDatabaseClient = FakeFirestoreHelper.getFakeFirestoreDatabaseClient();
    var transaction = FakeFirestoreHelper.getFakeTransaction(fakeBackend);

    LogEntry logEntry1 =
        FleetEventsTestHelper.createTaskLog1().toBuilder()
            .setTimestamp(Timestamp.newBuilder().setSeconds(0))
            .build();
    LogEntry logEntry2 =
        FleetEventsTestHelper.updateTaskLog1().toBuilder()
            .setTimestamp(Timestamp.newBuilder().setSeconds(2))
            .build();

    UpdateWatermarkTransaction watermarkTransaction1 =
        new UpdateWatermarkTransaction(
            "providers/test-project/tasks/task1", logEntry1, firestoreDatabaseClient);
    UpdateWatermarkTransaction watermarkTransaction2 =
        new UpdateWatermarkTransaction(
            "providers/test-project/tasks/task1", logEntry2, firestoreDatabaseClient);

    watermarkTransaction1.updateCallback(transaction);
    watermarkTransaction2.updateCallback(transaction);

    assertTrue(fakeBackend.containsKey("watermark/task1"));
    assertEquals(
        TimeUtil.protobufToLong(logEntry2.getTimestamp()),
        ((HashMap<String, Long>) fakeBackend.get("watermark/task1")).get(DOCUMENT_KEY));
  }

  @Test
  public void updateWatermark_lateLogDoesNotUpdateWatermark() throws Exception {
    HashMap<String, Object> fakeBackend = new HashMap<>();
    var firestoreDatabaseClient = FakeFirestoreHelper.getFakeFirestoreDatabaseClient();
    var transaction = FakeFirestoreHelper.getFakeTransaction(fakeBackend);

    LogEntry logEntry1 =
        FleetEventsTestHelper.createTaskLog1().toBuilder()
            .setTimestamp(Timestamp.newBuilder().setSeconds(3))
            .build();
    LogEntry logEntry2 =
        FleetEventsTestHelper.updateTaskLog1().toBuilder()
            .setTimestamp(Timestamp.newBuilder().setSeconds(1))
            .build();

    UpdateWatermarkTransaction watermarkTransaction1 =
        new UpdateWatermarkTransaction(
            "providers/test-project/tasks/task1", logEntry1, firestoreDatabaseClient);
    UpdateWatermarkTransaction watermarkTransaction2 =
        new UpdateWatermarkTransaction(
            "providers/test-project/tasks/task1", logEntry2, firestoreDatabaseClient);

    watermarkTransaction1.updateCallback(transaction);
    watermarkTransaction2.updateCallback(transaction);

    assertTrue(fakeBackend.containsKey("watermark/task1"));
    assertEquals(
        TimeUtil.protobufToLong(logEntry1.getTimestamp()),
        ((HashMap<String, Long>) fakeBackend.get("watermark/task1")).get(DOCUMENT_KEY));
  }
}
