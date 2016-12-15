package threads;

import crawler.Messenger;
import databases.DynamoWrapper;
import databases.S3Wrapper;
import filelogger.FileLogger;
import frontier.Frontier;
import frontier.FrontierWrapper;
import httpClient.HttpClient;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import remote.frontierServer.SyncMultQueue;
import robots.Robots;
import robots.RobotsTxtInfo;
import url.URLInfo;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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
    private static final int MAX_TRIES = 15;
    private static final int WAIT_TIME = 5;
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
     * @param master master of the threads
     */
    public void initWorkerEssentials(String id, Master master, Messenger msgr) throws IOException {
        setID(id);
        Frontier frontier = new FrontierWrapper();
        frontier.init(getID());
        log.info("INIT FRONTIER WITH ID == " + getID());
        switch (master.nodeIndex) {
            case 0:
                frontier.enqueue(seeds0[Integer.parseInt(getID())]);
                break;
            case 1:
                frontier.enqueue(seeds1[Integer.parseInt(getID())]);
                break;
            case 2:
                frontier.enqueue(seeds2[Integer.parseInt(getID())]);
                break;
            default:
                log.error("No node index given, exiting...");
                System.exit(-1);
        }

        setFrontier(frontier);
        setMaster(master);
        setMessenger(msgr);

    }

    /**
     * Each worker thread
     */
    @Override
    public void run() {

        try {
            int docsSoFar;
            while ((docsSoFar = master.getCurrentNumDocumentsProcessed()) <= master.getMaxDocuments()) {
                if (docsSoFar % 100 == 0)
                    FileLogger.info("Number of documents processed so far: " + docsSoFar);

                // Should I sleep, or keep going
                boolean isEmpty;
                try {
                    isEmpty = this.frontier.isEmpty();
                } catch (IOException e) {
                    Thread.sleep(15000);
                    log.info("Thread " + getID() + " is sleeping because: " + e.getMessage());
                    continue;
                }

                if (isEmpty) {
                    int trials = 0;

                    while (true) {
//                        log.info("Checking if queue is empty for workerID " + this.getID());
                        try {
                            isEmpty = this.frontier.isEmpty();
                        } catch (IOException e) {
                            Thread.sleep(10000);
                            log.info("Thread " + getID() + " is sleeping because: " + e.getMessage());
                            continue;
                        }
                        if (!isEmpty) {
                            break;
                        }
                        if (trials == MAX_TRIES) {
                            master.terminateThread(this.getID());
                            return;
                        }
                        trials++;
                        Thread.sleep(WAIT_TIME * 1000);
                    }
                }

                // Keep going
                String url = null;
                try {
                    String url_not_normalized = this.frontier.poll();
                    url = normalize(url_not_normalized);

                   // log.info("Polled and got URL " + url_not_normalized + " normalized to " + url);
                } catch (IOException e) {
                    log.error("Error with polling the queue... Continuing");
                    continue;
                }

                if (master.haveSeenUrl(url)/* || url.equals(BAD)*/) {
                    log.error("url " + url + " was seen");
                }

                if (!master.haveSeenUrl(url) && !url.equals(BAD)) {

                    boolean isSecure = url.contains("https://");
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
                                msgr.message(this.id, "Restricted: " + url + ". Not downloading");
                                continue;
                            }
                        } else if (rInfo.containsUserAgent("*")) {
                            ArrayList<String> allowed = rInfo.getAllowedLinks("*");
                            ArrayList<String> disallowed = rInfo.getDisallowedLinks("*");
                            if (rInfo.hasCrawlDelay("*")) {
                                crawlDelay = rInfo.getCrawlDelay("*");
                            }

                            if (restrictedPath(url, allowed, disallowed)) {
                                msgr.message(this.id, "Restricted: " + url + ". Not downloading");
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

                        String statusCode = client.getProperty(client.RESPONSE_STATUS_CODE);

                        if (statusCode.equals("304")) {
                            log.info(url + ": Not modified");
                            continue;
                        }

                        // check redirected
                        String redirectedTo = client.getProperty(client.LOCATION);

                        if (redirectedTo != null) {
                         //   log.info("Redirecting " + url + " to " + redirectedTo + " bc statusCode was " + statusCode);
                            if (redirectedTo.startsWith("http")) {
                                // absolute url
                                try {
                                    this.frontier.enqueue(redirectedTo /*+ IS_WWW_REQUIRED*/);
                                } catch (IOException e) {
                                    log.error("Error in enqueuing a url " + e.getMessage());
                                }
                            } else {
                                try {
                                    URL base = new URL(url);
                                    String absolute = new URL(base, redirectedTo).toString();
                                    try {
                                        this.frontier.enqueue(absolute /*+ IS_WWW_REQUIRED*/);
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
                            this.master.addSeenUrl(url);
                            if (isCrawlableFile(contentType)) {
                         //       log.info("url " + url + " is crawlable file");
                                Date now = new Date();
                                String docType = documentType(contentType);
                                String body = client.getProperty(client.RESPONSE_BODY);
                                master.crawlTimes.put(normalizedInfo.getHostName(), now.getTime());
                                // TODO store stuff here later
                                // TODO save thread id + hash in metadata (S3Wrapper)
                                // important bc frontiers siloed by worker

                                Map<String, String> metadata = new HashMap<>();

                                metadata.put("threadID", getID());
                                metadata.put("now", String.valueOf(now));
                                metadata.put("contentType", contentType);
                                metadata.put("url", url);

                                S3Wrapper.addDocument(url, body, metadata);

                                if (msgr ==  null)
                                    log.error("MSGR IS NULL");

                                msgr.message(this.id, "Downloading: " + url);
                                master.increaseProcessedDocCount();
    //                            log.info(url + ": Downloading");
                                if (docType.equals(HttpClient.HTML)) {
                              //      log.info("EXTRACTING LINKS FOR URL == " + url);
                                    try {
                                        extractLinks(body, url);
                                    } catch (IOException e) {
                                        log.error("Worker: Error with enqueuing a link " + e.getMessage());
                                    }
                                }
                            }
                        }
                    } else {
                        log.info("HEAD req to " + url + " was not successful. Continuing...");
                    }
                }
            }
//       } catch (IOException e) {
//            log.error("Could not reach remote queue... terminating thread " + e.getMessage());
//            e.printStackTrace();

        } catch (InterruptedException e) {
            log.error("Could not sleep... terminating thread " + e.getMessage());
            e.printStackTrace();
        }
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
            if (link.length() == 0) continue;
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
    private void extractLinks(String html, String url) throws IOException {
//        InputStream htmlStream = new ByteArrayInputStream(html.getBytes(StandardCharsets.UTF_8));
//        Tidy tidy = new Tidy();
//        tidy.setShowWarnings(false);
//        tidy.setShowErrors(0);
//        tidy.setQuiet(true);
//        org.w3c.dom.Document doc = tidy.parseDOM(htmlStream, null);
//        NodeList anchors = doc.getElementsByTagName("a");

        Set<String> anchors = extractLinks(html);

//        log.info("Size of anchors is " + anchors.size());
//        System.exit(0);
        if (this.frontier.size() == SyncMultQueue.MAX_QUEUE_SIZE) {
            throw new IOException("Worker id: "+ this.getID() +" Queue is full. Not saving links from URL = " + url);
        }

        Set<String> outgoingLinks = new HashSet<>();
        for (String link : anchors) {
//            org.w3c.dom.Node n = anchors.get(i);
//            if (n.getAttributes() == null || n.getAttributes().getNamedItem("href") == null) {
//                continue;
//            }

//            String link = n.getAttributes().getNamedItem("href").getNodeValue();
            if (link.startsWith("http")) {
                // absolute link
                handleLink(outgoingLinks, link);

            } else {
                try {
                    URL base = new URL(url);
                    String absolute = new URL(base, link).toString();

                    handleLink(outgoingLinks, absolute);
                } catch (MalformedURLException e) {
                    log.error("Error turning a relative url into an absolute url");
                }
            }
        }

        // Save outgoing links to Dynamo and Frontier
        batchSaveLinks(outgoingLinks, url);
    }

    private void batchSaveLinks(Set<String> links, String url) throws IOException {
        List<String> linksList = links.stream().collect(Collectors.toList());
        DynamoWrapper.storeURLOutgoingLinks(url, linksList);
        this.frontier.enqueue(linksList);
    }

    private Set<String> extractLinks(String rawHtml) {
        Set<String> links = new HashSet<>();
        String pattern = "<a.[^<]*href=[\"|\'](\\S+)[\"|\'].[^<]*</a>";
        Pattern r = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
        Matcher m = r.matcher(rawHtml);

        while (m.find()) {
            links.add(m.group(1));
        }
        return links;
    }

    private void handleLink(Set<String> outgoingLinks, String url) throws IOException {
        outgoingLinks.add(url);
    }

    String[] seeds0 = {"https://www.google.com/",
            "https://www.facebook.com/",
            "https://twitter.com/",
            "https://www.youtube.com/",
            "https://wordpress.org/",
            "https://www.linkedin.com/",
            "https://www.instagram.com/",
            "https://www.pinterest.com/",
            "https://www.skyscanner.com",
            "http://www.adobe.com/",
            "https://www.blogger.com",
            "http://www.rollingstones.com/",
            "https://wordpress.com/",
            "http://www.apple.com/",
            "https://www.tumblr.com/",
            "https://www.amazon.com/",
            "https://vimeo.com/",
            "https://www.yahoo.com/",
            "https://www.microsoft.com/",
            "http://www.nytimes.com/",
            "http://www.bbc.com",
            "https://soundcloud.com/",
            "http://www.stumbleupon.com/",
            "http://www.ticketmaster.com/",
            "https://github.com/",
            "https://www.google.com/flights/",
            "http://www.imdb.com/",
            "http://www.foodnetwork.com/",
            "https://www.nih.gov/",
            "http://www.forbes.com/",
            "https://www.yelp.com/",
            "http://www.wsj.com/",
            "http://www.slideshare.net/",
            "https://www.etsy.com/",
            "http://www.ebay.com/",
            "http://www.about.com/",
            "http://www.aol.com/",
            "https://www.eventbrite.com/",
            "https://archive.org/",
            "http://www.reuters.com/",
            "http://www.telegraph.co.uk/",
            "http://www.usatoday.com/",
            "https://www.wikimedia.org/",
            "http://www.bloomberg.com/",
            "http://www.cdc.gov/",
            "http://time.com/",
            "https://www.meetup.com/",
            "http://www.latimes.com/",
            "http://www.harvard.edu/",
            "http://www.npr.org/",
            "https://www.tripadvisor.com/",
            "http://bandcamp.com/",
            "https://foursquare.com/",
            "http://web.mit.edu/",
            "https://www.wired.com/",
            "https://www.nasa.gov/",
            "http://www.economist.com/",
            "https://www.kickstarter.com/",
            "http://www.upenn.edu/"};

    String[] seeds1 = {"http://www.cnn.com/",
            "http://www.nytimes.com/",
            "https://www.theguardian.com/",
            "http://www.huffingtonpost.com/",
            "https://www.yahoo.com/news/",
            "http://www.forbes.com/",
            "http://www.foxnews.com/",
            "www.bbc.co.uk",
            "https://weather.com/",
            "https://news.google.com/",
            "https://www.bloomberg.com",
            "http://www.wsj.com/",
            "www.usatoday.com",
            "http://time.com/",
            "http://www.cbsnews.com/",
            "http://www.nbcnews.com/",
            "https://www.netflix.com",
            "https://www.theatlantic.com/",
            "https://en.wikipedia.org/wiki/Main_Page",
            "http://fortune.com/",
            "https://news.ycombinator.com/",
            "http://www.theverge.com/",
            "http://www.recode.net/",
            "http://www.vox.com/",
            "http://money.cnn.com/",
            "http://genius.com/",
            "http://www.investopedia.com/",
            "http://stackoverflow.com/",
            "https://www.hbonow.com/",
            "http://www.politico.com/",
            "http://fivethirtyeight.com/",
            "http://www.visitphilly.com/",
            "http://www.sas.upenn.edu/frd/faculty/index",
            "https://www.khanacademy.org/",
            "https://vimeo.com/",
            "http://www.zillow.com/",
            "https://www.redfin.com/",
            "https://www.glassdoor.com/index.htm",
            "https://www.etsy.com/",
            "http://www.epicurious.com/",
            "http://www.azworldairports.com/indexes/p-apnma.cfm",
            "https://www.priceline.com/",
            "https://www.kayak.com/",
            "http://www.nasdaq.com/",
            "https://www.nyse.com/index",
            "http://www.pandora.com/",
            "http://www.hulu.com/",
            "http://www.alexa.com/",
            "https://www.dropbox.com/",
            "https://snapchat.com/",
            "http://www.indeed.com/",
            "http://www.craigslist.org/about/sites",
            "https://www.gilt.com/",
            "http://www.refinery29.com/",
            "https://www.walmart.com/",
            "http://us.asos.com/",
            "http://www.hm.com/",
            "http://www.zappos.com/",
            "http://www.espn.com/"};

    String[] seeds2 = {"http://www.booking.com/",
            "https://www.reddit.com/",
            "http://www.businessinsider.com/",
            "https://www.goodreads.com/",
            "http://mashable.com/",
            "http://www.nationalgeographic.com/",
            "http://www.cbsnews.com/",
            "https://www.whitehouse.gov/",
            "https://www.spotify.com/us/",
            "https://medium.com/",
            "https://techcrunch.com/",
            "https://www.buzzfeed.com/",
            "https://www.ahd.com/",
            "http://www.webmd.com/",
            "https://www.trustpilot.com/",
            "http://www.nature.com/",
            "https://www.usa.gov/",
            "http://www.clas.ufl.edu/au/",
            "https://www.nhl.com/",
            "http://www.eurosport.com/",
            "https://www.quora.com/",
            "http://stackexchange.com/",
            "https://www.bankofamerica.com/",
            "https://www.rottentomatoes.com/",
            "http://www.ikea.com/us/en/",
            "http://www.fandango.com/movie-reviews",
            "https://www.wolframalpha.com/",
            "https://itunes.apple.com/us/genre/ios",
            "https://www.eventbrite.com/",
            "https://news.ycombinator.com/best",
            "https://www.producthunt.com/",
            "http://www.memes.com/",
            "http://knowyourmeme.com/",
            "https://www.merriam-webster.com/",
            "http://www.metmuseum.org/",
            "http://www.lonelyplanet.com/",
            "http://www.louvre.fr/en",
            "http://www.whitepages.com/",
            "http://www.wsop.com/",
            "https://www.collegeboard.org/",
            "http://www.nationalgeographic.com/",
            "https://www.scientificamerican.com",
            "http://www.apartments.com/",
            "https://www.hotels.com/",
            "https://www.twitch.tv/",
            "https://theintercept.com/",
            "https://play.google.com/store",
            "https://www.python.org/",
            "https://developer.apple.com/reference/",
            "https://developers.google.com/",
            "https://docs.oracle.com/javase/7/docs/api/",
            "http://devdocs.io/",
            "https://www.kernel.org/doc/man-pages/",
            "https://www.gutenberg.org/",
            "https://linux.die.net/man/",
            "https://www.apache.org/",
            "https://www.grubhub.com",
            "http://www.ted.com/",
            "http://mlb.mlb.com/home"};
}
