package com.example;

import com.brettonw.bag.Bag;
import com.brettonw.bag.BagObject;
import com.brettonw.bag.service.Base;
import com.brettonw.bag.service.Event;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static com.brettonw.bag.service.Keys.POST_DATA;

public class Microservice extends Base {
    private static final Logger log = LogManager.getLogger (Microservice.class);

    public Microservice () {
    }

    public void handleEventExample (Event event) {
        event.ok (event.getQuery ());
    }

    public void handleEventExampleNonstrict (Event event) {
        event.respond (event.getQuery ());
    }

    public void handleEventExamplePost (Event event) {
        BagObject query = event.getQuery ();
        Bag postData = query.getBagObject (POST_DATA);
        event.respond (postData);
    }
}
