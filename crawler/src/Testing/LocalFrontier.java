package Testing;

import Frontier.Frontier;

/**
 * Created by igorpogorelskiy on 12/1/16.
 */
public class LocalFrontier implements Frontier {

    @Override
    public void init(String threadID) {

    }

    @Override
    public synchronized String poll() {
        return null;
    }

    @Override
    public synchronized void enqueue(String URL) {

    }

    @Override
    public synchronized int size() {
        return 0;

    }

    @Override
    public synchronized boolean isEmpty() {
        return size() == 0;
    }
}
