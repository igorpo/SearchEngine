package com.pageranker.job1;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.*;

import java.io.IOException;
import java.util.Iterator;

/**
 * Created by kierajmumick on 12/8/16.
 */
public class Reduce extends MapReduceBase implements Reducer<Text, Text, Text, Text> {

    @Override
    public void configure(JobConf job) {

    }

    @Override
    public void reduce(Text key,
                       Iterator<Text> values,
                       OutputCollector<Text, Text> output,
                       Reporter reporter) throws IOException {

    }

}
