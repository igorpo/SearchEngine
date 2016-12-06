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
import org.apache.hadoop.mapred.*;
import org.jsoup.Jsoup;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.indexer.IndexerJob.bucketName;

/**
 * Created by azw on 11/30/16.
 * index + ifidf mapper
 */
public class MapImpl extends MapReduceBase implements Mapper<LongWritable, Text, Text, Text> {

    private final static IntWritable one = new IntWritable(1);
    private Text word = new Text();
    private Text textUrl = new Text();
    private AmazonS3 s3;
    private String stopList = "stopList.txt";
    private Set<String> stopWords;

    @Override
    public void configure(JobConf job){
        s3 = new AmazonS3Client();
        Region reg = Region.getRegion(Regions.US_EAST_1);
        s3.setRegion(reg);

        // Stop list setup
        S3Object stopDoc = s3.getObject(new GetObjectRequest(bucketName, stopList));
        if (stopDoc == null){
//            System.err.println("Could not get stop word file: " + stopList);
            return;
        }
        BufferedReader stopReader = new BufferedReader(new
                InputStreamReader(stopDoc.getObjectContent()));

        String stopLine;
        Set<String> stopWords = new HashSet<>();
        Stemmer stemmer = new Stemmer();
        String stemmedWord;
        try {
            while ((stopLine = stopReader.readLine()) != null) {
                stemmer.add(stopLine.toCharArray(), stopLine.length());
                stemmer.stem();
                stemmedWord = stemmer.toString();
                stopWords.add(stemmedWord);
            }
        } catch(IOException e) {
            System.err.println("IOException: Could not read stop word file: " + stopList);
        }
        // end of stop list setup
    }

    @Override
    public void map(LongWritable key, Text value, OutputCollector<Text, Text> output, Reporter reporter) throws IOException {
        String url = value.toString();
        if(url == null || url.equals("")) {
//            word.set(url);
//            output.collect(word, one);
            return;
        }
        S3Object urlDoc = s3.getObject(new GetObjectRequest(bucketName, url));
        if (urlDoc == null){
//            word.set("BAD");
//            output.collect(word, one);
            return;
        }

        BufferedReader reader = new BufferedReader(new
                InputStreamReader(urlDoc.getObjectContent()));

        String line;
        StringBuilder sb = new StringBuilder();
        while((line = reader.readLine()) != null){
            sb.append(line);

        }

        String document = Jsoup.parse(sb.toString()).text();

        System.err.println("URL: " + url + ",document: " + document);

        String[] words = document.toLowerCase().split("[^\\p{Alnum}']+");

        Stemmer stemmer = new Stemmer();
        //TODO: If running too slow, might want to get rid of stemming or extract to another for loop to stem only once
        Map<String, Integer> tfs = new HashMap<>();
        String stemmedWord;
        for (String w : words){
            stemmer.add(w.toCharArray(), w.length());
            stemmer.stem();
            stemmedWord = stemmer.toString();
            if (tfs.containsKey(stemmedWord)){
                tfs.put(stemmedWord, tfs.get(stemmedWord) + 1);
            } else {
                tfs.put(stemmedWord, 1);
            }
        }

        //find max

        float max = -1;
        for (Map.Entry<String, Integer> e : tfs.entrySet()){
            if (e.getValue() > max){
                max = e.getValue();
            }
        }


        double tf;
        for (String w : tfs.keySet()){
            if(!stopWords.contains(w)) {
                word.set(w);
                textUrl.set(url);

                tf = .5 + .5 * (double) tfs.get(w) / (double) max;
                textUrl.set(new Double(tf).toString() + "," + url);
                output.collect(word, textUrl);
            }
        }


       // S3Object object = s3.getObject(new GetObjectRequest(bucketName, key));


//        StringTokenizer tokenizer = new StringTokenizer(line);
//        while (tokenizer.hasMoreTokens()) {
//            word.set(tokenizer.nextToken());
//            output.collect(word, one);
//        }



    }
}