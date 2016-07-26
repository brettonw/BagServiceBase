package com.brettonw.servlet;

import com.brettonw.bag.BagObject;
import com.brettonw.bag.BagObjectFrom;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.junit.Assert.assertTrue;

public class Test_ServletBase extends ServletBase {
    ServletTester servletTester;

    public Test_ServletBase () {
        servletTester = new ServletTester (this);
    }

    @Override
    public void handleCommand (String command, BagObject query, HttpServletRequest request, HttpServletResponse response) throws IOException {
        makeJsonResponse (response, query);
    }

    @Test
    public void testGet () throws IOException {
        BagObject bagObject = servletTester.bagObjectFromGet ("command=hello&param1=1&param2=2");
        assertTrue ("hello".equals (bagObject.getString ("command")));
        assertTrue ("1".equals (bagObject.getString ("param1")));
        assertTrue ("2".equals (bagObject.getString ("param2")));
    }
    @Test
    public void testPost () throws IOException {
        BagObject postData = BagObjectFrom.resource (getClass (), "/testPost.json");
        BagObject bagObject = servletTester.bagObjectFromPost ("command=goodbye&param1=1&param2=2", postData);
        assertTrue ("goodbye".equals (bagObject.getString ("command")));
        assertTrue ("1".equals (bagObject.getString ("param1")));
        assertTrue ("2".equals (bagObject.getString ("param2")));
        assertTrue (bagObject.has (POST_DATA_KEY));
        assertTrue (bagObject.getBagObject (POST_DATA_KEY).equals (postData));
    }
}
