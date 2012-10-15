/**
 * Copyright 2011, Stanford University. This file is licensed under GPL v2 plus
 * a special exception, as described in included LICENSE_EXCEPTION.txt.
 */
package net.beaconcontroller.web.view.json;

import org.codehaus.jackson.map.annotate.JsonSerialize;


/**
 * Serialization support
 * @author David Erickson (daviderickson@cs.stanford.edu)
 *
 */
public abstract class OFMatchMixin {

    @JsonSerialize(using=ByteArrayJsonSerializer.class)
    public abstract byte[] getDataLayerDestination();

    @JsonSerialize(using=ByteArrayJsonSerializer.class)
    public abstract byte[] getDataLayerSource();

    @JsonSerialize(using=UnsignedShortJsonSerializer.class)
    public abstract short getDataLayerType();

    @JsonSerialize(using=UnsignedShortJsonSerializer.class)
    public abstract short getDataLayerVirtualLan();

    @JsonSerialize(using=UnsignedByteJsonSerializer.class)
    public abstract byte getDataLayerVirtualLanPriorityCodePoint();

    @JsonSerialize(using=UnsignedShortJsonSerializer.class)
    public abstract short getInputPort();

    @JsonSerialize(using=UnsignedIntegerJsonSerializer.class)
    public abstract int getNetworkDestination();

    @JsonSerialize(using=UnsignedByteJsonSerializer.class)
    public abstract byte getNetworkProtocol();

    @JsonSerialize(using=UnsignedIntegerJsonSerializer.class)
    public abstract int getNetworkSource();

    @JsonSerialize(using=UnsignedByteJsonSerializer.class)
    public abstract byte getNetworkTypeOfService();

    @JsonSerialize(using=UnsignedShortJsonSerializer.class)
    public abstract short getTransportDestination();

    @JsonSerialize(using=UnsignedShortJsonSerializer.class)
    public abstract short getTransportSource();

    @JsonSerialize(using=UnsignedIntegerJsonSerializer.class)
    public abstract int getWildcards();
}
