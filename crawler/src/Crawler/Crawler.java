package Crawler;

import Threads.Master;
import Frontier.Frontier;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Created by igorpogorelskiy on 12/1/16.
 */
public class Crawler implements Messenger {

    private static final Log log = LogFactory.getLog(Crawler.class);

    public Crawler(int maxThreads, Frontier frontier) {
        Master master = new Master(maxThreads, frontier, this);
    }

    public void terminate(int threadID) {
        log.info("[" + threadID + "] terminated");
    }

    public void message(int threadID, String msg) {
        log.info("[" + threadID + "] " + msg);
    }

}
