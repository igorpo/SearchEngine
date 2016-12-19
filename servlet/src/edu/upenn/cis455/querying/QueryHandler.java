package edu.upenn.cis455.querying;


import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.*;
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
    String pagetableName;
    Table pagetable;

    private Map<String, Double> urlToTfidf;
    private Map<String, Double> urlToPage;


    public QueryHandler(String tableName, String pageTableName) {
        AmazonDynamoDBClient client = new AmazonDynamoDBClient();
        dynamoDB = new DynamoDB(client);
        Region page = Region.getRegion(Regions.US_EAST_1);
        client.setRegion(page);

        table = dynamoDB.getTable(tableName);
        urlToTfidf = new HashMap<>();
        urlToPage = new HashMap<>();

        AmazonDynamoDBClient pageclient = new AmazonDynamoDBClient();
        Region rpage = Region.getRegion(Regions.US_WEST_2);
        pageclient.setRegion(rpage);
        pagedynamoDB = new DynamoDB(pageclient);
        this.pagetableName = pageTableName;
        pagetable = pagedynamoDB.getTable(pageTableName);

    }

    private double getTFIDF(List<String> l){
        return Double.parseDouble(l.get(1));
    }
    private String getURL(List<String> l){
        return l.get(0);
    }


    private double weightWithPageRank(String url, double tfidf){
        // get pank rank from dynamodb
        double pagerank = 0.0;
        double pagerankWeight = 0.0;
        Double d = urlToPage.get(url);
        if (d != null){
            pagerank = d;
            pagerankWeight = 1.0;


        }
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
            String fixed = new String(new Base32().decode(s.getBytes()));
            outList.add(0, fixed);
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
        int num = 0;
        int total = 0;
        TableKeysAndAttributes batchAttribute = new TableKeysAndAttributes(pagetableName);
        for(String u : urlToTfidf.keySet()) {
            total++;
            urlToTfidf.put(u, urlToTfidf.get(u)/numQueries);
            batchAttribute.addHashOnlyPrimaryKey("url", new String(new Base32().decode(u.getBytes())));
            num++;
            if (num >= 25){
                System.out.println("getting1");
                BatchGetItemOutcome outcome = pagedynamoDB.batchGetItem(
                        batchAttribute);
                System.out.println(outcome);
                System.out.println("getting2");
                List<Item> items = outcome.getTableItems().get(pagetableName);
                for (Item item : items) {
                    System.out.println(item);
                    System.out.println("putting in map");
                    urlToPage.put(item.getString("url"), Double.parseDouble(item.getString("pageRank")));
                }
                System.out.println(outcome.getUnprocessedKeys().size());

                num = 0;
                batchAttribute = new TableKeysAndAttributes(pagetableName);
            }
        }
        if (num != 0 ){
            System.out.println("getting1last");
            BatchGetItemOutcome outcome = pagedynamoDB.batchGetItem(
                    batchAttribute);
            System.out.println("getting2last");
            List<Item> items = outcome.getTableItems().get(pagetableName);
            for (Item item : items) {
                System.out.println("putting in map");
                urlToPage.put(item.getString("url"), Double.parseDouble(item.getString("pageRank")));
            }

            num = 0;
            batchAttribute = new TableKeysAndAttributes(pagetableName);
        }

        System.out.println("total: " + total);
        return sortMap();
    }

    public List<String> query(String q){

        if (q == null || q.isEmpty()){
            return null;
        }
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
            if(item != null) {
                List<List<String>> s = item.getList("data");
                if (item != s) {
                    results.add(s);
                }
            }
        }


        // perform union
        // AND sort by tfidf
        if (results == null || results.isEmpty()){
            return null;
        }
        List<String> yeahbud = combineResults(results);

        return yeahbud;
    }

}
