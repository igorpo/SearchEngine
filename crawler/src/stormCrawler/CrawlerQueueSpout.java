package stormCrawler;

import edu.upenn.cis.stormlite.OutputFieldsDeclarer;
import edu.upenn.cis.stormlite.TopologyContext;
import edu.upenn.cis.stormlite.routers.IStreamRouter;
import edu.upenn.cis.stormlite.spout.IRichSpout;
import edu.upenn.cis.stormlite.spout.SpoutOutputCollector;
import edu.upenn.cis.stormlite.tuple.Fields;
import edu.upenn.cis.stormlite.tuple.Values;

import java.io.BufferedReader;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;

/**
 * Created by YagilB on 17/12/2016.
 */
public class CrawlerQueueSpout implements IRichSpout {
//    static Logger log = Logger.getLogger(CrawlerQueueSpout.class);

    /**
     * To make it easier to debug: we have a unique ID for each
     * instance of the WordSpout, aka each "executor"
     */
    String executorId = UUID.randomUUID().toString();

    /**
     * The collector is the destination for tuples; you "emit" tuples there
     */
    SpoutOutputCollector collector;

    /**
     * This is a simple file reader for words.txt
     */
    BufferedReader reader;
    Random r = new Random();

    String[] words = {"big", "bad", "wolf", "little", "red", "riding", "hood"};

    public CrawlerQueueSpout() {
//        log.debug("Starting spout");
    }

    /**
     * Initializes the instance of the spout (note that there can be multiple
     * objects instantiated)
     */
    @SuppressWarnings("rawtypes")
    @Override
    public void open(Map conf, TopologyContext context, SpoutOutputCollector collector) {
        this.collector = collector;

//        try {
//        	log.debug(getExecutorId() + " opening file reader");
//			reader = new BufferedReader(new FileReader("words.txt"));
//		} catch (FileNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
    }

    /**
     * Shut down the spout
     */
    @Override
    public void close() {
//    	if (reader != null)
//	    	try {
//				reader.close();
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
    }

    /**
     * The real work happens here, in incremental fashion.  We process and output
     * the next item(s).  They get fed to the collector, which routes them
     * to targets
     */
    @Override
    public void nextTuple() {
        BlockingQueue<String> frontierQueue = null;//XPathCrawlerFactory.getFrontier();
//        BlockingSet<String> visitedURLs = null;//XPathCrawlerFactory.getVisitedURLs();
        if (frontierQueue.size() > 0) {
            String url = "";
//            try {
//                url = frontierQueue.poll();
//                //log.debug("url found: " + url);
//            } catch (InterruptedException e1) {
//                //log.debug("url NOT found");
//                e1.printStackTrace();
//            }

//            if (visitedURLs.contains(url)) {
//                nextTuple();
//                return;
//            }

            //log.debug(getExecutorId() + " emitting " + url);
            this.collector.emit(new Values<Object>(url));

//            visitedURLs.add(url);
        }

        Thread.yield();
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("url"));
    }


    @Override
    public String getExecutorId() {

        return executorId;
    }


    @Override
    public void setRouter(IStreamRouter router) {
        this.collector.setRouter(router);
    }

}
