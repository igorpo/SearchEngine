package edu.upenn.cis455.querying;


import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import edu.upenn.cis455.server.MainServer;
import org.apache.commons.codec.binary.Base32;

import java.util.*;

/**
 * Created by azw on 12/6/16.
 * yah bud
 */
public class QueryHandler {


    // GET query
    // lowercase
    // split
    // stem
    // query dynamo for all words
    // perform union
    // sory by tfidf
    //


    DynamoDB dynamoDB;
    Table table;
    DynamoDB pagedynamoDB;
    Table pagetable;

    private Map<String, Double> urlToTfidf;


    public QueryHandler(String tableName) {
        AmazonDynamoDBClient client = new AmazonDynamoDBClient();
        dynamoDB = new DynamoDB(client);

        table = dynamoDB.getTable(tableName);
        urlToTfidf = new HashMap<>();

        AmazonDynamoDBClient pageclient = new AmazonDynamoDBClient();
        pagedynamoDB = new DynamoDB(client);

        table = dynamoDB.getTable(tableName);
        urlToTfidf = new HashMap<>();

    }

    private double getTFIDF(List<String> l){
        return Double.parseDouble(l.get(1));
    }
    private String getURL(List<String> l){
        return l.get(0);
    }


    private double weightWithPageRank(String url, double tfidf){
        // get pank rank from dynamodb
        double pagerank = 1000.0;
        double pagerankWeight = 0.0;
        return pagerank*pagerankWeight + (1-pagerankWeight)*tfidf;
    }


    private List<String> sortMap(){
        List<String> keys = new ArrayList<>(urlToTfidf.keySet());



//        keys.sort(new Comparator<String>() {
//            @Override
//            public int compare(String s, String t1) {
//                double val1 = weightWithPageRank(s, urlToTfidf.get(s));
//                double val2 = weightWithPageRank(t1, urlToTfidf.get(t1));
//                if (val1 < val2){
//                    return 1;
//                } else if (val1 > val2){
//                    return -1;
//                } else {
//                    return 0;
//                }
//            }
//        });



        PriorityQueue<String> pq = new PriorityQueue<>(new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                double val1 = weightWithPageRank(s1, urlToTfidf.get(s1));
                double val2 = weightWithPageRank(s2, urlToTfidf.get(s2));
                if (val1 > val2){
                    return 1;
                } else if (val1 < val2){
                    return -1;
                } else {
                    return 0;
                }
            }
        });

        for (String s : keys){
            pq.offer(s);
            if (pq.size() > MainServer.N){
                pq.poll();
            }
        }
        List<String> outList = new LinkedList<>();
        for (String s : pq){
            new String(new Base32().decode(s.getBytes()));
            outList.add(0, s);
        }


        return outList;
    }



    private List<String> combineResults(Set<List<List<String>>> results){


        int i;
        double tfidf;
        List<String> urlTfidf;
        String url;
        for (List<List<String>> result : results) {
            for(i = 0; i < result.size(); i++) {
                urlTfidf = result.get(i);
                url = getURL(urlTfidf);
                tfidf = getTFIDF(urlTfidf);
                if(urlToTfidf.containsKey(url)) {
                    urlToTfidf.put(url, urlToTfidf.get(url) + tfidf);
                } else {
                    urlToTfidf.put(url, tfidf);
                }
            }
        }
        int numQueries = results.size();
        //re-normalize tfidf
        for(String u : urlToTfidf.keySet()) {
            urlToTfidf.put(u, urlToTfidf.get(u)/numQueries);
        }

        return sortMap();
    }

    public List<String> query(String q){

        // lowercase
        // split
        String[] queryWords = q.toLowerCase().split("[^\\p{Alnum}']+");
        // stem
        Stemmer stemmer = new Stemmer();
        List<String> stemmedWordsToSearch = new ArrayList<>();
        String stemmedWord;
        for (String w : queryWords){
            stemmer.add(w.toCharArray(), w.length());
            stemmer.stem();
            stemmedWord = stemmer.toString();
            stemmedWordsToSearch.add(stemmedWord);
        }

        // query dynamo for all words
        Set<List<List<String>>> results = new HashSet<>();
        for (String word : stemmedWordsToSearch){
            Item item = table.getItem("word", word);
            List<List<String>> s = item.getList("data");
            results.add(s);
        }


        // perform union
        // AND sort by tfidf
        List<String> yeahbud = combineResults(results);

        return yeahbud;
    }

}
