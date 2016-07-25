package com.brettonw.servlet;

import com.brettonw.bag.BagObject;
import com.brettonw.bag.BagObjectFrom;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertTrue;

public class ServletBaseTest extends ServletBase {

    @Override
    public void handleCommand (String command, BagObject query, HttpServletRequest request, HttpServletResponse response) throws IOException {
        makeJsonResponse (response, query);
    }

    @Test
    public void testGet () throws IOException {
        TestResponse response = new TestResponse ("test-get-response.json");
        TestRequest request = new TestRequest ();
        request.setQueryString ("command=hello&param1=1&param2=2");

        doGet (request, response);

        File testFile = new File ("target", response.getWriterFileName ());
        BagObject bagObject = BagObjectFrom.file (testFile);
        assertTrue ("hello".equals (bagObject.getString ("command")));
        assertTrue ("1".equals (bagObject.getString ("param1")));
        assertTrue ("2".equals (bagObject.getString ("param2")));
    }
    @Test
    public void testPost () throws IOException {
        TestResponse response = new TestResponse ("test-post-response.json");
        TestRequest request = new TestRequest ();
        request.setQueryString ("command=goodbye&param1=1&param2=2");

        doPost (request, response);

        File testFile = new File ("target", response.getWriterFileName ());
        BagObject bagObject = BagObjectFrom.file (testFile);
        assertTrue ("goodbye".equals (bagObject.getString ("command")));
        assertTrue ("1".equals (bagObject.getString ("param1")));
        assertTrue ("2".equals (bagObject.getString ("param2")));
        assertTrue (bagObject.has (POST_DATA_KEY));
        BagObject testPost = BagObjectFrom.resource (getClass (), "/testPost.json");
        assertTrue (bagObject.getBagObject (POST_DATA_KEY).equals (testPost));
    }
}
