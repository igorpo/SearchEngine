package testing;

import crawler.Crawler;
import crawler.CrawlerRunner;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;

/**
 * Created by YagilB on 16/12/2016.
 */
public class CrawlerTester {
    private static final Log log = LogFactory.getLog(CrawlerRunner.class);

    public static void main(String[] args) throws IOException {
        int maxDocuments = 300000;

//        if (args.length == 1) {
//            try {
//                maxThreads = Integer.parseInt(args[1]);
//            } catch (NumberFormatException e) {
//                log.error("Malformed argument for number of threads: " + e.getMessage());
//                return;
//            }
//        }

        int maxThreads = -1;
        int nodeIndex = -1;

        if (args.length == 2) {
            try {
                nodeIndex = Integer.parseInt(args[0]);
                maxThreads = Integer.parseInt(args[1]);
                log.info("Starting node index " + nodeIndex + " max thrads = " + maxThreads);
            } catch (NumberFormatException e) {
                log.error("Malformed argument for number of documents: " + e.getMessage());
                return;
            }
        }

        if (args.length >= 3) {
            log.info("Usage is [maxThreads maxDocs]\nDefault is 8 threads and unlimited documents");
            return;
        }

        log.info("Initializing the crawler with whitelisted seed(s) with maximum number of " + maxThreads + " threads " +
                "and searching for " + ((maxDocuments != -1) ? maxDocuments : "unlimited") + " documents.");

        Crawler crawler = new Crawler(maxThreads, maxDocuments, nodeIndex);
    }
}
