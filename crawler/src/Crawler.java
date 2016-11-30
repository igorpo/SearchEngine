import java.io.IOException;
import java.io.InputStream;

/**
 * Created by YagilB on 29/11/2016.
 */
public class Crawler {

    public static void main(String[] args) {
        S3Wrapper.init("cis-455");

        S3Wrapper.addDocument("mydoc", "igor");
        InputStream stream = S3Wrapper.getDocument("igor.com");
        try {
            S3Wrapper.displayTextInputStream(stream);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
