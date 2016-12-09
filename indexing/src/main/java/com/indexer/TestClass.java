package com.indexer;

/**
 * Created by azw on 12/9/16.
 */
public class TestClass {

    public static void main(String[] args) {
        S3Wrapper.init("cis-455");
        S3Wrapper.listObjects();
    }
}
