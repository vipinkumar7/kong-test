package org.example;


import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;
import io.vertx.kafka.client.consumer.KafkaConsumer;
import org.apache.http.HttpHost;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author  Vipin Kumar
 */
public class KafkaToElasticsearchVerticle extends AbstractVerticle {

    private static final String KAFKA_BROKER = "localhost:9092";
    private static final String KAFKA_TOPIC = "file-topic";
    private static final String ELASTICSEARCH_HOST = "localhost";
    private static final int ELASTICSEARCH_PORT = 9200;

    private RestClient elasticsearchClient;

    @Override
    public void start() {
        // Initialize Elasticsearch Client
        RestClientBuilder builder = RestClient.builder(
                new HttpHost(ELASTICSEARCH_HOST, ELASTICSEARCH_PORT, "http")
        );
        elasticsearchClient =builder.build();

        // Kafka Consumer Configuration
        Map<String, String> consumerConfig = new HashMap<>();
        consumerConfig.put("bootstrap.servers", KAFKA_BROKER);
        consumerConfig.put("group.id", "vertx-consumer-group");
        consumerConfig.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        consumerConfig.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        consumerConfig.put("auto.offset.reset", "earliest");

        KafkaConsumer<String, String> consumer = KafkaConsumer.create(vertx, consumerConfig);
        consumer.subscribe(KAFKA_TOPIC);

        consumer.handler(record -> {
            // Create an Elasticsearch Index Request
            String jsonString = record.value();
            Request request = new Request("POST", "/my-index/_doc/");
            request.setJsonEntity(jsonString);

            // Asynchronously send request to Elasticsearch
            vertx.executeBlocking(promise -> {
                try {
                    Response indexResponse = elasticsearchClient.performRequest(request);
                    promise.complete(indexResponse);
                } catch (IOException e) {
                    e.printStackTrace();
                    promise.fail(e);
                }
            }, res -> {
                if (res.succeeded()) {
                    Response response = (Response)res.result();
                    System.out.println("Document indexed successfully. Index: " + response.toString() );
                } else {
                    System.err.println("Failed to index document: " + res.cause());
                }
            });
        });

        consumer.exceptionHandler(Throwable::printStackTrace);
    }

    @Override
    public void stop() {
        try {
            if (elasticsearchClient != null) {
                elasticsearchClient.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        Vertx vertx = Vertx.vertx();
        vertx.deployVerticle(new KafkaToElasticsearchVerticle(), res -> {
            if (res.succeeded()) {
                System.out.println("Verticle deployed successfully!");
            } else {
                System.err.println("Failed to deploy verticle: " + res.cause());
            }
        });
    }
}
