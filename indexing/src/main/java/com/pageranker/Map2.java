package com.pageranker;

import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.*;

import java.io.IOException;

/**
 * Created by kierajmumick on 12/8/16.
 */
public class Map2 extends MapReduceBase implements Mapper<LongWritable, Text, Text, Text> {

    @Override
    public void configure(JobConf job) {

    }

    @Override
    public void map(LongWritable key,
                    Text value,
                    OutputCollector<Text, Text> output,
                    Reporter reporter) throws IOException {

//        int tabAfterPageInx = value.find("\t");
//        int tabAfterRankInx = value.find("\t", tabAfterPageInx + 1);
//        if (tabAfterPageInx < 0 || tabAfterRankInx < 0){
//            return;
//        }
//
//        // Parse out the information stored in the value.
//        String page = value.toString()
//                           .substring(0, tabAfterPageInx)
//                           .trim();
//        String rank = value.toString()
//                           .substring(tabAfterPageInx, tabAfterRankInx)
//                           .trim();

        String[] parts = value.toString().split("\t");
        if (parts.length != 3){
            return;
        }
        String page = parts[0];
        String rank = parts[1];

        // Mark the page as  existing from our crawler.
        output.collect(new Text(page), new Text(PageJob2.IS_CRAWLED_PREFIX));


        // Parse out the other links after we are sure that other links exist,
        // and we know a StringOutOfBounds exception won't be thrown by getting
        // the specified substring.
        String links = parts[2];
        String[] toPages = links.split(",");
        int numberOfLinks = toPages.length;

        // Map each of the pages liked to to the page it was linked from, along
        // with the number of links that the page it was linked from contained.
        // The format for this is:
        //      toPage -> fromPage pageRank(fromPage) numberOfLinks(linkedFrom)
        for (String toPage : toPages) {
            output.collect(
                    new Text(toPage),
                    new Text(page + "\t" + rank + "\t" + numberOfLinks));
        }

        // Save the information regarding which pages were linked to what.
        output.collect(new Text(page), new Text(PageJob2.LINKS_PREFIX + links));

        // At the end of this job, the reducer will then have an input which
        // maps a page to whether or not that page exists ("*" prefix), the
        // pages that link to it (No Prefix), and the pages that it links to
        // ("-" prefix).
    }

}
