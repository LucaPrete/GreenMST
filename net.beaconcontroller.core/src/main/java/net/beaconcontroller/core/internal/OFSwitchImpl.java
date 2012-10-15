/**
 * Copyright 2011, Stanford University. This file is licensed under GPL v2 plus
 * a special exception, as described in included LICENSE_EXCEPTION.txt.
 */
package net.beaconcontroller.core.internal;

import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import net.beaconcontroller.core.IBeaconProvider;
import net.beaconcontroller.core.IOFSwitch;
import net.beaconcontroller.core.io.OFMessageSafeOutStream;

import org.openflow.io.OFMessageInStream;
import org.openflow.protocol.OFFeaturesReply;
import org.openflow.protocol.OFStatisticsRequest;
import org.openflow.protocol.OFType;
import org.openflow.protocol.statistics.OFStatistics;
import org.openflow.util.HexString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author David Erickson (daviderickson@cs.stanford.edu)
 */
public class OFSwitchImpl implements IOFSwitch {
    protected static Logger log = LoggerFactory.getLogger(OFSwitchImpl.class);
    protected ConcurrentMap<Object, Object> attributes;
    protected IBeaconProvider beaconProvider;
    protected Date connectedSince;
    protected OFFeaturesReply featuresReply;
    protected OFMessageInStream inStream;
    protected OFMessageSafeOutStream outStream;
    protected long lastReceivedMessageTime;
    protected SocketChannel socketChannel;
    protected AtomicInteger transactionIdSource;

    public OFSwitchImpl() {
        this.attributes = new ConcurrentHashMap<Object, Object>();
        this.connectedSince = new Date();
        this.lastReceivedMessageTime = this.connectedSince.getTime();
        this.transactionIdSource = new AtomicInteger();
    }

    public SocketChannel getSocketChannel() {
        return this.socketChannel;
    }

    public void setSocketChannel(SocketChannel channel) {
        this.socketChannel = channel;
    }

    public OFMessageInStream getInputStream() {
        return inStream;
    }

    public OFMessageSafeOutStream getOutputStream() {
        return outStream;
    }

    public void setInputStream(OFMessageInStream stream) {
        this.inStream = stream;
    }

    public void setOutputStream(OFMessageSafeOutStream stream) {
        this.outStream = stream;
    }

    /**
     *
     */
    public OFFeaturesReply getFeaturesReply() {
        return this.featuresReply;
    }

    /**
     * @param featuresReply the featuresReply to set
     */
    public void setFeaturesReply(OFFeaturesReply featuresReply) {
        this.featuresReply = featuresReply;
    }

    @Override
    public long getId() {
        if (this.featuresReply == null)
            throw new RuntimeException("Features reply has not yet been set");
        return this.featuresReply.getDatapathId();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "OFSwitchImpl [" + socketChannel.socket() + " DPID[" + ((featuresReply != null) ? HexString.toHexString(featuresReply.getDatapathId()) : "?") + "]]";
    }

    @Override
    public ConcurrentMap<Object, Object> getAttributes() {
        return this.attributes;
    }

    @Override
    public Date getConnectedSince() {
        return connectedSince;
    }

    @Override
    public int getNextTransactionId() {
        return this.transactionIdSource.incrementAndGet();
    }

    @Override
    public Future<List<OFStatistics>> getStatistics(OFStatisticsRequest request) throws IOException {
        request.setXid(getNextTransactionId());
        OFStatisticsFuture future = new OFStatisticsFuture(beaconProvider, this, request.getXid());
        this.beaconProvider.addOFMessageListener(OFType.STATS_REPLY, future);
        this.beaconProvider.addOFSwitchListener(future);
        this.getOutputStream().write(request);
        return future;
    }

    /**
     * @param beaconProvider the beaconProvider to set
     */
    public void setBeaconProvider(IBeaconProvider beaconProvider) {
        this.beaconProvider = beaconProvider;
    }

    @Override
    public long getLastReceivedMessageTime() {
        return lastReceivedMessageTime;
    }

    @Override
    public void setLastReceivedMessageTime(long epochMS) {
        this.lastReceivedMessageTime = epochMS;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (!(obj instanceof OFSwitchImpl))
            return false;
        OFSwitchImpl other = (OFSwitchImpl) obj;
        if (this.getId() != other.getId())
            return false;
        return true;
    }
}
