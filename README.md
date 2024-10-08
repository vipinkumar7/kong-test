
# APPROACH

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

#### **Pushing Data from Kafka topic to Elastic Search**

Use following connector for Kafka connect elastic sync 
https://d2p6pa21dvn84.cloudfront.net/api/plugins/confluentinc/kafka-connect-elasticsearch/versions/14.1.1/confluentinc-kafka-connect-elasticsearch-14.1.1.zip


#### Create  one more connector for Elastic Sync :

```
curl --location 'http://localhost:8083/connectors' \
--header 'Content-Type: application/json' \
--data '{
    "name": "elasticsearch-sink",
    "config": {
      "connector.class": "io.confluent.connect.elasticsearch.ElasticsearchSinkConnector",
       "tasks.max": "1",
       "connection.url":"http://host.docker.internal:9200",
       "type.name":"kafka-connect",
       "key.ignore":"true",
       "schema.ignore": "true",
       "topics": "file-topic",
       "value.converter": "org.apache.kafka.connect.json.JsonConverter",
       "value.converter.schemas.enable": "false",
       "name": "elasticsearch-sink"
    }
  }'
```

**We can now list the data in file-topic index** 

```
curl localhost:9200/file-topic/_search  | python -m json.tool
```






**We can put Schema also for this index** 

<details>
  <summary>Json Schema (Click to expand)  </summary>

```
curl --location --request PUT 'http://localhost:9200/file-topic' \
--header 'Content-Type: application/json' \
--data '{
  "mappings": {
    "properties": {
      "before": {
        "type": "object",
        "enabled": false
      },
      "after": {
        "type": "object",
        "properties": {
          "key": {
            "type": "text",
            "fields": {
              "keyword": {
                "type": "keyword",
                "ignore_above": 256
              }
            }
          },
          "value": {
            "type": "object",
            "properties": {
              "type": {
                "type": "integer"
              },
              "object": {
                "type": "object",
                "properties": {
                  "id": {
                    "type": "keyword"
                  },
                  "type": {
                    "type": "text",
                    "fields": {
                      "keyword": {
                        "type": "keyword",
                        "ignore_above": 256
                      }
                    }
                  },
                  "version": {
                    "type": "text",
                    "fields": {
                      "keyword": {
                        "type": "keyword",
                        "ignore_above": 256
                      }
                    }
                  },
                  "hostname": {
                    "type": "text",
                    "fields": {
                      "keyword": {
                        "type": "keyword",
                        "ignore_above": 256
                      }
                    }
                  },
                  "last_ping": {
                    "type": "long"
                  },
                  "created_at": {
                    "type": "long"
                  },
                  "updated_at": {
                    "type": "long"
                  },
                  "config_hash": {
                    "type": "text",
                    "fields": {
                      "keyword": {
                        "type": "keyword",
                        "ignore_above": 256
                      }
                    }
                  },
                  "process_conf": {
                    "type": "object",
                    "enabled": false
                  },
                  "connection_state": {
                    "type": "object",
                    "properties": {
                      "is_connected": {
                        "type": "boolean"
                      }
                    }
                  },
                  "data_plane_cert_id": {
                    "type": "keyword"
                  }
                }
              }
            }
          }
        }
      },
      "op": {
        "type": "keyword"
      },
      "ts_ms": {
        "type": "long"
      }
    }
  }
}'

```
</details>