/**
 * Copyright 2011, Stanford University. This file is licensed under GPL v2 plus
 * a special exception, as described in included LICENSE_EXCEPTION.txt.
 */
package net.beaconcontroller.topology;

import net.beaconcontroller.core.IOFSwitch;

/**
 *
 *
 * @author David Erickson (daviderickson@cs.stanford.edu)
 */
public interface ITopologyAware {
    /**
     * 
     * @param src the source switch
     * @param srcPort the source port from the source switch
     * @param dst
     * @param dstPort
     * @param added
     */
    public void linkUpdate(IOFSwitch src, short srcPort,
            IOFSwitch dst, short dstPort, boolean added);
}
