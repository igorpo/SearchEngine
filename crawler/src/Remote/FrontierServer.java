package Remote;

import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;

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

                return SyncMultQueue.poll(threadID);
            }
        });

        Spark.get(new Route("/:threadID/size") {

            @Override
            public Object handle(Request request, Response response) {
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

                System.out.println("CALLED /" + threadID + "/enqueue with url = " + url);
                return SyncMultQueue.enqueue(threadID, url);
            }
        });
    }

}
