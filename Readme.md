//For generating the data

Downloaded the sample data for the sensors of the beach (beach.csv)

started the zookeeper and kafka servers 
Creatad a topic --sensorsdata
kafka program - for the timeseries data producer
Created the producer and made it run to generate data

Installed the elassandra and started running the elassandra one node cluster
Opened the cqlsh with http:127.0.0.1:9042 as cluster connection
created a keyspace named output (it should have networkTopology strategy and there should be a replication factor of more thanor equal to 2 for the respective datacenter);
created a table  sensor_data witht the below structure

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
)

Once after creation after the table we should re-index the table or the entire cluster (if for the first time)

curl -XPUT -H 'Content-Type: application/json' 'http://localhost:9200/output' -d '{
    "settings": { "keyspace":"output" },
    "mappings": {
        "sensor_data" : {
            "discover":".*"
        }
    }
}'

It will create the index which will be used by the elastic search for the search or any other operations



//Ingesting the data into cassandra(elassandra )
Created a simple Strom application with a KafkaSprout and Splitter,Cassandra Bolt
Ingested the real time streaming data into cassandra output keyspace and sensor_data table.



After the ingestion or in the mean time also,we can execute the elastic-search operations 
below are some of the aggregations and their respective sqls 




SQL1
  
select beach_name ,sum(battery_life)  as battery_life_sum ,
sum(transducer_depth) as transducer_depth_sum from sensor_data  where water_temperature >=18 group by beach_name;

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
  //output
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


2.SQL select * from sensor_data where beach_name='Osterman Beach' and water_temperature>=18;

//filetering
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
3.SQL select beach_name ,count(*) from sensor_data group by beach_name
curl -X GET "localhost:9200/output/_search?pretty" -H 'Content-Type: application/json' -d'
{
    "aggs" : {
        "toal_per_beachs" : {
            "terms" : { "field" : "beach_name" } 
        }
    }
}
'
4.select max(wave_height) from sensor_data;
curl -X POST "localhost:9200/output/_search?size=0&pretty" -H 'Content-Type: application/json' -d'
{
    "aggs" : {
        "max_price" : { "max" : { "field" : "wave_height" } }
    }
}
'
.....
...
..
