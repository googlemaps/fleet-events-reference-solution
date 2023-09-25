package com.google.fleetevents.beam;

import static org.mockito.Mockito.doReturn;

import com.google.fleetevents.beam.client.FirestoreEmulatorDatabaseClient;
import com.google.fleetevents.beam.config.DataflowJobConfig;
import com.google.fleetevents.beam.model.output.TaskOutcomeChangeOutputEvent;
import com.google.fleetevents.beam.util.SampleLogs;
import google.maps.fleetengine.delivery.v1.Task;
import java.io.IOException;
import java.util.Arrays;
import org.apache.beam.sdk.extensions.protobuf.ProtoCoder;
import org.apache.beam.sdk.testing.PAssert;
import org.apache.beam.sdk.testing.TestPipeline;
import org.apache.beam.sdk.testing.TestStream;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.TimestampedValue;
import org.joda.time.Instant;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mockito;

@RunWith(JUnit4.class)
public class TaskOutcomeChangeTest {
  @Rule public final transient TestPipeline pipeline = TestPipeline.create();

  // window in minutes
  private static final int WINDOW_SIZE = 3;
  private static final String PROJECT_ID = "gcp-project";

  private TaskOutcomeChange mockedTaskOutcomeChange;
  private FirestoreEmulatorDatabaseClient firestoreClient;
  private DataflowJobConfig config;

  @Before
  public void setup() throws IOException {
    config =
        DataflowJobConfig.Builder.newBuilder()
            .setWindowSize(WINDOW_SIZE)
            .setDatastoreProjectId(PROJECT_ID)
            .build();
    TaskOutcomeChange taskOutcomeChange = new TaskOutcomeChange(config);
    mockedTaskOutcomeChange = Mockito.spy(taskOutcomeChange);
    firestoreClient = new FirestoreEmulatorDatabaseClient();
    doReturn(firestoreClient).when(mockedTaskOutcomeChange).getFirestoreDatabaseClient();
  }

  @After
  public void cleanup() throws IOException {
    firestoreClient.cleanupTest(PROJECT_ID);
  }

  @Test
  public void testTaskOutcome_newOutcomeTriggers() throws IOException {
    Task createTask = SampleLogs.getCreateTask1();
    createTask =
        createTask
            .toBuilder()
            .setName("providers/fake-gcp-project/deliveryTasks/testTask1")
            .build();
    PCollection<Task> input = pipeline.apply(Create.of(Arrays.asList(createTask)));

    PCollection<String> output = mockedTaskOutcomeChange.getTaskOutcomeChanges(input, config);

    TaskOutcomeChangeOutputEvent expectedResult = new TaskOutcomeChangeOutputEvent();
    expectedResult.setNewOutcome("TASK_OUTCOME_UNSPECIFIED");
    expectedResult.setTask(createTask);
    PAssert.that(output).containsInAnyOrder(expectedResult.toString());
    pipeline.run();
  }

  @Test
  public void testTaskOutcome_outcomeChanges() throws IOException {
    // Task is just created, should trigger an UNSPECIFIED update
    Task createTask =
        SampleLogs.getCreateTask1()
            .toBuilder()
            .setName("providers/fake-gcp-project/deliveryTasks/testTask1")
            .build();
    Task updateTask =
        SampleLogs.getUpdateTask1()
            .toBuilder()
            .setName("providers/fake-gcp-project/deliveryTasks/testTask1")
            .setTaskOutcome(Task.TaskOutcome.valueOf("SUCCEEDED"))
            .build();
    TestStream<Task> tasks =
        TestStream.create(ProtoCoder.of(Task.class))
            .addElements(timestampedTask(createTask, 0))
            .addElements(timestampedTask(updateTask, 1))
            .advanceWatermarkToInfinity();

    PCollection<Task> input1 = pipeline.apply(tasks);

    PCollection<String> output1 = mockedTaskOutcomeChange.getTaskOutcomeChanges(input1, config);

    TaskOutcomeChangeOutputEvent expectedResult1 = new TaskOutcomeChangeOutputEvent();
    expectedResult1.setNewOutcome("TASK_OUTCOME_UNSPECIFIED");
    expectedResult1.setTask(createTask);
    TaskOutcomeChangeOutputEvent expectedResult2 = new TaskOutcomeChangeOutputEvent();
    expectedResult2.setPreviousOutcome("TASK_OUTCOME_UNSPECIFIED");
    expectedResult2.setNewOutcome("SUCCEEDED");
    expectedResult2.setTask(updateTask);
    PAssert.that(output1)
        .containsInAnyOrder(expectedResult1.toString(), expectedResult2.toString());
    pipeline.run();
  }

  @Test
  public void testTaskOutcome_noChange() throws IOException {
    Task updateTask1 =
        SampleLogs.getUpdateTask1()
            .toBuilder()
            .setName("providers/fake-gcp-project/deliveryTasks/testTask1")
            .setTaskOutcome(Task.TaskOutcome.valueOf("SUCCEEDED"))
            .build();
    Task updateTask2 =
        SampleLogs.getUpdateTask1()
            .toBuilder()
            .setName("providers/fake-gcp-project/deliveryTasks/testTask1")
            .setTaskOutcome(Task.TaskOutcome.valueOf("SUCCEEDED"))
            .build();
    TestStream<Task> tasks =
        TestStream.create(ProtoCoder.of(Task.class))
            .addElements(timestampedTask(updateTask1, 0))
            .addElements(timestampedTask(updateTask2, 1))
            .advanceWatermarkToInfinity();

    PCollection<Task> input1 = pipeline.apply(tasks);

    PCollection<String> output1 = mockedTaskOutcomeChange.getTaskOutcomeChanges(input1, config);

    TaskOutcomeChangeOutputEvent expectedResult1 = new TaskOutcomeChangeOutputEvent();
    expectedResult1.setNewOutcome("SUCCEEDED");
    expectedResult1.setTask(updateTask1);
    PAssert.that(output1).containsInAnyOrder(expectedResult1.toString());
    pipeline.run();
  }

  private TimestampedValue<Task> timestampedTask(Task task, long d) {
    return TimestampedValue.of(task, new Instant(0).plus(d));
  }
}
