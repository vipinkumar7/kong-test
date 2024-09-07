package org.example;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.kafka.client.producer.KafkaProducer;
import io.vertx.kafka.client.producer.KafkaProducerRecord;

import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * @author  Vipin Kumar
 */
public class FileToKafkaPipe extends AbstractVerticle {

    private static final String FILE_PATH = "stream.jsonl";
    private static final String KAFKA_TOPIC = "file-topic";
    private long lastFilePosition = 0;

    @Override
    public void start() throws FileNotFoundException {
        // Kafka Producer configuration
        Map<String, String> config = new HashMap<>();
        config.put("bootstrap.servers", "localhost:9092");
        config.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        config.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        config.put("acks", "1");

        URL resource = FileToKafkaPipe.class.getClassLoader().getResource(FILE_PATH);

        if (resource == null) {
            throw new IllegalArgumentException("File not found!");
        }

        // Create a Kafka producer
        KafkaProducer<String, String> producer = KafkaProducer.create(vertx, config);

        // Monitor the file for changes
        vertx.setPeriodic(1000, id -> {

                try (RandomAccessFile file = new RandomAccessFile(resource.getFile(), "r")) {
                // Move to the last known position
                System.out.println(Thread.currentThread() + " " + lastFilePosition);
                file.seek(lastFilePosition);

                String line;
                while ((line = file.readLine()) != null) {
                    // Create a Kafka producer record for each line
                    KafkaProducerRecord<String, String> record = KafkaProducerRecord.create(KAFKA_TOPIC, line);

                    // Send the record to Kafka
                    producer.send(record, result -> {
                        if (result.succeeded()) {
                            System.out.println("Message sent successfully to Kafka");
                        } else {
                            result.cause().printStackTrace();
                        }
                    });
                    // Update the last known position
                    lastFilePosition = file.getFilePointer();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new FileToKafkaPipe());
    }
}

