

Approach 2 


We have 2 simple runners  **FileToKafkaPipe** and **KafkaToElasticsearchVerticle**


We are using Vertex.io framework so we can extend this code to be deployed at scale 

FileToKafkaPipe => Read from file and push to Kafka
KafkaToElasticsearchVerticle => Read form Kafka and push to Elastic Search