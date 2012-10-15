/**
 * Copyright 2011, Stanford University. This file is licensed under GPL v2 plus
 * a special exception, as described in included LICENSE_EXCEPTION.txt.
 */
package net.beaconcontroller.logging.bridge;

import org.osgi.service.log.LogEntry;
import org.osgi.service.log.LogListener;
import org.osgi.service.log.LogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author David Erickson (daviderickson@cs.stanford.edu)
 */
public class SLF4JLogListener implements LogListener {
    private static Logger log = LoggerFactory.getLogger(SLF4JLogListener.class);

    public void logged(LogEntry entry) {
        if (entry.getBundle() != null) {
            log = LoggerFactory.getLogger(entry.getBundle().getSymbolicName()
                    + "-" + entry.getBundle().getVersion().toString());
        }

        switch (entry.getLevel()) {
            case LogService.LOG_DEBUG:
                log.debug(entry.getMessage(), entry.getException());
                break;
            case LogService.LOG_ERROR:
                log.error(entry.getMessage(), entry.getException());
                break;
            case LogService.LOG_INFO:
                log.info(entry.getMessage(), entry.getException());
                break;
            case LogService.LOG_WARNING:
                log.warn(entry.getMessage(), entry.getException());
                break;
        }
    }
}
