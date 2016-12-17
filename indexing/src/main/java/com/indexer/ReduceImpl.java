package com.indexer;

import com.amazonaws.services.dynamodbv2.document.Table;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

/**
 * Created by azw on 11/30/16.
 * index + ifidf reducer
 */
public class ReduceImpl extends Reducer<Text, Text, Text, Text> {

    private final static int TF_URL_PAIR_TF_INDEX = 0;
    private final static int TF_URL_PAIR_URL_INDEX = 1;

    Table table;
    double N;

    @Override
    public void setup(Context c){

//        AmazonDynamoDBClient client = new AmazonDynamoDBClient();
//        DynamoDB dynamoDBcd = new DynamoDB(client);
//
//        table = dynamoDB.getTable("smalltest2");
        this.N = Double.parseDouble(c.getConfiguration().get("num"));

    }

    @Override
    public void reduce(Text key, Iterable<Text> values, Context c) throws IOException, InterruptedException {
        // If the key is or empty, don't output anything.
        if (key == null || key.toString().equals("")) { return; }

        // Add all value,url pairs to a set.
        List<String> urls = new ArrayList<>();
        for (Text t : values) {
            urls.add(t.toString());
        }

        // Calculate the IDF.
        double idf = Math.log10(N / urls.size());

        // Create the list to add URL/TF-IDF terms to.
        List<List<String>> full = new ArrayList<>();

        while (String tfUrlPair : urls) {
            // Create the pair for the output.
            List<String> urlTfIdfPair = new ArrayList<>();

            // Parse out the URL and TF of the key in that URL.
            String[] tfAndUrl = tfUrlPair.split(",");
            double tf = Double.parseDouble(tfAndUrl[TF_URL_PAIR_TF_INDEX]);
            String url = ufAndUrl[TF_URL_PAIR_URL_INDEX];

            // Add the information to the pair and add it to the output.
            urlTfIdfPair.add(url);
            urlTfIdfPair.add(Double.toString(tf * idf));
            full.add(urlTfIdfPair);
        }

        // Write to the output.
        c.write(key, new Text(full.toString()));
    }
}

