package Remote;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Created by YagilB on 03/12/2016.
 */
public class SyncMultQueue {

    private static final int QUEUE_SIZE = 1000;
    private static final Map<String, BlockingQueue<String>> subqueues = new HashMap<>();

    public static String poll(String threadID) {
        BlockingQueue<String> queue = subqueues.get(threadID);
        if (queue == null) {
            queue = new ArrayBlockingQueue(QUEUE_SIZE);
            subqueues.put(threadID, queue);
        }
        return queue.poll();
    }

    public static boolean enqueue(String threadID, String obj) {
        BlockingQueue<String> queue = subqueues.get(threadID);
        if (queue == null) {
            System.out.println("THREADID == " + threadID + " and queue doesn't exist");
            queue = new ArrayBlockingQueue(QUEUE_SIZE);
            subqueues.put(threadID, queue);
        }
        return queue.add(obj);
    }

    public static int size(String threadID) {
        System.out.println("Calling /size for threadID " + threadID);

        BlockingQueue<String> queue = subqueues.get(threadID);

        if (queue == null) {
            queue = new ArrayBlockingQueue(QUEUE_SIZE);
            subqueues.put(threadID, queue);
        }

        System.out.println("Size for threadID " + threadID + " is " + queue.size());

        return queue.size();
    }


}
