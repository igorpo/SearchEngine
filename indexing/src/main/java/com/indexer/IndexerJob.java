package com.indexer;

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


public class IndexerJob {

    static final String bucketName = "jarbuckpage";

    public static void main(String[] args) throws Exception {

//        String roleArn = "arn:aws:iam::172510697573:role/Chris_S3";
//        AWSSecurityTokenServiceClient sts = new AWSSecurityTokenServiceClient(
//                new PropertiesCredentials(IndexerJob.class.getResourceAsStream("AwsCredentials.properties"))
//        );
//
//        AssumeRoleRequest assumeRoleRequest = new AssumeRoleRequest()
//                .withRoleArn(roleArn);
//
//        AssumeRoleResult ar = sts.assumeRole(assumeRoleRequest);

        Configuration conf = new Configuration();
        conf.set("num", args[2]);
        conf.setLong("mapreduce.task.timeout", 0);
        conf.setLong("mapred.task.timeout", 0);

        Job job = Job.getInstance(conf,"wordcount");
        job.setJobName("wordcount");

        job.setJarByClass(IndexerJob.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);

        job.setMapperClass(MapImpl.class);
        //job.setCombinerClass(ReduceImpl.class);
        job.setReducerClass(ReduceImpl.class);

        job.setInputFormatClass(NoSplitter.class);
        job.setOutputFormatClass(TextOutputFormat.class);

        FileInputFormat.setInputPaths(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        /*
        job.set("num",
                Long.toString(FileSystem.get(job).getContentSummary(new Path(args[0])).getFileCount()));
        */
        //job.set("num", args[2]);

        job.waitForCompletion(true);

    }

    public static class NoSplitter extends TextInputFormat {
        @Override
        public boolean isSplitable(JobContext a, Path file){
            return false;
        }

        @Override
        public RecordReader<LongWritable, Text> createRecordReader(
                InputSplit split, TaskAttemptContext context){
            return new CustomLineRecordReader();
        }
    }

    public static class CustomLineRecordReader
            extends RecordReader<LongWritable, Text> {

        private long start;
        private long pos;
        private long end;
        private LineReader in;
        private int maxLineLength;
        private LongWritable key = new LongWritable();
        private Text value = new Text();


        @Override
        public void initialize(InputSplit genericSplit, TaskAttemptContext context) throws IOException {

            FileSplit split = (FileSplit) genericSplit;
            final Path file = split.getPath();

            Configuration job = context.getConfiguration();
            this.maxLineLength = job.getInt("mapred.linerecordreader.maxlength", Integer.MAX_VALUE);

            start = split.getStart();
            end = start + split.getLength();

            FileSystem fs = file.getFileSystem(job);
            FSDataInputStream fileIn = fs.open(split.getPath());

            in = new LineReader(fileIn, job);

            // Position is the actual start
            this.pos = start;
        }


        @Override
        public boolean nextKeyValue() throws IOException {
            // Current offset is the key
            key.set(pos);

            int newSize = 0;
            StringBuilder sb = new StringBuilder();
            while (pos < end) {
                newSize = in.readLine(value, maxLineLength,
                        Math.max((int) Math.min(
                                Integer.MAX_VALUE, end - pos),
                                maxLineLength));

                if (newSize == 0) {
                    break;
                }
                pos += newSize;
                sb.append(value.toString());
            }
            value.set(sb.toString());

            if (newSize == 0) {
                key = null;
                value = null;
                return false;
            } else {
                return true;
            }
        }

        @Override
        public LongWritable getCurrentKey() throws IOException,
                InterruptedException {
            return key;
        }

        @Override
        public Text getCurrentValue() throws IOException, InterruptedException {
            return value;
        }

        @Override
        public float getProgress() throws IOException, InterruptedException {
            if (start == end) {
                return 0.0f;
            } else {
                return Math.min(1.0f, (pos - start) / (float) (end - start));
            }
        }

        @Override
        public void close() throws IOException {
            if (in != null) {
                in.close();
            }
        }

    }
}
