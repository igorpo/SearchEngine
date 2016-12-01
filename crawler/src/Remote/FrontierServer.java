package Remote;

import spark.Spark;
import spark.Request;
import spark.Response;
import spark.Route;

/**
 * Created by YagilB on 01/12/2016.
 */
public class FrontierServer {

    public FrontierServer() {
        Spark.post(new Route("/runjob") {

            @Override
            public Object handle(Request arg0, Response arg1) {

                return "Started";
            }
        });
    }

}
