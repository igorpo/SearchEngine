package HttpClient;

/**
 * Created by igorpogorelskiy on 12/1/16.
 */
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import javax.net.ssl.HttpsURLConnection;

public class HttpClient {

    static final String NEWLINE = "\r\n";
    public static final String USER_AGENT = "user-agent";
    public static final String CIS455_CRAWLER = "cis455crawler";
    static final String REQUEST_METHOD = "request-method";
    public static final String CONTENT_TYPE = "content-type";
    public static final String CONTENT_LENGTH = "content-length";
    static final String CONNECTION = "connection";
    static final String CONN_TYPE = "close";
    public final String RESPONSE_STATUS_CODE = "response-code";
    public final String RESPONSE_BODY = "response-body";
    static final String IF_MOD = "if-modified-since";
    public final String LOCATION = "location";
    public static final String HOST = "host";
    public static final String HTML = "HTML";
    public static final String XML = "XML";
    public static final String UNRECOGNIZED_DOCTYPE = "N/A";
    static final int SECONDS_TIMEOUT = 5;
    private static final Log log = LogFactory.getLog(HttpClient.class);

    /**************************************************************************************
     * SENDER CODE
     **************************************************************************************/
    private class HttpSender {
        HashMap<String, ArrayList<String>> requestHeaders;


        protected HttpSender() {
            requestHeaders = new HashMap<String, ArrayList<String>>();
        }

        public void sendRequest(String method, String path, String url, String host, int port, Date ifModifiedSince) {
            log.info("Sending request for " + url);
            setRequestHeader(USER_AGENT, CIS455_CRAWLER);
            setRequestHeader(HOST, host);
            setRequestHeader(CONNECTION, CONN_TYPE);

            StringBuilder sb = new StringBuilder();
            sb.append(method + " ");
            sb.append(path + " ");
            sb.append("HTTP/1.0"); // avoid chunked
            sb.append(NEWLINE);

            if (ifModifiedSince != null) {
                setRequestHeader(IF_MOD, dateString(ifModifiedSince));
            }

            for (Map.Entry<String, ArrayList<String>> entry : requestHeaders.entrySet()) {
                String headerKey = entry.getKey();
                ArrayList<String> headerValues = entry.getValue();
                sb.append(headerKey + ": ");
                for (String value : headerValues) {
                    sb.append(value + ",");
                }
                sb.deleteCharAt(sb.length() - 1); // delete trailing delimiter
                sb.append(NEWLINE);
            }
            sb.append(NEWLINE);
            // may need to append a body at some point, but so far so good
            try {
                socket = new Socket(host, port);
                out = new PrintWriter(socket.getOutputStream());
                out.println(sb.toString());
                out.flush();
                input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            } catch (IOException e) {
                if (url.endsWith("/robots.txt")) {
                    // cannot find a robots.txt file
                    noRobotsTxtFileExists = true;
                    return;
                }
                couldNotOpenConnection = true;
                log.error("Could not open a connection to " + url);
                System.out.println("Could not open connection to host: " + host);
            }
        }

        public String getHeader(String key) {
            if (requestHeaders.containsKey(key)) {
                return requestHeaders.get(key).get(0);
            }
            return null;
        }

        public void setHeader(String header, String value) {
            if (requestHeaders.containsKey(header)) {
                ArrayList<String> list = requestHeaders.get(header);
                list.add(value);
            } else {
                ArrayList<String> list = new ArrayList<String>();
                list.add(value);
                requestHeaders.put(header, list);
            }
        }
    }

    /***************************************************************************************
     * RECEIVER CODE
     ***************************************************************************************/

    private class HttpReceiver {
        HashMap<String, ArrayList<String>> responseHeaders;

        protected HttpReceiver() {
            responseHeaders = new HashMap<String, ArrayList<String>>();
        }

        public boolean parseResponse(String url) {
            // response line
            try {
                if (noRobotsTxtFileExists || couldNotOpenConnection) {
                    return false;
                }

                String line = input.readLine().toLowerCase();
                String[] responseLine = line.split("\\s+", 3);

                log.info("URL = "+url+" and response line is " + line);

                if (responseLine.length != 3) {
                    log.info("response line length " + responseLine.length);
                    return false;
                }

                String status = responseLine[1];
                setProperty(RESPONSE_STATUS_CODE, status);
                String prevHeader = "";
                // parse headers
                while ((line = input.readLine().toLowerCase()) != null) {
                    if (line.trim().isEmpty()) {
                        break; // go to body parsing
                    }
                    if (Character.isWhitespace(line.charAt(0))) {
                        // multiliner
                        setResponseHeader(prevHeader, line);
                    } else {
                        String[] headerLine = line.split(":\\s*", 2);
                        setResponseHeader(headerLine[0], headerLine[1]);
                        prevHeader = headerLine[0];
                    }
                }
                setProperty(CONTENT_TYPE, getResponseHeader(CONTENT_TYPE));
                setProperty(CONTENT_LENGTH, getResponseHeader(CONTENT_LENGTH));
                if (status.startsWith("4") || status.startsWith("5")) {
                    return false;
                }
                if (status.equals("301") || status.equals("302") || status.equals("303")) {
                    setProperty(LOCATION, getResponseHeader(LOCATION)); // here we set the redirect
                }
                if (!getProperty(REQUEST_METHOD).equals("HEAD")) {
                    // parse body
                    StringBuilder body = new StringBuilder();
                    while ((line = input.readLine()) != null) {
                        body.append(line + "\n");
                    }
                    setProperty(RESPONSE_BODY, body.toString());
                }

            } catch (IOException e) {
                log.error("Problem with input reader: " + e.getMessage());
                return false;
            }
            return true;
        }

