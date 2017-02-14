package com.brettonw.servlet;

import com.brettonw.bag.*;
import org.junit.Test;

import java.io.IOException;

import static com.brettonw.servlet.Keys.*;
import static org.junit.Assert.assertTrue;

public class Test_Base extends Base {
    ServletTester servletTester;

    public Test_Base () {
        servletTester = new ServletTester (this);
        onEvent ("hello", event -> event.ok (BagObject.open  ("testing", "123")));
        onEvent ("goodbye", event -> event.ok (BagObject.open  ("testing", "456")));
    }

    private void assertGet (BagObject bagObject, BagObject query) {
        assertTrue (bagObject.getString (STATUS).equals (OK));
        bagObject = bagObject.getBagObject (QUERY).select (new SelectKey (SelectType.EXCLUDE, POST_DATA));
        assertTrue (bagObject.equals (query));
    }

    @Test
    public void testGet () throws IOException {
        BagObject query = BagObject
                .open (EVENT, "hello")
                .put ("param1", 1)
                .put ("param2", 2);
        assertGet (servletTester.bagObjectFromGet (query), query);
    }

    @Test
    public void testPost () throws IOException {
        BagObject query = BagObject
                .open (EVENT, "goodbye")
                .put ("param1", 1)
                .put ("param2", 2);
        BagObject postData = BagObjectFrom.resource (getClass (), "/testPost.json");
        BagObject response = servletTester.bagObjectFromPost (query, postData);
        assertGet (response, query);
        assertTrue (response.getBagObject (QUERY).has (POST_DATA));
        assertTrue (response.getBagObject (QUERY).getBagObject (POST_DATA).equals (postData));
    }

    @Test
    public void testEmptyRequest () throws IOException {
        BagObject response = servletTester.bagObjectFromGet ("");
        assertTrue (response.getString (STATUS).equals (ERROR));
        assertTrue (response.getString (Key.cat (ERROR, 0)).equals ("Missing: '" + EVENT + "'"));
    }

    @Test
    public void testHelp () throws IOException {
        BagObject query = BagObject
                .open (EVENT, "help")
                .put ("param1", 1)
                .put ("param2", 2);
        BagObject response = servletTester.bagObjectFromGet (query);
        assertTrue (response.getString (STATUS).equals (OK));
        assertTrue (response.getBagObject (RESPONSE).equals (BagObjectFrom.resource (Test_Base.class, "/api.json")));
    }

    @Test
    public void testBadGet () throws IOException {
        BagObject query = BagObject
                .open (EVENT, "halp")
                .put ("param1", 1)
                .put ("param2", 2);
        assertTrue (servletTester.bagObjectFromGet (query).getString (STATUS).equals (ERROR));
    }

    @Test
    public void testBadParameters () throws IOException {
        BagObject query = BagObject
                .open (EVENT, "hello")
                .put ("param1", 1)
                .put ("param3", 3);
        assertTrue (servletTester.bagObjectFromGet (query).getString (STATUS).equals (ERROR));
    }

    @Test
    public void testVersion () throws IOException {
        BagObject query = BagObject.open(EVENT, VERSION);
        BagObject response = servletTester.bagObjectFromGet (query);
        assertTrue (response.getString (Key.cat (RESPONSE, VERSION)).equals (UNKNOWN));
        assertTrue (response.getString (STATUS).equals (OK));
    }
}
