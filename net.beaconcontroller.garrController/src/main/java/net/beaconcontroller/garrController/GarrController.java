/**
 * This file is licensed under GPL v2 plus.
 * 
 * A special exception, as described in included LICENSE_EXCEPTION.txt.
 * 
 * This file is a modified version of net.beaconcontroller.learningswitch.LearningSwitch.java
 * 
 */

package net.beaconcontroller.garrController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openflow.protocol.OFMatch;
import org.openflow.protocol.OFMessage;
import org.openflow.protocol.OFPacketIn;
import org.openflow.protocol.OFPacketOut;
import org.openflow.protocol.OFPort;
import org.openflow.protocol.OFType;
import org.openflow.protocol.OFFlowMod;
import org.openflow.protocol.action.OFAction;
import org.openflow.protocol.action.OFActionDataLayerDestination;
import org.openflow.protocol.action.OFActionOutput;
import org.openflow.util.HexString;
import org.openflow.util.U16;
import org.openflow.util.U8;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.beaconcontroller.core.IOFMessageListener;
import net.beaconcontroller.core.IOFSwitch;
import net.beaconcontroller.core.IBeaconProvider;
import net.beaconcontroller.devicemanager.Device;
import net.beaconcontroller.devicemanager.IDeviceManager;
import net.beaconcontroller.devicemanager.IDeviceManagerAware;
import net.beaconcontroller.core.IOFSwitchListener;
import net.beaconcontroller.packet.*;
import net.beaconcontroller.util.LongShortHopscotchHashMap;

/**
* 
* @author David Erickson (daviderickson@cs.stanford.edu) - 04/04/10
* @author Luca Prete (luca.prete@garr.it) - 15/10/12
* @author Andrea Biancini (andrea.biancini@mib.infn.it) - 15/10/12
* @author Fabio Farina (fabio.farina@garr.it) - 15/10/12
* 
*/

public class GarrController implements IOFMessageListener, IOFSwitchListener, IDeviceManagerAware {
    
    protected IBeaconProvider beaconProvider;
    protected IDeviceManager deviceManager;
    protected static Logger logger = LoggerFactory.getLogger(GarrController.class);
    protected Map<IOFSwitch, LongShortHopscotchHashMap> macTables =
              new HashMap<IOFSwitch, LongShortHopscotchHashMap>();
    private RouterMap routerMap = new RouterMap();
    private SwitchMap switchMap = new SwitchMap();
    
    // USER CONFIG 1 (follow...)
    private final int research = 35000;
    private final int ip = 0x800;
    private final int tcp = 6;
    
    // END USER CONFIG 1
    
    public IBeaconProvider getBeaconProvider() {
        return beaconProvider;
    }
    
    public void setBeaconProvider(IBeaconProvider beaconProvider) {
        this.beaconProvider = beaconProvider;
    }
    
    /**
     * @param deviceManager the deviceManager to set
     */
    public void setDeviceManager(IDeviceManager deviceManager) {
        this.deviceManager = deviceManager;
    }

    public void startUp() {
        logger.info(getName() + " - starting...");
        beaconProvider.addOFMessageListener(OFType.PACKET_IN, this);
        beaconProvider.addOFSwitchListener(this);
        // USER CONFIG 2 (end...)
        // ROUTER (Mandatory) - Name, MAC, Priority (Master router has higher priority)
        routerMap.addRouter(new GarrRouter("Vyatta01", "00:50:56:8a:14:13", 100));
        routerMap.addRouter(new GarrRouter("Vyatta02", "00:50:56:8a:14:20", 50));
        // SWITCH (Optional) - Name, id
        switchMap.addSwitch(new GarrSwitch("OVS01", 52239747210L));
        switchMap.addSwitch(new GarrSwitch("OVS02", 52237674301L));
        switchMap.addSwitch(new GarrSwitch("OVS03", 52237092159L));
        switchMap.addSwitch(new GarrSwitch("OVS04", 52243146544L));
        logger.info(getName() + " - started and ready...");
        // END USER CONFIG 2
    }

    public void shutDown() {
        logger.info(getName() + " - stopping...");
        beaconProvider.removeOFMessageListener(OFType.PACKET_IN, this);
        logger.info(getName() + " - stopped...");
    }
    
    public String getName() {
        return "GARR modified L2Switch";
    }
    
