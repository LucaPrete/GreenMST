/**
 * Copyright 2011, Stanford University. This file is licensed under GPL v2 plus
 * a special exception, as described in included LICENSE_EXCEPTION.txt.
 */
package net.beaconcontroller.core;

/**
 *
 *
 * @author David Erickson (daviderickson@cs.stanford.edu)
 */
public interface IOFSwitchListener {

    /**
     * Fired when a switch is connected to the controller, and has sent
     * a features reply.
     * @param sw
     */
    public void addedSwitch(IOFSwitch sw);

    /**
     * Fired when a switch is disconnected from the controller.
     * @param sw
     */
    public void removedSwitch(IOFSwitch sw);
    
    /**
     * The name assigned to this listener
     * @return
     */
    public String getName();
}
