package com.google.fleetevents.beam;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Transaction;
import com.google.fleetevents.beam.client.FirestoreDatabaseClient;
import com.google.fleetevents.beam.config.DataflowJobConfig;
import com.google.fleetevents.beam.model.TaskMetadata;
import com.google.fleetevents.beam.model.output.TaskOutcomeResult;
import com.google.fleetevents.beam.util.ProtoParser;
import com.google.logging.v2.LogEntry;
import com.google.protobuf.InvalidProtocolBufferException;
import google.maps.fleetengine.delivery.v1.Task;
import java.io.IOException;
import java.io.Serializable;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.transforms.windowing.FixedWindows;
import org.apache.beam.sdk.transforms.windowing.Window;
import org.apache.beam.sdk.values.PCollection;
import org.joda.time.Duration;

public class TaskOutcome implements Serializable {
  private static final Logger logger = Logger.getLogger(TaskOutcome.class.getName());

  private class ConvertToTask extends DoFn<String, Task> {
    private LogEntry stringToLogEntry(String json) throws InvalidProtocolBufferException {
      LogEntry.Builder logEntryBuilder = LogEntry.newBuilder();
      ProtoParser.parseJson(json, logEntryBuilder);
      return logEntryBuilder.build();
    }

    @DoFn.ProcessElement
    public void processElement(@Element String element, OutputReceiver<Task> receiver)
        throws InvalidProtocolBufferException {
      LogEntry logEntry;
      try {
        logEntry = stringToLogEntry(element);
      } catch (InvalidProtocolBufferException e) {
        logger.log(Level.WARNING, "unable to translate " + element);
        throw new RuntimeException(e);
      }
      int split = logEntry.getLogName().indexOf("%2F");
      if (split == -1) {
        // this is not a fleet log.
        return;
      }
      String truncatedLogName = logEntry.getLogName().substring(split + 3);
      if (truncatedLogName.equals("update_task") || truncatedLogName.equals("create_task")) {
        Task response;
        try {
          response = ProtoParser.parseLogEntryResponse(logEntry, Task.getDefaultInstance());
        } catch (Exception e) {
          logger.log(Level.WARNING, "unable to translate! " + element);
          e.printStackTrace();
          return;
        }
        receiver.output(response);
      }
    }
  }

  protected FirestoreDatabaseClient getFirestoreDatabaseClient() throws IOException {
    return new FirestoreDatabaseClient();
  }

  private class GetTaskStateChanges extends DoFn<Task, TaskOutcomeResult> {

    private FirestoreDatabaseClient firestoreClient = getFirestoreDatabaseClient();

    private DataflowJobConfig config;

    public GetTaskStateChanges(DataflowJobConfig config) throws IOException {
      this.config = config;
    }

    @DoFn.Setup
    public void setup() throws IOException {
      firestoreClient.initFirestore(
          config.getDatastoreProjectId(), TaskOutcome.class.getName() + "_" + UUID.randomUUID());
    }

    @DoFn.Teardown
    public void teardown() {
      firestoreClient.shutdown();
    }

    @DoFn.ProcessElement
    public void processElement(@Element Task element, OutputReceiver<TaskOutcomeResult> receiver)
        throws ExecutionException, InterruptedException {

      DocumentReference taskReference = firestoreClient.getTaskReference(element.getName());
      ApiFuture<TaskOutcomeResult> taskOutcomeResult =
          firestoreClient.runTransaction(
              new Transaction.Function<TaskOutcomeResult>() {
                @Override
                public TaskOutcomeResult updateCallback(Transaction transaction)
                    throws ExecutionException, InterruptedException {
                  try {
                    TaskMetadata taskMetadata = firestoreClient.getTask(taskReference);
                    TaskOutcomeResult result = null;
                    if (taskMetadata == null) {
                      // this task is new
                      result = new TaskOutcomeResult();
                      result.setTask(element);
                      result.setPrevState(null);
                      result.setNewState(element.getTaskOutcome().toString());
                      receiver.output(result);
                      // create new metadata
                      TaskMetadata newTaskMetadata = new TaskMetadata();
                      newTaskMetadata.setName(element.getName());
                      newTaskMetadata.setTaskOutcome(element.getTaskOutcome().toString());
                      firestoreClient.updateTask(taskReference, newTaskMetadata);
                    } else {
                      // check if state has changed
                      if (!element
                          .getTaskOutcome()
                          .toString()
                          .equals(taskMetadata.getTaskOutcome())) {
                        result = new TaskOutcomeResult();
                        result.setTask(element);
                        result.setPrevState(taskMetadata.getTaskOutcome());
                        result.setNewState(element.getTaskOutcome().toString());
                        receiver.output(result);
                        // update metadata
                        taskMetadata.setTaskOutcome(element.getTaskOutcome().toString());
                        firestoreClient.updateTask(taskReference, taskMetadata);
                      }
                    }
                    return result;
                  } catch (Exception e) {
                    e.printStackTrace();
                    throw e;
                  }
                }
              });
      TaskOutcomeResult metadata = taskOutcomeResult.get();
    }
  }

  private static class ConvertToString extends DoFn<TaskOutcomeResult, String> {
    @DoFn.ProcessElement
    public void processElement(
        @Element TaskOutcomeResult element, OutputReceiver<String> receiver) {
      logger.log(Level.INFO, String.format("Outputting element %s", element.toString()));
      receiver.output(element.toString());
    }
  }

  protected PCollection<String> getTaskOutcomeChanges(
      PCollection<Task> tasks, DataflowJobConfig config) throws IOException {
    PCollection<String> results =
        tasks
            .apply(ParDo.of(new GetTaskStateChanges(config)))
            .apply(ParDo.of(new ConvertToString()))
            .apply(Window.into(FixedWindows.of(Duration.standardMinutes(config.getWindowSize()))));
    return results;
  }

  public PCollection<String> run(PCollection<String> input, DataflowJobConfig config)
      throws IOException {
    PCollection<Task> processedInput = input.apply(ParDo.of(new ConvertToTask()));
    return getTaskOutcomeChanges(processedInput, config);
  }
}
