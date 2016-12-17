package com.pageranker;

import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.*;

import java.io.IOException;
import java.util.Iterator;

/**
 * Created by kierajmumick on 12/8/16.
 */
public class Reduce3 extends MapReduceBase implements Reducer<FloatWritable, Text, FloatWritable, Text> {

    @Override
    public void configure(JobConf job) {

    }

    @Override
    public void reduce(FloatWritable key,
                       Iterator<Text> values,
                       OutputCollector<FloatWritable, Text> output,
                       Reporter reporter) throws IOException {
        while (values.hasNext()) { output.collect(key, values.next()); }
    }

}
