package Threads;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by igorpogorelskiy on 12/1/16.
 */
public class Worker extends Thread {
    private int id;
    private ConcurrentLinkedQueue<String> frontier;
    private Master master;

    /**
     * Set the id of the thread
     * @param id id generated for this thread
     */
    public void setID(int id) {
        this.id = id;
    }

    /**
     * Set the url frontier
     * @param frontier queue
     */
    public void setFrontier(ConcurrentLinkedQueue<String> frontier) {
        this.frontier = frontier;
    }

    /**
     * Set the master thread
     * @param master master to set
     */
    public void setMaster(Master master) {
        this.master = master;
    }

    /**
     * Getter for id
     * @return id of thread
     */
    public int getID() {
        return this.id;
    }

    /**
     * Used to let the worker know of his ID, the master, and the shared frontier queue
     * @param id id
     * @param frontier frontier queue of links
     * @param master master of the threads
     */
    public void initWorkerEssentials(int id, ConcurrentLinkedQueue<String> frontier, Master master) {
        setID(id);
        setFrontier(frontier);
        setMaster(master);
    }

    /**
     * Each worker thread
     */
    @Override
    public void run() {

    }

    /**
     * Apply processing to the url that we are working on. This
     * includes extracting links, etc.
     * @param url url extracted from frontier
     */
    public void processLink(String url) {

    }
}
