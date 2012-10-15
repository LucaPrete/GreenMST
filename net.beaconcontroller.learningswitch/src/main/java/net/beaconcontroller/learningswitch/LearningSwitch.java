/**
 * Copyright 2011, Stanford University. This file is licensed under GPL v2 plus
 * a special exception, as described in included LICENSE_EXCEPTION.txt.
 */
package net.beaconcontroller.learningswitch;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import net.beaconcontroller.core.IBeaconProvider;
import net.beaconcontroller.core.IOFMessageListener;
import net.beaconcontroller.core.IOFSwitch;
import net.beaconcontroller.core.IOFSwitchListener;
import net.beaconcontroller.packet.Ethernet;
import net.beaconcontroller.packet.IPv4;
import net.beaconcontroller.util.LongShortHopscotchHashMap;

import org.openflow.protocol.OFFlowMod;
import org.openflow.protocol.OFMatch;
import org.openflow.protocol.OFMessage;
import org.openflow.protocol.OFPacketIn;
import org.openflow.protocol.OFPacketOut;
import org.openflow.protocol.OFPort;
import org.openflow.protocol.OFType;
import org.openflow.protocol.action.OFAction;
import org.openflow.protocol.action.OFActionOutput;
import org.openflow.util.U16;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author David Erickson (daviderickson@cs.stanford.edu) - 04/04/10
 */
public class LearningSwitch implements IOFMessageListener, IOFSwitchListener {
    protected static Logger log = LoggerFactory.getLogger(LearningSwitch.class);
    protected IBeaconProvider beaconProvider;
    protected Map<IOFSwitch, LongShortHopscotchHashMap> macTables =
        new HashMap<IOFSwitch, LongShortHopscotchHashMap>();

    /**
     * @param beaconProvider the beaconProvider to set
     */
    public void setBeaconProvider(IBeaconProvider beaconProvider) {
        this.beaconProvider = beaconProvider;
    }

    public void startUp() {
        log.trace("Starting");
        beaconProvider.addOFMessageListener(OFType.PACKET_IN, this);
        beaconProvider.addOFSwitchListener(this);
    }

    public void shutDown() {
        log.trace("Stopping");
        beaconProvider.removeOFMessageListener(OFType.PACKET_IN, this);
        beaconProvider.removeOFSwitchListener(this);
    }

    public String getName() {
        return "switch";
    }

    public Command receive(IOFSwitch sw, OFMessage msg) {
        //log.debug("################# Ricevo ed elaboro pacchetto su switch " + sw);
        OFPacketIn pi = (OFPacketIn) msg;
        LongShortHopscotchHashMap macTable = macTables.get(sw);
        if (macTable == null) {
            macTable = new LongShortHopscotchHashMap();
            macTables.put(sw, macTable);
        }

        // Build the Match
        OFMatch match = new OFMatch();
        match.loadFromPacket(pi.getPacketData(), pi.getInPort());
        byte[] dlDst = match.getDataLayerDestination();
        byte[] dlSrc = match.getDataLayerSource();
        long dlSrcLong = Ethernet.toLong(dlSrc);
        int bufferId = pi.getBufferId();
        //log.debug("############### BUFFER ID INIZIALE: " + bufferId);
        //log.debug("############### PORTA DEST: " + ((int)(match.getTransportDestination() & 0xffff)));
        
        // if the src is not multicast, learn it
        if ((dlSrc[0] & 0x1) == 0 && dlSrcLong != 0) {
            if (!macTable.contains(dlSrcLong) ||
                    macTable.get(dlSrcLong) != pi.getInPort()) {
                macTable.put(dlSrcLong, pi.getInPort());
            }
        }

        short outPort = -1;
        long dlDstLong = Ethernet.toLong(dlDst);
        // if the destination is not multicast, look it up
        if ((dlDst[0] & 0x1) == 0 && dlDstLong != 0) {
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
                sw.getOutputStream().write(fm);
            } catch (IOException e) {
                log.error("Failure writing FlowMod", e);
            }
        }

        //log.debug("############### OUTPORT: " + outPort);
        //log.debug("############### BUFFER ID: " + pi.getBufferId());
        
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
                sw.getOutputStream().write(po);
            } catch (IOException e) {
                log.error("Failure writing PacketOut", e);
            }
        }
        //log.debug("################# Finisco di ricevere ed elaborare pacchetto su switch " + sw + "\n");
        return Command.CONTINUE;
    }

    @Override
    public void addedSwitch(IOFSwitch sw) {
    }

    @Override
    public void removedSwitch(IOFSwitch sw) {
        if (macTables.remove(sw) != null)
            log.debug("Removed l2 table for {}", sw);
    }

    /**
     * @return the macTables
     */
    public Map<IOFSwitch, LongShortHopscotchHashMap> getMacTables() {
        return macTables;
    }
}
