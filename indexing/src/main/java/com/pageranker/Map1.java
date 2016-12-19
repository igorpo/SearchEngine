package com.pageranker;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;

/**
 * The mapping job for the first step of the PageRank job. This job will read
 * from a S3 bucket with the format:
 *     |   key   |   val   |
 *     |---------|---------|
 *     |  <URL>  | <HTML>  |
 * It will then ignor the value element from the S3 Table, and read from
 * DynamoDB to get the list of urls that the url maps to.
 */
public class Map1 extends Mapper<LongWritable, Text, Text, Text> {

    @Override
    public void setup(Context c) {
    }

    @Override
    public void map(LongWritable key,
                    Text value,
                    Context c) throws IOException, InterruptedException {
        // Get the URL
        // TODO: Gus & Chris: Make it work
        //String url = S3Wrapper.decodeSafeKey((((FileSplit) c.getInputSplit()).getPath()).getName());

        // Get the list of URLs
        // List<String> links = null;
        // int numTries = 1;
        // try {
        //     links = DynamoWrapper.retrieveOutgoingLinksForURL(url);
        // } catch (ProvisionedThroughputExceededException e){
        //     Thread.sleep(1000);
        //     numTries++;
        //     if (numTries > 100){
        //         throw e;
        //     }
        // }

        String[] firstSplit = value.toString().split("\t");
        if (firstSplit.length != 3)
            return;
        String url = firstSplit[2];
        String[] links = firstSplit[1].split("\\|");

        if (links != null) {
            for (String link : links) {
                c.write(new Text(url), new Text(link));
            }
        }

    }

}
