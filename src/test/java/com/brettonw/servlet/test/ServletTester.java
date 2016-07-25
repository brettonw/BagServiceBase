package com.brettonw.servlet.test;

import com.brettonw.bag.Bag;
import com.brettonw.servlet.ServletBase;

import javax.servlet.ServletException;
import java.io.File;
import java.io.IOException;

public class ServletTester {
    public static final String TARGET_DIR = "target";
    public static final String TEST_DIR = "test-files";

    private File targetTestDir;
    private ServletBase servletBase;

    public ServletTester (ServletBase servletBase) {
        this.servletBase = servletBase;
        try {
            servletBase.init (new TestServletConfig (getClass ().getName ()));
            targetTestDir = new File (TARGET_DIR, TEST_DIR);
            targetTestDir.mkdirs ();
        } catch (ServletException e) {
            e.printStackTrace ();
        }
    }

    public File get (String queryString) throws IOException {
        File outputFile = new File (targetTestDir, java.util.UUID.randomUUID().toString ());
        TestResponse response = new TestResponse (outputFile);
        TestRequest request = new TestRequest (queryString);
        servletBase.doGet (request, response);
        return outputFile;
    }

    public File post (String queryString, Bag postData) throws IOException {
        File outputFile = new File (targetTestDir, java.util.UUID.randomUUID().toString ());
        TestResponse response = new TestResponse (outputFile);
        TestRequest request = new TestRequest (queryString, postData);
        servletBase.doPost (request, response);
        return outputFile;
    }

}
