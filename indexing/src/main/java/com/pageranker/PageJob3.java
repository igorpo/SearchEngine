package com.pageranker;

import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.*;

import java.io.IOException;

/**
 * The sub-job for PageRank which will get the crawled pages and output a
 * mapping between each page and all of the pages that it links to.
 */
public class PageJob3 implements RunnableJob {

    /**
     * The name for the Amazon S3 Bucket which contains the input for this
     * map-reduce job.
     */
    static final String inputBucketName = "step2Output";

    /**
     * The name for the Amazon S3 Bucket which should be written to with the
     * data this job outputs.
     */
    static final String outputBucketName = "step3Output";

    @Override
    public void run(String inputPath, String outputPath, String numNodes)
            throws IOException {
        // Create the job and set its name.
        JobConf conf = new JobConf(PageJob3.class);
        conf.setJobName("pageranker.job3");

        // Set the output key and value classes.
        conf.setOutputKeyClass(FloatWritable.class);
        conf.setOutputValueClass(Text.class);

        // Set the mapper and reducer classes.
        conf.setMapperClass(Map3.class);
        conf.setReducerClass(Reduce3.class);

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
        PageJob3 job = new PageJob3();
        job.run(args[0], args[1], args[2]);
    }
}