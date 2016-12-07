package databases;

import java.io.File;
import java.util.*;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicSessionCredentials;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.model.*;
import com.amazonaws.services.securitytoken.AWSSecurityTokenServiceClient;
import com.amazonaws.services.securitytoken.model.AssumeRoleRequest;
import com.amazonaws.services.securitytoken.model.AssumeRoleResult;

/**
 * Created by igorpogorelskiy on 12/6/16.
 */
public class DynamoWrapper {

    private static AWSCredentials longTermCredentials_;
    private static AmazonDynamoDBClient dynamoClient = null;
    private static DynamoDB dynamoDB;
    private static String table = "";
    private static final String ROLE_ARN = "arn:aws:iam::172510697573:policy/AmazonDynamoDBFullAccess-CIS455";
    private static String ROLE_SESSION = "455-searchengine";

    /**
     * The only information needed to create a client are security credentials
     * consisting of the AWS Access Key ID and Secret Access Key. All other
     * configuration, such as the service endpoints, are performed
     * automatically. Client parameters, such as proxies, can be specified in an
     * optional ClientConfiguration object when constructing a client.
     *
     * @param tableName table we are initing
     * @see com.amazonaws.auth.BasicAWSCredentials
     * @see com.amazonaws.ClientConfiguration
     */
    public static void init(String tableName) {
        table = tableName;

        // acquire long term credentials from the properties file

        longTermCredentials_ = new ProfileCredentialsProvider().getCredentials();

        dynamoClient = new AmazonDynamoDBClient(longTermCredentials_);
        dynamoDB = new DynamoDB(dynamoClient);

        // this is needed for it to know where to find our objects
        Region usWest2 = Region.getRegion(Regions.US_WEST_2);
        dynamoClient.setRegion(usWest2);
    }

    /**
     * Stores the url key mapping it to the links it has on the page
     * @param url url to store
     * @param links outgoing links of that url
     * @return true if success, false otherwise
     */
    public static boolean storeURLOutgoingLinks(String url, List<String> links) {
        try {

            Map<String, AttributeValue> newEntry = new HashMap<String, AttributeValue>();
            String safeKey = S3Wrapper.encodeSafeKey(url);
            newEntry.put("url", new AttributeValue(safeKey));
            newEntry.put("url_unsafe", new AttributeValue(url));
            newEntry.put("outgoingLinks", new AttributeValue().withSS(links));
            PutItemRequest putItemRequest = new PutItemRequest(table, newEntry);
            PutItemResult putItemResult = dynamoClient.putItem(putItemRequest);
            System.out.println("Item added to DynamoDB: " + url + ", encoded to: `" + safeKey + "` | " + putItemResult);

        } catch (AmazonServiceException ase) {
            System.out.println("Caught an AmazonServiceException, which means your request made it "
                    + "to AWS, but was rejected with an error response for some reason.");
            System.out.println("Error Message:    " + ase.getMessage());
            System.out.println("HTTP Status Code: " + ase.getStatusCode());
            System.out.println("AWS Error Code:   " + ase.getErrorCode());
            System.out.println("Error Type:       " + ase.getErrorType());
            System.out.println("Request ID:       " + ase.getRequestId());
            return false;
        } catch (AmazonClientException ace) {
            System.out.println("Caught an AmazonClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with AWS, "
                    + "such as not being able to access the network.");
            System.out.println("Error Message: " + ace.getMessage());
            return false;
        }
        return true;
    }

    /**
     * Tests if a given url has been visited before
     * @param url url to check
     * @return true if visited, false if not
     */
    public static boolean urlHasBeenSeen(String url) {
        String safeKey = S3Wrapper.encodeSafeKey(url);
        Table t = dynamoDB.getTable(table);
        Item item = t.getItem("url", safeKey);
        return item != null;
    }

    /**
     * Get the outgoing links list from a url
     * @param url url to get
     * @return the links list. will be empty if nothing was found in the db call
     */
    @SuppressWarnings("unchecked")
    public static List<String> retrieveOutgoingLinksForURL(String url) {
        String safeKey = S3Wrapper.encodeSafeKey(url);
        List<String> results = new ArrayList<>();
        Table t = dynamoDB.getTable(table);
        Item item = t.getItem("url", safeKey);
        if (item != null) {
            results.addAll(((Set<String>) item.get("outgoingLinks")));
        }
        return results;
    }

}
