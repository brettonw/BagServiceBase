package com.brettonw.servlet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class HandlerAutoInstalled implements Handler<Event> {
    private static final Logger log = LogManager.getLogger (Base.class);

    private Object container;
    private Method method;

    public HandlerAutoInstalled (String event, Object container) throws NoSuchMethodException {
        this.container = container;
        // construct the event handler name, and look it up in the container
        String eventHandlerName = "handleEvent" + event.substring (0, 1).toUpperCase () + event.substring (1);
        Class type = container.getClass ();
        method = type.getMethod (eventHandlerName, Event.class);
    }

    @Override
    public void handle (Event event) throws IOException {
        try {
            method.invoke (container, event);
        } catch (IllegalAccessException | InvocationTargetException exception) {
            log.error (exception);
        }
    }
}
