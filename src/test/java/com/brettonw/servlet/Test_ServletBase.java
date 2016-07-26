package com.brettonw.servlet;

import com.brettonw.bag.BagObject;
import com.brettonw.bag.BagObjectFrom;
import com.brettonw.bag.SelectKey;
import com.brettonw.bag.SelectType;
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
        makeSuccessResponse (query, response, new BagObject ().put ("testing", "123"));
    }

    private void assertGet (BagObject bagObject, BagObject query) {
        assertTrue (bagObject.getString (STATUS_KEY).equals (OK_KEY));
        bagObject = bagObject.getBagObject (QUERY_KEY).select (new SelectKey (SelectType.EXCLUDE, POST_DATA_KEY));
        assertTrue (bagObject.equals (query));
    }

    @Test
    public void testGet () throws IOException {
        BagObject query = new BagObject ()
                .put (COMMAND_KEY, "hello")
                .put ("param1", 1)
                .put ("param2", 2);
        assertGet (servletTester.bagObjectFromGet (query), query);
    }

    @Test
    public void testPost () throws IOException {
        BagObject query = new BagObject ()
                .put (COMMAND_KEY, "goodbye")
                .put ("param1", 1)
                .put ("param2", 2);
        BagObject postData = BagObjectFrom.resource (getClass (), "/testPost.json");
        BagObject response = servletTester.bagObjectFromPost (query, postData);
        assertGet (response, query);
        assertTrue (response.getBagObject (QUERY_KEY).has (POST_DATA_KEY));
        assertTrue (response.getBagObject (QUERY_KEY).getBagObject (POST_DATA_KEY).equals (postData));
    }

    @Test
    public void testEmptyRequest () throws IOException {
        BagObject response = servletTester.bagObjectFromGet ("");
        assertTrue (response.getString (STATUS_KEY).equals (ERROR_KEY));
    }
}
