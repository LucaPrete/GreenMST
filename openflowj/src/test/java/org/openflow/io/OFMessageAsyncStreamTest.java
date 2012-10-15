/**
 * 
 */
package org.openflow.io;

import org.openflow.protocol.*;
import org.openflow.protocol.factory.BasicFactory;

import java.util.*;
import java.nio.channels.*;
import java.net.InetSocketAddress;


import junit.framework.TestCase;

/**
 * @author Rob Sherwood (rob.sherwood@stanford.edu)
 *
 */
public class OFMessageAsyncStreamTest extends TestCase {
    public void testMarshalling() throws Exception {
        OFMessage h = new OFHello();
        
        ServerSocketChannel serverSC = ServerSocketChannel.open();
        serverSC.socket().bind(new java.net.InetSocketAddress(0));
        serverSC.configureBlocking(false);
        
        SocketChannel client = SocketChannel.open(
                new InetSocketAddress("localhost",
                        serverSC.socket().getLocalPort())
                );
        SocketChannel server = serverSC.accept();
        OFMessageAsyncStream clientStream = new OFMessageAsyncStream(client, new BasicFactory());
        OFMessageAsyncStream serverStream = new OFMessageAsyncStream(server, new BasicFactory());
        
        clientStream.write(h);
        while(clientStream.needsFlush()) {
            clientStream.flush();
        }
        List<OFMessage> l = serverStream.read();
        TestCase.assertEquals(l.size(), 1);
        OFMessage m = l.get(0);
        TestCase.assertEquals(m.getLength(),h.getLength());
        TestCase.assertEquals(m.getVersion(), h.getVersion());
        TestCase.assertEquals(m.getType(), h.getType());
        TestCase.assertEquals(m.getType(), h.getType());
    }
}
