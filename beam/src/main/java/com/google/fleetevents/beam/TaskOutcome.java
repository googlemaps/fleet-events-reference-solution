package com.google.fleetevents.beam;

import com.google.fleetevents.beam.util.ProtoParser;
import com.google.logging.v2.LogEntry;
import com.google.protobuf.InvalidProtocolBufferException;
import google.maps.fleetengine.delivery.v1.Task;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.beam.sdk.transforms.DoFn;

public class TaskOutcome {
  private static final Logger logger = Logger.getLogger(TaskOutcome.class.getName());

  static class ConvertToTask extends DoFn<String, Task> {
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
      if (truncatedLogName.equals("update_task")) {
        Task response;
        try {
          response = ProtoParser.parseLogEntryResponse(logEntry, Task.getDefaultInstance());
        } catch (Exception e) {
          e.printStackTrace();
          return;
        }
        receiver.output(response);
      }
    }
  }

  public static class ConvertToString extends DoFn<Task, String> {
    @DoFn.ProcessElement
    public void processElement(@Element Task element, OutputReceiver<String> receiver) {
      receiver.output(element.getName());
    }
  }
}
