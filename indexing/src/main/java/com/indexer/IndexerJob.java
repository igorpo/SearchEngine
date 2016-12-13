package com.indexer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;


public class IndexerJob {



    static final String bucketName = "jarbuck123412341234";



    public static void main(String[] args) throws Exception {

//
//        String roleArn = "arn:aws:iam::172510697573:role/Chris_S3";
//        AWSSecurityTokenServiceClient sts = new AWSSecurityTokenServiceClient(
//                new PropertiesCredentials(IndexerJob.class.getResourceAsStream("AwsCredentials.properties"))
//        );
//
//        AssumeRoleRequest assumeRoleRequest = new AssumeRoleRequest()
//                .withRoleArn(roleArn);
//
//        AssumeRoleResult ar = sts.assumeRole(assumeRoleRequest);


        Configuration c = new Configuration();
        c.set("num", args[2]);
        Job conf = Job.getInstance(c,"wordcount");
        conf.setJobName("wordcount");


        conf.setJarByClass(IndexerJob.class);
        conf.setOutputKeyClass(Text.class);
        conf.setOutputValueClass(Text.class);

        conf.setMapperClass(MapImpl.class);
        //conf.setCombinerClass(ReduceImpl.class);
        conf.setReducerClass(ReduceImpl.class);

        conf.setInputFormatClass(NoSplitter.class);
        conf.setOutputFormatClass(TextOutputFormat.class);

        FileInputFormat.setInputPaths(conf, new Path(args[0]));
        FileOutputFormat.setOutputPath(conf, new Path(args[1]));

        /*
        conf.set("num",
                Long.toString(FileSystem.get(conf).getContentSummary(new Path(args[0])).getFileCount()));
        */
        //conf.set("num", args[2]);



        conf.waitForCompletion(true);


    }

    public static class NoSplitter extends TextInputFormat {


        @Override
        public boolean isSplitable(JobContext a, Path file){
            return false;
        }

        @Override
        public RecordReader<LongWritable, Text> createRecordReader(InputSplit split, TaskAttemptContext context){
            
        }
    }
}
