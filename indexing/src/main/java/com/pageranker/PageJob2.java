package com.pageranker;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.*;

import java.io.IOException;

/**
 * Created by kierajmumick on 12/8/16.
 */
public class PageJob2 implements RunnableJob {

    /**
     * The prefix used for marking an output value that indicates that the
     * link has been crawled.
     */
    public static final String IS_CRAWLED_PREFIX = "[IS_CRAWLED]";

    /**
     * The prefix used for marking an output value that indicates the links
     * which each url maps to.
     */
    public static final String LINKS_PREFIX = "[OUT_LINKS]";

    /**
     * The dampening constant for the page rank job.
     */
    static final double PAGE_RANK_DAMPENING_CONST = 0.85;

    /**
     * The name for the Amazon S3 Bucket which contains the input for this
     * map-reduce job.
     */
    static final String inputBucketName = "step1Output";

    /**
     * The name for the Amazon S3 Bucket which should be written to with the
     * data this job outputs.
     */
    static final String outputBucketName = "step2Output";

    @Override
    public void run(String inputPath, String outputPath)
            throws IOException {
        // Create the job and set its name.
        JobConf conf = new JobConf(PageJob2.class);
        conf.setJobName("pageranker.job2");

        // Set the output key and value classes.
        conf.setOutputKeyClass(Text.class);
        conf.setOutputValueClass(Text.class);

        // Set the mapper and reducer classes.
        conf.setMapperClass(Map2.class);
        conf.setReducerClass(Reduce2.class);

        // Set the i/o formats for the MapReduce job.
        conf.setInputFormat(TextInputFormat.class);
        conf.setOutputFormat(TextOutputFormat.class);

        // Set the input and output paths.
        FileInputFormat.setInputPaths(conf, new Path(inputPath));
        FileOutputFormat.setOutputPath(conf, new Path(outputPath));

        // Run the job.
        JobClient.runJob(conf);
    }

    public static void main(String[] args) throws IOException {
        PageJob2 job = new PageJob2();
        job.run(args[0], args[1]);
    }
}