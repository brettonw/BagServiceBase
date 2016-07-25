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
    void handleCommand (String command, BagObject query, HttpServletRequest request, HttpServletResponse response) throws IOException {
        makeErrorResponse (query, response, "test");
    }

    @Test
    public void testServletBase () throws IOException {
        TestResponse response = new TestResponse ();
        TestRequest request = new TestRequest ();
        request.setQueryString ("command=hello&param1=1param2=2");

        doGet (request, response);

        File testFile = new File ("target", "test-response.json");
        BagObject bagObject = BagObjectFrom.file (testFile);
        assertTrue ("hello".equals (bagObject.getString ("command")));
    }
}
