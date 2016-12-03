package Frontier;

import java.io.IOException;

/**
 * Created by YagilB on 12/1/2016.
 */
public interface Frontier {

    /**
     * init the thread with the given ID
     * @param threadID
     */
    public void init(String threadID);

    /**
     * Poll next URL from queue
     * @return head of queue
     */
    public String poll() throws IOException;

    /**
     * Provide a url to push to the queue
     * @param URL url to be pushed
     * @throws IOException
     */
    public void enqueue(String URL) throws IOException;

    /**
     * Returns number of remaining URLs in worker's frontier queue
     * @return size of queue
     */
    public int size() throws IOException;

    /**
     * Convenience method for checking empty queue
     * @return true if empty, false otherwise
     */
    public boolean isEmpty() throws IOException;

}
