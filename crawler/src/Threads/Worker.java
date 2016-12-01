package Threads;

import Crawler.Messenger;
import Frontier.Frontier;

/**
 * Created by igorpogorelskiy on 12/1/16.
 */
public class Worker extends Thread {
    private String id;
    private Frontier frontier;
    private Master master;
    private Messenger msgr;

    /**
     * Set the id of the thread
     * @param id id generated for this thread
     */
    public void setID(String id) {
        this.id = id;
    }

    /**
     * Set the url frontier
     * @param frontier queue
     */
    public void setFrontier(Frontier frontier) {
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
     * Messenger for talking to crawler set here
     * @param m msgr to set
     */
    public void setMessenger(Messenger m) {
        this.msgr = m;
    }

    /**
     * Getter for id
     * @return id of thread
     */
    public String getID() {
        return this.id;
    }

    /**
     * Used to let the worker know of his ID, the master, and the shared frontier queue
     * @param id id
     * @param frontier frontier queue of links
     * @param master master of the threads
     */
    public void initWorkerEssentials(String id, Frontier frontier, Master master, Messenger msgr) {
        setID(id);
        setFrontier(frontier);
        setMaster(master);
        setMessenger(msgr);
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
