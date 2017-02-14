package com.brettonw.servlet;

import com.brettonw.bag.Bag;
import com.brettonw.bag.BagArray;
import com.brettonw.bag.BagObject;
import com.brettonw.bag.formats.MimeType;
import lombok.NonNull;
import lombok.Value;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

import static com.brettonw.servlet.Keys.*;

@Value
public class Event {
    @NonNull private final BagObject query;
    @NonNull private final HttpServletRequest request;
    @NonNull private final HttpServletResponse response;

    public void respond (String mimeType, String responseText) throws IOException {
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

    public void respondJson (Bag bag) throws IOException {
        respond (MimeType.JSON, bag.toString (MimeType.JSON));
    }

    public void respondHtml (String html) throws IOException {
        respond ("text/html", html);
    }

    public void ok (Bag bag) throws IOException {
        respondJson (new BagObject ()
                .put (QUERY, query)
                .put (STATUS, OK)
                .put (RESPONSE, bag));
    }

    public void error (BagArray errors) throws IOException {
        respondJson (new BagObject ()
                .put (QUERY, query)
                .put (STATUS, ERROR)
                .put (ERROR, errors));
    }

    public void error (String error) throws IOException {
        error (new BagArray ().add (error));
    }

    public boolean hasRequiredParameters (String... requiredParameters) throws IOException {
        BagArray missingFields = new BagArray (requiredParameters.length);
        for (String queryField : requiredParameters) {
            if (!query.has (queryField)) {
                missingFields.add ("Missing: '" + queryField + "'");
            }
        }
        if (missingFields.getCount () > 0) {
            error (missingFields);
            return false;
        }
        return true;
    }

    public boolean hasRequiredParameters (BagArray requiredParameters) throws IOException {
        return (requiredParameters != null) ? hasRequiredParameters (requiredParameters.toArray (String.class)) : true;
    }
}
