/**
 * Copyright 2011, Stanford University. This file is licensed under GPL v2 plus
 * a special exception, as described in included LICENSE_EXCEPTION.txt.
 */
package net.beaconcontroller.core;

import org.openflow.protocol.OFMessage;

/**
 *
 *
 * @author David Erickson (daviderickson@cs.stanford.edu)
 */
public interface IOFMessageListener {
    public enum Command {
        CONTINUE, STOP
    }

  /**
   * This is the method Beacon uses to call listeners with OpenFlow messages
   * @param sw the OpenFlow switch that sent this message
   * @param msg the message
   * @return the command to continue or stop the execution
   */
  public Command receive(IOFSwitch sw, OFMessage msg);

  /**
   * The name assigned to this listener
   * @return
   */
  public String getName();
}
