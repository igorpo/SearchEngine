package com.indexer;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import static com.indexer.IndexerJob.bucketName;

/**
 * Created by azw on 11/30/16.
 * index + ifidf mapper
 */
public class MapImpl extends Mapper<LongWritable, Text, Text, Text> {

    private final static IntWritable one = new IntWritable(1);

    private AmazonS3 s3;
    private String stopList = "stopList.txt";
    private Set<String> stopWords;

    @Override
    public void setup(Context context){
        s3 = new AmazonS3Client();
        Region reg = Region.getRegion(Regions.US_EAST_1);
        s3.setRegion(reg);

         //Stop list setup
        S3Object stopDoc = s3.getObject(new GetObjectRequest(bucketName, stopList));
        if (stopDoc == null){
            //System.err.println("Could not get stop word file: " + stopList);
            return;
        }
        BufferedReader stopReader = new BufferedReader(new
                InputStreamReader(stopDoc.getObjectContent()));

        String stopLine;
        stopWords = new HashSet<>();
        String stemmedWord;
        try {
            while ((stopLine = stopReader.readLine()) != null) {
                Stemmer stemmer = new Stemmer();
                stemmer.add(stopLine.toCharArray(), stopLine.length());
                stemmer.stem();
                stemmedWord = stemmer.toString();
                stopWords.add(stemmedWord);
            }
        } catch(IOException e) {
            //System.err.println("IOException: Could not read stop word file: " + stopList);
        }
        // end of stop list setup
    }

    @Override
    public void map(LongWritable key, Text value, Context c) throws IOException, InterruptedException {

        // If the document has no text, skip it.
        if (value.toString().equals("")) { return; }

        // Get the file url.
        String url = ((FileSplit) c.getInputSplit()).getPath().getName();

        // Parse out the document and ensure that is valid.
        Document test = Jsoup.parse(value.toString());
        if (test == null || test.body() == null) { return; }

        // Remove any code from the HTML.
        test.select("script,jscript,style").remove();

        // Parse out the text from the document.
        String document = test.body().text();
        if (test.head() != null) { document += test.head().text(); }
        String[] words = document.toLowerCase().split("\\W+");

        // TODO: If running too slow, might want to get rid of stemming or
        // extract to another for loop to stem only once
        Map<String, Integer> tfs = new HashMap<>();
        String stemmedWord;
        for (String w : words) {
            Stemmer stemmer = new Stemmer();
            stemmer.add(w.toCharArray(), w.length());
            stemmer.stem();
            stemmedWord = stemmer.toString();
            if (tfs.containsKey(stemmedWord)) {
                tfs.put(stemmedWord, tfs.get(stemmedWord) + 1);
            } else {
                tfs.put(stemmedWord, 1);
            }
        }

        // Find the maximum term frequency.
        int maxVal = Integer.MIN_VALUE;
        for (Map.Entry<String, Integer> e : tfs.entrySet()) {
            if (e.getValue() > maxVal) {
                maxVal = e.getValue();
            }
        }

        // Patterns that we should not be matching
        final Pattern p = Pattern.compile("[0-9]+");
        final Pattern p2 = Pattern.compile("[0-9]+[\\p{Alnum}]+]");
        final Pattern p3 = Pattern.compile("[\\p{Alpha}]+[0-9]\\p{Alnum}]*");

        for (String w : tfs.keySet()) {
            if (p.matcher(w).matches()
                    || p2.matcher(w).matches()
                    || p3.matcher(w).matches()) {
                continue;
            }

            // Calculate the term frequency and write it out.
            if (!stopWords.contains(w)) {
                double tf = .5 + (.5 * (double) tfs.get(w) / maxVal);
                c.write(new Text(w), new Text(tf + "," + url));
            }
        }
    }
}