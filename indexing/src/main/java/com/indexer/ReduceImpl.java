package com.indexer;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.PutItemOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.MapReduceBase;
import org.apache.hadoop.mapred.OutputCollector;
import org.apache.hadoop.mapred.Reducer;
import org.apache.hadoop.mapred.Reporter;

import java.io.IOException;
import java.util.Iterator;

/**
 * Created by azw on 11/30/16.
 */
public class ReduceImpl extends MapReduceBase implements Reducer<Text, IntWritable, Text, IntWritable> {

    @Override
    public void reduce(Text key, Iterator<IntWritable> values, OutputCollector<Text, IntWritable> output, Reporter reporter) throws IOException {


        AmazonDynamoDBClient client = new AmazonDynamoDBClient();
        DynamoDB dynamoDB = new DynamoDB(client);

        Table table = dynamoDB.getTable("testcount");

        int sum = 0;
        while (values.hasNext()) {
            sum += values.next().get();
        }

        if (key != null && !key.toString().isEmpty()){
            try {
                System.out.println("Adding a new item...");
                PutItemOutcome outcome = table.putItem(new Item().withPrimaryKey("word", key.toString()).withInt("count", sum));

                System.out.println("PutItem succeeded:\n" + outcome.getPutItemResult());

            } catch (Exception e) {
                System.err.println("Unable to add item: " + key.toString() + " " + sum);
                System.err.println(e.getMessage());
            }

        }
        output.collect(key, new IntWritable(sum));

    }
}
