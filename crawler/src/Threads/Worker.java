package Threads;

import Crawler.Messenger;
import Frontier.Frontier;
import HttpClient.HttpClient;
import Robots.*;
import URL.URLInfo;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.NodeList;
import org.w3c.tidy.Tidy;

import java.net.URL;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by igorpogorelskiy on 12/1/16.
 */
public class Worker extends Thread {
    private String id;
    private Frontier frontier;
    private Master master;
    private Messenger msgr;

    private static final String IS_WWW_REQUIRED = "#www-required";
    private static final String BAD = "http://null:0null";
    private static final Log log = LogFactory.getLog(Worker.class);

    /**
     * Set the id of the thread
     * @param id id generated for this thread
     */
    public void setID(String id) {
        this.id = id;
    }

    /**
     * Set the url frontier
     * @param frontier queue
     */
    public void setFrontier(Frontier frontier) {
        this.frontier = frontier;
    }

    /**
     * Set the master thread
     * @param master master to set
     */
    public void setMaster(Master master) {
        this.master = master;
    }

    /**
     * Messenger for talking to crawler set here
     * @param m msgr to set
     */
    public void setMessenger(Messenger m) {
        this.msgr = m;
    }

    /**
     * Getter for id
     * @return id of thread
     */
    public String getID() {
        return this.id;
    }

    /**
     * Used to let the worker know of his ID, the master, and the shared frontier queue
     * @param id id
     * @param frontier frontier queue of links
     * @param master master of the threads
     */
    public void initWorkerEssentials(String id, Frontier frontier, Master master) {
        setID(id);
        setFrontier(frontier);
        setMaster(master);
        setMessenger(msgr);
    }

