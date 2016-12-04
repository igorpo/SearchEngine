package Frontier;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Created by YagilB on 01/12/2016.
 */
public class FrontierWrapper implements Frontier {

    // AWS
    private static String remoteAddr = "http://0.0.0.0:4567"; // the EC2 instance address
    private static String secret = "vix0jw1H6Vi/wBRAB9QDGHxFPOJtUm9e2r9GuANmjrY=";

    private String threadID = "";

    // Provide threadID
    public void init(String threadID) {
        this.threadID = threadID;
    }

    @Override
    public String poll() throws RuntimeException, IOException {
        HttpURLConnection conn = sendReq("poll",null, "GET");

        if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
            throw new RuntimeException("Attempt to poll for threadID =  " +threadID + " failed. Server said: " + conn.getResponseCode());
        }

        StringBuffer stringBuffer = new StringBuffer();
        BufferedReader in = new BufferedReader(new InputStreamReader(
                conn.getInputStream()));
        String inputLine;
        while ((inputLine = in.readLine()) != null)
            stringBuffer.append(inputLine);
        in.close();

        return stringBuffer.toString();
    }

    @Override
    public int size() throws IOException {
        HttpURLConnection conn = sendReq("size",null, "GET");
        System.out.println("FrontierWrapper: Getting size ID == "+this.threadID+". Response code == " + conn.getResponseCode());
        if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
            throw new RuntimeException("Attempt to get size of threadID " + threadID + " failed. Server said: " + conn.getResponseCode());
        }

        BufferedReader in = new BufferedReader(new InputStreamReader(
                conn.getInputStream()));

        String inputLine;
        StringBuffer stringBuffer = new StringBuffer();

        while ((inputLine = in.readLine()) != null)
            stringBuffer.append(inputLine);
        in.close();

        System.out.println("STRING BUFFER ==== " + stringBuffer.toString());

        return Integer.parseInt(stringBuffer.toString());
    }

    @Override
    public boolean isEmpty() throws IOException {
        return size() == 0;
    }

    @Override
    public void enqueue(String URL) throws RuntimeException, IOException {

        HttpURLConnection conn = sendReq("enqueue", "url="+URL, "POST");

        if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
            throw new RuntimeException("Attempt to enqueue " +URL + " failed. Server said: " + conn.getResponseCode());
        }

    }


    private HttpURLConnection sendReq(String action, String parameters, String method) throws IOException {
        String u = remoteAddr + "/" + this.threadID + "/" + action;
        URL url = new URL(u);

        System.out.println("Send req to URL " + u);

        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        conn.setRequestProperty("Secret", this.secret);

        conn.setRequestMethod(method);
        conn.setDoOutput(true);

        if (method.equals("POST")) {

            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            if (parameters != null) {
                byte[] toSend = parameters.getBytes(StandardCharsets.UTF_8);
                conn.setRequestProperty("Content-Length", Integer.toString(toSend.length));
                conn.setRequestProperty("charset", "utf-8");
                DataOutputStream os = new DataOutputStream(conn.getOutputStream());
                os.write(toSend);
                os.flush();
            }

        } else {
            //conn.getOutputStream();
            System.out.println("SendReq with GET");
        }

        return conn;
    }
}
