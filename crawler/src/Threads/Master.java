package Threads;

import Crawler.Messenger;
import Frontier.Frontier;

/**
 * Created by igorpogorelskiy on 12/1/16.
 */
public class Master {

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
     * Counter that will be synchronized for ID generation
     */
    private int idCounter;

    /**
     * The extended thread class that Master has control over
     */
    private Class threadClass;

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
     * @param threadClass Extended thread class that master rules over
     * @param maxThreads Max number of threads to be run for this job
     * @param frontier Frontier queue of URLs
     */
    public Master(Class threadClass, int maxThreads, Frontier frontier, Messenger msgr) {
        this.threadClass = threadClass;
        this.maxThreads = maxThreads;
        this.frontier = frontier;
        this.msgr = msgr;
        idCounter = currentNumThreads = 0;
    }

    public synchronized void start() {

    }

    public synchronized void end(int threadID) {

    }

    /**
     * Ensures a unique thread number for a newly spawned thread. Since we are
     * not using a thread pool this can get a bit high, but the threads will
     * kill themselves off when they finish so no worries!
     * @return id number
     */
    public synchronized int generateID() {
        return idCounter++;
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
