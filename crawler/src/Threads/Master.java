package threads;

import crawler.Messenger;
import databases.DynamoWrapper;
import frontier.Frontier;
import robots.RobotsTxtInfo;
import databases.S3Wrapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by igorpogorelskiy on 12/1/16.
 */
public class Master {

    private static final Log log = LogFactory.getLog(Master.class);

    /**
     * MAX number of parallel threads per crawler
     */
    private int maxThreads;

    /**
     * Max documents to be crawled by our crawler. If this is
     * set to -1, we keep going forever until something breaks or
     * we run out of URLs (ha).
     */
    private int maxDocuments;

    /**
     * Current number of threads running which will
     * be kept track of through ThreadIDs
     */
    private int currentNumThreads;

    /**
     * A concurrent frontier queue that must be shared across the threads
     */
    private Frontier frontier;

    /**
     * Messenger interface for contacting crawler
     */
    private Messenger msgr;

    /**
     * Current number of processed docs
     */
    private int currentNumDocumentsProcessed;

    /**
     * Keeps track of hosts visited for crawl delay
     */
    protected ConcurrentHashMap<String, Long> crawlTimes;

    /**
     * Keeps track of robots for base link
     */
    protected ConcurrentHashMap<String, RobotsTxtInfo> robotsForUrl;

    /**
     * Keep track of visited links
     */
    protected HashSet<String> visitedUrls;

    /**
     * Bucket name for S3 db
     */
    static final String BUCKET_NAME = "cis-455";

    /**
     * Dynamo table name for {url -> [outgoing link list]}
     */
    static final String DYNAMO_DB_NAME = "visitedUrlsOutgoingLinks";

    /**
     * The Master class controls the extended specified thread class
     * that will be provided.
     * @param maxThreads Max number of threads to be run for this job
     * @param maxDocuments Max number of documents to be crawled
     *                     If -1, then we keep going forever
     */
    public Master(int maxThreads, int maxDocuments, Messenger msgr) {
        this.maxThreads = maxThreads;
        this.msgr = msgr;
        this.maxDocuments = maxDocuments;
        this.currentNumThreads = this.currentNumDocumentsProcessed = 0;
        robotsForUrl = new ConcurrentHashMap<>();
        crawlTimes = new ConcurrentHashMap<>();
        visitedUrls = new HashSet<>();
        S3Wrapper.init(BUCKET_NAME);
        DynamoWrapper.init(DYNAMO_DB_NAME);
        try {
            initThreads();
        } catch (Exception e) {
            log.error("Could not launch initial thread pool: " + e.getMessage());
        }
    }

    /**
     * Our own personal thread spawner. Instead of keeping a
     * dedicated threadpool open, we just spawn and kill threads off as
     * they finish working.
     */
    public synchronized void initThreads() {
        for (int i = 0; i < getMaxThreads(); i++) {
            try {
                Worker workerThread = new Worker();
                String id = String.valueOf(i);
                log.info("THREAD WITH ID " + id + " CREATED");
                workerThread.initWorkerEssentials(id, this, this.msgr);
                workerThread.start();
            } catch (IOException e) {
                log.error("Failed to contact the remote queue service " + e.getMessage());
            }
        }
    }

    /**
     * Final master handler of terminating thread
     * @param threadID id of thread that is terminating
     */
    public synchronized void terminateThread(String threadID) {
        decreaseThreadCount();

        // send the messenger (crawler) the termination request in case we want to do something else
        this.msgr.terminate(threadID);

    }

    /**
     * Checks whether or not we have achieved the limit of the documents collected
     * taking into account the infinity possibility.
     * @return true if limit achieved, false if not or if limit is infinity
     */
    public boolean achievedLimit() {
        return getCurrentNumDocumentsProcessed() >= getMaxDocuments() && getMaxDocuments() != -1;
    }

    /**
     * Increase the current thread count. This also serves as an ID
     * generator, since it will ensure that the threads that have just died
     * IDs are reassigned to the newly spawned threads to fill them up.
     * @return new current number of threads/ID
     */
    public synchronized String increaseThreadCount() {
        return String.valueOf(this.currentNumThreads++);
    }

    /**
     * Decrease the current thread count once a thread has died off
     */
    public synchronized void decreaseThreadCount() {
        this.currentNumThreads--;
    }

    /**
     * Increase the count of the processed docs
     */
    public synchronized void increaseProcessedDocCount() {
        this.currentNumDocumentsProcessed++;
    }

    /**
     * Add a url that has been seen
     * @param url url that we want to add
     */
    public void addSeenUrl(String url) {
        synchronized (visitedUrls) {
            visitedUrls.add(url);
        }
    }

    /**
     * Have we seen the url
     * @param url url to check for
     * @return true if seen, false otherwise
     */
    public boolean haveSeenUrl(String url) {
        return DynamoWrapper.urlHasBeenSeen(url);
//        synchronized (visitedUrls) {
//            return visitedUrls.contains(url);
//        }
    }

    /**
     * Remove a seen url from the queue
     * @param url url that has been seen
     */
    public void removeFromSeenURLs(String url) {
        synchronized (visitedUrls) {
            this.visitedUrls.remove(url);
        }
    }

    /**
     * Get the current number of documents processed
     * @return number of documents that have been processed and downloaded
     */
    public synchronized int getCurrentNumDocumentsProcessed() {
        return this.currentNumDocumentsProcessed;
    }

    /**
     * Getter for maximum number of documents
     * @return the maximum number of docs to crawl
     */
    public int getMaxDocuments() {
        return this.maxDocuments;
    }

    /**
     * Getter for max threads
     * @return the max threads for this job
     */
    public int getMaxThreads() {
        return this.maxThreads;
    }

    /**
     * Getter for the number of currently running threads
     * @return num of currently running threads on the job
     */
    public int getCurrentNumThreads() {
        return this.currentNumThreads;
    }

}
