
Below is the solutions with Kafka Connect as pipeline

The  Solution is placed under kafka-connect folder:

For reading File Data I am using Kafka Connect with FileStreamSource plugin

example 
```
tar -xzf kafka_2.13-<version>.tgz
cd kafka_2.13-<version>/libs

you can see connect-file-3.1.0.jar add it to plugin directory

```
To add File source plugin we can get it from kafka source code 


```
curl --location 'http://localhost:8083/connectors' \
--header 'Content-Type: application/json' \
--data '{
    "name": "file-source-connector",
    "config": {
      "connector.class": "FileStreamSource",
      "tasks.max": "1",
      "file": "/data/stream.jsonl",
      "topic": "file-topic"
    }
  }'

```

**List the plugins :**
```
curl --location 'http://localhost:8083/connectors?expand=info&expand=status'

```


you can now see the file data in Kafka UI 

Add new data to file and that will be pushed to Kafka topics 
```
head stream.jsonl > test.jsonl
cat test.jsonl >> stream.jsonl

```

#### **Pushing Data from Kafka topic to Openobserve**