package Frontier;

import java.io.IOException;

/**
 * Created by YagilB on 12/1/2016.
 */
public interface Frontier {

    public void init(String threadID);

    /*
     * Pop next URL from queue
     * Returns URL
     */
    public String poll() throws IOException;

    /*
     * Provide URL to push to queue
     */
    public void enqueue(String URL) throws IOException;

    /*
     * Returns number of remaining URLs in worker's frontier queue
     */
    public int size();

}
