package producer;

import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class TimeSeriesProducer {

    private static String kafkaBrokerEndPoint = "http://127.0.0.1:9092";
    private static String topic = "sensordata";//"kafka-storm";
    private static String csvPath = "/home/jayanth/Downloads/beach.csv";//"S:\\beach.csv";

    public static void main(String args[]) throws IOException, InterruptedException, ExecutionException {
        Properties properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBrokerEndPoint);
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        Producer<String, String> csvProd = new KafkaProducer<String, String>(properties);
        BufferedReader br = new BufferedReader(new FileReader(csvPath));
        String line;
        line=br.readLine();
        while ((line = br.readLine()) != null && !line.isEmpty()) {
            final ProducerRecord<String, String> csvRecord = new ProducerRecord<String, String>(
                    topic, UUID.randomUUID().toString(), line
            );
            RecordMetadata metadata = csvProd.send(csvRecord).get(); //Synchronous
            //Future<RecordMetadata> meta = producer.send(csvRecord); //To access the values we have to use get method same like above
            System.out.println("Topic is->"+metadata.topic()+" partition is-> "+metadata.partition()+" offset is->" +metadata.offset());
            Thread.sleep(300);
        }

    }
}
