package Crawler;

/**
 * Created by igorpogorelskiy on 12/1/16.
 */
public interface Messenger {
    /**
     * Send a message via the master
     */
    public void message(int threadID, String msg);

    /**
     * Termination of the thread with the given ID
     * @param threadID
     */
    public void terminate(int threadID);
}
