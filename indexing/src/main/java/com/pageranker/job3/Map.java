package com.pageranker.job3;

import org.apache.hadoop.io.FloatWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.*;

import java.io.IOException;

/**
 * Created by kierajmumick on 12/8/16.
 */
public class Map extends MapReduceBase implements Mapper<LongWritable, Text, FloatWritable, Text> {

    @Override
    public void configure(JobConf job) {

    }

    @Override
    public void map(LongWritable key,
                    Text value,
                    OutputCollector<FloatWritable, Text> output,
                    Reporter reporter) throws IOException {
        String pageWithRank = value.toString();

        int firstTabInx = pageWithRank.indexOf('\t');
        int secondTabInx = pageWithRank.indexOf('\t', firstTabInx);

        String page = pageWithRank.substring(0, firstTabInx);
        float rank = Float.parseFloat(pageWithRank.substring(firstTabInx, secondTabInx).trim());

        output.collect(new FloatWritable(rank), new Text(page));
    }

}
