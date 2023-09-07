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

import com.google.fleetevents.beam.config.DataflowJobConfig;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.gcp.pubsub.PubsubIO;
import org.apache.beam.sdk.options.Default;
import org.apache.beam.sdk.options.Description;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.options.StreamingOptions;
import org.apache.beam.sdk.options.Validation.Required;
import org.apache.beam.sdk.values.PCollection;

// run with
// mvn compile exec:java \
//        -Dexec.mainClass=com.google.fleetevents.beam.FleetEventRunner \
//        -Dexec.cleanupDaemonThreads=false \
//        -Dexec.args=" \
//    --project=$PROJECT_ID \
//    --region=$REGION \
//    --runner=DataflowRunner \
//    --outputTopic=projects/$PROJECT_ID/topics/$OUTPUT_TOPIC_ID \
//    --inputTopic=projects/$PROJECT_ID/topics/$TOPIC_ID  \
//    --functionName=$JOB_NAME \
//    --datastoreProjectId=$PROJECT_ID \
//    --windowSize=3 "
public class FleetEventRunner {
  private static final Logger logger = Logger.getLogger(FleetEventRunner.class.getName());

  public enum SampleFunction {
    TASK_OUTCOME,
    VEHICLE_OFFLINE;
  }

  public interface PubSubToGcsOptions extends StreamingOptions {

    @Description("Choose the sample function to run.")
    @Required
    SampleFunction getFunctionName();

    void setFunctionName(SampleFunction value);

    @Description("Choose the gcp project with datastore.")
    @Required
    String getDatastoreProjectId();

    void setDatastoreProjectId(String value);

    @Description("Choose the database.")
    @Required
    String getDatabaseId();

    void setDatabaseId(String value);

    @Description("The Cloud Pub/Sub topic to read from.")
    @Required
    String getInputTopic();

    void setInputTopic(String value);

    @Description("How long to wait (in minutes) before considering a Vehicle to be offline.")
    @Default.Integer(3)
    Integer getGapSize();

    void setGapSize(Integer value);

    @Description(
        "Window size to use to process events, in minutes. This parameter does not apply to"
            + " VEHICLE_OFFLINE jobs.")
    @Default.Integer(3)
    Integer getWindowSize();

    void setWindowSize(Integer value);

    @Description("Path of the output Pub/Sub topic.")
    @Required
    String getOutputTopic();

    void setOutputTopic(String value);
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
    PCollection<String> processedMessages;
    DataflowJobConfig config =
        DataflowJobConfig.Builder.newBuilder()
            .setWindowSize(options.getWindowSize())
            .setGapSize(options.getGapSize())
            .setDatastoreProjectId(options.getDatastoreProjectId())
            .setDatabaseId(options.getDatabaseId())
            .build();
    switch (options.getFunctionName()) {
      case TASK_OUTCOME:
        {
          processedMessages = new TaskOutcome().run(messages, config);
          break;
        }
      case VEHICLE_OFFLINE:
        {
          processedMessages = VehicleOffline.run(messages, config);
          break;
        }
      default:
        logger.log(
            Level.WARNING,
            String.format(
                "Function name %s is not supported. Exiting without running job.",
                options.getFunctionName().toString()));
        System.exit(0);
        return;
    }
    processedMessages.apply(
        "Write messages to topic", PubsubIO.writeStrings().to(options.getOutputTopic()));

    pipeline.run();
    // .waitUntilFinish();
  }
}
