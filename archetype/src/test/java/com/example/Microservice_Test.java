package com.example;

import com.brettonw.bag.BagObject;
import com.brettonw.bag.BagObjectFrom;
import com.brettonw.servlet.ServletTester;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import java.io.IOException;

import static com.brettonw.bag.service.Keys.*;
import static org.junit.Assert.assertTrue;

public class Microservice_Test extends Microservice {
    private static final Logger log = LogManager.getLogger (Microservice_Test.class);

    ServletTester servletTester;

    public Microservice_Test () {
        servletTester = new ServletTester (this);
    }

    @Test
    public void testGetOk () throws IOException {
        BagObject query = BagObject.open (EVENT, OK);
        BagObject response = servletTester.bagObjectFromGet (query);
        assertTrue (response.getString (STATUS).equals (OK));
    }

    @Test
    public void testGetExample () throws IOException {
        BagObject query = BagObject.open (EVENT, "example").put ("xyz", 123);
        BagObject response = servletTester.bagObjectFromGet (query);
        assertTrue (response.getString (STATUS).equals (OK));
        assertTrue (response.getBagObject (RESPONSE).equals (query));
    }

    @Test
    public void testGetExampleNonstrict () throws IOException {
        BagObject query = BagObject.open (EVENT, "example-nonstrict");
        BagObject response = servletTester.bagObjectFromGet (query);
        assertTrue (response.equals (query));
    }

    @Test
    public void testPostExample () throws IOException {
        BagObject query = BagObject.open (EVENT, "example-post");
        BagObject postData = BagObjectFrom.resource (getClass (), "/testPost.json");
        BagObject response = servletTester.bagObjectFromPost (query, postData);
        assertTrue (response.equals (postData));
    }
}
