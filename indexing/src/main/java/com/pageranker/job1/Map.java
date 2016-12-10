package com.pageranker.job1;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.*;

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
public class Map extends MapReduceBase implements Mapper<LongWritable, Text, Text, Text> {

    private AmazonS3 s3;

    @Override
    public void configure(JobConf job) {
        // Set up Amazon S3
        this.s3 = new AmazonS3Client();
        Region region = Region.getRegion(Regions.US_EAST_1);
        this.s3.setRegion(region);
    }

    @Override
    public void map(LongWritable key,
                    Text value,
                    OutputCollector<Text, Text> output,
                    Reporter reporter) throws IOException {
        // Get the URL


        // Get the list of URLs
    }

}
