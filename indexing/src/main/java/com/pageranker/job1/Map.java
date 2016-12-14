package com.pageranker.job1;

import com.amazonaws.services.s3.AmazonS3;
import com.pageranker.model.DynamoWrapper;
import com.pageranker.model.S3Wrapper;
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
public class Map extends Mapper<LongWritable, Text, Text, Text> {

    private AmazonS3 s3;

    @Override
    public void setup(Context c) {
        // Set up Amazon S3
        S3Wrapper.init("cis-455");

        // Set up Amazon DynamoDB
        DynamoWrapper.init();
        DynamoWrapper.setTable("visitedUrlsOutgoingLinks");
    }

    @Override
    public void map(LongWritable key,
                    Text value,
                    Context c) throws IOException, InterruptedException {
        // Get the URL
        // TODO: Gus & Chris: Make it work
        String url = S3Wrapper.decodeSafeKey((((FileSplit) c.getInputSplit()).getPath()).getName());

        // Get the list of URLs
        List<String> links = DynamoWrapper.retrieveOutgoingLinksForURL(url);

        for (String link : links) {
            c.write(new Text(url), new Text(link));
        }

    }

}
