package edu.upenn.cis455.server;

import edu.upenn.cis455.querying.QueryHandler;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;

import java.util.List;

/**
 * Created by YagilB on 01/12/2016.
 *
 */
public class MainServer {

    public static final int N = 10;
    public static void main(String[] args) {

        Spark.get(new Route("/") {

            @Override
            public Object handle(Request request, Response response) {
                StringBuilder sb = new StringBuilder();
                sb.append("<html><head><title>Test</title></head><body>");
                sb.append("<form action=\"search\" method=\"get\">");
                sb.append("Come on bud hit me with that search bro:<br><input type=\"text\" name=\"q\"><br>");
                sb.append("<input type=\"submit\" value=\"Submit\"></form>");
                sb.append("</body></html>");
                return sb.toString();
            }
        });


        Spark.get(new Route("/search") {

            @Override
            public Object handle(Request request, Response response) {
                System.out.println(request.queryParams("q"));
                StringBuilder sb = new StringBuilder();
                String query = request.queryParams("q");
                QueryHandler qh = new QueryHandler("testcount");
                List<String> results = qh.query(query);

                sb.append("<htm l><head><title>Test</title></head><body>");
                for (String s : results){
                    sb.append("<p>"+s+"</p>");
                }
                sb.append("</body></html>");
                return sb.toString();
            }
        });
        Spark.get(new Route("/vr/query") {

            @Override
            public Object handle(Request request, Response response) {
                System.out.println(request.queryParams("q"));
                StringBuilder sb = new StringBuilder();
                String query = request.queryParams("q");
                QueryHandler qh = new QueryHandler("testcount");
                List<String> results = qh.query(query);

                sb.append("<htm l><head><title>Test</title></head><body>");
                for (String s : results){
                    sb.append("<p>"+s+"</p>");
                }
                sb.append("</body></html>");
                return sb.toString();
            }
        });
    }

}
