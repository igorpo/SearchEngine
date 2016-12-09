package remote.frontierServer;

import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;

import java.io.IOException;
import java.util.List;

/**
 * Created by YagilB on 01/12/2016.
 */
public class FrontierServer {

    private static String secret = "vix0jw1H6Vi/wBRAB9QDGHxFPOJtUm9e2r9GuANmjrY=";

    public FrontierServer() {}

    public static void main(String[] args) {

        Spark.get(new Route("/:threadID/poll") {

            @Override
            public Object handle(Request request, Response response) {
                if (request.headers("Secret") == null ||
                        !request.headers("secret").equals(secret)) {
                    response.status(403);
                    return "Not Authorized";
                }

                String threadID = request.params("threadID");
              //  System.out.println("CALLED /" + threadID + "/poll");
                return SyncMultQueue.poll(threadID);
            }
        });

        Spark.get(new Route("/:threadID/size") {
            @Override
            public Object handle(Request request, Response response) {
           //     System.out.println("got request: /"+request.params("threadID")+"/size");
                if (request.headers("Secret") == null ||
                        !request.headers("secret").equals(secret)) {
                    response.status(403);
                    return "Not Authorized";
                }

                String threadID = request.params("threadID");

                return SyncMultQueue.size(threadID);
            }
        });

        Spark.post(new Route("/:threadID/enqueue") {

            @Override
            public Object handle(Request request, Response response) {
                if (request.headers("Secret") == null ||
                        !request.headers("secret").equals(secret)) {
                    response.status(403);
                    return "Not Authorized";
                }

                String threadID = request.params("threadID");
                String url = request.queryParams("url");

             //   System.out.println("CALLED /" + threadID + "/enqueue with url = " + url);
                return SyncMultQueue.enqueue(threadID, url);
            }
        });

        Spark.post(new Route("/:threadID/batch/enqueue") {

            @Override
            public Object handle(Request request, Response response) {
                if (request.headers("Secret") == null ||
                        !request.headers("secret").equals(secret)) {
                    response.status(403);
                    return "Not Authorized";
                }

                String threadID = request.params("threadID");

                // urls is a list
                String urlsString = request.queryParams("urls");
                List<String> linksList = null;
                try {
                    linksList = SerializableArrayList.deserialize(urlsString);
                } catch (IOException e) {
                    System.out.println("Tried to deserialize list from thread id = " + threadID);
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    System.out.println("Tried to deserialize list from thread id = " + threadID);
                    e.printStackTrace();
                }
                if (linksList == null) return false;

             //   System.out.println("CALLED /" + threadID + "/enqueue with batch");
                boolean res = SyncMultQueue.enqueue(threadID, linksList);
                if (res) {
                    System.out.println("**********************************************SUCCESSFULLY BATCH ADDED "+linksList.size()+" LINKS FROM THREAD ID = " + threadID);
//                    for (String u : linksList) {
//                        System.out.println(u);
//                    }
                }

                return res;
            }
        });
    }

}
