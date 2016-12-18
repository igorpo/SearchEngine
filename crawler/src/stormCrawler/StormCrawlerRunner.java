package stormCrawler;

import edu.upenn.cis.stormlite.Config;
import edu.upenn.cis.stormlite.LocalCluster;
import edu.upenn.cis.stormlite.Topology;
import edu.upenn.cis.stormlite.TopologyBuilder;

/**
 * Created by YagilB on 17/12/2016.
 */
public class StormCrawlerRunner {
    private static String initialURL;
    private static int numOfDocs = Integer.MAX_VALUE;

    private static final String CRAWLER_QUEUE = "CRAWLER_QUEUE";
    private static final String HTTP_CLIENT = "HTTP_CLIENT";
    private static final String DOC_SAVER = "DOC_SAVER";
    private static final String LINK_EXTRACTOR = "LINK_EXTRACTOR";

    public static void main(String args[]) {
        System.out.println("Starting to crawl");
        if (args.length < 3) {
            System.err.println("Crawler error: not enough arguments");
            return;
        }

        initialURL = args[0];
        String dbEnvDir = System.getProperty("user.dir") + "/"+ args[1];
        double maxDocSize = Double.parseDouble(args[2]);

        System.out.println("initialURL: " + initialURL);
        System.out.println("dbEnvDir " + dbEnvDir);
        System.out.println("maxDocSize "+ maxDocSize);

        if (args.length == 4) {
            numOfDocs = Integer.parseInt(args[3]);
        }

        Config config = new Config();

//        XPathCrawlerFactory.setNumOfDocs(numOfDocs);
//        XPathCrawlerFactory.getFrontier().add(initialURL);

        CrawlerQueueSpout crawlerQueueSpout = new CrawlerQueueSpout();
        HTTPClientBolt httpClientBolt = new HTTPClientBolt();
        DocSaverBolt docSaverBolt = new DocSaverBolt();
        LinkExtractorBolt linkExtractorBolt = new LinkExtractorBolt();

        // crawlerQueueSpout ==> httpClientBolt ==> docSaverBolt ==> linkExtractorBolt
        TopologyBuilder builder = new TopologyBuilder();

        // Only one source ("spout") for the URLs
        builder.setSpout(CRAWLER_QUEUE, crawlerQueueSpout, 1);

        // Bolts
        builder.setBolt(HTTP_CLIENT, httpClientBolt, 4).shuffleGrouping(CRAWLER_QUEUE);
        builder.setBolt(DOC_SAVER, docSaverBolt, 4).shuffleGrouping(HTTP_CLIENT);
        builder.setBolt(LINK_EXTRACTOR, linkExtractorBolt, 4).shuffleGrouping(DOC_SAVER);

        LocalCluster cluster = new LocalCluster();
        Topology topo = builder.createTopology();

//        ObjectMapper mapper = new ObjectMapper();
//        try {
//            String str = mapper.writeValueAsString(topo);
//
//            System.out.println("The StormLite topology is:\n" + str);
//        } catch (JsonProcessingException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }

        cluster.submitTopology("test", config,
                builder.createTopology());
//        try {
//			Thread.sleep(30000);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//        cluster.killTopology("test");
//        cluster.shutdown();
//        System.exit(0);

    }

}