        public String getHeader(String key) {
            if (responseHeaders.containsKey(key)) {
                return responseHeaders.get(key).get(0);
            }
            return null;
        }

        public void setHeader(String header, String value) {
            if (responseHeaders.containsKey(header)) {
                ArrayList<String> list = responseHeaders.get(header);
                list.add(value);
            } else {
                ArrayList<String> list = new ArrayList<String>();
                list.add(value);
                responseHeaders.put(header, list);
            }
        }
    }

    /****************************************************************************************
     * CLIENT CODE
     ****************************************************************************************/

    private HttpSender httpSender;
    private HttpReceiver httpReceiver;
    private Socket socket = null;
    private PrintWriter out = null;
    private BufferedReader input = null;
    private HashMap<String, String> properties;
    private boolean noRobotsTxtFileExists = false;
    private boolean couldNotOpenConnection = false;

    public HttpClient() {
        httpSender = new HttpSender();
        httpReceiver = new HttpReceiver();
        properties = new HashMap<String, String>();
    }

    public boolean execute(String method, boolean isSecure, String path, String url, int port, String host, Date ifModifiedSince) {
        reset(); // clear old headers, properties, etc.
        log.info("URL is "+url+" and Status code is " + properties.get(RESPONSE_STATUS_CODE));
        setProperty(REQUEST_METHOD, method);
        URL httpsUrl;
        HttpsURLConnection connection = null;
        if (isSecure && method.equals("GET")) { // we can handle this directly
            log.info("GET " + url);
            try {
                httpsUrl = new URL(url);
                connection = (HttpsURLConnection) httpsUrl.openConnection();
                connection.setRequestMethod(method);
                connection.addRequestProperty(HOST, host);
                connection.addRequestProperty(USER_AGENT, CIS455_CRAWLER);
                connection.setRequestProperty(CONNECTION, CONN_TYPE);
                connection.setConnectTimeout(SECONDS_TIMEOUT * 1000);

                input = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                setProperty(RESPONSE_STATUS_CODE, String.valueOf(connection.getResponseCode()));
                setProperty(CONTENT_TYPE, connection.getContentType());

                // parse body
                StringBuilder sb = new StringBuilder();
                String next;
                while ((next = input.readLine()) != null) {
                    sb.append(next + "\n");
                }
                setProperty(RESPONSE_BODY, sb.toString());
                setProperty(CONTENT_LENGTH, String.valueOf(connection.getContentLength()));
                return true;
            } catch (MalformedURLException e) {
                log.error(url + " is malformed: " + e.getMessage());
                return false;
            } catch (IOException e) {
                log.error("Could not connect to " + url + ": " + e.getMessage());
                return false;
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        } else if (isSecure && method.equals("HEAD")) {
            log.info("HEAD " + url);
            try {
                httpsUrl = new URL(url);
                connection = (HttpsURLConnection) httpsUrl.openConnection();
                connection.setRequestMethod(method);
                connection.addRequestProperty(USER_AGENT, CIS455_CRAWLER);
                connection.addRequestProperty(HOST, host);
                connection.setRequestProperty(CONNECTION, CONN_TYPE);
                connection.setConnectTimeout(SECONDS_TIMEOUT * 1000);
                if (ifModifiedSince != null) {
                    connection.addRequestProperty(IF_MOD, dateString(ifModifiedSince));
                }
                String status = String.valueOf(connection.getResponseCode());
                setProperty(RESPONSE_STATUS_CODE, status);
                if (status.equals("301") || status.equals("302") || status.equals("303")) {
                    setProperty(LOCATION, connection.getHeaderField(LOCATION)); // here we set the redirect
                }
                setProperty(RESPONSE_STATUS_CODE, status);
                setProperty(CONTENT_TYPE, connection.getContentType());
                setProperty(CONTENT_LENGTH, String.valueOf(connection.getContentLength()));
                return true;
            } catch (MalformedURLException e) {
                log.error(url + " is malformed: " + e.getMessage());
                return false;
            } catch (IOException e) {
                log.error("Could not connect to " + url + ": " + e.getMessage());
                return false;
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }

        httpSender.sendRequest(method, path, url, host, port, ifModifiedSince);

        boolean success = httpReceiver.parseResponse(host);
        closeResources();
        return success;
    }

    private void setProperty(String prop, String val) {
        properties.put(prop, val);
    }

    public String getProperty(String prop) {
        return properties.get(prop);
    }

    public void setRequestHeader(String key, String val) {
        httpSender.setHeader(key, val);
    }

    public void setResponseHeader(String key, String val) {
        httpReceiver.setHeader(key, val);
    }

    public String getRequestHeader(String key) {
        return httpSender.getHeader(key);
    }

    public String getResponseHeader(String key) {
        return httpReceiver.getHeader(key);
    }

    public String dateString(Date date) {
        SimpleDateFormat d = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        d.setTimeZone(TimeZone.getTimeZone("GMT"));
        String dateString = d.format(date.getTime());
        return dateString;
    }

    private void closeResources() {
        try {
            if (input != null) input.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            log.error("Could not close properly " + e.getMessage());
        }
    }

    private void reset() {
        input = null;
        out = null;
        socket = null;
        properties.clear();
        httpSender.requestHeaders.clear();
        httpReceiver.responseHeaders.clear();
    }

}
