package com.indexer;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.*;
import org.jsoup.Jsoup;


public class IndexerJob {



    static final String bucketName = "testdocs";



    public static void main(String[] args) throws Exception {
        JobConf conf = new JobConf(IndexerJob.class);
        conf.setJobName("wordcount");

        String test = Jsoup.parse("<html><body>test1<p>test2</p></body></html>").text();
        System.err.printf(test);
        conf.setOutputKeyClass(Text.class);
        conf.setOutputValueClass(IntWritable.class);

        conf.setMapperClass(MapImpl.class);
        conf.setCombinerClass(ReduceImpl.class);
        conf.setReducerClass(ReduceImpl.class);

        conf.setInputFormat(TextInputFormat.class);
        conf.setOutputFormat(TextOutputFormat.class);

        FileInputFormat.setInputPaths(conf, new Path(args[0]));
        FileOutputFormat.setOutputPath(conf, new Path(args[1]));



        JobClient.runJob(conf);


    }
}
