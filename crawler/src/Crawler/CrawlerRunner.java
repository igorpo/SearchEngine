package Crawler;

import Frontier.Frontier;
import Testing.LocalFrontier;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;

/**
 * Created by igorpogorelskiy on 12/1/16.
 */
public class CrawlerRunner {

    private static final Log log = LogFactory.getLog(CrawlerRunner.class);

    public static void main(String[] args) throws IOException {
        int maxDocuments = -1;
        int maxThreads = 8;

        if (args.length < 1) {
            log.info("Usage is <seed url> [maxThreads maxDocs]\nDefault is 8 threads and unlimited documents");
            return;
        }

        if (args.length == 2) {
            try {
                maxThreads = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                log.error("Malformed argument for number of threads: " + e.getMessage());
                return;
            }
        }

        if (args.length == 3) {
            try {
                maxDocuments = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                log.error("Malformed argument for number of documents: " + e.getMessage());
                return;
            }
        }

        if (args.length >= 4) {
            log.info("Usage is <seed url> [maxThreads maxDocs]\nDefault is 8 threads and unlimited documents");
            return;
        }
        String seed = args[0];

        log.info("Initializing the crawler with seed(s) " + seed + " with maximum number of " + maxThreads + " threads " +
                "and searching for " + ((maxDocuments != -1) ? maxDocuments : "unlimited") + " documents.");

        Frontier frontier = new LocalFrontier();
        Crawler crawler = new Crawler(maxThreads, maxDocuments, frontier);
        frontier.enqueue(seed);
    }
}
