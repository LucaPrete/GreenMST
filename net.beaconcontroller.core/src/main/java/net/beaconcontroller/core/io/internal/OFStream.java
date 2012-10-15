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
import java.nio.channels.SocketChannel;
import java.util.List;

import net.beaconcontroller.core.io.OFMessageSafeOutStream;

import org.openflow.protocol.OFMessage;
import org.openflow.protocol.factory.OFMessageFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is a thread-safe implementation of OFMessageAsyncStream, but only
 * for the OutStream portion. The write functions now trigger the select stream
 * to send the messages after being queued.
 * @author David Erickson (daviderickson@cs.stanford.edu)
 *
 */
public class OFStream extends OFMessageAsyncStream implements OFMessageSafeOutStream {
    protected Logger log = LoggerFactory.getLogger(OFStream.class);

    protected SelectionKey key;
    protected IOLoop ioLoop;
    protected boolean writeFailure = false;
    protected boolean needsSelect = false;
    protected boolean wrote = false;
    protected boolean immediate = false;

    /**
     * @param sock
     * @param messageFactory
     * @param key
     * @param selectLoop
     * @throws IOException
     */
    public OFStream(SocketChannel sock, OFMessageFactory messageFactory,
            SelectionKey key, IOLoop selectLoop) throws IOException {
        super(sock, messageFactory);
        this.key = key;
        this.ioLoop = selectLoop;
    }

    /**
     * Buffers a single outgoing openflow message
     */
    @Override
    public void write(OFMessage m) throws IOException {
        synchronized (outBuf) {
            appendMessageToOutBuf(m);
            if (!wrote && !needsSelect) {
                flush();
            }
        }
      }

    /**
     * Buffers a list of OpenFlow messages
     */
    @Override
    public void write(List<OFMessage> l) throws IOException {
        synchronized (outBuf) {
            for (OFMessage m : l) {
                appendMessageToOutBuf(m);
            }
            if (!wrote && !needsSelect) {
                flush();
            }
        }
    }

    /**
     * Flush buffered outgoing data. Keep flushing until needsFlush() returns
     * false. Each flush() corresponds to a SocketChannel.write(), so this is
     * designed for one flush() per select() event
     */
    public void flush() throws IOException {
        synchronized (outBuf) {
            if (wrote || needsSelect)
                return;
            outBuf.flip(); // swap pointers; lim = pos; pos = 0;
            try {
                sock.write(outBuf); // write data starting at pos up to lim
            } catch (IOException e) {
                // Unrecoverable exception, generally the remote switch disconnected
                log.info("Detected remote switch hangup {}", sock);
                this.writeFailure = true;
                // TODO should we propogate this failure?
            }
            if (!immediate)
                wrote = true;
            outBuf.compact();
            if (outBuf.position() > 0) {
                needsSelect = true;
            }
        }
    }

    /**
     * Is there outgoing buffered data that needs to be flush()'d?
     */
    public boolean needsFlush() {
        synchronized (outBuf) {
            return outBuf.position() > 0;
        }
    }

    /**
     * Returns true if there has been a failure to write by the stream, indicating
     * the remote end has been disconnected
     * @return the writeFailure
     */
    public boolean getWriteFailure() {
        return writeFailure;
    }

    /**
     * @return the needsSelect
     */
    public boolean getNeedsSelect() {
        synchronized (outBuf) {
            return needsSelect;
        }
    }

    public void clearWrote() throws IOException {
        synchronized (outBuf) {
            this.wrote = false;
            if (outBuf.position() > 0 && !needsSelect)
                flush();
        }
    }

    public void clearSelect() throws IOException {
        synchronized (outBuf) {
            this.wrote = false;
            this.needsSelect = false;
            if (outBuf.position() > 0)
                flush();
        }
    }

    /**
     * @return the key
     */
    public SelectionKey getKey() {
        return key;
    }

    /**
     * @return the ioLoop
     */
    public IOLoop getIOLoop() {
        return ioLoop;
    }

    /**
     * Returns true if this stream is set to flush on every write, false otherwise
     * @return the immediate
     */
    public boolean getImmediate() {
        return immediate;
    }

    /**
     * Sets the stream to flush on every write, similar to fsync for file systems
     * @param immediate the immediate to set
     */
    public void setImmediate(boolean immediate) {
        this.immediate = immediate;
    }
}
