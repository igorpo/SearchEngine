package com.indexer;

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.*;


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



        JobConf conf = new JobConf(IndexerJob.class);
        conf.setJobName("wordcount");


        conf.setOutputKeyClass(Text.class);
        conf.setOutputValueClass(Text.class);

        conf.setMapperClass(MapImpl.class);
        //conf.setCombinerClass(ReduceImpl.class);
        conf.setReducerClass(ReduceImpl.class);

        conf.setInputFormat(NoSplitter.class);
        conf.setOutputFormat(TextOutputFormat.class);

        FileInputFormat.setInputPaths(conf, new Path(args[0]));
        FileOutputFormat.setOutputPath(conf, new Path(args[1]));

        /*
        conf.set("num",
                Long.toString(FileSystem.get(conf).getContentSummary(new Path(args[0])).getFileCount()));
        */
        conf.set("num", args[2]);



        JobClient.runJob(conf);


    }

    public static class NoSplitter extends TextInputFormat {

        @Override
        public boolean isSplitable(FileSystem fs, Path file){
            return false;
        }
    }
}
