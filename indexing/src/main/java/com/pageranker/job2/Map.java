package com.pageranker.job2;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.*;

import java.io.IOException;

/**
 * Created by kierajmumick on 12/8/16.
 */
public class Map extends MapReduceBase implements Mapper<Text, Text, Text, Text> {

    @Override
    public void configure(JobConf job) {

    }

    @Override
    public void map(Text key,
                    Text value,
                    OutputCollector<Text, Text> output,
                    Reporter reporter) throws IOException {

    }

}
