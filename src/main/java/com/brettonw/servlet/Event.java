package com.brettonw.servlet;

import com.brettonw.bag.Bag;
import com.brettonw.bag.BagArray;
import com.brettonw.bag.BagObject;
import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

import static com.brettonw.servlet.Keys.*;

@Data @RequiredArgsConstructor
public class Event {
    private static final Logger log = LogManager.getLogger (Event.class);

    @Getter @NonNull private final BagObject query;
    @Getter @NonNull private final HttpServletRequest request;
    @Getter          private BagObject response;

    public String getEventName () {
        return query.getString (EVENT);
    }

    public Event ok (Bag bag) throws IOException {
        response = BagObject.open (QUERY, query).put (STATUS, OK).put (RESPONSE, bag);
        return this;
    }

    public Event ok () throws IOException {
        return ok (null);
    }

    public Event error (BagArray errors) throws IOException {
        // log the errors
        for (int i = 0, end = errors.getCount (); i < end; ++i) {
            log.error (errors.getString (i));
        }

        // and respond to the end user...
        response = BagObject.open (QUERY, query).put (STATUS, ERROR).put (ERROR, errors);
        return this;
    }

    public Event error (String error) throws IOException {
        return error (BagArray.open (error));
    }
}
