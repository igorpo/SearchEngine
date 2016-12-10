package com.pageranker.job1;

import com.pageranker.RunnableJob;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.*;

import java.io.IOException;

/**
 * Created by kierajmumick on 12/8/16.
 */
public class Job implements RunnableJob {

    /**
     * The name for the Amazon S3 Bucket which contains the input for this
     * map-reduce job.
     */
    static final String inputBucketName = "crawlerOutputBucket";

    /**
     * The name for the Amazon S3 Bucket which should be written to with the
     * data this job outputs.
     */
    static final String outputBucketName = "step1Output";

    @Override
    public void run(String inputPath, String outputPath, String numNodes)
            throws IOException {
        // Create the job and set its name.
        JobConf conf = new JobConf(Job.class);
        conf.setJobName("pageranker.job1");

        // Set the output key and value classes.
        conf.setOutputKeyClass(Text.class);
        conf.setOutputValueClass(Text.class);

        // Set the mapper and reducer classes.
        conf.setMapperClass(Map.class);
        conf.setReducerClass(Reduce.class);

        // Set the i/o formats for the MapReduce job.
        conf.setInputFormat(TextInputFormat.class);
        conf.setOutputFormat(TextOutputFormat.class);

        // Set the input and output paths.
        FileInputFormat.setInputPaths(conf, new Path(inputPath));
        FileOutputFormat.setOutputPath(conf, new Path(outputPath));

        // Set the number of nodes to run the job on.
        conf.set("num", numNodes);

        // Run the job.
        JobClient.runJob(conf);
    }

}
