/**
 * Copyright 2011, Stanford University. This file is licensed under GPL v2 plus
 * a special exception, as described in included LICENSE_EXCEPTION.txt.
 */
package net.beaconcontroller.packet;

/**
*
* @author David Erickson (daviderickson@cs.stanford.edu)
*/
public interface IPacket {
    /**
     * 
     * @return
     */
    public IPacket getPayload();

    /**
     * 
     * @param packet
     * @return
     */
    public IPacket setPayload(IPacket packet);

    /**
     * 
     * @return
     */
    public IPacket getParent();

    /**
     * 
     * @param packet
     * @return
     */
    public IPacket setParent(IPacket packet);

    /**
     * Sets all payloads parent packet if applicable, then serializes this 
     * packet and all payloads
     * @return a byte[] containing this packet and payloads
     */
    public byte[] serialize();

    /**
     * Deserializes this packet layer and all possible payloads
     * @param data
     * @param offset offset to start deserializing from
     * @param length length of the data to deserialize
     * @return the deserialized data
     */
    public IPacket deserialize(byte[] data, int offset, int length);
}
