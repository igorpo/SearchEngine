package com.indexer;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.PutItemOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by azw on 11/30/16.
 * index + ifidf reducer
 */
public class ReduceImpl extends MapReduceBase implements Reducer<Text, Text, Text, Text> {


    Table table;
    long N;
    @Override
    public void configure(JobConf job){

        AmazonDynamoDBClient client = new AmazonDynamoDBClient();
        DynamoDB dynamoDB = new DynamoDB(client);

        table = dynamoDB.getTable("bigtest1");
        N = Long.parseLong(job.get("num"));

    }

    @Override
    public void reduce(Text key, Iterator<Text> values, OutputCollector<Text, Text> output, Reporter reporter) throws IOException {


        ArrayList<Text> urls = new ArrayList<>();
        while (values.hasNext()) {
            urls.add(values.next());
        }

        int len = urls.size();
        double idf = Math.log10((double)N/(double)len);

        List<List<String>> full = new ArrayList<>();


        for (Text o : urls) {
            List<String> thing = new ArrayList<>();

            String[] stuff = o.toString().split(",", 2);
            String url = stuff[1];
            double tf = Double.parseDouble(stuff[0]);
            thing.add(url);
            thing.add(new Double(tf*idf).toString());
            full.add(thing);

        }


        if (key != null && !key.toString().isEmpty()){
            try {
                System.out.println("Adding a new item...");
                PutItemOutcome outcome = table.putItem(new Item().withPrimaryKey("word", key.toString())
                        .withList("data", full));

                System.out.println("PutItem succeeded:\n" + outcome.getPutItemResult());

            } catch (Exception e) {
                System.err.println("Unable to add item: " + key.toString() + " " + urls.toString());
                System.err.println(e.getMessage());
            }

        }
        output.collect(key, new Text(full.toString()));

    }
}
