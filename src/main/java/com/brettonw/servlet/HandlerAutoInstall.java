package com.brettonw.servlet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class HandlerAutoInstall implements Handler<Event> {
    private static final Logger log = LogManager.getLogger (HandlerAutoInstall.class);

    private Object container;
    private Method method;

    public HandlerAutoInstall (String event, Object container) throws NoSuchMethodException {
        this.container = container;
        // construct the event handler name, and look it up in the container
        int dash;
        while ((dash = event.indexOf ('-')) >= 0) {
            int skip = dash + 1, end = skip + 1;
            event = event.substring (0, dash) + event.substring (skip, end).toUpperCase () + event.substring (end);
        }
        String eventHandlerName = "handleEvent" + event.substring (0, 1).toUpperCase () + event.substring (1);
        Class type = container.getClass ();
        method = type.getMethod (eventHandlerName, Event.class);
    }

    @Override
    public void handle (Event event) {
        try {
            method.invoke (container, event);
        } catch (IllegalAccessException exception) {
            event.error (exception.toString ());
        } catch (InvocationTargetException exception) {
            Throwable cause = exception.getCause ();
            event.error (cause.toString ());
            log.error (method.getName () + " failed", cause);
        }
    }
}
