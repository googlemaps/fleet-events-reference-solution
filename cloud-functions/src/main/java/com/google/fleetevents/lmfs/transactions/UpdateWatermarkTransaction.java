package com.google.fleetevents.lmfs.transactions;

import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Transaction;
import com.google.fleetevents.common.database.FirestoreDatabaseClient;
import com.google.fleetevents.common.util.NameFormatter;
import com.google.fleetevents.common.util.TimeUtil;
import com.google.logging.v2.LogEntry;
import java.util.HashMap;
import java.util.Map;
import net.logstash.logback.argument.StructuredArguments;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Update watermark per entity. Returns the current watermark for the entity.
// The watermark is based on LogEntry.Timestamp. If the current timestamp seen is less than the
// watermark,
// that timestamp is considered out of order. For example:
//   1. Vehicle V1 is created at t1. watermark=t1
//   2. Vehicle V1 starts moving from t2 to t10. The function receives up to log t4 in order,
// watermark=t4.
//   3. The function receives t10. watermark=t10
//   4. The function sees t5. watermark=t10 and t5 is out of order
//   5. The function sees t6, t7, t8, t9. watermark=t10 and t6-9 are out of order.
//   6. The function sees t11. watermark=t11
public class UpdateWatermarkTransaction implements Transaction.Function<Long> {

  private static final Logger logger =
      LoggerFactory.getLogger(UpdateWatermarkTransaction.class.getName());
  public static final String DOCUMENT_KEY = "value";
  private final String id;
  private final LogEntry logEntry;
  private FirestoreDatabaseClient firestoreDatabaseClient;

  public UpdateWatermarkTransaction(
      String name, LogEntry logEntry, FirestoreDatabaseClient firestoreDatabaseClient) {
    this.id = NameFormatter.getIdFromName(name);
    this.logEntry = logEntry;
    this.firestoreDatabaseClient = firestoreDatabaseClient;
  }

  @Override
  public Long updateCallback(Transaction transaction) throws Exception {
    DocumentReference watermarkDocRef = firestoreDatabaseClient.getWaterMarkReference(id);
    DocumentSnapshot watermark = transaction.get(watermarkDocRef).get();
    long currentWatermark;
    if (watermark.exists()) {
      // Check if logEntry is out of order
      currentWatermark = watermark.getLong(DOCUMENT_KEY);
      if (currentWatermark > TimeUtil.protobufToLong(logEntry.getTimestamp())) {
        logger.warn(
            String.format("found out of order event"),
            StructuredArguments.kv("entityId", id),
            StructuredArguments.kv("logId", logEntry.getInsertId()));
        return currentWatermark;
      }
    }
    currentWatermark = TimeUtil.protobufToLong(logEntry.getTimestamp());
    setWatermark(watermarkDocRef, currentWatermark, transaction);
    return currentWatermark;
  }

  public void setWatermark(
      DocumentReference documentReference, Long timestamp, Transaction transaction) {
    Map<String, Long> values = new HashMap<>();
    values.put(DOCUMENT_KEY, timestamp);
    transaction.set(documentReference, values);
  }
}
