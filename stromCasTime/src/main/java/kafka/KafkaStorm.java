package kafka;
import StormBolt.SplitBolt;
import org.apache.log4j.BasicConfigurator;
import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.kafka.spout.KafkaSpout;
import org.apache.storm.kafka.spout.KafkaSpoutConfig;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.cassandra.bolt.CassandraWriterBolt;

import org.apache.log4j.Logger;

import static org.apache.storm.cassandra.DynamicStatementBuilder.*;
public class KafkaStorm {
    private final static Logger logger = Logger.getLogger(KafkaStorm.class);

    public static void main(String[] args) throws Exception{
        Config config = new Config();
        config.setDebug(false);
        config.put(Config.TOPOLOGY_MAX_SPOUT_PENDING, 1);
        config.put("cassandra.keyspace","output");
        config.put("cassandra.nodes","localhost");
        config.put("cassandra.port",9042);
        BasicConfigurator.configure();
        logger.info("Topology is created");
        TopologyBuilder builder = new TopologyBuilder();
        builder.setSpout("kafka-spout", new KafkaSpout<String, String>(KafkaSpoutConfig.builder("localhost:9092", "sensordata").build()), 1);
        builder.setBolt("word-splitter", new SplitBolt()).shuffleGrouping("kafka-spout");

        builder.setBolt("WORD_COUNT_CASSANDRA_BOLT", new CassandraWriterBolt(
                async(
                        simpleQuery("INSERT INTO output.sensor_data  (Beach_Name,Measurement_Timestamp,Water_Temperature,Turbidity,Transducer_Depth,Wave_Height,Wave_Period," +
                                "Battery_Life,Measurement_Timestamp_Label,Measurement_ID ) VALUES (?,?,?,?,?,?,?,?,?,?);")
                                .with(
                                        fields("Beach Name","Measurement Timestamp","Water Temperature","Turbidity","Transducer Depth",
                                        "Wave Height","Wave Period","Battery Life","Measurement Timestamp Label","Measurement ID")
                                )
                )
        ),1).globalGrouping("word-splitter");//globalGrouping("call-log-counter-bolt");


        logger.info("Cluster is running");
        LocalCluster cluster = new LocalCluster();
        //LocalCluster cluster = new LocalCluster("localhost",2181L);  //toexplicitly state the zookeeper
        cluster.submitTopology("KafkaStormSample", config, builder.createTopology());

        Thread.sleep(5000);
        //cluster.killTopology("KafkaStormSample"); not killing the topology so that it will run continuously
        //cluster.shutdown();

    }
}
