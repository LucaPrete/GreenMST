/**
 * Copyright 2011, Stanford University. This file is licensed under GPL v2 plus
 * a special exception, as described in included LICENSE_EXCEPTION.txt.
 */
/**
 *
 */
package net.beaconcontroller.core.io.internal;

import java.io.IOException;
import java.nio.channels.SelectionKey;

/**
 * @author Rob Sherwood (rob.sherwood@stanford.edu)
 *
 */
public interface SelectListener {
    /**
     * Tell the select listener that an event took place on the passed object
     * @param key the key used on the select
     * @param arg some parameter passed by the caller when registering
     * @throws IOException
     */
    void handleEvent(SelectionKey key, Object arg) throws IOException;
}
