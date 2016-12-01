import S3.S3Wrapper;

import java.io.IOException;
import java.io.InputStream;

public class Crawler {

    public static void main(String[] args) {
        S3Wrapper.init("cis-455");

        S3Wrapper.addDocument("mydoc2", "igor2");
        InputStream stream = S3Wrapper.getDocument("igor.com");
        try {
            S3Wrapper.displayTextInputStream(stream);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
