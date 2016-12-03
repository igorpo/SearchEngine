package Testing;

import S3.S3Wrapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by YagilB on 03/12/2016.
 */
public class S3Tester {
    public static void main(String[] args) {
        S3Wrapper.init("cis-455");

        Map<String, String> meta = new HashMap<>();

        meta.put("something", "yeah baby");

//        S3Wrapper.addDocument("https://www.yagil.org", "this is a doc", meta);

        try {
            S3Wrapper.displayTextInputStream(S3Wrapper.getDocument("https://www.yagil.org"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
