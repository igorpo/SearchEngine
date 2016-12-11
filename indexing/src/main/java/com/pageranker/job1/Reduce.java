package com.pageranker.job1;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.*;

import java.io.IOException;
import java.util.Iterator;

/**
 * Reduces the output of the Map class to be of the format
 *     |  Key  |     Val     |
 *     |-------|-------------|
 *     | <url> | 1.0 <links> |
 * This therefore maps each url to be initialized with a page rank of 1
 * and also to
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
        StringBuilder sb = new StringBuilder();

        sb.append("1.0\t");

        boolean first = false;
        while (values.hasNext()) {
            if (!first) {
                sb.append(",");
                first = true;
            }

            sb.append(values.next().toString());
        }

        // TODO: Gus & Chris - Make this work by putting output to S3/Dynamo/Whatever works
        output.collect(key, new Text(sb.toString()));
    }

}
