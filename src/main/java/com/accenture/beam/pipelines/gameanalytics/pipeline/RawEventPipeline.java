package com.accenture.beam.pipelines.gameanalytics.pipeline;

import com.google.api.services.bigquery.model.TableFieldSchema;
import com.google.api.services.bigquery.model.TableReference;
import com.google.api.services.bigquery.model.TableRow;
import com.google.api.services.bigquery.model.TableSchema;
import com.google.gson.JsonParser;
import org.apache.beam.runners.dataflow.options.DataflowPipelineOptions;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.AvroIO;
import org.apache.beam.sdk.io.gcp.bigquery.BigQueryIO;
import org.apache.beam.sdk.io.gcp.pubsub.PubsubIO;
import org.apache.beam.sdk.io.gcp.pubsub.PubsubMessage;
import org.apache.beam.sdk.options.*;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.transforms.windowing.AfterWatermark;
import org.apache.beam.sdk.transforms.windowing.FixedWindows;
import org.apache.beam.sdk.transforms.windowing.Window;
import org.apache.beam.sdk.values.PCollection;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.joda.time.Duration;

import java.io.IOException;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

// ref: https://towardsdatascience.com/a-simple-and-scalable-analytics-pipeline-53720b1dbd35
// todo:  https://cloud.google.com/architecture/streaming-avro-records-into-bigquery-using-dataflow
public class RawEventPipeline {

    /**
     * The topic to subscribe to for game events
     */
//    private static ValueProvider<String> topic = "projects/your_project_id/topics/raw-events";
    private static String avroFile = "avro/raw-events.avro";

    private static DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    /**
     * Provides an interface for setting the GCS temp location and streaming mode
     */
    public interface MyOptions extends DataflowPipelineOptions {
//        @Description("The Avro saved location. Must end with /")
//        @Validation.Required
//        ValueProvider<String> getAvroGcsLocation();
//        void setAvroGcsLocation();

//        @Description("PubSub topic to read the input from")
//        ValueProvider<String> getInputTopic();
//        void setInputTopic(ValueProvider<String> value);

    }

    public static void main(String[] args) {
        // set up pipeline options
        MyOptions options = PipelineOptionsFactory.fromArgs(args).withValidation().as(MyOptions.class);
        options.setStreaming(true);

        run(options);
    }

    /**
     * Runs the pipeline with the supplied options.
     * Creates a streaming dataflow pipeline that saves events to AVRO and bigquery.
     */
    public static void run(MyOptions options) {
        Pipeline pipeline = Pipeline.create(options);

        String topic = "projects/" + options.getProject() + "/topics/raw-events";

        // event schema for the raw_events table
        List<TableFieldSchema> fields = new ArrayList<>();
        fields.add(new TableFieldSchema().setName("eventType").setType("STRING"));
        fields.add(new TableFieldSchema().setName("eventVersion").setType("STRING"));
        fields.add(new TableFieldSchema().setName("serverTime").setType("STRING"));
        fields.add(new TableFieldSchema().setName("message").setType("STRING"));

        TableSchema schema = new TableSchema().setFields(fields);

        // BQ output table information
        TableReference table = new TableReference();
        table.setProjectId(options.getProject());
        table.setDatasetId("tracking");
        table.setTableId("raw_events");

        /*
         * Steps:
         *  1) Read messages in from Kafka
         *  2) Transform the Kafka Messages into TableRows
         *     - Transform message payload via UDF
         *     - Convert UDF result to TableRow objects
         *  3) Write records out to BigQuery
         *
         *  2) Transform the pubsub messages into Avro
         *  3) Write avro to GCS
         */
        // read game events from PubSub
        PCollection<PubsubMessage> events = pipeline
                /*
                 * Step #1: Read messages in from Kafka
                 */
                .apply("Read PubSub Events",
                        PubsubIO.readMessages().fromTopic(topic));
        /*
         * Step #2: stream the events to Big Query
         */
        events
                .apply("ConvertMessageToTableRow", new MessageToTableRow(options))
                .apply("ToBigQuery",
                        BigQueryIO.writeTableRows()
                                .to(table)
                                .withSchema(schema)
                                .withCreateDisposition(BigQueryIO.Write.CreateDisposition.CREATE_IF_NEEDED)
                                .withWriteDisposition(BigQueryIO.Write.WriteDisposition.WRITE_APPEND)
                );

        /*
         * Step #2 AVRO output portion of the pipeline
         */
        events
                .apply("ToString", ParDo.of(new MessageToStringFn()))
                /*
                 *   Batch events into 5 minute windows
                 */
                .apply("BatchEvents", Window.<String>into(
                                FixedWindows.of(Duration.standardMinutes(5)))
                        .triggering(AfterWatermark.pastEndOfWindow())
                        .discardingFiredPanes()
                        .withAllowedLateness(Duration.standardMinutes(5))
                )
                /*
                 *  Save the events in ARVO format
                 */
                .apply("ToAvro",
                        AvroIO.write(String.class)
                                .to(options.getAvroGcsLocation() + avroFile)
                                .withWindowedWrites()
                                .withNumShards(8)
                                .withSuffix(".avro")
                );

        // run the pipeline
        pipeline.run();
    }

    /**
     * parse the PubSub events and create rows to insert into BigQuery
     */
    private static class MessageToTableRow extends PTransform<PCollection<PubsubMessage>, PCollection<TableRow>> {
        public MessageToTableRow(MyOptions options) {
        }

        @Override
        public PCollection<TableRow> expand(PCollection<PubsubMessage> input) {
            return input.apply("To Predictions", ParDo.of(new MessageToTableRowFn()));
        }

    }

    private static class MessageToTableRowFn extends DoFn<PubsubMessage, TableRow> {
        @ProcessElement
        public void processElement(@Element PubsubMessage pubsubMessage, OutputReceiver<TableRow> out) throws IOException {
            String message = new String(pubsubMessage.getPayload());

            // parse the json message for attributes
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(message);
            String eventType = jsonNode.get("eventType").asText();
            String eventVersion = jsonNode.get("eventVersion").asText();
            String serverTime = dateTimeFormatter.format(Instant.now());

            // create and output the table row
            TableRow record = new TableRow();
            record.set("eventType", eventType);
            record.set("eventVersion", eventVersion);
            record.set("serverTime", serverTime);
            record.set("message", message);

            out.output(record);
        }
    }

    private static class MessageToStringFn extends DoFn<PubsubMessage, String> {
        @ProcessElement
        public void processElement(@Element PubsubMessage pubsubMessage, OutputReceiver<String> out) {
            out.output(new String(pubsubMessage.getPayload()));
        }
    }
}
