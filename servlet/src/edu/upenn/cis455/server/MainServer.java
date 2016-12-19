package edu.upenn.cis455.server;

import edu.upenn.cis455.querying.QueryHandler;
import spark.Request;
import spark.Response;
import spark.Route;
import spark.Spark;

import java.util.ArrayList;
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
                String base = "<!DOCTYPE html>\n" +
                        "<html>\n" +
                        "<head>\n" +
                        "\t<title>Search</title>\n" +
                        "\t<link rel=\"stylesheet\" type=\"text/css\" href=\"http://guswynn.github.io/style_for_search/style.css\">\n" +
                        "</head>\n" +
                        "<body>\n" +
                        "\t<div class=\"body-container front-page\">\n" +
                        "\t\t<div class=\"logo\">\n" +
                        "\t\t\t<img src=\"http://guswynn.github.io/style_for_search/logo.png\">\n" +
                        "\t\t</div>\n" +
                        "\t\t<form action=\"search\" method=\"get\">\n" +
                        "\t\t\t<input name=\"q\" class=\"search-box front\" type=\"text\" placeholder=\"Type yo search\" autofocus/>\n" +
                        "\t\t\t<div class=\"search-controls\">\n" +
                        "\t\t\t\t<button type=\"submit\" class=\"control-btn\">Search</button>\n" +
                        "\t\t\t\t<button type=\"submit\" class=\"control-btn\">I'm Feeling Spicy</button>\n" +
                        "\t\t\t</div>\n" +
                        "\t\t</form>\n" +
                        "\t\t<form action=\"vr/search\" method=\"get\">\n" +
                        "\t\t\t<input name=\"q\" class=\"search-box front\" type=\"text\" placeholder=\"Type yo vr search\" autofocus/>\n" +
                        "\t\t\t<div class=\"search-controls\">\n" +
                        "\t\t\t\t<button type=\"submit\" class=\"control-btn\">Search</button>\n" +
                        "\t\t\t\t<button type=\"submit\" class=\"control-btn\">I'm Feeling Spicy</button>\n" +
                        "\t\t\t</div>\n" +
                        "\t\t\t<p>Learn more about <a target=\"_blank\" href=\"http://duboispa.gov/\">Da B0iz</a></p>\t\n" +
                        "\t\t</form>\n" +
                        "\t</div>\n" +
                        "</body>\n" +
                        "</html>";
                return base;
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

                String base = "<!DOCTYPE html>\n" +
                        "<html>\n" +
                        "<head>\n" +
                        "\t<title>Results</title>\n" +
                        "\t<link rel=\"stylesheet\" type=\"text/css\" href=\"http://guswynn.github.io/style_for_search/style.css\">\n" +
                        "</head>\n" +
                        "<body>\n" +
                        "\t<div class=\"body-container\">\n" +
                        "\t\t<div class=\"top-bar results-page\">\n" +
                        "\t\t\t<div class=\"logo results\">\n" +
                        "\t\t\t\t<img class=\"small\" src=\"http://guswynn.github.io/style_for_search/logo.png\">\n" +
                        "\t\t\t</div>\n" +
                        "\t\t\t<form action=\"\" method=\"\">\n" +
                        "\t\t\t\t<input class=\"search-box results\" type=\"text\" placeholder=\"Type yo search\" autofocus/>\n" +
                        "\t\t\t</form>\t\n" +
                        "\t\t</div>\n" +
                        "\t\t\n" +
                        "\t\t<div class=\"search-results-container\">\n" +
                        "\t\t\t<p class=\"search-stats\">About 177,000,000 results (0.89 seconds)</p>\n";
                sb.append(base);
                if (results == null){
                    sb.append("<p>Uh oh, no results</p>");
                } else {
                    for (String s : results) {
                        sb.append(
                        "\t\t\t<div class=\"search-result\">\n" +
                        "\t\t\t\t<h3 class=\"title\"><a href=\""+s+"\">"+s+"</a>" +
                        "\t\t\t\t<p class=\"url\">www.upenn.edu/</p>\n</h3>\n" +
                        "\t\t\t</div>\n");
                    }
                }
                sb.append("\n" +
                        "\n" +
                        "\t\t</div>\n" +
                        "\t</div>\n" +
                        "</body>\n" +
                        "</html>");
                return sb.toString();
            }
        });
        Spark.get(new Route("/vr/search") {

            @Override
            public Object handle(Request request, Response response) {
                System.out.println(request.queryParams("q"));
                String query = request.queryParams("q");
                QueryHandler qh = new QueryHandler("testcount");
                List<String> results = qh.query(query);

                //return sb.toString();
                if (results == null || results.isEmpty()) {
                    results = new ArrayList<String>();
                    results.add("TEST1");
                    results.add("TEST2");
                    results.add("TEST3");
                    results.add("TEST4");
                    results.add("TEST5");
                    results.add("TEST1");
                    results.add("TEST2");
                    results.add("TEST3");
                    results.add("TEST4");
                    results.add("TEST5");
                    results.add("TEST1");
                    results.add("TEST2");
                    results.add("TEST3");
                    results.add("TEST4");
                    results.add("TEST5");
                    results.add("TEST1");
                    results.add("TEST2");
                    results.add("TEST3");
                    results.add("TEST4");
                    results.add("TEST5");
                }

                String html = null;

                html = "<!DOCTYPE html>\n" +
                        "<html>\n" +
                        "\t<head>\n" +
                        "\t\t<title>Aframe example</title>\n" +
                        "\n" +
                        "\t\t<!-- Scripts -->\n" +
                        "\t\t<script src=\"https://aframe.io/releases/0.3.2/aframe.js\"></script>\n" +
                        "\t\t<script src=\"https://npmcdn.com/aframe-animation-component@3.0.1\"></script>\n" +
                        "\t    <script src=\"https://npmcdn.com/aframe-event-set-component@3.0.1\"></script>\n" +
                        "\t    <script src=\"https://npmcdn.com/aframe-layout-component@3.0.1\"></script>\n" +
                        "\t    <script src=\"https://npmcdn.com/aframe-template-component@3.0.1\"></script>\n" +
                        "\t\t<script src=\"https://rawgit.com/bryik/aframe-bmfont-text-component/master/dist/aframe-bmfont-text-component.min.js\"></script>\n" +
                        "\t    <script src=\"components/set-image.js\"></script>\n" +
                        "\t    <script src=\"components/update-raycaster.js\"></script>\n" +
                        "\t</head>\n" +
                        "\t<body>\n" +
                        "\t\t<a-scene>\n" +
                        "\t\t\t<a-assets>\n" +
                        "\t\t\t\t<img id=\"background\" src=\"http://3.bp.blogspot.com/-nKsvHDKHNvY/Usrb398L_CI/AAAAAAAALIU/ssDn6p7sRQc/s1600/bergsjostolen.jpg\">\n" +
                        "\t\t\t</a-assets>\n" +
                        "\n" +
                        "\t\t\t<!-- <a-sky color=\"rgb(250, 250, 250)\" radius=\"50\"></a-sky> -->\n" +
                        "\t\t\t<a-sky src=\"#background\" radius=\"50\"></a-sky>\n" +
                        "\n" +
                        "\t\t\t<!-- Search results template to be reused -->\n" +
                        "\t\t\t<script id=\"search-result\" type=\"text/nunjucks\">\n" +
                        "\t\t\t\t<a-plane \n" +
                        "\t\t\t\t\tclass=\"search-result\" \n" +
                        "\t\t\t\t\theight=\"1\" \n" +
                        "\t\t\t\t\twidth=\"3\"\n" +
                        "\t\t            material=\"shader: flat; color: rgba(255, 255, 255, .8)\"\n" +
                        "\t\t            event-set__1=\"_event: mousedown; scale: 1 1 1\"\n" +
                        "\t\t            event-set__2=\"_event: mouseup; scale: 1.1 1.1 1\"\n" +
                        "\t\t            event-set__3=\"_event: mouseenter; scale: 1.1 1.1 1\"\n" +
                        "\t\t            event-set__4=\"_event: mouseleave; scale: 1 1 1\"\n" +
                        "\t\t            update-raycaster=\"#cursor\">\n" +
                        "\n" +
                        "\t\t            <!-- Title Text -->\n" +
                        "\t\t\t    \t<a-entity \n" +
                        "\t\t\t    \t\tbmfont-text=\"text: Search Result Page Name; color: #234099\"\n" +
                        "\t\t\t    \t\tposition=\"-1.45 .275 0.001\">\t\n" +
                        "\t\t\t    \t</a-entity>\n" +
                        "\n" +
                        "\t\t\t    \t<!-- Subtitle Text -->\n" +
                        "\t\t\t    \t<a-entity \n" +
                        "\t\t\t    \t\tbmfont-text=\"text: Sample Text Sample Text; color: #AAA\"\n" +
                        "\t\t\t    \t\tscale=\".7 .7 1\"\n" +
                        "\t\t\t    \t\tposition=\"-1.45 .1 0.001\">\t\n" +
                        "\t\t\t    \t</a-entity>\n" +
                        "\n" +
                        "\t\t\t    \t<!-- Border -->\n" +
                        "\t\t\t    \t<a-plane\n" +
                        "\t\t\t    \t\theight=\"1.02\"\n" +
                        "\t\t\t    \t\twidth=\"3.02\"\n" +
                        "\t\t\t    \t\tmaterial=\"shader: flat; color: #CCC\"\n" +
                        "\t\t\t            position=\"0 0 -.002\">\n" +
                        "\t\t\t    \t</a-plane>\n" +
                        "\t            </a-plane>\n" +
                        "\t\t\t</script>\n" +
                        "\n";
                StringBuilder sb = new StringBuilder(html);
                for (int i = 0; i < results.size(); i++) {
                    if (i % 10 == 0) {
                        sb.append("\t\t\t<a-entity class=\"results\" layout=\"type: circle; radius: 5\" position=\"0 " + (1.0 + ((i * .6) / 10)) + " 0\">\n");
                    }
                    sb.append("<a-entity look-at='#camera'>");
                    sb.append("\t\t\t\t<a-plane \n" +
                            "\t\t\t\t\tclass=\"search-result\" \n" +
                            "\t\t\t\t\theight=\"1\" \n" +
                            "\t\t\t\t\twidth=\"3\"\n" +
                            "\t\t            material=\"shader: flat; color: rgba(255, 255, 255, .8)\"\n" +
                            "\t\t            event-set__1=\"_event: mousedown; scale: 1 1 1\"\n" +
                            "\t\t            event-set__2=\"_event: mouseup; scale: 1.1 1.1 1\"\n" +
                            "\t\t            event-set__3=\"_event: mouseenter; scale: 1.1 1.1 1\"\n" +
                            "\t\t            event-set__4=\"_event: mouseleave; scale: 1 1 1\"\n" +
                            "\t\t            update-raycaster=\"#cursor\">\n" +
                            "\n" +
                            "\t\t            <!-- Title Text -->\n" +
                            "\t\t\t    \t<a-entity \n" +
                            "\t\t\t    \t\tbmfont-text=\"text:");

                    sb.append(results.get(i));

                    sb.append("; color: #234099\"\n" +
                            "\t\t\t    \t\tposition=\"-1.45 .275 0.001\">\t\n" +
                            "\t\t\t    \t</a-entity>\n" +
                            "\n" +
                            "\n" +
                            "\t\t\t    \t<!-- Border -->\n" +
                            "\t\t\t    \t<a-plane\n" +
                            "\t\t\t    \t\theight=\"1.02\"\n" +
                            "\t\t\t    \t\twidth=\"3.02\"\n" +
                            "\t\t\t    \t\tmaterial=\"shader: flat; color: #CCC\"\n" +
                            "\t\t\t            position=\"0 0 -.002\">\n" +
                            "\t\t\t    \t</a-plane>\n" +
                            "\t            </a-plane>\n");
                    sb.append("</a-entity>");
                    if (i % 10 == 9){
                        sb.append("\t\t\t</a-entity>\n");
                    }
                }


                 sb.append(
                        "\n" +
                        "\n" +
                        "\t\t\t<a-entity look-controls>\n" +
                        "\t\t\t\t<a-camera id=\"camera\" position=\"0 1.8 5\">\n" +
                        "\t\t\t\t\t<a-cursor id=\"cursor\"\n" +
                        "\t\t\t\t\t\tanimation__click=\"property: scale; startEvents: click; from: 0.1 0.1 0.1; to: 1 1 1; dur: 150\"\n" +
                        "\t\t\t\t\t\tanimation__fusing=\"property: fusing; startEvents: fusing; from: 1 1 1; to: 0.1 0.1 0.1; dur: 1500\"\n" +
                        "\t\t\t\t\t\tevent-set__1=\"_event: mouseenter; color: gray\"\n" +
                        "\t\t\t\t\t\tevent-set__2=\"_event: mouseleave; color: black\"\n" +
                        "\t\t\t\t\t\traycaster=\"objects: .link\">\n" +
                        "\t\t\t\t\t</a-cursor>\n" +
                        "\t\t\t\t</a-camera>\n" +
                        "\t\t\t</a-entity>\n" +
                        "\n" +
                        " \t\t</a-scene>\n" +
                        "\t</body>\n" +
                        "</html>");













                return sb.toString();
            }
        });
    }

}
