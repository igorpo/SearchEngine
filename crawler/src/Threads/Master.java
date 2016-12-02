package Threads;

import Crawler.Messenger;
import Frontier.Frontier;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Created by igorpogorelskiy on 12/1/16.
 */
public class Master {

    private static final Log log = LogFactory.getLog(Master.class);

    /**
     * MAX number of parallel threads per Crawler.Crawler
     */
    private int maxThreads;

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
     * The Master class controls the extended specified thread class
     * that will be provided.
     * @param maxThreads Max number of threads to be run for this job
     * @param frontier Frontier queue of URLs
     */
    public Master(int maxThreads, Frontier frontier, Messenger msgr) {
        this.maxThreads = maxThreads;
        this.frontier = frontier;
        this.msgr = msgr;
        this.currentNumThreads = 0;
    }

    /**
     * Our own personal thread spawner. Instead of keeping a
     * dedicated threadpool open, we just spawn and kill threads off as
     * they finish working.
     */
    public synchronized void initThreads() {
        int threadsToStart = maxThreads - currentNumThreads;
        for (int i = 0; i < threadsToStart; i++) {
            Worker workerThread = new Worker();
            workerThread.initWorkerEssentials(increaseThreadCount(), this.frontier, this);
            workerThread.start();
        }
    }

    public synchronized void end(String threadID) {

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
