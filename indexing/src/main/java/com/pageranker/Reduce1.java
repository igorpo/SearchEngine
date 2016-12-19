package com.pageranker;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;

/**
 * Reduces the output of the Map class to be of the format
 *     |  Key  |     Val     |
 *     |-------|-------------|
 *     | <url> | 1.0 <links> |
 * This therefore maps each url to be initialized with a page rank of 1
 * and also to
 */
public class Reduce1 extends Reducer<Text, Text, Text, Text> {

    @Override
    public void setup(Context c) {

    }

    @Override
    public void reduce(Text key,
                       Iterable<Text> values,
                       Context c) throws IOException, InterruptedException {
        StringBuilder sb = new StringBuilder();

        sb.append("1.0\t");

        boolean first = true;
        for (Text t : values) {
            if (!first) { sb.append(","); }

            sb.append(t.toString());
            first = false;
        }

        // TODO: Gus & Chris - Make this work by putting output to S3/Dynamo/Whatever works
        c.write(key, new Text(sb.toString()));
    }

}
