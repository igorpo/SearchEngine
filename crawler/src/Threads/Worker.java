package Threads;

import Crawler.Messenger;
import Frontier.Frontier;
import HttpClient.HttpClient;
import URL.URLInfo;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.NodeList;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

/**
 * Created by igorpogorelskiy on 12/1/16.
 */
public class Worker extends Thread {
    private String id;
    private Frontier frontier;
    private Master master;
    private Messenger msgr;

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

        }
    }

    /**
     * Apply processing to the url that we are working on. This
     * includes extracting links, etc.
     * @param url url extracted from frontier
     */
    public void processLink(String url) {

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
