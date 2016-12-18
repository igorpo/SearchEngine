package com.pageranker;

import com.amazonaws.services.kinesis.model.ProvisionedThroughputExceededException;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;

import java.io.IOException;
import java.util.List;

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
        // Set up Amazon S3
        S3Wrapper.init("cis-455-final");

        // Set up Amazon DynamoDB
        DynamoWrapper.init();
        DynamoWrapper.setTable("testing_urls");
    }

    @Override
    public void map(LongWritable key,
                    Text value,
                    Context c) throws IOException, InterruptedException {
        // Get the URL
        // TODO: Gus & Chris: Make it work
        String url = S3Wrapper.decodeSafeKey((((FileSplit) c.getInputSplit()).getPath()).getName());

        // Get the list of URLs
        List<String> links = null;
        int numTries = 1;
        try {
            links = DynamoWrapper.retrieveOutgoingLinksForURL(url);
        } catch (ProvisionedThroughputExceededException e){
            Thread.sleep(1000);
            numTries++;
            if (numTries > 100){
                throw e;
            }
        }

        if (links != null) {
            for (String link : links) {
                c.write(new Text(url), new Text(link));
            }
        }

    }

}