    public Command receive(IOFSwitch sw, OFMessage msg) {
        
        OFPacketIn pi = (OFPacketIn)msg;
        OFMatch match = new OFMatch();
        match.loadFromPacket(pi.getPacketData(), pi.getInPort());
        long switchId = sw.getId();
        
        byte[] dlSrcByte = match.getDataLayerSource();
        long dlSrcLong = Ethernet.toLong(dlSrcByte);
        String dlSrcString = HexString.toHexString(match.getDataLayerSource());
        String dlDstMasterString = HexString.toHexString(match.getDataLayerDestination());
        
        // If there's a new switch add the MAC table for it
        LongShortHopscotchHashMap macTable = macTables.get(sw);
        if (macTable == null) {
            macTable = new LongShortHopscotchHashMap();
            macTables.put(sw, macTable);
        }
        
        int bufferId = pi.getBufferId();
        
        /* Research traffic logger - Informations are printed if:
         * - source MAC is not the MAC of my rotuer
         * - transport source port is different from 22 (to avoid issues with SSH tunnels)
         * - transport destination is > 35000 (typical of research grid traffic)
         */
        if ((GarrUtil.shortToInt(match.getTransportSource()) != 22) &&
            (GarrUtil.shortToInt(match.getTransportDestination()) >= research) &&
            (routerMap.macExists(dlSrcString)==false)) {
                logger.debug("\n\n###############################################\n" +
                                 "#####   New research traffic going out    #####\n" +
                                 "###############################################\n" +
                                 "Values: \n" +
                                 "MAC source: "+ dlSrcString + " (" + routerMap.getNameByMac(dlSrcString) + ")\n" +
                                 "MAC destination: "+ dlDstMasterString + " (" + routerMap.getNameByMac(dlDstMasterString) + ")\n" +
                                 "IP source: "+ IPv4.fromIPv4Address(match.getNetworkSource()) + "\n" +
                                 "IP destination: "+ IPv4.fromIPv4Address(match.getNetworkDestination()) + "\n" +
                                 "Port source: " + GarrUtil.shortToInt(match.getTransportSource()) + "\n" +
                                 "Port destination: " + GarrUtil.shortToInt(match.getTransportDestination()) + "\n" +
                                 "Switch: " + switchId + " (" + switchMap.getNameById(switchId) + ")\n" +
                                 "##############################################\n");
        }

        // if the src is not multicast, learn it
        if ((dlSrcByte[0] & 0x1) == 0 && dlSrcLong != 0) {
            if (!macTable.contains(dlSrcLong) ||
                    macTable.get(dlSrcLong) != pi.getInPort()) {
                macTable.put(dlSrcLong, pi.getInPort());
//                logger.debug("GARR - New MAC " + dlSrcString + " added to MAC table of switch " + sw);
            }
        }
        
        // RESEARCH TRAFFIC CONTROLLER
        /* Procedure of flowWriting and packetOut modifying the destination router is performed if:
         * - source MAC is not the MAC of my rotuer
         * - transport source port is different from 22 (to avoid issues with SSH tunnels)
         * - transport destination is > research (values > 35000 are typical for research grid traffic)
         * - dl_dst of the match equals masdter router MAC
         */
        if ((GarrUtil.shortToInt(match.getTransportSource()) != 22) &&
            (GarrUtil.shortToInt(match.getTransportDestination()) >= research) &&
            (dlDstMasterString.equals(routerMap.getMasterRouter().getMac())) &&
            (routerMap.macExists(dlSrcString)==false)) {
            
            // Declaration of outport - for the moment unknown
            short outPort = -1;
            
            // Declaration of the secondary (slave) router MAC destination 
            byte[] dlDstSlaveByte = HexString.fromHexString(routerMap.getSlaveRouter().getMac());
            long dlDstSlaveLong = Ethernet.toLong(dlDstSlaveByte);
            
            // If the destination is not multicast, look it up
            if ((dlDstSlaveByte[0] & 0x1) == 0 && dlDstSlaveLong != 0) {
                outPort = macTable.get(dlDstSlaveLong);
            }
            
            if (outPort != -1) {
                // If out port equals in port of the packet, don't do anything
                if (outPort == pi.getInPort()) {
                    return Command.CONTINUE;
                }
                
                // If out port is known build the flowMod and write it on the switch
                 
                // Set flow match
                OFMatch rule = new OFMatch();
                rule.setWildcards(OFMatch.OFPFW_ALL ^ (OFMatch.OFPFW_DL_DST | OFMatch.OFPFW_DL_TYPE | OFMatch.OFPFW_NW_PROTO | OFMatch.OFPFW_TP_DST))
                    .setInputPort(pi.getInPort())
                    .setDataLayerDestination(routerMap.getMasterRouter().getMac())
                    .setDataLayerType(U16.t(ip))
                    .setNetworkProtocol(U8.t(U16.t(tcp)))
                    .setNetworkSource(match.getNetworkSource())
                    .setTransportDestination(U16.t(research));

                // Set flow action
                List<OFAction> actions = new ArrayList<OFAction>();
                
                OFActionDataLayerDestination changeMacAction = new OFActionDataLayerDestination();
                changeMacAction.setDataLayerAddress(dlDstSlaveByte);
                
                OFActionOutput setOutPort = new OFActionOutput();
                setOutPort.setPort(outPort);
                
                actions.add(changeMacAction);
                actions.add(setOutPort);
                
                // Build of the flow and sending it to the switch
                OFFlowMod fm = (OFFlowMod) sw.getInputStream().getMessageFactory().getMessage(OFType.FLOW_MOD);
                  fm.setBufferId(bufferId)
                    .setCommand(OFFlowMod.OFPFC_ADD)
                    .setMatch(rule)
                    .setIdleTimeout((short) 60)
                    .setHardTimeout((short) 0)
                    .setOutPort((short) OFPort.OFPP_NONE.getValue())
                    .setPriority((short)50)
                    .setActions(actions)
                    .setLength(U16.t(OFFlowMod.MINIMUM_LENGTH+OFActionDataLayerDestination.MINIMUM_LENGTH+OFActionOutput.MINIMUM_LENGTH));
                try {
                    logger.info("Sending rule to the switch - Actions: " + fm.toString() + ".");
                    sw.getOutputStream().write(fm);
                } catch (IOException ioE) {
                    logger.error("Error while writing the new flow.", ioE);
                }
            }

            if(outPort == -1 || pi.getBufferId() == 0xffffffff){
                
                // Set packet out actions
                List<OFAction> actions = new ArrayList<OFAction>();
                
                OFActionDataLayerDestination changeMacAction = new OFActionDataLayerDestination();
                changeMacAction.setDataLayerAddress(dlDstSlaveByte);
                
                OFActionOutput setOutPort = new OFActionOutput()
                .setPort((short) ((outPort == -1) ? OFPort.OFPP_FLOOD.getValue() : outPort));
                
                actions.add(changeMacAction);
                actions.add(setOutPort);
                
                // build packet out
                OFPacketOut po = new OFPacketOut()
                    .setBufferId(bufferId)
                    .setInPort(pi.getInPort())
                    .setActions(actions)
                    .setActionsLength((short) (OFActionOutput.MINIMUM_LENGTH+OFActionDataLayerDestination.MINIMUM_LENGTH));
    
                // set data if it is included in the packetIn
                if (bufferId == 0xffffffff) {
                    byte[] packetData = pi.getPacketData();
                    po.setLength(U16.t(OFPacketOut.MINIMUM_LENGTH
                            + po.getActionsLength() + packetData.length));
                    po.setPacketData(packetData);
                    
                } else {
                    po.setLength(U16.t(OFPacketOut.MINIMUM_LENGTH
                            + po.getActionsLength()));
                }
                try {
//                    logger.debug("Switch " + sw.getId() + " doesn't have entry for MAC " + routerMap.getSlaveRouter().getMac() + ". Sending packetOut message: " + po.toString());
                    sw.getOutputStream().write(po);
                } catch (IOException e) {
                    logger.error("Failure writing PacketOut " + po.toString(), e);
                }
            }
        }else{
            
         // L2 LEARNING SWITCH
         // Build the Match
            match = new OFMatch();
            match.loadFromPacket(pi.getPacketData(), pi.getInPort());
            byte[] dlDstMasterByte = match.getDataLayerDestination();
            dlSrcByte = match.getDataLayerSource();
            dlSrcLong = Ethernet.toLong(dlSrcByte);
            bufferId = pi.getBufferId();

            // if the src is not multicast, learn it
            if ((dlSrcByte[0] & 0x1) == 0 && dlSrcLong != 0) {
                if (!macTable.contains(dlSrcLong) ||
                        macTable.get(dlSrcLong) != pi.getInPort()) {
                    macTable.put(dlSrcLong, pi.getInPort());
                }
            }

            short outPort = -1;
            long dlDstLong = Ethernet.toLong(dlDstMasterByte);
            // if the destination is not multicast, look it up
            if ((dlDstMasterByte[0] & 0x1) == 0 && dlDstLong != 0) {
                outPort = macTable.get(dlDstLong);
            }

            // push a flow mod if we know where the destination lives
            if (outPort != -1) {
                if (outPort == pi.getInPort()) {
                    // don't send out the port it came in
                    return Command.CONTINUE;
                }
                match.setInputPort(pi.getInPort());

                // build action
                OFActionOutput action = new OFActionOutput()
                    .setPort(outPort);

                // build flow mod
                OFFlowMod fm = (OFFlowMod) sw.getInputStream().getMessageFactory()
                        .getMessage(OFType.FLOW_MOD);
                fm.setBufferId(bufferId)
                    .setIdleTimeout((short) 5)
                    .setOutPort((short) OFPort.OFPP_NONE.getValue())
                    .setMatch(match)
                    .setActions(Collections.singletonList((OFAction)action))
                    .setLength(U16.t(OFFlowMod.MINIMUM_LENGTH+OFActionOutput.MINIMUM_LENGTH));
                try {
//                	logger.debug("Sending FlowMod " + fm.toString() + " to switch " + sw.getId());
                    sw.getOutputStream().write(fm);
                } catch (IOException e) {
                    logger.error("Failure writing FlowMod " + fm.toString(), e);
                }
            }

            // Send a packet out
            if (outPort == -1 || pi.getBufferId() == 0xffffffff) {
                // build action
                OFActionOutput action = new OFActionOutput()
                    .setPort((short) ((outPort == -1) ? OFPort.OFPP_FLOOD
                        .getValue() : outPort));

                // build packet out
                OFPacketOut po = new OFPacketOut()
                    .setBufferId(bufferId)
                    .setInPort(pi.getInPort())
                    .setActions(Collections.singletonList((OFAction)action))
                    .setActionsLength((short) OFActionOutput.MINIMUM_LENGTH);

                // set data if it is included in the packetin
                if (bufferId == 0xffffffff) {
                    byte[] packetData = pi.getPacketData();
                    po.setLength(U16.t(OFPacketOut.MINIMUM_LENGTH
                            + po.getActionsLength() + packetData.length));
                    po.setPacketData(packetData);
                } else {
                    po.setLength(U16.t(OFPacketOut.MINIMUM_LENGTH
                            + po.getActionsLength()));
                }

                try {
//                    logger.debug("Writing packetOut: " + po.toString() + " on switch " + sw.getId());
                    sw.getOutputStream().write(po);
                } catch (IOException e) {
                    logger.error("Failure writing PacketOut " + po.toString(), e);
                }
            }
        }
        return Command.CONTINUE;
    }
    
    @Override
    public void addedSwitch(IOFSwitch sw) {
        // NOOP
    }

    @Override
    public void removedSwitch(IOFSwitch sw) {
        if (macTables.remove(sw) != null)
            logger.debug("Removed L2 table for switch {}", sw);
    }

    @Override
    public void deviceAdded(Device device) {
        // NOOP
    }

    @Override
    public void deviceRemoved(Device device) {
        for(IOFSwitch swi : macTables.keySet()){
            LongShortHopscotchHashMap macTab = macTables.get(swi);
            long macDevice = HexString.toLong(HexString.toHexString(device.getDataLayerAddress()));
            if(macTab.contains(macDevice)){
                macTab.put(macDevice, (short)-1);
            }
        }
    }

    @Override
    public void deviceMoved(Device device, IOFSwitch oldSw, Short oldPort,
            IOFSwitch sw, Short port) {
        deviceRemoved(device);
    }

    @Override
    public void deviceNetworkAddressAdded(Device device,
            Set<Integer> networkAddresses, Integer networkAddress) {
        // NOOP
    }

    @Override
    public void deviceNetworkAddressRemoved(Device device,
            Set<Integer> networkAddresses, Integer networkAddress) {
        // NOOP
    }
}