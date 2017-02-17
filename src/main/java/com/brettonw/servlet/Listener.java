package com.brettonw.servlet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class Listener implements ServletContextListener {
    private static final Logger log = LogManager.getLogger (Listener.class);

    @Override
    public void contextInitialized (ServletContextEvent servletContextEvent) {
        log.info ("(" + Base.class.getPackage ().getImplementationTitle () + ") STARTING");
        Base.get ().onStart ();
    }

    @Override
    public void contextDestroyed (ServletContextEvent servletContextEvent) {
        log.info ("(" + Base.class.getPackage ().getImplementationTitle () + ") STOPPING");
        Base.get ().onStop ();
    }
}
