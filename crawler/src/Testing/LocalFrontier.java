package testing;

import frontier.Frontier;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Created by igorpogorelskiy on 12/1/16.
 */
public class LocalFrontier implements Frontier {

    Queue<String> q;

    public LocalFrontier(String seed) {
        q = new LinkedList<>();
        q.add(seed);
    }

    @Override
    public void init(String threadID) {}

    @Override
    public synchronized String poll() {
        return q.poll();
    }

    @Override
    public synchronized void enqueue(String URL) {
        q.add(URL);
    }

    @Override
    public void enqueue(List<String> linksList) throws IOException {

    }

    @Override
    public synchronized int size() {
        return q.size();

    }

    @Override
    public synchronized boolean isEmpty() {
        return size() == 0;
    }
}
