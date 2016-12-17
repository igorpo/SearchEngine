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

        /*
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
        */

        String url = ((FileSplit) c.getInputSplit()).getPath().getName();

        Document test = Jsoup.parse(value.toString());
        if (test == null){
            return;
        }
        test.select("script,jscript,style").remove();
        String document;
        if (test.body() != null) {
            document = test.body().text();
        } else {
            return;
        }
        if (test.head() != null) {
            document += test.head().text();
        }
        //Text word = new Text();
        //Text textUrl = new Text();
        //word.set(url);
        //textUrl.set(value.toString());
        //c.write(word, textUrl);


        String[] words = document.toLowerCase().split("\\W+");

        //TODO: If running too slow, might want to get rid of stemming or extract to another for loop to stem only once
        Map<String, Integer> tfs = new HashMap<>();
        String stemmedWord;
        for (String w : words){
            Stemmer stemmer = new Stemmer();
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

        int maxVal = -1;
        for (Map.Entry<String, Integer> e : tfs.entrySet()){
            if (e.getValue() > maxVal){
                maxVal = e.getValue();
            }
        }

        Pattern p = Pattern.compile("[0-9]+");
        Pattern p2 = Pattern.compile("[0-9]+[\\p{Alnum}]+]");
        Pattern p3 = Pattern.compile("[\\p{Alpha}]+[0-9]\\p{Alnum}]*");
        double tf;
        for (String w : tfs.keySet()){
            if(!stopWords.contains(w)) {
                if (w.length() > 4 && (p.matcher(w).matches() || p2.matcher(w).matches() || p3.matcher(w).matches())){
                    continue;
                }
                Text word = new Text();
                Text textUrl = new Text();
                word.set(w);
                //textUrl.set(url);

                tf = .5 + (.5 * (double) tfs.get(w) / maxVal);
                textUrl.set(new Double(tf).toString() + "," + url);
                c.write(word, textUrl);
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