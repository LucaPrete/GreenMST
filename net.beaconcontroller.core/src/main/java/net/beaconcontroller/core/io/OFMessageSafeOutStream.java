/**
 * Copyright 2011, Stanford University. This file is licensed under GPL v2 plus
 * a special exception, as described in included LICENSE_EXCEPTION.txt.
 */
/**
 * 
 */
package net.beaconcontroller.core.io;

import org.openflow.io.OFMessageOutStream;

/**
 * This is a thread-safe implementation of the OFMessageOutStream
 * @author David Erickson (daviderickson@cs.stanford.edu)
 *
 */
public interface OFMessageSafeOutStream extends OFMessageOutStream {
}
