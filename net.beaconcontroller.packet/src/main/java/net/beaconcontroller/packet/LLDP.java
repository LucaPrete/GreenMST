/**
 * Copyright 2011, Stanford University. This file is licensed under GPL v2 plus
 * a special exception, as described in included LICENSE_EXCEPTION.txt.
 */
/**
 * 
 */
package net.beaconcontroller.packet;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * @author David Erickson (daviderickson@cs.stanford.edu)
 *
 */
public class LLDP extends BasePacket {
    protected LLDPTLV chassisId;
    protected LLDPTLV portId;
    protected LLDPTLV ttl;
    protected List<LLDPTLV> optionalTLVList;

    /**
     * @return the chassisId
     */
    public LLDPTLV getChassisId() {
        return chassisId;
    }

    /**
     * @param chassisId the chassisId to set
     */
    public LLDP setChassisId(LLDPTLV chassisId) {
        this.chassisId = chassisId;
        return this;
    }

    /**
     * @return the portId
     */
    public LLDPTLV getPortId() {
        return portId;
    }

    /**
     * @param portId the portId to set
     */
    public LLDP setPortId(LLDPTLV portId) {
        this.portId = portId;
        return this;
    }

    /**
     * @return the ttl
     */
    public LLDPTLV getTtl() {
        return ttl;
    }

    /**
     * @param ttl the ttl to set
     */
    public LLDP setTtl(LLDPTLV ttl) {
        this.ttl = ttl;
        return this;
    }

    /**
     * @return the optionalTLVList
     */
    public List<LLDPTLV> getOptionalTLVList() {
        return optionalTLVList;
    }

    /**
     * @param optionalTLVList the optionalTLVList to set
     */
    public LLDP setOptionalTLVList(List<LLDPTLV> optionalTLVList) {
        this.optionalTLVList = optionalTLVList;
        return this;
    }

    @Override
    public byte[] serialize() {
        int length = 2+this.chassisId.getLength() + 2+this.portId.getLength() +
            2+this.ttl.getLength() + 2;
        if (this.optionalTLVList != null) {
            for (LLDPTLV tlv : this.optionalTLVList) {
                length += 2 + tlv.getLength();
            }
        }

        byte[] data = new byte[length];
        ByteBuffer bb = ByteBuffer.wrap(data);
        bb.put(this.chassisId.serialize());
        bb.put(this.portId.serialize());
        bb.put(this.ttl.serialize());
        if (this.optionalTLVList != null) {
            for (LLDPTLV tlv : this.optionalTLVList) {
                bb.put(tlv.serialize());
            }
        }
        bb.putShort((short) 0); // End of LLDPDU

        if (this.parent != null && this.parent instanceof Ethernet)
            ((Ethernet)this.parent).setEtherType(Ethernet.TYPE_LLDP);

        return data;
    }

    @Override
    public IPacket deserialize(byte[] data, int offset, int length) {
        ByteBuffer bb = ByteBuffer.wrap(data, offset, length);
        LLDPTLV tlv;
        do {
            tlv = new LLDPTLV().deserialize(bb);

            // if there was a failure to deserialize stop processing TLVs
            if (tlv == null)
                break;
            switch (tlv.getType()) {
                case 0x0:
                    // can throw this one away, its just an end delimiter
                    break;
                case 0x1:
                    this.chassisId = tlv;
                    break;
                case 0x2:
                    this.portId = tlv;
                    break;
                case 0x3:
                    this.ttl = tlv;
                    break;
                default:
                    if (this.optionalTLVList == null)
                        this.optionalTLVList = new ArrayList<LLDPTLV>();
                    this.optionalTLVList.add(tlv);
                    break;
            }
        } while (tlv.getType() != 0 && bb.hasRemaining());
        return this;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 883;
        int result = super.hashCode();
        result = prime * result
                + ((chassisId == null) ? 0 : chassisId.hashCode());
        result = prime * result
                + ((optionalTLVList == null) ? 0 : optionalTLVList.hashCode());
        result = prime * result + ((portId == null) ? 0 : portId.hashCode());
        result = prime * result + ((ttl == null) ? 0 : ttl.hashCode());
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (!(obj instanceof LLDP))
            return false;
        LLDP other = (LLDP) obj;
        if (chassisId == null) {
            if (other.chassisId != null)
                return false;
        } else if (!chassisId.equals(other.chassisId))
            return false;
        if (optionalTLVList == null) {
            if (other.optionalTLVList != null)
                return false;
        } else if (!optionalTLVList.equals(other.optionalTLVList))
            return false;
        if (portId == null) {
            if (other.portId != null)
                return false;
        } else if (!portId.equals(other.portId))
            return false;
        if (ttl == null) {
            if (other.ttl != null)
                return false;
        } else if (!ttl.equals(other.ttl))
            return false;
        return true;
    }
}
