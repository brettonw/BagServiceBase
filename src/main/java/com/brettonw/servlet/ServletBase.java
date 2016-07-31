package com.brettonw.servlet;

import com.brettonw.bag.*;
import com.brettonw.bag.formats.MimeType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

public abstract class ServletBase extends HttpServlet {
    private static final Logger log = LogManager.getLogger (ServletBase.class);

    public static final String STATUS_KEY = "status";
    public static final String OK_KEY = "ok";
    public static final String QUERY_KEY = "query";
    public static final String RESPONSE_KEY = "response";
    public static final String ERROR_KEY = "error";
    public static final String SERVLET_KEY = "servlet";
    public static final String COMMAND_KEY = "command";
    public static final String POST_DATA_KEY = "post-data";
    public static final String CONTENT_TYPE_KEY = "Content-Type";
    public static final String UTF_8 = StandardCharsets.UTF_8.name ();

    private static ServletContext context;

    public static ServletContext getContext () {
        return context;
    }

    public static Object getAttribute (String key) {
        return (context != null) ? context.getAttribute (key) : null;
    }

    public static Object setAttribute (String key, Object value) {
        if (context != null) {
            Object oldValue = context.getAttribute (key);
            context.setAttribute (key, value);
            return oldValue;
        }
        return null;
    }

    @Override
    public void init (ServletConfig config) throws ServletException {
        super.init (config);
        context = config.getServletContext ();
        setAttribute (SERVLET_KEY, this);
    }


    @Override
    public void doGet (HttpServletRequest request, HttpServletResponse response) throws IOException {
        BagObject query = BagObjectFrom.string (request.getQueryString (), MimeType.URL, () -> new BagObject ());
        handleRequest (query, request, response);
    }

    @Override
    public void doPost (HttpServletRequest request, HttpServletResponse response) throws IOException {
        // get the request data type, then tease out the response type (use a default if it's not present) and the
        // charset (if given, otherwise default to UTF-8, because that's what it will be in Java)
        String mimeType = MimeType.DEFAULT;
        String contentTypeHeader = request.getHeader (CONTENT_TYPE_KEY);
        if (contentTypeHeader != null) {
            String[] contentType = contentTypeHeader.replace (" ", "").split (";");
            mimeType = contentType[0];
            log.debug ("'Content-Type' is (" + mimeType + ")");
        } else {
            log.warn ("'Content-Type' is not set by the requestor, using default (" + mimeType + ")");
        }

        // extract the bag data that's been posted
        SourceAdapter sourceAdapter = new SourceAdapterReader(request.getInputStream (), mimeType);
        String requestString = sourceAdapter.getStringData ();
        Bag postData = BagObjectFrom.string (requestString, mimeType);
        if (postData == null) postData = BagArrayFrom.string (requestString);

        // handle the query part normally, but add the post data to it
        BagObject query = BagObjectFrom.string (request.getQueryString (), MimeType.URL, () -> new BagObject ())
                .put (POST_DATA_KEY, postData);
        handleRequest (query, request, response);
    }

    public void makeResponse (String mimeType, HttpServletResponse response, String responseText) throws IOException {
        // set the response types
        String UTF_8 = StandardCharsets.UTF_8.name ();
        response.setContentType (mimeType + "; charset=" + UTF_8);
        response.setCharacterEncoding (UTF_8);

        // write out the response
        PrintWriter out = response.getWriter ();
        out.println (responseText);
        //out.flush ();
        out.close ();
    }

    public void makeJsonResponse (HttpServletResponse response, Bag bag) throws IOException {
        makeResponse (MimeType.JSON, response, bag.toString ());
    }

    public void makeHtmlResponse (HttpServletResponse response, String html) throws IOException {
        makeResponse ("text/html", response, html);
    }

    public void makeSuccessResponse (BagObject query, HttpServletResponse response, Bag bag) throws IOException {
        makeJsonResponse (response, new BagObject ()
                .put (QUERY_KEY, query)
                .put (STATUS_KEY, OK_KEY)
                .put (RESPONSE_KEY, bag));
    }

    public void makeErrorResponse (BagObject query, HttpServletResponse response, BagArray errors) throws IOException {
        makeJsonResponse (response, new BagObject ()
                .put (QUERY_KEY, query)
                .put (STATUS_KEY, ERROR_KEY)
                .put (ERROR_KEY, errors));
    }

    public void makeErrorResponse (BagObject query, HttpServletResponse response, String error) throws IOException {
        makeErrorResponse (query, response, new BagArray ().add (error));
    }

    public boolean hasRequiredParameters (BagObject query, HttpServletResponse response, String... queryFields) throws IOException {
        BagArray missingFields = new BagArray (queryFields.length);
        for (String queryField : queryFields) {
            if (! query.has (queryField)) {
                missingFields.add ("Missing: '" + queryField + "'");
            }
        }
        if (missingFields.getCount () > 0) {
            makeErrorResponse (query, response, missingFields);
            return false;
        }
        return true;
    }

    private void handleRequest (BagObject query, HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (hasRequiredParameters (query, response, COMMAND_KEY)) {
            handleCommand (query, request, response);
        }
    }

    public abstract void handleCommand (BagObject query, HttpServletRequest request, HttpServletResponse response) throws IOException;
}
