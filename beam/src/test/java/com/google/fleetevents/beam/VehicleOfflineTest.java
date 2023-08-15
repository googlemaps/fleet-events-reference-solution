package com.google.fleetevents.beam;

import com.google.fleetevents.beam.config.DataflowJobConfig;
import com.google.fleetevents.beam.util.SampleLogs;
import com.google.logging.v2.LogEntry;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.Timestamp;
import com.google.protobuf.util.JsonFormat;
import java.io.IOException;
import java.util.Arrays;
import org.apache.beam.sdk.coders.StringUtf8Coder;
import org.apache.beam.sdk.testing.PAssert;
import org.apache.beam.sdk.testing.TestPipeline;
import org.apache.beam.sdk.testing.TestStream;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.TimestampedValue;
import org.joda.time.Instant;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class VehicleOfflineTest {
  @Rule public final transient TestPipeline pipeline = TestPipeline.create();
  private static final int GAP_SIZE = 3;
  private static final int THRESHOLD = 60 * GAP_SIZE;
  private static final int SECONDS_TO_MILLIS = 1000;

  private DataflowJobConfig config;

  @Before
  public void setup() {
    config = DataflowJobConfig.Builder.newBuilder().setGapSize(GAP_SIZE).build();
  }

  @Test
  public void testOneLog() throws IOException {
    LogEntry logEntry = SampleLogs.getUpdateDeliveryVehicleLogEntry1();
    PCollection<String> input = pipeline.apply(Create.of(Arrays.asList(getJson(logEntry))));
    PCollection<String> output = VehicleOffline.run(input, config);

    String expectedResult =
        "providers/fake-gcp-project/deliveryVehicles/sample_fleet_events_demo_vehicle_d0fba8: "
            + "Boundary{min=1681502796, max=1681502796, "
            + "minId='159733b879b0e36f5c35d41998aa51c9, "
            + "maxId='159733b879b0e36f5c35d41998aa51c9}";
    PAssert.that(output).containsInAnyOrder(expectedResult);
    pipeline.run();
  }

  @Test
  public void testMultiLogOneSession() throws IOException {
    LogEntry logEntry1 =
        SampleLogs.getUpdateDeliveryVehicleLogEntry1().toBuilder()
            .setTimestamp(Timestamp.newBuilder().setSeconds(0))
            .build();
    LogEntry logEntry2 =
        SampleLogs.getUpdateDeliveryVehicleLogEntry1().toBuilder()
            .setTimestamp(Timestamp.newBuilder().setSeconds(THRESHOLD))
            .build();

    PCollection<String> input =
        pipeline.apply(Create.of(Arrays.asList(getJson(logEntry1), getJson(logEntry2))));
    PCollection<String> output = VehicleOffline.run(input, config);

    String expectedResult =
        "providers/fake-gcp-project/deliveryVehicles/sample_fleet_events_demo_vehicle_d0fba8: "
            + "Boundary{min=0, max=180, "
            + "minId='159733b879b0e36f5c35d41998aa51c9, "
            + "maxId='159733b879b0e36f5c35d41998aa51c9}";
    PAssert.that(output).containsInAnyOrder(expectedResult);
    pipeline.run();
  }

  @Test
  public void testMultiLogTwoSessions() throws IOException {
    Instant startTime = new Instant(0);
    LogEntry logEntry1 =
        SampleLogs.getUpdateDeliveryVehicleLogEntry1().toBuilder()
            .setTimestamp(Timestamp.newBuilder().setSeconds(0))
            .build();
    LogEntry logEntry2 =
        SampleLogs.getUpdateDeliveryVehicleLogEntry1().toBuilder()
            .setTimestamp(Timestamp.newBuilder().setSeconds(THRESHOLD + 1))
            .build();
    TestStream<String> createLogs =
        TestStream.create(StringUtf8Coder.of())
            .addElements(timestampedString(logEntry1, 0))
            .advanceProcessingTime(org.joda.time.Duration.millis(THRESHOLD * SECONDS_TO_MILLIS + 1))
            .addElements(timestampedString(logEntry2, THRESHOLD * SECONDS_TO_MILLIS + 1))
            .advanceWatermarkToInfinity();

    PCollection<String> input = pipeline.apply(createLogs);
    PCollection<String> output = VehicleOffline.run(input, config);

    String expectedResult1 =
        "providers/fake-gcp-project/deliveryVehicles/sample_fleet_events_demo_vehicle_d0fba8: "
            + "Boundary{min=0, max=0, "
            + "minId='159733b879b0e36f5c35d41998aa51c9, "
            + "maxId='159733b879b0e36f5c35d41998aa51c9}";
    String expectedResult2 =
        "providers/fake-gcp-project/deliveryVehicles/sample_fleet_events_demo_vehicle_d0fba8: "
            + "Boundary{min=181, max=181, "
            + "minId='159733b879b0e36f5c35d41998aa51c9, "
            + "maxId='159733b879b0e36f5c35d41998aa51c9}";
    PAssert.that(output).containsInAnyOrder(Arrays.asList(expectedResult1, expectedResult2));
    pipeline.run();
  }

  @Test
  public void testMultiKeyOneSession() throws IOException {
    LogEntry logEntry1 =
        SampleLogs.getUpdateDeliveryVehicleLogEntry1().toBuilder()
            .setTimestamp(Timestamp.newBuilder().setSeconds(0))
            .setInsertId("testStartId")
            .build();
    LogEntry logEntry2 =
        SampleLogs.getUpdateDeliveryVehicleLogEntry1().toBuilder()
            .setTimestamp(Timestamp.newBuilder().setSeconds(THRESHOLD / 2))
            .setInsertId("testEndId")
            .build();
    LogEntry logEntry3 =
        SampleLogs.getUpdateDeliveryVehicleLogEntry2().toBuilder()
            .setTimestamp(Timestamp.newBuilder().setSeconds(0))
            .setInsertId("testOtherId")
            .build();

    PCollection<String> input =
        pipeline.apply(
            Create.of(Arrays.asList(getJson(logEntry1), getJson(logEntry2), getJson(logEntry3))));
    PCollection<String> output = VehicleOffline.run(input, config);

    String expectedResult1 =
        "providers/fake-gcp-project/deliveryVehicles/sample_fleet_events_demo_vehicle_d0fba8: "
            + "Boundary{min=0, max=90, "
            + "minId='testStartId, "
            + "maxId='testEndId}";
    String expectedResult2 =
        "providers/fake-gcp-project/deliveryVehicles/sample_fleet_events_demo_vehicle_1884d0: "
            + "Boundary{min=0, max=0, "
            + "minId='testOtherId, "
            + "maxId='testOtherId}";
    PAssert.that(output).containsInAnyOrder(Arrays.asList(expectedResult1, expectedResult2));
    pipeline.run();
  }

  @Test
  public void testMultiKeyTwoSession() throws IOException {
    LogEntry logEntry1 =
        SampleLogs.getUpdateDeliveryVehicleLogEntry1().toBuilder()
            .setTimestamp(Timestamp.newBuilder().setSeconds(0))
            .setInsertId("testKey1Window1")
            .build();
    LogEntry logEntry2 =
        SampleLogs.getUpdateDeliveryVehicleLogEntry1().toBuilder()
            .setTimestamp(Timestamp.newBuilder().setSeconds(THRESHOLD + 1))
            .setInsertId("testKey1Window2")
            .build();
    LogEntry logEntry3 =
        SampleLogs.getUpdateDeliveryVehicleLogEntry2().toBuilder()
            .setTimestamp(Timestamp.newBuilder().setSeconds(0))
            .setInsertId("testKey2Window1")
            .build();

    TestStream<String> createLogs =
        TestStream.create(StringUtf8Coder.of())
            .addElements(timestampedString(logEntry1, 0), timestampedString(logEntry3, 0))
            .advanceProcessingTime(org.joda.time.Duration.millis(THRESHOLD * SECONDS_TO_MILLIS + 1))
            .addElements(timestampedString(logEntry2, THRESHOLD * SECONDS_TO_MILLIS + 1))
            .advanceWatermarkToInfinity();

    PCollection<String> input = pipeline.apply(createLogs);
    PCollection<String> output = VehicleOffline.run(input, config);

    String expectedResult1 =
        "providers/fake-gcp-project/deliveryVehicles/sample_fleet_events_demo_vehicle_d0fba8: "
            + "Boundary{min=0, max=0, "
            + "minId='testKey1Window1, "
            + "maxId='testKey1Window1}";
    String expectedResult2 =
        "providers/fake-gcp-project/deliveryVehicles/sample_fleet_events_demo_vehicle_1884d0: "
            + "Boundary{min=0, max=0, "
            + "minId='testKey2Window1, "
            + "maxId='testKey2Window1}";
    String expectedResult3 =
        "providers/fake-gcp-project/deliveryVehicles/sample_fleet_events_demo_vehicle_d0fba8: "
            + "Boundary{min=181, max=181, "
            + "minId='testKey1Window2, "
            + "maxId='testKey1Window2}";
    PAssert.that(output)
        .containsInAnyOrder(Arrays.asList(expectedResult1, expectedResult2, expectedResult3));
    pipeline.run();
  }

  private TimestampedValue<String> timestampedString(LogEntry s, long d)
      throws InvalidProtocolBufferException {
    return TimestampedValue.of(getJson(s), new Instant(0).plus(d));
  }

  public static String getJson(Message message) throws InvalidProtocolBufferException {
    String json = JsonFormat.printer().print(message);
    return json;
  }
}
