package stormCrawler;

import edu.upenn.cis.stormlite.OutputFieldsDeclarer;
import edu.upenn.cis.stormlite.TopologyContext;
import edu.upenn.cis.stormlite.bolt.IRichBolt;
import edu.upenn.cis.stormlite.bolt.OutputCollector;
import edu.upenn.cis.stormlite.routers.IStreamRouter;
import edu.upenn.cis.stormlite.tuple.Fields;
import edu.upenn.cis.stormlite.tuple.Tuple;
import edu.upenn.cis.stormlite.tuple.Values;
import org.apache.http.HttpResponse;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by YagilB on 17/12/2016.
 */
public class LinkExtractorBolt implements IRichBolt {
    Fields schema = new Fields("document");

    /**
     * This is a simple map from word to a running count.
     * If we have multiple "sharded" copies of the WordCounter
     * we'll need to make sure they get different words, otherwise
     * we'll have multiple partial counts instead of a single unified
     * one.
     *
     */
    private Map<String, Integer> wordCounter = new HashMap<>();

    /**
     * To make it easier to debug: we have a unique ID for each
     * instance of the WordCounter, aka each "executor"
     */
    String executorId = UUID.randomUUID().toString();

    /**
     * This is where we send our output stream
     */
    private OutputCollector collector;

    public LinkExtractorBolt() {
    }

    /**
     * Initialization, just saves the output stream destination
     */
    @Override
    public void prepare(Map<String,String> stormConf,
                        TopologyContext context, OutputCollector collector) {
        this.collector = collector;
    }

    /**
     * Process a tuple received from the stream, incrementing our
     * counter and outputting a result
     */
    @Override
    public void execute(Tuple input) {
        String url = input.getStringByField("url");
        HttpResponse response = (HttpResponse) input.getObjectByField("response");
        if (response == null) return;
        collector.emit(new Values<Object>(/* TODO HERE */));
    }

    /**
     * Shutdown, just frees memory
     */
    @Override
    public void cleanup() {
//    	System.out.println("WordCount executor " + getExecutorId() + " has words: " + wordCounter.keySet());
//
//    	wordCounter.clear();
    }

    /**
     * Lets the downstream operators know our schema
     */
    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(schema);
    }

    /**
     * Used for debug purposes, shows our exeuctor/operator's unique ID
     */
    @Override
    public String getExecutorId() {
        return executorId;
    }

    /**
     * Called during topology setup, sets the router to the next
     * bolt
     */
    @Override
    public void setRouter(IStreamRouter router) {
        this.collector.setRouter(router);
    }

    /**
     * The fields (schema) of our output stream
     */
    @Override
    public Fields getSchema() {
        return schema;
    }
}
