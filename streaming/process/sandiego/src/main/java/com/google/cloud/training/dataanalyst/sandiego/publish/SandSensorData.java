package com.google.cloud.training.dataanalyst.sandiego.publish;

import com.google.api.core.ApiFuture;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;
import com.google.pubsub.v1.TopicName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;

import static java.lang.System.out;

/**
 * cloud_user_p_a4993d0b@cloudshell:~/input-file (playground-s-11-d0a57501)$ gzip -d sensor_obs2008.csv.gz
 * cloud_user_p_a4993d0b@cloudshell:~/input-file (playground-s-11-d0a57501)$ ls
 * sensor_obs2008.csv
 * cloud_user_p_a4993d0b@cloudshell:~/input-file (playground-s-11-d0a57501)$ head sensor_obs2008.csv
 * TIMESTAMP,LATITUDE,LONGITUDE,FREEWAY_ID,FREEWAY_DIR,LANE,SPEED
 * 2008-11-01 00:00:00,32.749679,-117.155519,163,S,1,71.2
 * 2008-11-01 00:00:00,32.749679,-117.155519,163,S,2,65.1
 * 2008-11-01 00:00:00,32.780922,-117.089026,8,W,1,76.5
 * 2008-11-01 00:00:00,32.780922,-117.089026,8,W,2,74
 * 2008-11-01 00:00:00,32.780922,-117.089026,8,W,3,72
 * 2008-11-01 00:00:00,32.780922,-117.089026,8,W,4,66.8
 */

public class SandSensorData {

    private static final Logger log = LoggerFactory.getLogger("SandSensorData");
    static DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");


    public static void main(String... args) throws Exception {
        // TODO(developer): Replace these variables before running the sample.
        String projectId = "your-project-id";
        String topicId = "your-topic-id";
        String file = "sensor_obs2008.csv.gz";

        compressFile(file);


//        publisherExample(projectId, topicId);
    }

    public static Path getFile(String file) {
        Path source = null;
        try {
            source = Paths.get(ClassLoader.getSystemResource(file).toURI());
            if (Files.notExists(source)) {
                System.err.printf("The path %s doesn't exist!", source);
                return source;
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return source;
    }

    public static void compressFile(String file) {
        Path path = getFile(file);
        long programStartTime = Instant.now().toEpochMilli();
        try(InputStream in = Files.newInputStream(path);
            GZIPInputStream gis = new GZIPInputStream(in);
            InputStreamReader isr = new InputStreamReader(gis, StandardCharsets.UTF_8);
            BufferedReader br = new BufferedReader(isr);
        ) {
            String s = br.lines().skip(1).findFirst().orElse("default");
            out.println("sensor data: " + s);

            br.lines().skip(1).forEach(line -> {
                String eventData = line; // entire line of input CSV is the message
                LocalDateTime obsTime = getTimeStamp(line);
                String firstCol = Arrays.stream(line.split(",")).findFirst().orElse(Instant.now().toString());
                LocalDateTime dateTime = LocalDateTime.parse(firstCol, dateTimeFormatter);
                out.println("datetime: " + dateTime);
            });

//            lines.stream().forEach(l -> {
//                String[] array =  l.split(",", 2);
//                if(array[0].equals("10"))
//                    System.out.println("Hello");
//            });


//            CsvParserSettings parserSettings = new CsvParserSettings();
//            CsvRoutines csvRoutines = new CsvRoutines(parserSettings);
//            Iterator<SandiegoSensorDTO> iterator = csvRoutines.iterate(SandiegoSensorDTO.class,br).iterator();
//            SandiegoSensorDTO next = iterator.next();
//            System.out.println("sensor data: " + next);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static LocalDateTime getTimeStamp(String line) {
        String[] split = line.trim().split(",");
        return LocalDateTime.parse(split[0], dateTimeFormatter);
    }

//    private static void computeSleepSecs() {
//        long diff = ChronoUnit.NANOS.between(startDate, endDate);
//    }

    public static void publisherExample(String projectId, String topicId)
            throws IOException, ExecutionException, InterruptedException {
        TopicName topicName = TopicName.of(projectId, topicId);

        Publisher publisher = null;
        try {
            // Create a publisher instance with default settings bound to the topic
            publisher = Publisher.newBuilder(topicName).build();

            String message = "Hello World!";
            ByteString data = ByteString.copyFromUtf8(message);
            PubsubMessage pubsubMessage = PubsubMessage.newBuilder().setData(data).build();

            // Once published, returns a server-assigned message id (unique within the topic)
            ApiFuture<String> messageIdFuture = publisher.publish(pubsubMessage);
            String messageId = messageIdFuture.get();
            out.println("Published message ID: " + messageId);
        } finally {
            if (publisher != null) {
                // When finished with the publisher, shutdown to free up resources.
                publisher.shutdown();
                publisher.awaitTermination(1, TimeUnit.MINUTES);
            }
        }
    }
}
