package com.google.fleetevents.beam;

import com.google.fleetevents.beam.config.DataflowJobConfig;
import com.google.fleetevents.beam.model.output.VehicleNotUpdatingOutputEvent;
import com.google.fleetevents.beam.util.ProtoParser;
import com.google.logging.v2.LogEntry;
import com.google.protobuf.InvalidProtocolBufferException;
import google.maps.fleetengine.delivery.v1.DeliveryVehicle;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.beam.sdk.transforms.Combine;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.MapElements;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.transforms.SimpleFunction;
import org.apache.beam.sdk.transforms.windowing.Sessions;
import org.apache.beam.sdk.transforms.windowing.Window;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;
import org.joda.time.Duration;

public class VehicleNotUpdating implements Serializable {
  private static final Logger logger = Logger.getLogger(VehicleNotUpdating.class.getName());
  private final int GAP_DURATION;

  public VehicleNotUpdating(DataflowJobConfig config) {
    this.GAP_DURATION = config.getGapSize();
  }

  static class Pair implements Serializable {
    private LogEntry logEntry;
    private DeliveryVehicle deliveryVehicle;

    public Pair(LogEntry logEntry, DeliveryVehicle deliveryVehicle) {
      this.logEntry = logEntry;
      this.deliveryVehicle = deliveryVehicle;
    }
  }

  static class ProcessLogEntryFn extends DoFn<String, Pair> {
    private LogEntry stringToLogEntry(String json) throws InvalidProtocolBufferException {
      LogEntry.Builder logEntryBuilder = LogEntry.newBuilder();
      ProtoParser.parseJson(json, logEntryBuilder);
      return logEntryBuilder.build();
    }

    @DoFn.ProcessElement
    public void processElement(@Element String element, OutputReceiver<Pair> receiver)
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
      if (truncatedLogName.equals("update_delivery_vehicle")) {
        DeliveryVehicle response;
        try {
          response =
              ProtoParser.parseLogEntryResponse(logEntry, DeliveryVehicle.getDefaultInstance());
        } catch (Exception e) {
          e.printStackTrace();
          return;
        }
        receiver.output(new Pair(logEntry, response));
      }
    }
  }

  static class PairVehicleIdToLogEntryFn extends DoFn<Pair, KV<String, Pair>> {
    @DoFn.ProcessElement
    public void processElement(@Element Pair element, OutputReceiver<KV<String, Pair>> receiver)
        throws InvalidProtocolBufferException {
      receiver.output(KV.of(element.deliveryVehicle.getName(), element));
    }
  }

  public static class GetBoundariesFn
      extends Combine.CombineFn<Pair, GetBoundariesFn.Boundary, GetBoundariesFn.Boundary> {
    public static class Boundary implements Serializable {
      long min = Long.MAX_VALUE;
      long max = Long.MIN_VALUE;

      String minId = "";

      String maxId = "";

      DeliveryVehicle maxVehicle;

      public Boundary(long min, long max) {
        this.min = min;
        this.max = max;
      }

      public Boundary() {}

      @Override
      public boolean equals(Object other) {
        if (other == null) return false;
        if (other == this) return true;
        if (!(other instanceof Boundary)) return false;

        Boundary o = (Boundary) other;
        return this.min == o.min && this.max == o.max;
      }

      @Override
      public String toString() {
        return "Boundary{"
            + "min="
            + min
            + ", max="
            + max
            + ", minId='"
            + minId
            + ", maxId='"
            + maxId
            + '}';
      }
    }

    @Override
    public Boundary createAccumulator() {
      return new Boundary();
    }

    @Override
    public Boundary addInput(Boundary accumulator, Pair input) {
      LogEntry logEntry = input.logEntry;
      DeliveryVehicle deliveryVehicle = input.deliveryVehicle;
      long eventTime = logEntry.getTimestamp().getSeconds();
      if (eventTime > accumulator.max) {
        accumulator.max = eventTime;
        accumulator.maxId = logEntry.getInsertId();
        accumulator.maxVehicle = deliveryVehicle;
      }
      if (eventTime < accumulator.min) {
        accumulator.min = eventTime;
        accumulator.minId = logEntry.getInsertId();
      }
      return accumulator;
    }

    @Override
    public Boundary mergeAccumulators(Iterable<Boundary> accumulators) {
      Boundary boundary = new Boundary();
      for (Boundary a : accumulators) {
        if (a.min < boundary.min) {
          boundary.min = a.min;
          boundary.minId = a.minId;
        }
        if (a.max > boundary.max) {
          boundary.max = a.max;
          boundary.maxId = a.maxId;
          boundary.maxVehicle = a.maxVehicle;
        }
      }
      return boundary;
    }

    @Override
    public Boundary extractOutput(Boundary boundary) {
      return boundary;
    }
  }

  public class ConvertToOutput
      extends SimpleFunction<KV<String, GetBoundariesFn.Boundary>, String> {
    @Override
    public String apply(KV<String, GetBoundariesFn.Boundary> input) {
      System.out.printf("got boundary %s:%s%n", input.getKey(), input.getValue());
      GetBoundariesFn.Boundary boundary = input.getValue();
      VehicleNotUpdatingOutputEvent output = new VehicleNotUpdatingOutputEvent();
      output.setGapDuration(GAP_DURATION);
      output.setFirstUpdateTime(boundary.min);
      output.setLastUpdateTime(boundary.max);
      output.setDeliveryVehicle(boundary.maxVehicle);
      return output.toString();
    }
  }

  public PCollection<String> run(PCollection<String> messages) {
    return messages
        .apply(Window.into(Sessions.withGapDuration(Duration.standardMinutes(GAP_DURATION))))
        .apply(ParDo.of(new ProcessLogEntryFn()))
        .apply(ParDo.of(new PairVehicleIdToLogEntryFn()))
        .apply(Combine.perKey(new GetBoundariesFn()))
        .apply(MapElements.via(new ConvertToOutput()));
  }
}
