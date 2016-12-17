package com.pageranker;

import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.*;

import java.io.IOException;

/**
 * Created by kierajmumick on 12/8/16.
 */
public class Map3 extends MapReduceBase implements Mapper<LongWritable, Text, FloatWritable, Text> {

    private final static int PAGE_INDEX = 0;
    private final static int RANK_INDEX = 1;

    @Override
    public void configure(JobConf job) {

    }

    @Override
    public void map(LongWritable key,
                    Text value,
                    OutputCollector<FloatWritable, Text> output,
                    Reporter reporter) throws IOException {
        String[] parts = value.toString().split("\t");
        String page = parts[PAGE_INDEX];
        float rank = Float.parseFloat(parts[RANK_INDEX]);
        output.collect(new FloatWritable(rank), new Text(page));
    }

}
