package databases;
/**
 * Created by YagilB on 29/11/2016.
 */

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.*;
import org.apache.commons.codec.binary.Base32;

import java.io.*;
import java.util.Map;

public class S3Wrapper {
    private static AWSCredentials credentials = null;
    private static AmazonS3 s3 = null;
    private static Region reg = null;
    private static String bucketName = "";
    private static AccessControlList acl = null;

    public static void init(String buckName) {
        try {
            credentials = new ProfileCredentialsProvider().getCredentials();
        } catch (Exception e) {
            throw new AmazonClientException(
                    "Cannot load the credentials from the credential profiles file. " +
                            "Please make sure that your credentials file is at the correct " +
                            "location (~/.aws/credentials), and is in valid format.",
                    e);
        }
        s3 = new AmazonS3Client(credentials);
        bucketName = buckName;
        acl = new AccessControlList();
        acl.grantPermission(GroupGrantee.AllUsers, Permission.Read);
    }

    public static void listBuckets() {
        System.out.println("Listing buckets");
        for (Bucket bucket : s3.listBuckets()) {
            System.out.println(" - " + bucket.getName());
        }
        System.out.println();
    }

    private static void createBucket(String bucketName) {
        reg = Region.getRegion(Regions.US_WEST_2);
        s3.setRegion(reg);
        System.out.println("Creating bucket " + bucketName + "\n");
        s3.createBucket(bucketName);
    }

    public static void addDocument(String key, String content) {
        addDocument(key, content, null);
    }

    public static boolean hasKey(String key) {
        return s3.doesObjectExist(bucketName, key);
    }

    public static void addDocument(String key, String content, Map<String, String> metaData) {
        System.out.println("Uploading a new object to S3 from a file\n");
        try {

            String safeKey = encodeSafeKey(key);

            // Adding user meta data

            ObjectMetadata metadata = new ObjectMetadata();

            if (metaData != null) {
                for (String s : metaData.keySet()) {
                    metadata.addUserMetadata(s, metaData.get(s));
                }
            }

            // Creating request with meta data and ACL

            PutObjectRequest req = new PutObjectRequest(bucketName, safeKey, createFile(content))
                    .withAccessControlList(acl).withMetadata(metadata);

            // Saving the object to S3

            s3.putObject(req);

        } catch (IOException e) {

            e.printStackTrace();

        } catch (AmazonServiceException ase) {

            System.out.println("Caught an AmazonServiceException, which means your request made it "
                    + "to Amazon S3, but was rejected with an error response for some reason.");
            System.out.println("Error Message:    " + ase.getMessage());
            System.out.println("HTTP Status Code: " + ase.getStatusCode());
            System.out.println("AWS Error Code:   " + ase.getErrorCode());
            System.out.println("Error Type:       " + ase.getErrorType());
            System.out.println("Request ID:       " + ase.getRequestId());

        } catch (AmazonClientException ace) {

            System.out.println("Caught an AmazonClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with S3, "
                    + "such as not being able to access the network.");
            System.out.println("Error Message: " + ace.getMessage());

        }

    }

    public static String encodeSafeKey(String arg0) {
        return new Base32().encodeAsString(arg0.getBytes());
    }

    public static String decodeSafeKey(String arg0) {
        return new String(new Base32().decode(arg0.getBytes()));
    }

    public static InputStream getDocument(String key) {
        String safeKey = encodeSafeKey(key);

        System.out.println("Downloading an object");

        S3Object document = s3.getObject(new GetObjectRequest(bucketName, safeKey));


        System.out.println("Content-Type: "  + document.getObjectMetadata().getContentType());
        System.out.println("Meta data:");

        for (String s : document.getObjectMetadata().getUserMetadata().keySet()) {
            System.out.println(s + " : " + document.getObjectMetadata().getUserMetaDataOf(s));
        }

        InputStream stream = null;

        try {

            stream = document.getObjectContent();

        } catch (AmazonServiceException ase) {

            System.out.println("Caught an AmazonServiceException, which means your request made it "
                    + "to Amazon S3, but was rejected with an error response for some reason.");
            System.out.println("Error Message:    " + ase.getMessage());
            System.out.println("HTTP Status Code: " + ase.getStatusCode());
            System.out.println("AWS Error Code:   " + ase.getErrorCode());
            System.out.println("Error Type:       " + ase.getErrorType());
            System.out.println("Request ID:       " + ase.getRequestId());

        } catch (AmazonClientException ace) {

            System.out.println("Caught an AmazonClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with S3, "
                    + "such as not being able to access the network.");
            System.out.println("Error Message: " + ace.getMessage());

        }

        return stream;


    }


    public static void listObjects() {
        System.out.println("Listing objects");
        ObjectListing objectListing = s3.listObjects(new ListObjectsRequest()
                .withBucketName(bucketName)
                .withPrefix("My"));
        for (S3ObjectSummary objectSummary : objectListing.getObjectSummaries()) {
            System.out.println(" - " + objectSummary.getKey() + "  " +
                    "(size = " + objectSummary.getSize() + ")");
        }
        System.out.println();
    }

    public static void deleteObject(String key) {
        System.out.println("Deleting an object\n");
        s3.deleteObject(bucketName, key);
    }

    public static void deleteBucket() {
        System.out.println("Deleting bucket " + bucketName + "\n");
        s3.deleteBucket(bucketName);
    }

    /**
     * Creates a temporary file with text data to demonstrate uploading a file
     * to Amazon S3
     *
     * @return A newly created temporary file with text data.
     *
     * @throws IOException
     */
    private static File createFile(String content) throws IOException {
        File file = File.createTempFile("aws-java-sdk-", ".txt");
        file.deleteOnExit();

        Writer writer = new OutputStreamWriter(new FileOutputStream(file));
        writer.write(content);
        writer.close();

        return file;
    }

    /**
     * Displays the contents of the specified input stream as text.
     *
     * @param input
     *            The input stream to display as text.
     *
     * @throws IOException
     */
    public static void displayTextInputStream(InputStream input) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        while (true) {
            String line = reader.readLine();
            if (line == null) break;

            System.out.println("    " + line);
        }
        System.out.println();
    }

}
