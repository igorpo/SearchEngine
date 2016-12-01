package Testing;

import Frontier.Frontier;

/**
 * Created by YagilB on 01/12/2016.
 */
public class LocalFrontier implements Frontier {

    // TODO: @igor

    @Override
    public synchronized String pop(String threadID) {
        return null;
    }

    @Override
    public synchronized void push(String URL, String threadID) {

    }

    @Override
    public synchronized int size(String threadID) {
        return 0;
    }
}
