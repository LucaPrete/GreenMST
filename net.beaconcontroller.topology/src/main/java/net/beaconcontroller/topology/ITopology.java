/**
 * Copyright 2011, Stanford University. This file is licensed under GPL v2 plus
 * a special exception, as described in included LICENSE_EXCEPTION.txt.
 */
package net.beaconcontroller.topology;

import java.util.Map;

/**
 *
 *
 * @author David Erickson (daviderickson@cs.stanford.edu)
 */
public interface ITopology {
    /**
     * Query to determine if the specified switch id and port tuple are
     * connected to another switch or not.  If so, this means the link
     * is passing LLDPs properly between two OpenFlow switches.
     * @param idPort
     * @return
     */
    public boolean isInternal(SwitchPortTuple idPort);

    /**
     * Retrieves a map of all known link connections between OpenFlow switches
     * and the last time each link was known to be functioning
     * @return
     */
    public Map<LinkTuple, Long> getLinks();
}
