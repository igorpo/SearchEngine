package com.pageranker;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.apache.hadoop.util.LineReader;

import java.io.IOException;


/**
 * Created by kierajmumick on 12/8/16.
 */
public class PageJob1 implements RunnableJob {

    /**
     * The name for the Amazon S3 Bucket which contains the input for this
     * map-reduce job.
     */
    static final String inputBucketName = "cis-455";

    /**
     * The name for the Amazon S3 Bucket which should be written to with the
     * data this job outputs.
     */
    static final String outputBucketName = "step1Output";

    @Override
    public void run(String inputPath, String outputPath)
            throws IOException {
        // Create the job and set its name.

        Configuration c = new Configuration();
        c.setLong("mapreduce.task.timeout", 0);
        c.setLong("mapred.task.timeout", 0);
        Job conf = Job.getInstance(c,"pageRank1");
        conf.setJobName("pageRank1");

        conf.setJarByClass(PageJob1.class);
        conf.setOutputKeyClass(Text.class);
        conf.setOutputValueClass(Text.class);

        conf.setMapperClass(Map1.class);
        //conf.setCombinerClass(ReduceImpl.class);
        conf.setReducerClass(Reduce1.class);

        conf.setInputFormatClass(TextInputFormat.class);
        conf.setOutputFormatClass(TextOutputFormat.class);

        FileInputFormat.setInputPaths(conf, new Path(inputPath));
        FileOutputFormat.setOutputPath(conf, new Path(outputPath));

        // Run the job.
        try {
            conf.waitForCompletion(true);
        } catch (Exception e){
            throw new IOException();
        }

    }

    public static void main(String[] args) throws Exception {
        PageJob1 job = new PageJob1();
        job.run(args[0], args[1]);
    }
}