/**
 * Copyright 2011, Stanford University. This file is licensed under GPL v2 plus
 * a special exception, as described in included LICENSE_EXCEPTION.txt.
 */
package net.beaconcontroller.web.view.json;

import org.codehaus.jackson.map.annotate.JsonSerialize;


/**
 * Serialization support for dealing with signed->unsigned
 * @author David Erickson (daviderickson@cs.stanford.edu)
 */
public abstract class OFFlowStatisticsReplyMixin {
    @JsonSerialize(using=UnsignedByteJsonSerializer.class)
    public abstract byte getTableId();

    @JsonSerialize(using=UnsignedIntegerJsonSerializer.class)
    public abstract int getDurationSeconds();

    @JsonSerialize(using=UnsignedIntegerJsonSerializer.class)
    public abstract int getDurationNanoseconds();

    @JsonSerialize(using=UnsignedShortJsonSerializer.class)
    public abstract short getPriority();

    @JsonSerialize(using=UnsignedShortJsonSerializer.class)
    public abstract short getIdleTimeout();

    @JsonSerialize(using=UnsignedShortJsonSerializer.class)
    public abstract short getHardTimeout();

    @JsonSerialize(using=UnsignedLongJsonSerializer.class)
    public abstract long getCookie();

    @JsonSerialize(using=UnsignedLongJsonSerializer.class)
    public abstract long getPacketCount();

    @JsonSerialize(using=UnsignedLongJsonSerializer.class)
    public abstract long getByteCount();
}
