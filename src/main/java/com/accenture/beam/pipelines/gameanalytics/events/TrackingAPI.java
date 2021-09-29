package com.accenture.beam.pipelines.gameanalytics.events;

import com.google.api.core.ApiFuture;
import com.google.api.core.ApiFutureCallback;
import com.google.api.core.ApiFutures;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;
import com.google.pubsub.v1.TopicName;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * An API for sending events to the game analytics pipeline.
 * Simulate event data and  send generated event data to the data pipeline.
 *
 * This class includes a main method that generates random events to send to
 * the pipeline to test connectivity.
 */
public class TrackingAPI implements ApiFutureCallback<String> {

    /** the publisher object used to send events */
    private Publisher publisher = null;

    /** the topic to publish to */
    private TopicName topicName;

    /** the project to send messages to */
    private static final String PROJECT_ID = "your_project_id";

    /** the topic to send messages to */
    private static final String TOPIC_ID = "raw-events";

    public TrackingAPI(String projectId, String topicId) {
        topicName = TopicName.of(projectId, topicId);
    }

    public static void main(String[] args) throws InterruptedException {
        TrackingAPI trackingAPI = new TrackingAPI(PROJECT_ID, TOPIC_ID);
        try {
            trackingAPI.start();
            trackingAPI.generateBachOfEvents();
            // back off every once in a while
            if (Math.random() < 0.01) {
                try{
                    Thread.sleep(1000); // sleep 1 sec
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }

            // stop publishing events
            trackingAPI.stop();
        } catch (IOException e) {
            e.printStackTrace();
//        } finally {
//            Publisher publisher = trackingAPI.getPublisher();
//            if (publisher != null) {
//                // When finished with the publisher, shutdown to free up resources.
//                trackingAPI.stop();
//                publisher.awaitTermination(1, TimeUnit.MINUTES);
//            }
        }
    }

    public Publisher getPublisher() {
        return publisher;
    }

    /**
     * send a batch of events
     */
    public void generateBachOfEvents() {
        for (int i=0; i <= 10000; i++) {
            // generate an event name
            String eventType = Math.random() < 0.5 ? "Session" : (Math.random() < 0.5 ? "Login" : "MatchStart");
            // create some attributes to send
            HashMap<String, String> attributes = new HashMap<>();
            attributes.put("userID", "" + (int)(Math.random()*10000));
            attributes.put("deviceType", Math.random() < 0.5 ? "Android" : (Math.random() < 0.5 ? "IOS" : "Web"));

            // send the event
            this.sendEvent(eventType, "V1", attributes);
        }
    }

    /**
     * Publishes an event to the analytics endpoint. The input attributes are converted into a
     * JSON string.
     */
    public void sendEvent(String eventType, String eventVersion, HashMap<String, String> attributes) {

        // build a json representation of the event
        StringBuilder event = new StringBuilder("{\"eventType\":\"" + eventType + "\","
                + "\"eventVersion\":\"" + eventVersion
        );
        for (Map.Entry<String, String> entry : attributes.entrySet()) {
            event.append(",\"" + entry.getKey() + "\":\"" + entry.getValue() + "\"");
        }
        event.append("}");

        // convert the event to bytes
        ByteString data = ByteString.copyFromUtf8(event.toString());

        //schedule a message to be published
        PubsubMessage pubsubMessage = PubsubMessage.newBuilder().setData(data).build();

        // publish the message, and add this class as a callback listener
        ApiFuture<String> apiFuture = publisher.publish(pubsubMessage);
        ApiFutures.addCallback(apiFuture, this, MoreExecutors.directExecutor());
    }

    /**
     * Creates a publisher object for sending events.
     */
    public void start() throws IOException {
        publisher = Publisher.newBuilder(topicName).build();
    }

    private void stop() {
        publisher.shutdown();
    }

    @Override
    public void onFailure(Throwable t) {
        System.err.println(t.getMessage());
    }

    @Override
    public void onSuccess(String result) {

    }
}
