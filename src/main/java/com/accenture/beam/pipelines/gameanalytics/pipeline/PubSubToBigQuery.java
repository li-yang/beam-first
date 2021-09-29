package com.accenture.beam.pipelines.gameanalytics.pipeline;

import com.accenture.beam.pipelines.teleport.coders.FailsafeElement;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.gcp.pubsub.PubsubIO;
import org.apache.beam.sdk.io.gcp.pubsub.PubsubMessage;
import org.apache.beam.sdk.options.*;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.PCollectionTuple;

/**
 * Li found: https://medium.com/@vallerylancey/error-handling-elements-in-apache-beam-pipelines-fffdea91af2a
 */
public class PubSubToBigQuery {

    public interface Options extends PipelineOptions {

        @Description(
                "The Cloud Pub/Sub subscription to consume from. "
                        + "The name should be in the format of "
                        + "projects/<project-id>/subscriptions/<subscription-name>.")
        ValueProvider<String> getInputSubscription();
        void setInputSubscription(ValueProvider<String> value);

        @Description("This determines whether the template reads from a pub/sub subscription or a topic")
        @Default.Boolean(false)
        Boolean getUserSubscription();
        void setUserSubscription(Boolean value);


        @Description("Pub/Sub topic to read the input from")
        ValueProvider<String> getInputTopic();
        void setInputTopic(String value);
    }

    public static void main(String[] args) {
        Options options = PipelineOptionsFactory.fromArgs(args).withValidation().as(Options.class);
        run(options);
    }

    public static void run(Options options) {
        // create pipeline
        Pipeline pipeline = Pipeline.create();

        /*
         * Steps:
         *  1) Read messages in from Pub/Sub
         *  2) Transform the PubsubMessages into TableRows
         *     - Transform message payload via UDF
         *     - Convert UDF result to TableRow objects
         *  3) Write successful records out to BigQuery
         *  4) Write failed records out to BigQuery
         */

        /*
         * Step #1: Read messages in from Pub/Sub
         * Either from a Subscription or Topic
         */
        PCollection<PubsubMessage> messages = null;
        if (options.getUserSubscription()) {
            messages =
                    pipeline.apply("ReadPubSubSubscription",
                            PubsubIO.readMessagesWithAttributes()
                                    .fromSubscription(options.getInputSubscription())
                    );
        } else {
            messages =
                    pipeline.apply("ReadPubSubTopic",
                            PubsubIO.readMessagesWithAttributes()
                                    .fromTopic(options.getInputTopic()));
        }

        /*
         * Step #2: Transform the PubsubMessages into TableRows
         */
        messages
                .apply("ConvertMessageToTableRow", new PubSubMessageToTableRow());

        /*
         * Step #3: Write the successful records out to BigQuery
         */


    }

    private static class PubSubMessageToTableRow extends PTransform<PCollection<PubsubMessage>, PCollectionTuple> {
        @Override
        public PCollectionTuple expand(PCollection<PubsubMessage> input) {
            // Map the incoming messages into FailsafeElements so we can recover from failures
            // across multiple transforms.
            input
                    .apply("MapToRecord", ParDo.of(new PubsubMessageToFailsafeElementFn()));
            return null;
        }

    }

    /**
     * The {@link PubsubMessageToFailsafeElementFn} wraps an incoming {@link PubsubMessage} with the
     * {@link com.accenture.beam.pipelines.teleport.coders.FailsafeElement} class so errors can be recovered from and the original message can be
     * output to a error records table.
     */
    static class PubsubMessageToFailsafeElementFn extends DoFn<PubsubMessage, FailsafeElement> {
    }
}
