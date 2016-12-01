package Crawler;

import Threads.Master;
import Threads.Worker;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.concurrent.ConcurrentLinkedQueue;

public class Crawler implements Messenger {

    private static final Log log = LogFactory.getLog(Crawler.class);

    public Crawler(int maxThreads, ConcurrentLinkedQueue<String> q) {
        Master master = new Master(Worker.class, maxThreads, q, this);
    }

    public void terminate(int threadID) {
        log.info("[" + threadID + "] terminated");
    }

    public void message(int threadID, String msg) {
        log.info("[" + threadID + "] " + msg);
    }

    public static void main(String[] args) {
        log.info("Initializing the crawler...");
    }

}
