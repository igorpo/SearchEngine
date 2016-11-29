package com.indexer;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.PutItemOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.*;

import java.io.IOException;
import java.util.Iterator;
import java.util.StringTokenizer;


public class IndexerJob {



    public static class Map extends MapReduceBase implements Mapper<LongWritable, Text, Text, IntWritable> {
        private final static IntWritable one = new IntWritable(1);
        private Text word = new Text();

        public void map(LongWritable key, Text value, OutputCollector<Text, IntWritable> output, Reporter reporter) throws IOException {
            String line = value.toString();
            StringTokenizer tokenizer = new StringTokenizer(line);
            while (tokenizer.hasMoreTokens()) {
                word.set(tokenizer.nextToken());
                output.collect(word, one);
            }
        }
    }

    public static class Reduce extends MapReduceBase implements  Reducer<Text, IntWritable, Text, IntWritable> {

        public void reduce(Text key, Iterator<IntWritable> values, OutputCollector<Text, IntWritable> output, Reporter reporter) throws IOException {


            AmazonDynamoDBClient client = new AmazonDynamoDBClient();
            DynamoDB dynamoDB = new DynamoDB(client);

            Table table = dynamoDB.getTable("testcount");

            int sum = 0;
            while (values.hasNext()) {
                sum += values.next().get();
            }

            if (key != null && !key.toString().isEmpty()){
                try {
                    System.out.println("Adding a new item...");
                    PutItemOutcome outcome = table.putItem(new Item().withPrimaryKey("word", key.toString()).withInt("count", sum));

                    System.out.println("PutItem succeeded:\n" + outcome.getPutItemResult());

                } catch (Exception e) {
                    System.err.println("Unable to add item: " + key.toString() + " " + sum);
                    System.err.println(e.getMessage());
                }

            }
            output.collect(key, new IntWritable(sum));

        }
    }

    public static void main(String[] args) throws Exception {
        JobConf conf = new JobConf(IndexerJob.class);
        conf.setJobName("wordcount");

        conf.setOutputKeyClass(Text.class);
        conf.setOutputValueClass(IntWritable.class);

        conf.setMapperClass(Map.class);
        conf.setCombinerClass(Reduce.class);
        conf.setReducerClass(Reduce.class);

        conf.setInputFormat(TextInputFormat.class);
        conf.setOutputFormat(TextOutputFormat.class);

        FileInputFormat.setInputPaths(conf, new Path(args[0]));
        FileOutputFormat.setOutputPath(conf, new Path(args[1]));



        JobClient.runJob(conf);


    }
}
