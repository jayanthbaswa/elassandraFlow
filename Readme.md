# ElassandraPOC

Download the sample data for the sensors of the beach (beach.csv) and place it in the respective folder.

## Installation

### Kafka & Zookeeper Services

Start the zookeeper and kafka servers.


```bash
#starting zookeeper services
bin/zkServer.sh start
bin/zkCli.sh
#starting kafka servers(brokers)
bin/kafka-server-start.sh config/server.properties
#create a topic named 'sensorsdata'
bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 
--partitions 3 --topic sensorsdata
```

The topic ***sensorsdata*** will be created.

### Elassandra 

Install the elassandra by following the below steps [installation](http://doc.elassandra.io/en/latest/installation.html)

```bash
#start cassandra with elastic search enabled(elassandra)
bin/cassandra -e 
#check the node status
bin/nodetool status
#starti cql server 
bin/cqlsh
#check elastic-search api (in new prompt)
curl -X GET http://localhost:9200/
```

Elassandra will be up and running.

The Cassandra cql server will be up and running on [http:127.0.0.1:9042](http:127.0.0.1:9042).


#### CQL
```sql
--create a keyspace name output
CREATE KEYSPACE output 
  WITH REPLICATION = { 
   'class' : 'NetworkTopologyStrategy', 
   'datacenter1' : 3 
  } ;
--Note: Replication factor the keyspace should be >=2 to perform elastic-search operations

USE output;

--create a table with name sensors_data with following metada 

create table sensor_data(
Beach_Name text,
Measurement_Timestamp  timestamp, 
Water_Temperature double,
Turbidity double,
Transducer_Depth double,
Wave_Height  double,
Wave_Period  int, 
Battery_Life double,
Measurement_Timestamp_Label text,
Measurement_ID text,
PRIMARY KEY (Measurement_ID)
);
```

#### Indexing the Tables& Keyspaces
```bash
curl -XPUT -H 'Content-Type: application/json' 'http://localhost:9200/output' -d '{
    "settings": { "keyspace":"output" },
    "mappings": {
        "sensor_data" : {
            "discover":".*"
        }
    }
}
'
```
It will create the index for the respective keyspace and columnar families which will be used by the elastic search for the search or any other of its operations.

Now once all of our pre-requisties are successfully and completely installed.

We can execute the flow in the below steps:

## Running the applications

#### Kafka Producer 


Run the program **[TimeSeriesProducer.java](https://github.com/jayanthbaswa/elassandraFlow/blob/master/kafkapoc/src/main/java/producer/TimeSeriesProducer.java)** 

The Kafka Produce will now generate the data.

You can check with the ***kafka-console-consumer***
```bash
#kafka-console-consumer command 
bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 
--topic sensorsdata --from-beginning
```
After checking that the data is getting generated please run the storm job.

Once after creation after the table we should re-index the table or the entire cluster (if for the first time)



#### Storm Job
Run the Strom Topology in the local mode(Local Cluster) [[Strom Job]](https://github.com/jayanthbaswa/elassandraFlow/blob/master/stromCasTime/src/main/java/kafka/KafkaStorm.java).


It will ingest the data into cassandra(elassandra cluster) at regular intervals.

## Elastic-search Operations

#### Aggregator Operations




After the ingestion or in the mean time also,we can execute the elastic-search operations.

Some of the few aggregator operations are below



##### SQL1
 
```sql
select beach_name ,sum(battery_life)  as battery_life_sum ,
sum(transducer_depth) as transducer_depth_sum from sensor_data  
where water_temperature >=18 group by beach_name;
```

   *it's respective elastic-search operation is below:*

```bash
curl -X GET "localhost:9200/output/_search?pretty" -H 'Content-Type: application/json' -d'
{
  "query": { 
    "bool": { 
      "filter": [
        { "range": { "water_temperature": { "gte": "12" }}}
      ]
    }
  }, "aggs": {
    "beach_names": {
      "terms": {
        "field": "beach_name"
      },
      "aggs": {
        "battery_life_sum": {
          "sum": {
            "field": "battery_life"
          }
        },
        "transducer_depth_sum": {
          "sum": {
            "field": "transducer_depth"
          }
        }
      }
    }
  }
}
'
# It's output will be like....
#ignoring the shards, hits,score and other default values
  {...
  "aggregations" : {
    "beach_names" : {
      "doc_count_error_upper_bound" : 0,
      "sum_other_doc_count" : 0,
      "buckets" : [
        {
          "key" : "Calumet Beach",
          "doc_count" : 223,
          "battery_life_sum" : {
            "value" : 2534.9
          },
          "transducer_depth_sum" : {
            "value" : 318.453
          }
        },
        {
          "key" : "Montrose Beach",
          "doc_count" : 25,
          "battery_life_sum" : {
            "value" : 287.5
          },
          "transducer_depth_sum" : {
            "value" : 34.153
          }
        },
        {
          "key" : "Ohio Street Beach",
          "doc_count" : 18,
          "battery_life_sum" : {
            "value" : 214.7
          },
          "transducer_depth_sum" : {
            "value" : 27.455
          }
        },
        {
          "key" : "Rainbow Beach",
          "doc_count" : 2,
          "battery_life_sum" : {
            "value" : 24.299999999999997
          },
          "transducer_depth_sum" : {
            "value" : 1.5910000000000002
          }
        },
        {
          "key" : "63rd Street Beach",
          "doc_count" : 1,
          "battery_life_sum" : {
            "value" : 11.0
          },
          "transducer_depth_sum" : {
            "value" : 1.517
          }
        },
        {
          "key" : "Osterman Beach",
          "doc_count" : 1,
          "battery_life_sum" : {
            "value" : 9.4
          },
          "transducer_depth_sum" : {
            "value" : 1.538
          }
        }
      ]
    }
  }
}
```

  



##### SQL2 
```sql
select * from sensor_data where beach_name='Osterman Beach' and water_temperature>=18;
```
*it's respective elastic-search operation is below:*
```bash
#filetering
curl -X GET "localhost:9200/output/_search?pretty" -H 'Content-Type: application/json' -d'
{
  "query": { 
    "bool": { 
      "filter": [ 
        { "term":  { "beach_name": "Osterman Beach" }},
        { "range": { "water_temperature": { "gte": "18" }}}
      ]
    }
  },
}
'
```
##### SQL3 
```sql
select beach_name ,count(*) from sensor_data group by beach_name;
```
```bash
curl -X GET "localhost:9200/output/_search?pretty" -H 'Content-Type: application/json' -d'
{
    "aggs" : {
        "toal_per_beachs" : {
            "terms" : { "field" : "beach_name" } 
        }
    }
}
'
```
##### SQL4
```sql
select max(wave_height) from sensor_data;
```
```bash
curl -X POST "localhost:9200/output/_search?size=0&pretty" -H 'Content-Type: application/json' -d'
{
    "aggs" : {
        "max_price" : { "max" : { "field" : "wave_height" } }
    }
}
'
```

.....

...

..

.
