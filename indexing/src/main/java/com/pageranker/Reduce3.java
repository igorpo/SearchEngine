package com.pageranker;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.PutItemOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Iterator;

/**
 * Created by kierajmumick on 12/8/16.
 */
public class Reduce3 extends MapReduceBase implements Reducer<FloatWritable, Text, FloatWritable, Text> {

    Table table;
    @Override
    public void configure(JobConf job) {
        AmazonDynamoDBClient client = new AmazonDynamoDBClient();
        //Region reg = Region.getRegion(Regions.US_EAST_1);
        //client.setRegion(reg);
        DynamoDB dynamoDB = new DynamoDB(client);

        table = dynamoDB.getTable("page1k");
    }

    @Override
    public void reduce(FloatWritable key,
                       Iterator<Text> values,
                       OutputCollector<FloatWritable, Text> output,
                       Reporter reporter) throws IOException {
        while (values.hasNext()) {
            Text t = values.next();
            String s = t.toString();

            try {
                PutItemOutcome outcome = table.putItem(new Item().withPrimaryKey("url", S3Wrapper.encodeSafeKey(s)).withNumber("rank", new BigDecimal((double)key.get())).withString("unsafe_url", s));
                outcome.getPutItemResult();
            } catch (Exception e) {
            }
            output.collect(key, new Text(s));
        }
    }

}
