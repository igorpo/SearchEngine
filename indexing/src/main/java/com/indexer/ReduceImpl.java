package com.indexer;

import com.amazonaws.services.dynamodbv2.document.Table;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by azw on 11/30/16.
 * index + ifidf reducer
 */
public class ReduceImpl extends Reducer<Text, Text, Text, Text> {


    Table table;
    long N;

    @Override
    public void setup(Context c){

//        AmazonDynamoDBClient client = new AmazonDynamoDBClient();
//        DynamoDB dynamoDBcd = new DynamoDB(client);
//
//        table = dynamoDB.getTable("smalltest2");
        N = Long.parseLong(c.getConfiguration().get("num"));

    }

    @Override
    public void reduce(Text key, Iterable<Text> values, Context c) throws IOException, InterruptedException {


//        Set<Text> urls = new HashSet<>();
        //Text url = new Text();
        for (Text t : values) {
//            urls.add(t);
            c.write(key, t);
        }
/*
        int len = urls.size();
        double idf = Math.log10((double)N/(double)len);

        List<List<String>> full = new ArrayList<>();

        List<String> pair;
        int count = 0;
        for (Text o : urls) {
            full.add(new ArrayList<>());

            String[] stuff = o.toString().split(",", 2);
            String url = stuff[1];
            double tf = Double.parseDouble(stuff[0]);
            full.get(count).add(url);
            full.get(count).add(new Double(tf*idf).toString());
            count++;
            */
//            if (key != null && !key.toString().isEmpty()){
//                try {
//                    System.out.println("Adding a new item...");
//                    PutItemOutcome outcome = table.putItem(new Item().withPrimaryKey("word", key.toString()
//                            +Integer.toString(count))
//                            .withList("data", full));
//
//                    System.out.println("PutItem succeeded:\n" + outcome.getPutItemResult());
//
//                } catch (Exception e) {
//                    System.err.println("Unable to add item: " + key.toString() + " " + urls.toString());
//                    System.err.println(e.getMessage());
//                }
//
//            }
        }



//        if (key != null && !key.toString().isEmpty()){
//            try {
//                System.out.println("Adding a new item...");
//                PutItemOutcome outcome = table.putItem(new Item().withPrimaryKey("word", key.toString())
//                        .withList("data", full));
//
//                System.out.println("PutItem succeeded:\n" + outcome.getPutItemResult());
//
//            } catch (Exception e) {
//                System.err.println("Unable to add item: " + key.toString() + " " + urls.toString());
//                System.err.println(e.getMessage());
//            }
//
//        }

    /*
        c.write(key, new Text(full.toString()));
        */

    }
}
