<<<<<<< HEAD
=======
/**
 * Copyright (C) 2013 Luca Prete, Andrea Biancini, Fabio Farina - www.garr.it - Consortium GARR
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * Implementation of the Floodlight GreenMST service.
 * 
 * @author Luca Prete <luca.prete@garr.it>
 * @author Andrea Biancini <andrea.biancini@garr.it>
 * @author Fabio Farina <fabio.farina@garr.it>
 * 
 * @version 0.90
 * @see it.garr.greenmst.GreenMST
 * @see it.garr.greenmst.IGreenMSTService
 * @see it.garr.greenmst.TopologyCostsLoader
 * @see it.garr.greenmst.algorithms.IMinimumSpanningTreeAlgorithm
 * @see it.garr.greenmst.algorithms.KruskalAlgorithm
 * @see it.garr.greenmst.types.LinkWithCost
 * @see it.garr.greenmst.types.TopologyCosts
 * @see it.garr.greenmst.web.GreenMSTWebRoutable
 * @see it.garr.greenmst.web.MSTEdgesResource
 * @see it.garr.greenmst.web.RedundantEdgesResource
 * @see it.garr.greenmst.web.TopoCostsResource
 * @see it.garr.greenmst.web.TopoEdgesResource
 * @see it.garr.greenmst.web.serializers.LinkWithCostJSONSerializer
 * @see it.garr.greenmst.web.serializers.TopologyCostsJSONDeserializer
 * @see it.garr.greenmst.web.serializers.TopologyCostsJSONSerializer
 * 
 */

>>>>>>> 68694ed7fdf8eb31c74ee228389dee4fec026f63
package it.garr.greenmst;

import static org.easymock.EasyMock.capture;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import it.garr.greenmst.types.LinkWithCost;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import net.floodlightcontroller.core.FloodlightContext;
import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.internal.OFSwitchImpl;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.test.FloodlightTestCase;

