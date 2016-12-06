package crawler;

import threads.Master;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Created by igorpogorelskiy on 12/1/16.
 */
public class Crawler implements Messenger {

    private static final Log log = LogFactory.getLog(Crawler.class);
    private Master master;

    /**
     * The crawler, which is a Messenger meaning it is able to
     * retrieve messages sent via Master from the Workers, will take
     * a frontier queue and a max thread and doc parameter and create the master
     * which will oversee the work done.
     * @param maxThreads maximum number of threads
     * @param maxDocuments maximum number of documents
     */
    public Crawler(int maxThreads, int maxDocuments) {
        this.master = new Master(maxThreads, maxDocuments, this);
    }

    /**
     * On termination of thread events launched here
     * @param threadID thread ID that is terminated
     */
    public void terminate(String threadID) {
        log.info("[" + threadID + "] terminated");
    }

    /**
     * Message sent through Messenger interface launched here
     * @param threadID thread that sent message
     * @param msg message to be sent
     */
    public void message(String threadID, String msg) {
        log.info("[" + threadID + "] " + msg);
    }

    /**
     * Launched when we are done the crawl
     */
    public void complete() {
        log.info("Finished the crawl with " + master.getCurrentNumDocumentsProcessed() + " documents processed!");
        System.exit(0);
    }

}