    /**
     * Each worker thread
     */
    @Override
    public void run() {
        while (!this.frontier.isEmpty() && master.getCurrentNumDocumentsProcessed() != master.getMaxDocuments()) {
            String url = null;
            try {
                url = normalize(this.frontier.poll());
            } catch (IOException e) {
                log.error("Error with polling the queue... Continuing");
                continue;
            }
            if (!master.haveSeenUrl(url) && !url.equals(BAD)) {
                master.addSeenUrl(url);
                boolean isSecure = url.contains("https://");
                log.info("\n\nFetching information for URL: " + url);
                URLInfo normalizedInfo = new URLInfo(url);

                // look for robots.txt
                RobotsTxtInfo rInfo = new RobotsTxtInfo();
                if (master.robotsForUrl.containsKey(normalizedInfo.getHostName())) {
                    rInfo = master.robotsForUrl.get(normalizedInfo.getHostName());
                } else {
                    Robots robotParser = new Robots(url, normalizedInfo);
                    rInfo = robotParser.getRobotsTxt();
                    master.robotsForUrl.put(normalizedInfo.getHostName(), rInfo);
                    master.crawlTimes.put(normalizedInfo.getHostName(), new Date().getTime());
                }
                int crawlDelay = 0;
                if (rInfo != null && (rInfo.containsUserAgent("*") || rInfo.containsUserAgent(HttpClient.CIS455_CRAWLER))) {
                    if (rInfo.containsUserAgent(HttpClient.CIS455_CRAWLER)) {
                        ArrayList<String> allowed = rInfo.getAllowedLinks(HttpClient.CIS455_CRAWLER);
                        ArrayList<String> disallowed = rInfo.getDisallowedLinks(HttpClient.CIS455_CRAWLER);

                        if (rInfo.hasCrawlDelay(HttpClient.CIS455_CRAWLER)) {
                            crawlDelay = rInfo.getCrawlDelay(HttpClient.CIS455_CRAWLER);
                        }

                        if (restrictedPath(url, allowed, disallowed)) {
                            log.info(url + ": Restricted. Not downloading");
                            System.out.println(url + ": Restricted. Not downloading");
                            continue;
                        }
                    } else if (rInfo.containsUserAgent("*")) {
                        ArrayList<String> allowed = rInfo.getAllowedLinks("*");
                        ArrayList<String> disallowed = rInfo.getDisallowedLinks("*");
                        if (rInfo.hasCrawlDelay("*")) {
                            crawlDelay = rInfo.getCrawlDelay("*");
                        }

                        if (restrictedPath(url, allowed, disallowed)) {
                            log.info(url + ": Restricted. Not downloading");
                            System.out.println(url + ": Restricted. Not downloading");
                            continue;
                        }
                    }
                }

                // check crawl delay
                if (master.crawlTimes.containsKey(normalizedInfo.getHostName())) {
                    long lastCrawledAt = master.crawlTimes.get(normalizedInfo.getHostName());
                    if ((new Date().getTime() - lastCrawledAt) < crawlDelay * 1000) {
                        log.info("Experiencing crawl delay... ");
                        try {
                            Thread.sleep(crawlDelay * 1000);
                        } catch (InterruptedException e) {
                            log.error("Interrupted while waiting for crawl delay to finish " + e.getMessage());
                            continue;
                        }
                    }
                }

                HttpClient client = new HttpClient();
                Date dateLastAccessed = null;
                //TODO fix this
//                Document doc = db.retrieveDocument(url);
//                if (doc != null) {
//                    log.info(url + " was found in the database");
//                    dateLastAccessed = doc.getLastAccessedTime();
//                }
                if (client.execute("HEAD",
                        isSecure,
                        normalizedInfo.getFilePath(),
                        url,
                        normalizedInfo.getPortNo(),
                        normalizedInfo.getHostName(),
                        dateLastAccessed)) {
                    String statusCode = client.getProperty(HttpClient.RESPONSE_STATUS_CODE);
                    if (statusCode.equals("304")) {
                        log.info(url + ": Not modified");
                        continue;
                    }

                    // check redirected
                    String redirectedTo = client.getProperty(HttpClient.LOCATION);
                    if (redirectedTo != null) {
                        log.info("Redirecting " + url + " to " + redirectedTo);
                        if (redirectedTo.startsWith("http")) {
                            // absolute url
                            try {
                                this.frontier.enqueue(redirectedTo + IS_WWW_REQUIRED);
                            } catch (IOException e) {
                                log.error("Error in enqueuing a url " + e.getMessage());
                            }
                        } else {
                            try {
                                URL base = new URL(url);
                                String absolute = new URL(base, redirectedTo).toString();
                                try {
                                    this.frontier.enqueue(absolute + IS_WWW_REQUIRED);
                                } catch (IOException e) {
                                    log.error("Error in enqueuing a url " + e.getMessage());
                                }
                            } catch (MalformedURLException e) {
                                log.error("Could not create redirect url. Continuing... ");
                            }
                        }
                        continue;
                    }

                    String contentType = client.getProperty(HttpClient.CONTENT_TYPE);
                    String contentLength = client.getProperty(HttpClient.CONTENT_LENGTH);
                    if (contentLength == null) {
                        contentLength = "0";
                    }
                    log.info(url + " has content type " + contentType + " and content length of " + contentLength + " bytes");
                    if (contentType == null || !isCrawlableFile(contentType)) {
                        log.info(normalizedInfo.getFilePath() + " is not the correct MIME type to crawl. Continuing...");
                        continue;
                    }


                    try {
                        // TODO refactor into switching to other tasks from the queue instead of sleep
                        if (crawlDelay > 0) {
                            log.info("Waiting out crawl delay between HEAD and GET of " + normalizedInfo.getHostName());
                            Thread.sleep(crawlDelay * 1000);
                        }
                    } catch (InterruptedException e) {
                        log.error("Delay between HEAD and GET for page was interrupted... going to next page");
                        continue;
                    }

                    // we can now send our GET, phew
                    if (client.execute("GET",
                            isSecure,
                            normalizedInfo.getFilePath(),
                            url,
                            normalizedInfo.getPortNo(),
                            normalizedInfo.getHostName(),
                            dateLastAccessed)) {
                        if (isCrawlableFile(contentType)) {
                            Date now = new Date();
                            String docType = documentType(contentType);
                            String body = client.getProperty(HttpClient.RESPONSE_BODY);
                            master.crawlTimes.put(normalizedInfo.getHostName(), now.getTime());
                            // TODO store stuff here later
//                            db.storeDocument(url, body, now, contentType);
                            msgr.message(this.id, "Downloading: " + url);
                            master.increaseProcessedDocCount();
//                            log.info(url + ": Downloading");
                            if (docType.equals(HttpClient.HTML)) {
                                extractLinks(body, url);
                            }

                        }
                    }
                } else {
                    log.info("HEAD req to " + url + " was not successful. Continuing...");
                }
            }
        }
        master.terminateThread(this.id); // thread finished once we are here
    }