import org.easymock.Capture;
import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.openflow.protocol.OFMatch;
import org.openflow.protocol.OFPhysicalPort;
import org.openflow.protocol.OFPhysicalPort.OFPortConfig;
import org.openflow.protocol.OFPortMod;
import org.openflow.util.HexString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GreenMSTTest extends FloodlightTestCase {
	
	protected static Logger logger = LoggerFactory.getLogger(GreenMSTTest.class);
	protected GreenMST greenMST = null;
	
	@Before
    public void setUp() throws Exception {
		super.setUp();
		greenMST = new GreenMST();
		
		mockFloodlightProvider = getMockFloodlightProvider();
		FloodlightModuleContext context = new FloodlightModuleContext();
		context.addService(IFloodlightProviderService.class, mockFloodlightProvider);
        
		greenMST.init(context);
        logger.info("Ended startUp.");
    }
	
	@Test
	public void testUpdateLinks() throws Exception {
		Map<Long, IOFSwitch> switches = new HashMap<Long, IOFSwitch>();
		Map<Long, Vector<byte[]>> hwAddrsSw = new HashMap<Long, Vector<byte[]>>();
		Map<Long, Capture<OFPortMod>> msgCaptures = new HashMap<Long, Capture<OFPortMod>>();
		
		for (Long swId = 1L; swId <= 4L; ++swId) {
			Capture<OFPortMod> msgCapture = new Capture<OFPortMod>();
			Vector<byte[]> hwAddrs = new Vector<byte[]>();
			Vector<Short> portNums = new Vector<Short>();
			
			for (short i = 1; i <= 3; ++i) {
				byte[] hwAddr = new byte[6];
				for (int j = 0; j < hwAddr.length; ++j) hwAddr[j] = (byte) Math.round(Math.random() * 256);
				hwAddrs.add(hwAddr);
				portNums.add(i);
			}
			
			IOFSwitch sw = createSwitch(swId, portNums, hwAddrs, msgCapture);
			hwAddrsSw.put(swId, hwAddrs);
			switches.put(swId, sw);
			msgCaptures.put(swId, msgCapture);
		}
    	
		mockFloodlightProvider.setSwitches(switches);
		
		addLinkToCollection(greenMST.topoEdges, 1L, 1, 2L, 1, 1);
		addLinkToCollection(greenMST.topoEdges, 1L, 2, 3L, 1, 4);
		addLinkToCollection(greenMST.topoEdges, 1L, 3, 4L, 1, 2);
		addLinkToCollection(greenMST.topoEdges, 2L, 2, 3L, 2, 3);
		addLinkToCollection(greenMST.topoEdges, 2L, 3, 4L, 2, 4);
		addLinkToCollection(greenMST.topoEdges, 3L, 3, 4L, 3, 1);
		
		for (Long swId = 1L; swId <= 4L; ++swId) replay(switches.get(swId));
		greenMST.updateLinks();
		for (Long swId = 1L; swId <= 4L; ++swId) verify(switches.get(swId));
		
		HashSet<LinkWithCost> expectedRedundantEdges = new HashSet<LinkWithCost>();
		addLinkToCollection(expectedRedundantEdges, 1L, 2, 3L, 1, 4);
		addLinkToCollection(expectedRedundantEdges, 2L, 3, 4L, 2, 4);
		addLinkToCollection(expectedRedundantEdges, 2L, 2, 3L, 2, 3);
			
		assertEquals("Redundant edges computed are right in number.", expectedRedundantEdges.size(), greenMST.redundantEdges.size());
		
		for (LinkWithCost expectedLink : expectedRedundantEdges) {
			assertTrue("Redundant edges computed contains the expected link " + expectedLink + ".", greenMST.redundantEdges.contains(expectedLink));
			
			for (LinkWithCost curLink : greenMST.redundantEdges) {
				if (curLink.equals(expectedLink)) {
					assertEquals("The cost for the rendundant edge link is correct.", expectedLink.getCost(), curLink.getCost());
				}
			}
		}
		
		verifyPortMod(msgCaptures.get(1L).getValue(), hwAddrsSw.get(1L).get(1), (short) 2, 63);
		verifyPortMod(msgCaptures.get(2L).getValue(), hwAddrsSw.get(2L).get(2), (short) 3, 63);
		verifyPortMod(msgCaptures.get(3L).getValue(), hwAddrsSw.get(3L).get(0), (short) 1, 63);
		verifyPortMod(msgCaptures.get(4L).getValue(), hwAddrsSw.get(4L).get(1), (short) 2, 63);
		
		logger.info("Ended testUpdateLinks.");
	}
	
	@Test
	public void testFindRedundantEdges() throws Exception {
		addLinkToCollection(greenMST.topoEdges, 1L, 1, 2L, 1, 1);
		addLinkToCollection(greenMST.topoEdges, 1L, 2, 3L, 1, 4);
		addLinkToCollection(greenMST.topoEdges, 1L, 3, 4L, 1, 2);
		addLinkToCollection(greenMST.topoEdges, 2L, 2, 3L, 2, 3);
		addLinkToCollection(greenMST.topoEdges, 2L, 3, 4L, 2, 4);
		addLinkToCollection(greenMST.topoEdges, 3L, 3, 4L, 3, 1);
		
		Vector<LinkWithCost> mstEdges = new Vector<LinkWithCost>();
		addLinkToCollection(mstEdges, 1L, 1, 2L, 1, 1);
		addLinkToCollection(mstEdges, 1L, 3, 4L, 1, 2);
		addLinkToCollection(mstEdges, 3L, 3, 4L, 3, 1);
		
		HashSet<LinkWithCost> redundantEdges = greenMST.findRedundantEdges(mstEdges);
		
		HashSet<LinkWithCost> expectedRedundantEdges = new HashSet<LinkWithCost>();
		addLinkToCollection(expectedRedundantEdges, 1L, 2, 3L, 1, 4);
		addLinkToCollection(expectedRedundantEdges, 2L, 3, 4L, 2, 4);
		addLinkToCollection(expectedRedundantEdges, 2L, 2, 3L, 2, 3);
			
		assertEquals("Redundant edges computed are right in number.", expectedRedundantEdges.size(), redundantEdges.size());
		
		for (LinkWithCost expectedLink : expectedRedundantEdges) {
			assertTrue("Redundant edges computed contains the expected link " + expectedLink + ".", redundantEdges.contains(expectedLink));
			
			for (LinkWithCost curLink : redundantEdges) {
				if (curLink.equals(expectedLink)) {
					assertEquals("The cost for the rendundant edge link is correct.", expectedLink.getCost(), curLink.getCost());
				}
			}
		}
		
		logger.info("Ended testFindRedundantEdges.");
	}
	
	@Test
	public void testModPortOpen() throws Exception {
		testModPort(true);
		logger.info("Ended testModPortOpen.");
	}
	
	@Test
	public void testModPortClose() throws Exception {
		testModPort(false);
		logger.info("Ended testModPortClose.");
	}
	
	private void testModPort(boolean open) throws Exception {
		Map<Long, IOFSwitch> switches = new HashMap<Long, IOFSwitch>();
		Capture<OFPortMod> msgCapture = new Capture<OFPortMod>();
		
		Vector<byte[]> hwAddrs = new Vector<byte[]>();
		byte[] hwAddr = new byte[6];
    	for (int j = 0; j < hwAddr.length; ++j) hwAddr[j] = (byte) Math.round(Math.random() * 256);
    	hwAddrs.add(hwAddr);
    	
    	Vector<Short> portNums = new Vector<Short>();
    	portNums.add((short) 2);
    	
		IOFSwitch sw = createSwitch(1L, portNums, hwAddrs, msgCapture);
		switches.put(1L, sw);
		mockFloodlightProvider.setSwitches(switches);
        
        replay(sw);
		greenMST.modPort(1L, (short) 2, open);
		verify(sw);
		verifyPortMod(msgCapture.getValue(), hwAddr, (short) 2, (open == true) ? 0 : 63);
	}
	
	private void verifyPortMod(OFPortMod msg, byte[] hwAddr, short portNum, int config) {
        assertEquals("The message does not have the right hardware address.", hwAddr, msg.getHardwareAddress());
        assertEquals("The message does not have the right hardware port number.", portNum, msg.getPortNumber());
        assertEquals("The message does not have the right mask.", OFPortConfig.OFPPC_NO_FLOOD.getValue(), msg.getMask());
        assertEquals("The message does not have the right config value.", config, msg.getConfig());
	}
	
	private void addLinkToCollection(Collection<LinkWithCost> topoEdges, long switchFrom, int portFrom, long switchTo, int portTo, int cost) {
		LinkWithCost link = new LinkWithCost(switchFrom, portFrom, switchTo, portTo, cost);
		
		logger.debug("Link added: {}.", new Object[] { link });
        topoEdges.add(link);
        
        logger.debug("Link added: {}.", new Object[] { link.getInverse() });
        topoEdges.add(link.getInverse());
	}
	
	private IOFSwitch createSwitch(long switchId, List<Short> portNums, List<byte[]> hwAddrs, Capture<OFPortMod> msgCapture) throws Exception {
        List<OFPhysicalPort> ports = new Vector<OFPhysicalPort>();
        
        for (int i = 0; i < portNums.size(); ++i) {
        	OFPhysicalPort curPort = new OFPhysicalPort();
        	curPort.setHardwareAddress(hwAddrs.get(i));
        	curPort.setPortNumber(portNums.get(i));
        	ports.add(curPort);
        }
		
		IOFSwitch sw = EasyMock.createMock(OFSwitchImpl.class);
		expect(sw.getId()).andReturn(switchId).anyTimes();
        expect(sw.getBuffers()).andReturn(1000).anyTimes();
        expect(sw.getStringId()).andReturn(HexString.toHexString(switchId)).anyTimes();
        expect(sw.getPorts()).andReturn(ports).anyTimes();
        expect(sw.getFeaturesReplyFromSwitch()).andReturn(null).anyTimes();
        
        Capture<FloodlightContext> context = new Capture<FloodlightContext>();
        sw.write(capture(msgCapture), capture(context));
        expectLastCall().anyTimes();
        
        int fastWildcards = 
                OFMatch.OFPFW_IN_PORT | 
                OFMatch.OFPFW_NW_PROTO | 
                OFMatch.OFPFW_TP_SRC | 
                OFMatch.OFPFW_TP_DST | 
                OFMatch.OFPFW_NW_SRC_ALL | 
                OFMatch.OFPFW_NW_DST_ALL |
                OFMatch.OFPFW_NW_TOS;

        expect(sw.getAttribute(IOFSwitch.PROP_FASTWILDCARDS)).andReturn((Integer)fastWildcards).anyTimes();
        expect(sw.hasAttribute(IOFSwitch.PROP_SUPPORTS_OFPP_TABLE)).andReturn(true).anyTimes();
        
        return sw;
	}
	
}
