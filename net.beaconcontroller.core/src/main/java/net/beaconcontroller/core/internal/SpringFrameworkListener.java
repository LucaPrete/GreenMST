/**
 * Copyright 2011, Stanford University. This file is licensed under GPL v2 plus
 * a special exception, as described in included LICENSE_EXCEPTION.txt.
 */
package net.beaconcontroller.core.internal;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.osgi.context.BundleContextAware;
import org.springframework.osgi.context.event.OsgiBundleApplicationContextEvent;
import org.springframework.osgi.context.event.OsgiBundleApplicationContextListener;
import org.springframework.osgi.context.event.OsgiBundleContextRefreshedEvent;
import org.springframework.osgi.extender.support.scanning.ConfigurationScanner;
import org.springframework.osgi.extender.support.scanning.DefaultConfigurationScanner;

/**
 * This class listens for all known Spring contexts to fully complete loading
 * then alerts the Controller class that this has occurred, and starts
 * the listening socket allowing switches to connect.
 *
 * @author David Erickson (daviderickson@cs.stanford.edu)
 */
public class SpringFrameworkListener
        implements
        OsgiBundleApplicationContextListener<OsgiBundleApplicationContextEvent>,
        BundleContextAware {
    protected static Logger log = LoggerFactory.getLogger(SpringFrameworkListener.class);

    protected BundleContext context;
    protected Controller controller;
    protected volatile boolean controllerListenerStarted = false;
    protected Map<Bundle, Boolean> springBundles;
    protected Timer timer;

    public void start() {
        this.timer = new Timer(true);
        this.timer.scheduleAtFixedRate(new TimerTask() {
            
            @Override
            public void run() {
                checkPublishedServices();
            }
        }, 100, 100);
    }

    /**
     * Checks for a published ApplicationContext in the OSGi service registry
     * for every Bundle containing a Spring context that has not been detected
     * as having been started.
     */
    protected synchronized void checkPublishedServices() {
        if (!controllerListenerStarted) {
            boolean falseFound = false;
            for (Map.Entry<Bundle, Boolean> entry : this.springBundles.entrySet()) {
                if (!entry.getValue()) {
                    // Check if there is a published service for this app context
                    try {
                        ServiceReference[] services = context
                                .getServiceReferences(
                                        "org.springframework.context.ApplicationContext",
                                        "(&(org.springframework.context.service.name="
                                                + entry.getKey()
                                                        .getSymbolicName()
                                                + ")(Bundle-SymbolicName="
                                                + entry.getKey()
                                                        .getSymbolicName()
                                                + "))");
                        if (services != null && services.length > 0) {
                            log.info(
                                    "Spring application previously started: {}",
                                    entry.getKey().getSymbolicName());
                            entry.setValue(true);
                        } else {
                            log.trace(
                                    "Waiting on Spring Application Context for bundle: {}",
                                    entry.getKey().getSymbolicName());
                            falseFound = true;
                        }
                    } catch (InvalidSyntaxException e) {
                        log.error("Failure getting service references", e);
                    }
                }
            }
            if (!falseFound) {
                triggerStarted();
            }

        } else {
            timer.cancel();
        }
    }

    /**
     * This method is called once all Bundles containing Spring application
     * context metadata have been detected as started.  It calls the Controller
     * class to alert it to start accepting switch connections.
     */
    protected synchronized void triggerStarted() {
        log.info("All Spring application contexts started");

        // Start the listening socket on the Controller, only once
        try {
            controller.startListener();
        } finally {
            timer.cancel();
            controllerListenerStarted = true;
        }
    }

    @Override
    public synchronized void setBundleContext(BundleContext context) {
        this.context = context;
        this.springBundles = new HashMap<Bundle, Boolean>();

        // Load the scanner to detect if a bundle is a Spring bundle
        ConfigurationScanner scanner = new DefaultConfigurationScanner();
        for (Bundle bundle : context.getBundles()) {
            // Don't wait on fragments
            if (bundle.getHeaders().get("fragment-host") == null) {
                int state = bundle.getState();
                // Only include bundles that intend to start
                if (state == Bundle.STARTING || state == Bundle.ACTIVE) {
                    String[] configurations = scanner.getConfigurations(bundle);
                    // If this bundle has Spring configurations put it in our wait list
                    if (configurations.length > 0) {
                        springBundles.put(bundle, false);
                    }
                }
            }
        }
    }

    @Override
    public synchronized void onOsgiApplicationEvent(OsgiBundleApplicationContextEvent event) {
        if (event instanceof OsgiBundleContextRefreshedEvent && !controllerListenerStarted) {
            // Refreshed means the app context for that bundle is fully loaded
            Bundle bundle = event.getBundle();
            springBundles.put(bundle, true);
            log.info("Spring application context started: {}", bundle.getSymbolicName());

            boolean started = true;
            for (Map.Entry<Bundle, Boolean> entry : this.springBundles.entrySet()) {
                if (!entry.getValue()) {
                    started = false;
                    break;
                }
            }
            if (started) {
                triggerStarted();
            }
        }
    }

    /**
     * @param controller the controller to set
     */
    public void setController(Controller controller) {
        this.controller = controller;
    }
}
