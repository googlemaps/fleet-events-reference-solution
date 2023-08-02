// Copyright 2019 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.fleetevents.beam;

import com.google.fleetevents.beam.util.ProtoParser;
import com.google.logging.v2.LogEntry;
import com.google.protobuf.InvalidProtocolBufferException;
import google.maps.fleetengine.delivery.v1.DeliveryVehicle;
import java.io.IOException;
import java.io.Serializable;
import org.apache.beam.examples.common.WriteOneFilePerWindow;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.gcp.pubsub.PubsubIO;
import org.apache.beam.sdk.options.Default;
import org.apache.beam.sdk.options.Description;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.options.StreamingOptions;
import org.apache.beam.sdk.options.Validation.Required;
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

// run with
// mvn compile exec:java \
// -Dexec.mainClass=com.google.fleetevents.beam.PubSubToGcs \
// -Dexec.cleanupDaemonThreads=false \
// -Dexec.args=" \
//    --project=$PROJECT_ID \
//    --region=$REGION \
//    --runner=DataflowRunner \
//    --gapSize=3 \
//    --output=gs://$BUCKET_NAME/samples/output "
public class PubSubToGcs {

  public interface PubSubToGcsOptions extends StreamingOptions {
    @Description("The Cloud Pub/Sub topic to read from.")
    @Required
    String getInputTopic();

    void setInputTopic(String value);

    //
    //    @Description("The Cloud Pub/Sub topic to write to.")
    //    @Required
    //    String getOutputTopic();

    // void setOutputTopic(String value);

    @Description("How long to wait (in minutes) before considering a Vehicle to be offline.")
    @Default.Integer(3)
    Integer getGapSize();

    void setGapSize(Integer value);

    @Description("Path of the output file including its filename prefix.")
    @Required
    String getOutput();

    void setOutput(String value);
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
      System.out.println("built " + logEntryBuilder.build().getLogName());
      return logEntryBuilder.build();
    }

    @DoFn.ProcessElement
    public void processElement(@Element String element, OutputReceiver<Pair> receiver)
        throws InvalidProtocolBufferException {
      LogEntry logEntry;
      try {
        logEntry = stringToLogEntry(element);
      } catch (InvalidProtocolBufferException e) {
        System.out.println("unable to translate " + element);
        throw new RuntimeException(e);
      }

      int split = logEntry.getLogName().indexOf("%2F");
      if (split == -1) {
        // this is not a fleet log.
        System.out.println("not a fleet log " + logEntry.getLogName());
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
          System.out.printf("updated min boundary %s:%d%n", a.minId, a.min);
          boundary.min = a.min;
          boundary.minId = a.minId;
        }
        if (a.max > boundary.max) {
          System.out.printf("updated max boundary %s:%d%n", a.maxId, a.max);
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

  public static class ConvertToString
      extends SimpleFunction<KV<String, GetBoundariesFn.Boundary>, String> {
    @Override
    public String apply(KV<String, GetBoundariesFn.Boundary> input) {
      System.out.printf("got boundary %s:%s%n", input.getKey(), input.getValue());
      return input.getKey() + ": " + input.getValue().toString();
    }
  }

  public static PCollection<String> processMessages(PCollection<String> messages, Integer gapSize) {
    return messages
        .apply(Window.into(Sessions.withGapDuration(Duration.standardMinutes(gapSize))))
        .apply(ParDo.of(new ProcessLogEntryFn()))
        .apply(ParDo.of(new PairVehicleIdToLogEntryFn()))
        .apply(Combine.perKey(new GetBoundariesFn()))
        .apply(MapElements.via(new ConvertToString()));
  }

  public static void main(String[] args) throws IOException {
    // The maximum number of shards when writing output.
    int numShards = 1;
    PubSubToGcsOptions options =
        PipelineOptionsFactory.fromArgs(args).withValidation().as(PubSubToGcsOptions.class);
    options.setStreaming(true);

    Pipeline pipeline = Pipeline.create(options);
    PCollection<String> messages =
        pipeline.apply(
            "Read PubSub Messages", PubsubIO.readStrings().fromTopic(options.getInputTopic()));
    PCollection<String> processedMessages = processMessages(messages, options.getGapSize());
    processedMessages.apply(
        "Write Files to GCS", new WriteOneFilePerWindow(options.getOutput(), numShards));
    pipeline.run().waitUntilFinish();
  }
}
