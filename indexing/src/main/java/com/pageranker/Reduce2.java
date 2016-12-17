package com.pageranker;

import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.*;

import java.io.IOException;
import java.util.Iterator;

/**
 * Created by kierajmumick on 12/8/16.
 */
public class Reduce2 extends MapReduceBase implements Reducer<Text, Text, Text, Text> {

    private final static int PAGE_RANK_ITERATION_STEP_LINKED_FROM_INDEX = 0;
    private final static int PAGE_RANK_ITERATION_STEP_RANK_INDEX = 1;
    private final static int PAGE_RANK_ITERATION_STEP_NUM_LINKS_INDEX = 2;

    @Override
    public void configure(JobConf job) {

    }

    @Override
    public void reduce(Text url,
                       Iterator<Text> values,
                       OutputCollector<Text, Text> output,
                       Reporter reporter) throws IOException {

        // Keeps track of whether or not this page has been crawled.
        boolean isCrawled = false;

        // Keeps track of the list of links that the url being reduced maps to.
        String links = "";

        // Sum of all of the page ranks.
        float pageRankSum = 0;

        while (values.hasNext()) {
            String val = values.next().toString();

            // Make sure that the |url| is crawled.
            if (val.length() >= PageJob2.IS_CRAWLED_PREFIX.length() &&
                    val.equals(PageJob2.IS_CRAWLED_PREFIX)) {
                isCrawled = true;
            }

            // Remember all of the previous links that were linked to by this URL.
            else if (val.length() >= PageJob2.LINKS_PREFIX.length() &&
                    val.substring(0, PageJob2.LINKS_PREFIX.length())
                        .equals(PageJob2.LINKS_PREFIX)) {
                links = val.substring(PageJob2.LINKS_PREFIX.length());
            }

            // Update the page rank sum for this URL.
            else {
                String[] parts = val.split("\t");
                double pageRank = Double.parseDouble(parts[PAGE_RANK_ITERATION_STEP_RANK_INDEX]);
                int numLinks = Integer.parseInt(parts[PAGE_RANK_ITERATION_STEP_NUM_LINKS_INDEX]);

                pageRankSum += (pageRank / numLinks);
            }
        }

        // If this page has never been crawled, don't collect the output.
        if (!isCrawled) { return; }

        // Update the page rank.
        double newPageRank = PageJob2.PAGE_RANK_DAMPENING_CONST * pageRankSum + (1 - PageJob2.PAGE_RANK_DAMPENING_CONST);

        // Output the new page rank.
        output.collect(url, new Text("" + newPageRank + "\t" + links));

    }

}
