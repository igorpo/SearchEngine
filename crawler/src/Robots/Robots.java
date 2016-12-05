package Robots;

import HttpClient.HttpClient;
import URL.URLInfo;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Created by igorpogorelskiy on 12/1/16.
 */
public class Robots {
    private static final Log log = LogFactory.getLog(Robots.class);
    private String url;
    private URLInfo urlInfo;

    public Robots(String url, URLInfo urlInfo) {
        this.url = url;
        this.urlInfo = urlInfo;
    }

    /**
     * Populate an object with information about robots.txt attributes
     * @return RobotsTxtInfo object or null if could not be created/file didnt exist
     */
    public RobotsTxtInfo getRobotsTxt() {
        RobotsTxtInfo robots = new RobotsTxtInfo();
        HttpClient client = new HttpClient();
        boolean isSecure = url.startsWith("https://");
        client.setRequestHeader(HttpClient.HOST, urlInfo.getHostName());
        StringBuilder robotsurl = new StringBuilder();
        if (isSecure) {
            robotsurl.append("https://");
        } else {
            robotsurl.append("http://");
        }
        robotsurl.append(urlInfo.getHostName() + ":");
        robotsurl.append(urlInfo.getPortNo());
        robotsurl.append("/robots.txt");
        if (client.execute("GET", isSecure, "/robots.txt", robotsurl.toString(), urlInfo.getPortNo(), urlInfo.getHostName(), null)) {
            log.info("Found robots.txt file of " + urlInfo.getHostName());
            String robotsdottxt = client.getProperty(client.RESPONSE_BODY);
            parse(robots, robotsdottxt);
        } else {
            log.info("Could not find robots.txt file of " + urlInfo.getHostName());
        }
        return robots;
    }

    /**
     * Parse the robots.txt file if it was found. Assumes correct formatting of robots.txt.
     * @param robotsdottxt body message of file to parse
     */
    private void parse(RobotsTxtInfo rInfo, String robotsdottxt) {
        // split on whitespace to get blocks of text
        String[] blocks = robotsdottxt.split("\\r?\\n\\s+");
        String currentAgent = "";
        for (String block : blocks) {
            String[] lines = block.split("\\r?\\n");
            for (String line : lines) {
                line = stripComment(line).trim();
                if (!line.isEmpty()) {
                    String[] directive = line.split(":\\s*", 2);
                    if (directive[0].equalsIgnoreCase("User-agent")) {
                        currentAgent = directive[1];
                        rInfo.addUserAgent(currentAgent);
                    } else if (directive[0].equalsIgnoreCase("Disallow")) {
                        rInfo.addDisallowedLink(currentAgent, directive[1]);
                    } else if (directive[0].equalsIgnoreCase("Allow")) {
                        rInfo.addAllowedLink(currentAgent, directive[1]);
                    } else if (directive[0].equalsIgnoreCase("Crawl-delay")) {
                        rInfo.addCrawlDelay(currentAgent, Integer.parseInt(directive[1]));
                    } else if (directive[0].equalsIgnoreCase("Sitemap")) {
                        rInfo.addSitemapLink(directive[1]);
                    }
                }

            }
        }
    }

    /**
     * Strip a line of its comments in robots.txt
     * @param line line to check
     * @return stripped line
     */
    private String stripComment(String line) {
        int hashIdx = line.indexOf('#');
        if (hashIdx < 0) {
            return line;
        }
        return line.substring(0, hashIdx);
    }
}
