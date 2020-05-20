package StormBolt;

import jnr.ffi.annotations.In;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.IRichBolt;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

public class SplitBolt implements IRichBolt {
    private OutputCollector outputCollector;
    private Timestamp timestamp;
    public void prepare(Map<String, Object> map, TopologyContext topologyContext, OutputCollector outputCollector) {
     this.outputCollector=outputCollector;
    }

    public void execute(Tuple tuple) {
        String str = tuple.getString(4);
        String [] row= str.split(",");
        if(str.contains("-")){
            SimpleDateFormat dateFormat1 = new SimpleDateFormat("dd-mm-yyyy hh:mm");
            Date parsedDate1 = null;
            try {
                parsedDate1 = dateFormat1.parse(row[1]);
            } catch (ParseException e) {
                e.printStackTrace();
            }
             timestamp = new java.sql.Timestamp(parsedDate1.getTime());

        }
        else {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/mm/yyyy hh:mm:ss a");
            Date parsedDate = null;
            try {
                parsedDate = dateFormat.parse(row[1]);
            } catch (ParseException e) {
                e.printStackTrace();
            }
             timestamp = new java.sql.Timestamp(parsedDate.getTime());

        }
        System.out.println(timestamp);
        outputCollector.emit(new Values(row[0],timestamp,Double.parseDouble(row[2]),Double.parseDouble(row[3]),Double.parseDouble(row[4]),Double.parseDouble(row[5]), Integer.parseInt(row[6]),Double.parseDouble(row[7]),row[8],row[9]));
        outputCollector.ack(tuple);
        System.out.println("output is "+ str);

    }


    public void cleanup() {

    }


    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
    outputFieldsDeclarer.declare(new Fields("Beach Name","Measurement Timestamp","Water Temperature","Turbidity",
            "Transducer Depth","Wave Height","Wave Period","Battery Life","Measurement Timestamp Label","Measurement ID"));
    }


    public Map<String, Object> getComponentConfiguration() {
        return null;
    }
}
