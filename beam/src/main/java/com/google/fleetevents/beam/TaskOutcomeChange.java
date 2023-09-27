package com.google.fleetevents.beam;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Transaction;
import com.google.fleetevents.beam.client.FirestoreDatabaseClient;
import com.google.fleetevents.beam.config.DataflowJobConfig;
import com.google.fleetevents.beam.model.TaskMetadata;
import com.google.fleetevents.beam.model.output.TaskOutcomeChangeOutputEvent;
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

public class TaskOutcomeChange implements Serializable {
  private static final Logger logger = Logger.getLogger(TaskOutcomeChange.class.getName());

  private final DataflowJobConfig config;

  public TaskOutcomeChange() {
    this.config = new DataflowJobConfig();
  }

  public TaskOutcomeChange(DataflowJobConfig config) {
    this.config = config;
  }

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

  private class GetTaskStateChanges extends DoFn<Task, TaskOutcomeChangeOutputEvent> {

    private FirestoreDatabaseClient firestoreClient = getFirestoreDatabaseClient();

    private DataflowJobConfig config;

    public GetTaskStateChanges(DataflowJobConfig config) throws IOException {
      this.config = config;
    }

    @DoFn.Setup
    public void setup() throws IOException {
      firestoreClient.initFirestore(
          config.getDatastoreProjectId(),
          config.getDatabaseId(),
          TaskOutcomeChange.class.getName() + "_" + UUID.randomUUID());
    }

    @DoFn.Teardown
    public void teardown() {
      firestoreClient.shutdown();
    }

    @DoFn.ProcessElement
    public void processElement(
        @Element Task element, OutputReceiver<TaskOutcomeChangeOutputEvent> receiver)
        throws ExecutionException, InterruptedException {

      DocumentReference taskReference = firestoreClient.getTaskReference(element.getName());
      ApiFuture<TaskOutcomeChangeOutputEvent> taskOutcomeResult =
          firestoreClient.runTransaction(
              new Transaction.Function<TaskOutcomeChangeOutputEvent>() {
                @Override
                public TaskOutcomeChangeOutputEvent updateCallback(Transaction transaction)
                    throws ExecutionException, InterruptedException {
                  try {
                    TaskMetadata taskMetadata = firestoreClient.getTask(taskReference);
                    TaskOutcomeChangeOutputEvent result = null;
                    if (taskMetadata == null) {
                      // this task is new
                      result = new TaskOutcomeChangeOutputEvent();
                      result.setTask(element);
                      result.setPreviousOutcome(null);
                      result.setNewOutcome(element.getTaskOutcome().toString());
                      // create new metadata
                      TaskMetadata newTaskMetadata = new TaskMetadata();
                      newTaskMetadata.setName(element.getName());
                      newTaskMetadata.setTaskOutcome(element.getTaskOutcome().toString());
                      firestoreClient.updateTask(taskReference, newTaskMetadata);
                    } else {
                      // check if state has changed
                      if ((element.getTaskOutcome() == null
                              && taskMetadata.getTaskOutcome() != null)
                          || !element
                              .getTaskOutcome()
                              .toString()
                              .equals(taskMetadata.getTaskOutcome())) {
                        result = new TaskOutcomeChangeOutputEvent();
                        result.setTask(element);
                        result.setPreviousOutcome(taskMetadata.getTaskOutcome());
                        result.setNewOutcome(element.getTaskOutcome().toString());
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
      TaskOutcomeChangeOutputEvent result = taskOutcomeResult.get();
      if (result != null) receiver.output(result);
    }
  }

  private class ConvertToString extends DoFn<TaskOutcomeChangeOutputEvent, String> {
    @DoFn.ProcessElement
    public void processElement(
        @Element TaskOutcomeChangeOutputEvent element, OutputReceiver<String> receiver)
        throws JsonProcessingException {
      ObjectMapper mapper = new ObjectMapper();
      String data = mapper.writeValueAsString(element);
      logger.log(Level.INFO, String.format("Outputting element %s", data));
      receiver.output(data);
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

  public PCollection<String> run(PCollection<String> input) throws IOException {
    PCollection<Task> processedInput = input.apply(ParDo.of(new ConvertToTask()));
    return getTaskOutcomeChanges(processedInput, config);
  }
}
