package Frontier;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by YagilB on 01/12/2016.
 */
public class FrontierWrapper implements Frontier {

    // AWS
    private static String remoteAddr = "0.0.0.0:4567"; // the EC2 instance address
    private static String secret = "vix0jw1H6Vi/wBRAB9QDGHxFPOJtUm9e2r9GuANmjrY=";

    private static String threadID = "";

    // Provide
    public void init(String threadID) {
        this.threadID = threadID;
    }

    @Override
    public String poll() throws RuntimeException, IOException {
        HttpURLConnection conn = sendReq("poll",null, "GET");
        if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
            throw new RuntimeException("Job definition request failed");
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
    public int size() {
        return 0;
    }

    @Override
    public void enqueue(String URL) throws RuntimeException, IOException {

        HttpURLConnection conn = sendReq("enqueue", "url="+URL, "POST");

        if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
            throw new RuntimeException("Job definition request failed");
        }

    }


    private HttpURLConnection sendReq(String action, String parameters, String method) throws IOException {
        URL url = new URL(remoteAddr + "/" + this.threadID + "/" + action);

        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        conn.setRequestProperty("Secret", this.secret);
        conn.setDoOutput(true);
        conn.setRequestMethod(method);

        if (method.equals("POST")) {
            conn.setRequestProperty("Content-Type", "application/json");

            OutputStream os = conn.getOutputStream();
            if (parameters != null) {
                byte[] toSend = parameters.getBytes();
                os.write(toSend);
            }

            os.flush();
        } else {
            conn.getOutputStream();
        }

        return conn;
    }
}