    /**
     * Determines if a file is crawlable
     * @param type type of file
     * @return true/false as you would assume
     */
    private boolean isCrawlableFile(String type) {
        return type.toLowerCase().contains("text/html") || type.toLowerCase().contains("text/xml")
                || type.toLowerCase().contains("application/xml") || type.toLowerCase().contains("+xml");
    }

    /**
     * Determines if the path is restricted
     * @param url url
     * @param allowed allowed list
     * @param disallowed disallowed list
     * @return true if able, false if unable to get link
     */
    private boolean restrictedPath(String url, ArrayList<String> allowed, ArrayList<String> disallowed) {
        if (disallowed == null) {
            return false;
        }
        for (String link : disallowed) {
            if ((link.endsWith("/") && url.contains(link) && !url.endsWith(link) && !allowedContains(url, allowed))
                    || (link.charAt(link.length() - 1) != '/' && url.endsWith(link))) {
                return true;
            }
        }
        return false;
    }

    /**
     * URL normalizer convenience method
     * @param url url to normalize
     * @return normalized url
     */
    private String normalize(String url) {
        boolean isSecure = url.startsWith("https://");
        StringBuilder normalizedURL = new StringBuilder();
        if (isSecure) {
            normalizedURL.append("https://");
        } else {
            normalizedURL.append("http://");
        }
        URLInfo urlInfo = new URLInfo(url);
        normalizedURL.append(urlInfo.getHostName() + ":");
        normalizedURL.append(urlInfo.getPortNo());
        normalizedURL.append(urlInfo.getFilePath());
        return normalizedURL.toString();
    }

    /**
     * Determine if we are allowed to hit this url based on the robots
     * @param link url desired to hit
     * @param allowed allowed list
     * @return true, if allowed to crawl link, false otherwise
     */
    private boolean allowedContains(String link, ArrayList<String> allowed) {
        if (allowed == null) {
            return false;
        }
        for (String s : allowed) {
            if (link.endsWith(s)) return true;
        }
        return false;
    }

    /**
     * Parse document type
     * @param contentType content type of input doc
     * @return doc type
     */
    private String documentType(String contentType) {
        if (contentType.toLowerCase().contains("text/html")) {
            return HttpClient.HTML;
        } else if (contentType.toLowerCase().contains("application/xml") || contentType.toLowerCase().contains("text/xml") || contentType.contains("+xml")) {
            return HttpClient.XML;
        }
        return HttpClient.UNRECOGNIZED_DOCTYPE;
    }

    /**
     * Link extraction module for crawler
     * @param html html to extract from
     * @param url url to create path from
     */
    private void extractLinks(String html, String url) {
        InputStream htmlStream = new ByteArrayInputStream(html.getBytes(StandardCharsets.UTF_8));
        Tidy tidy = new Tidy();
        tidy.setShowWarnings(false);
        tidy.setShowErrors(0);
        tidy.setQuiet(true);
        org.w3c.dom.Document doc = tidy.parseDOM(htmlStream, null);
        NodeList anchors = doc.getElementsByTagName("a");
        for (int i = 0; i < anchors.getLength(); i++) {
            org.w3c.dom.Node n = anchors.item(i);
            if (n.getAttributes() == null || n.getAttributes().getNamedItem("href") == null) {
                continue;
            }
            String link = n.getAttributes().getNamedItem("href").getNodeValue();
            if (link.startsWith("http")) {
                try {
                    // absolute link
                    this.frontier.enqueue(link);
                    log.info("Adding to queue: " + link);
                } catch (IOException e) {
                    log.error("Error with enqueuing a link " + e.getMessage());
                }

            } else {
                try {
                    URL base = new URL(url);
                    String absolute = new URL(base, link).toString();
                    log.info("Adding to queue: " + absolute);
                    this.frontier.enqueue(absolute);
                } catch (MalformedURLException e) {
                    log.error("Error turning a relative url into an absolute url");
                } catch (IOException e) {
                    log.error("Error with enqueueing a link " + e.getMessage());
                }
            }
        }
    }
}
