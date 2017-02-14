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
import java.util.HashMap;
import java.util.Map;

import static com.brettonw.servlet.Keys.*;

public class Base extends HttpServlet {
    private static final Logger log = LogManager.getLogger (Base.class);

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

    private final Map<String, Handler<Event>> handlers;
    protected Handler<Event> defaultHandler;
    protected BagObject apiSchema;

    protected Base () {
        handlers = new HashMap<> ();
        defaultHandler = event -> {
            event.error ("Unhandled event: '" + event.getEventName () + "'");
        };

        // try to load the schema, give a default HELP handler
        apiSchema = BagObjectFrom.resource (Base.class, "/api.json");
        if (apiSchema != null) {
            onEvent (HELP, event -> event.ok (apiSchema));
        }
    }

    @Override
    public void init (ServletConfig config) throws ServletException {
        super.init (config);
        context = config.getServletContext ();
        setAttribute (SERVLET, this);
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
        String contentTypeHeader = request.getHeader (CONTENT_TYPE);
        if (contentTypeHeader != null) {
            String[] contentType = contentTypeHeader.replace (" ", "").split (";");
            mimeType = contentType[0];
            log.debug ("'Content-Type' is (" + mimeType + ")");
        } else {
            log.warn ("'Content-Type' is not set by the requestor, using default (" + mimeType + ")");
        }

        // extract the bag data that's been posted, we do it this roundabout way because
        // we don't know a priori if it's an object or array
        SourceAdapter sourceAdapter = new SourceAdapterReader(request.getInputStream (), mimeType);
        String requestString = sourceAdapter.getStringData ();
        Bag postData = BagObjectFrom.string (requestString, mimeType);
        if (postData == null) postData = BagArrayFrom.string (requestString);

        // handle the query part normally, but add the post data to it (if any)
        BagObject query = BagObjectFrom.string (request.getQueryString (), MimeType.URL, () -> new BagObject ())
                .put (POST_DATA, postData);
        handleRequest (query, request, response);
    }

    private void handleRequest (BagObject query, HttpServletRequest request, HttpServletResponse response) throws IOException {
        // create the event object around the request parameters, and validate that it is
        // a known event
        Event event = new Event (query, request, response);
        if (event.hasRequiredParameters (EVENT)) {
            // get the name, and try to validate the event against the schema if we have one
            String eventName = event.getEventName ();
            if ((apiSchema == null) || event.hasRequiredParameters (apiSchema.getBagArray (Key.cat (eventName, REQUIRED)))) {
                // get the handler, and try to take care of business...
                Handler<Event> handler = handlers.get (eventName);
                if (handler != null) {
                    handler.handle (event);
                } else {
                    defaultHandler.handle (event);
                }
            }
        }
    }

    public Base onEvent (String name, Handler<Event> handler) {
        handlers.put (name, handler);
        return this;
    }
}
