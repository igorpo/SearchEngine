package com.indexer;

import org.apache.hadoop.io.Text;

/**
 * Created by azw on 12/9/16.
 */
public class TestClass {

    public static void main(String[] args) {
        //S3Wrapper.init("cis-455");
        //S3Wrapper.listObjects();

        Text t = new Text();
        String val = t.toString();
        System.out.println(val.equals(""));
        System.out.println(val == null);
        System.out.println("".equals(val));
        System.out.println(val.isEmpty());
    }
}
