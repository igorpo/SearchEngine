package remote.frontierServer;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by YagilB on 03/12/2016.
 */
public class SyncMultQueue {

    public static final int MAX_QUEUE_SIZE = (int) Math.pow(2,12);//(int) Math.pow(2,15);
    public static final double MAX_FILE_SIZE = Math.pow(2,30) * 1.5;
    private static final int QUEUE_SIZE = MAX_QUEUE_SIZE;// Integer.MAX_VALUE;
    private static final String QUEUES_PATH = System.getProperty("user.dir")+ "/queues/";
    private static Map<String, BlockingQueue<String>> subqueues = new HashMap<>();

    // Dump to file related
    private static ConcurrentHashMap<String, Boolean> queueFull = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<String, Integer> queueLastLine = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<String, Integer> queueCurrentFile = new ConcurrentHashMap<>();

    private static BufferedWriter d = null;
    private static String NL = "\n";

    // Methods
    public static String poll(String threadID) {
        BlockingQueue<String> queue = subqueues.get(threadID);
        if (queue == null) {
            queue = new ArrayBlockingQueue(QUEUE_SIZE);
            queueFull.put(threadID, false);
            subqueues.put(threadID, queue);
        }
        return queue.poll();
    }

    public static boolean enqueue(String threadID, List<String> links) {
        BlockingQueue<String> queue = subqueues.get(threadID);

        if (queue == null) {
            System.out.println("THREADID == " + threadID + " and queue doesn't exist");
            queue = new ArrayBlockingQueue(QUEUE_SIZE);
            queueFull.put(threadID, false);
            subqueues.put(threadID, queue);
        }

        if (queue.size() + links.size() >= MAX_QUEUE_SIZE - 1) {
            queueFull.put(threadID, true);
            System.out.println("QUEUE FULL FOR THREADID == " + threadID + ". Writing to disk.");
        }

        return addAll(queue, links, threadID);
    }

    public static boolean enqueue(String threadID, String obj) {
        BlockingQueue<String> queue = subqueues.get(threadID);
        if (queue == null) {
            System.out.println("THREADID == " + threadID + " and queue doesn't exist");
            queue = new ArrayBlockingQueue(QUEUE_SIZE);
            queueFull.put(threadID, false);
            subqueues.put(threadID, queue);
        }

        List<String> links = new ArrayList<>();
        links.add(obj);

        if (queue.size() + links.size() >= MAX_QUEUE_SIZE - 1) {
            queueFull.put(threadID, true);
            System.out.println("QUEUE FULL FOR THREADID == " + threadID + ". Writing to disk.");
        }



        return addAll(queue, links, threadID);
    }

    public static int size(String threadID) {
//        System.out.println("Calling /size for threadID " + threadID);

        BlockingQueue<String> queue = subqueues.get(threadID);

        if (queue == null) {
            queue = new ArrayBlockingQueue(QUEUE_SIZE);
            queueFull.put(threadID, false);
            subqueues.put(threadID, queue);
        }

        System.out.println("************************************ Size for threadID #[" + threadID + "] is " + queue.size());

        if (queue.size() == 0) {
            fillQueueFromFile(threadID);
        }

        return queue.size();
    }

    private static boolean addAll(BlockingQueue<String> queue, List<String> links, String threadID) {
        boolean ret;
        if (queueFull.get(threadID)) {
            ret = dumpToFile(threadID, links);
        } else {
            ret = queue.addAll(links);
        }
        return ret;
    }

    private static int getCurrentFile(String threadID) {
        int curFile = 0;
        if (queueCurrentFile.get(threadID) != null) {
            curFile = queueCurrentFile.get(threadID);
        } else {
            queueCurrentFile.put(threadID, 0);
        }
        return curFile;
    }

    private static int incrementFileCount(String threadID) {
        int curFile = queueCurrentFile.get(threadID);
        queueCurrentFile.put(threadID, curFile+1);
        return curFile+1;
    }

    private static boolean dumpToFile(String threadID, List<String> links) {
        int curFile = getCurrentFile(threadID);
        String path = QUEUES_PATH + threadID +  "_" + curFile + ".queue";

        boolean success = false;
        d = null;

        try {
            // Check size of file. If exceeds 1.5GB, create new file
            // Assuming 2GB is max file size
            File queueFile = new File(path);
            if (queueFile.exists() && queueFile.length() >= MAX_FILE_SIZE) {
                curFile = incrementFileCount(threadID);
                path = QUEUES_PATH + threadID +  "_" + curFile + ".queue";
                System.out.println("QUEUE FILE SIZE FOR THREAD ID " + threadID + " EXCEEDED SIZE. CREATING NEW ONE");
            }

            d = new BufferedWriter(new FileWriter(path, true));
            for (String link : links) {
                d.write(link);
                d.write(NL);
            }
            d.flush();
            success = true;
            System.out.println("THREAD ID: "+threadID+" dumped " + links.size() + " links to file");
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return success;
    }

    private static void fillQueueFromFile(String threadID) {
        int curFile = getCurrentFile(threadID);
        String path = QUEUES_PATH + threadID +  "_" + curFile + ".queue";

        // Check if file exists
        if (!new File(path).exists())
            return;

        // Get and init queue
        BlockingQueue<String> queue = subqueues.get(threadID);
        if (queue == null) {
            queue = new ArrayBlockingQueue(QUEUE_SIZE);
            subqueues.put(threadID, queue);
            queueFull.put(threadID, false);
        }

        // Read from file, fill queue
        BufferedReader br;
        try {
            br = new BufferedReader(new FileReader(path));
            String currentLine;

            // Check if there's a start line
            int startLine = 0;
            if (queueLastLine.get(threadID) != null) {
                startLine = queueLastLine.get(threadID);
                for (int i = 0; i < startLine; i++)
                    br.readLine();
            }

            int count = 0;
            while ((currentLine = br.readLine()) != null && count < MAX_QUEUE_SIZE) {
                queue.add(currentLine);
                count++;
            }
            br.close();

            if (count < MAX_QUEUE_SIZE) {
                // Flag queue as not full
                queueFull.put(threadID, false);

                // Remove the file
                File f = new File(path);
                f.delete();
            } else {
                queueLastLine.put(threadID, count);
            }


            System.out.println("THREAD ID: "+threadID+" read " + queue.size() + " links from file");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
