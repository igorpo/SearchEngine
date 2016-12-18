package com.indexer;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.PutItemOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by azw on 11/30/16.
 * index + ifidf reducer
 */
public class ReduceImpl extends Reducer<Text, Text, Text, Text> {

    /**
     * The Index of the TF in the TF/URL pair when it is parsed from the map
     * job.
     */
    private final static int TF_URL_PAIR_TF_INDEX = 0;

    /**
     * The Index of the URL in the TF/URL pair when it is parsed from the map
     * job.
     */
    private final static int TF_URL_PAIR_URL_INDEX = 1;

    Table table;

    /**
     * The total number of documents being indexed.
     */
    double N;

    @Override
    public void setup(Context c){

        AmazonDynamoDBClient client = new AmazonDynamoDBClient();
        //Region reg = Region.getRegion(Regions.US_EAST_1);
        //client.setRegion(reg);
        DynamoDB dynamoDB = new DynamoDB(client);

        table = dynamoDB.getTable("finalIndex250keast");
        this.N = Double.parseDouble(c.getConfiguration().get("num"));
    }

    @Override
    public void reduce(Text key, Iterable<Text> values, Context c) throws IOException, InterruptedException {
        // If the key is or empty, don't output anything.
        if (key == null){
            return;
        }
        String key_s = key.toString();
        if (key_s.equals("")) { return; }

        // Add all value,url pairs to a set.
        List<String> urls = new ArrayList<>();
        for (Text t : values) { urls.add(t.toString()); }

        // Calculate the IDF.
        double idf = Math.log10(N / urls.size());

        // Create the list to add URL/TF-IDF terms to.
        List<List<String>> full = new ArrayList<>();

        for (String tfUrlPair : urls) {
            // Create the pair for the output.
            List<String> urlTfIdfPair = new ArrayList<>();

            // Parse out the URL and TF of the key in that URL.
            String[] tfAndUrl = tfUrlPair.split(",");
            double tf = Double.parseDouble(tfAndUrl[TF_URL_PAIR_TF_INDEX]);
            String url = tfAndUrl[TF_URL_PAIR_URL_INDEX];

            // Add the information to the pair and add it to the output.
            urlTfIdfPair.add(url);
            urlTfIdfPair.add(Double.toString(tf * idf));
            full.add(urlTfIdfPair);
        }

        try {
            PutItemOutcome outcome = table.putItem(new Item().withPrimaryKey("word", key_s).withList("data", full));
            outcome.getPutItemResult();
        } catch (Exception e) {
        }


        // Write to the output.
        //c.write(key, new Text(full.toString()));
    }
}

