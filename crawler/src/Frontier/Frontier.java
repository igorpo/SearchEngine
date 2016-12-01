package Frontier;

/**
 * Created by YagilB on 01/12/2016.
 */
public interface Frontier {

    /*
     * Provide worker thread ID to pop next URL from queue
     * Returns URL
     */
    public String pop(String threadID);

    /*
     * Provide worker thread ID and URL to push to queue
     */
    public void push(String URL, String threadID);

    /*
     * Provide worker thread ID to get size of queue
     */
    public int size(String threadID);

}
