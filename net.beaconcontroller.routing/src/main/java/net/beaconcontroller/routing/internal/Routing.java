/**
 * Copyright 2011, Stanford University. This file is licensed under GPL v2 plus
 * a special exception, as described in included LICENSE_EXCEPTION.txt.
 */
package net.beaconcontroller.routing.internal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import net.beaconcontroller.core.IBeaconProvider;
import net.beaconcontroller.core.IOFMessageListener;
import net.beaconcontroller.core.IOFSwitch;
import net.beaconcontroller.core.io.OFMessageSafeOutStream;
import net.beaconcontroller.devicemanager.Device;
import net.beaconcontroller.devicemanager.IDeviceManager;
import net.beaconcontroller.devicemanager.IDeviceManagerAware;
import net.beaconcontroller.routing.IRoutingEngine;
import net.beaconcontroller.routing.Link;
import net.beaconcontroller.routing.Route;

import org.openflow.io.OFMessageInStream;
import org.openflow.protocol.OFFlowMod;
import org.openflow.protocol.OFMatch;
import org.openflow.protocol.OFMessage;
import org.openflow.protocol.OFPacketIn;
import org.openflow.protocol.OFPacketOut;
import org.openflow.protocol.OFPort;
import org.openflow.protocol.OFType;
import org.openflow.protocol.action.OFAction;
import org.openflow.protocol.action.OFActionOutput;
import org.openflow.protocol.factory.OFMessageFactory;
import org.openflow.util.HexString;
import org.openflow.util.U16;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author David Erickson (daviderickson@cs.stanford.edu)
 */
public class Routing implements IOFMessageListener, IDeviceManagerAware {
    protected static Logger log = LoggerFactory.getLogger(Routing.class);

    protected IBeaconProvider beaconProvider;
    protected IDeviceManager deviceManager;
    protected boolean packetOutDirectly = false;
    protected IRoutingEngine routingEngine;

    public void startUp() {
        beaconProvider.addOFMessageListener(OFType.PACKET_IN, this);
    }

    public void shutDown() {
        beaconProvider.removeOFMessageListener(OFType.PACKET_IN, this);
    }

    @Override
    public String getName() {
        return "routing";
    }

    @Override
    public Command receive(IOFSwitch sw, OFMessage msg) {
        OFPacketIn pi = (OFPacketIn) msg;
        OFMatch match = new OFMatch();
        match.loadFromPacket(pi.getPacketData(), pi.getInPort());

        // Check if we have the location of the destination
        Device dstDevice = deviceManager.getDeviceByDataLayerAddress(match.getDataLayerDestination());

        if (dstDevice != null) {
            // does a route exist?
            Route route = routingEngine.getRoute(sw.getId(), dstDevice.getSw().getId());
            if (route != null) {
                // set the route
                if (log.isTraceEnabled())
                    log.trace("Pushing route match={} route={} destination={}:{}", new Object[] {match, route, dstDevice.getSw(), dstDevice.getSwPort()});
                OFMessageInStream in = sw.getInputStream();

                /**
                 * Send the packet directly to the destination via a PacketOut if desired, as opposed to pushing the
                 * route then releasing the packet from the first switch it entered.
                 */
                if (packetOutDirectly) {
                    pushRoute(in.getMessageFactory(), match, route, dstDevice, 0xffffffff);
                    pushPacketDirect(in.getMessageFactory(), sw, match, pi, dstDevice);
                } else {
                    pushRoute(in.getMessageFactory(), match, route, dstDevice, pi.getBufferId());

                    // send the packet if it is not buffered
                    if (pi.getBufferId() == 0xffffffff) {
                        pushPacket(in.getMessageFactory(), sw, match, pi);
                    }
                }

                return Command.STOP;
            } else {
                if (log.isTraceEnabled()) {
                    log.trace("No route found from {}:{} to device {}",
                            new Object[] { HexString.toHexString(sw.getId()),
                                    pi.getInPort(), dstDevice });
                }
            }
        } else {
            if (log.isTraceEnabled()) {
                // filter multicast destinations
                if ((match.getDataLayerDestination()[0] & 0x1) == 0) {
                    log.trace("Unable to locate device with address {}",
                            HexString.toHexString(match
                                    .getDataLayerDestination()));
                }
            }
        }

        return Command.CONTINUE;
    }

    /**
     * Push routes from back to front
     * @param factory
     * @param match
     * @param route
     * @param dstDevice
     */
    public void pushRoute(OFMessageFactory factory, OFMatch match, Route route, Device dstDevice, int bufferId) {
        OFFlowMod fm = (OFFlowMod) factory.getMessage(OFType.FLOW_MOD);
        OFActionOutput action = new OFActionOutput();
        List<OFAction> actions = new ArrayList<OFAction>();
        actions.add(action);
        fm.setIdleTimeout((short)5)
            .setBufferId(0xffffffff)
            .setMatch(match.clone())
            .setActions(actions)
            .setLengthU(OFFlowMod.MINIMUM_LENGTH+OFActionOutput.MINIMUM_LENGTH);
        IOFSwitch sw = beaconProvider.getSwitches().get(route.getId().getDst());
        OFMessageSafeOutStream out = sw.getOutputStream(); // to prevent NoClassDefFoundError
        ((OFActionOutput)fm.getActions().get(0)).setPort(dstDevice.getSwPort());

        for (int i = route.getPath().size() - 1; i >= 0; --i) {
            Link link = route.getPath().get(i);
            fm.getMatch().setInputPort(link.getInPort());
            if (fm.getMatch().getInputPort() == ((OFActionOutput) fm
                    .getActions().get(0)).getPort()) {
                log.warn("Stale flows detected from {} to {}, removing existing flows",
                        HexString.toHexString(fm.getMatch()
                                .getDataLayerSource()), dstDevice);
                deviceMoved(dstDevice, null, null, null, null);
                return;
            }
            try {
                out.write(fm);
            } catch (IOException e) {
                log.error("Failure writing flow mod", e);
            }
            //try {
                fm = fm.clone();
            /*} catch (CloneNotSupportedException e) {
                log.error("Failure cloning flow mod", e);
            }*/

            // setup for the next loop iteration
            ((OFActionOutput)fm.getActions().get(0)).setPort(link.getOutPort());
            if (i > 0) {
                sw = beaconProvider.getSwitches().get(route.getPath().get(i-1).getDst());
            } else {
                sw = beaconProvider.getSwitches().get(route.getId().getSrc());
            }
            if (sw == null) {
                if (log.isWarnEnabled()) {
                    log.warn(
                            "Unable to push route, switch at DPID {} not available",
                            (i > 0) ? HexString.toHexString(route.getPath()
                                    .get(i - 1).getDst()) : HexString
                                    .toHexString(route.getId().getSrc()));
                }
                return;
            }
            out = sw.getOutputStream();
        }
        // set the original match for the first switch, and buffer id
        fm.setMatch(match)
            .setBufferId(bufferId);

        if (fm.getMatch().getInputPort() == ((OFActionOutput) fm
                .getActions().get(0)).getPort()) {
            log.warn("Stale flows detected from {} to {}, removing existing flows",
                    HexString.toHexString(fm.getMatch()
                            .getDataLayerSource()), dstDevice);
            deviceMoved(dstDevice, null, null, null, null);
            return;
        }
        try {
            out.write(fm);
        } catch (IOException e) {
            log.error("Failure writing flow mod", e);
        }
    }

    public void pushPacket(OFMessageFactory factory, IOFSwitch sw, OFMatch match, OFPacketIn pi) {
        OFPacketOut po = (OFPacketOut) factory.getMessage(OFType.PACKET_OUT);
        po.setBufferId(pi.getBufferId());
        po.setInPort(pi.getInPort());

        // set actions
        List<OFAction> actions = new ArrayList<OFAction>();
        actions.add(new OFActionOutput(OFPort.OFPP_TABLE.getValue(), (short) 0));
        po.setActions(actions)
            .setActionsLength((short) OFActionOutput.MINIMUM_LENGTH);

        byte[] packetData = pi.getPacketData();
        po.setLength(U16.t(OFPacketOut.MINIMUM_LENGTH
                + po.getActionsLength() + packetData.length));
        po.setPacketData(packetData);

        try {
            sw.getOutputStream().write(po);
        } catch (IOException e) {
            log.error("Failure writing packet out", e);
        }
    }

    public void pushPacketDirect(OFMessageFactory factory, IOFSwitch sw, OFMatch match, OFPacketIn pi, Device dstDevice) {
        OFActionOutput action = new OFActionOutput()
            .setPort(dstDevice.getSwPort());

        // build packet out
        OFPacketOut po = new OFPacketOut()
            .setBufferId(0xffffffff)
            .setInPort(OFPort.OFPP_NONE.getValue())
            .setActions(Collections.singletonList((OFAction)action))
            .setActionsLength((short) OFActionOutput.MINIMUM_LENGTH);

        byte[] packetData = pi.getPacketData();
        po.setLength(U16.t(OFPacketOut.MINIMUM_LENGTH
                + po.getActionsLength() + packetData.length));
        po.setPacketData(packetData);

        try {
            sw.getOutputStream().write(po);
        } catch (IOException e) {
            log.error("Failure writing PacketOut", e);
        }
    }

    /**
     * @param beaconProvider the beaconProvider to set
     */
    public void setBeaconProvider(IBeaconProvider beaconProvider) {
        this.beaconProvider = beaconProvider;
    }

    /**
     * @param routingEngine the routingEngine to set
     */
    public void setRoutingEngine(IRoutingEngine routingEngine) {
        this.routingEngine = routingEngine;
    }

    /**
     * @param deviceManager the deviceManager to set
     */
    public void setDeviceManager(IDeviceManager deviceManager) {
        this.deviceManager = deviceManager;
    }

    @Override
    public void deviceAdded(Device device) {
        // NOOP
    }

    @Override
    public void deviceRemoved(Device device) {
        // NOOP
    }

    @SuppressWarnings("unchecked")
    @Override
    public void deviceMoved(Device device, IOFSwitch oldSw, Short oldPort,
            IOFSwitch sw, Short port) {
        // Build flow mod to delete based on destination mac == device mac
        OFMatch match = new OFMatch();
        match.setDataLayerDestination(device.getDataLayerAddress());
        match.setWildcards(OFMatch.OFPFW_ALL ^ OFMatch.OFPFW_DL_DST);
        OFFlowMod fm = (OFFlowMod) device.getSw().getInputStream()
                .getMessageFactory().getMessage(OFType.FLOW_MOD);
        fm.setCommand(OFFlowMod.OFPFC_DELETE)
            .setOutPort((short) OFPort.OFPP_NONE.getValue())
            .setMatch(match)
            .setActions(Collections.EMPTY_LIST)
            .setLength(U16.t(OFFlowMod.MINIMUM_LENGTH));

        // Flush to all switches
        for (IOFSwitch outSw : beaconProvider.getSwitches().values()) {
            try {
                outSw.getOutputStream().write(fm);
            } catch (IOException e) {
                log.error("Failure sending flow mod delete for moved device", e);
            }
        }
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

    /**
     * When set to true, routing will send the data received via a PacketIn
     * directly to the destination when it is known. Note for this to work
     * the PacketIn must contain the full packet.
     * @param packetOutDirectly the packetOutDirectly to set
     */
    public void setPacketOutDirectly(boolean packetOutDirectly) {
        this.packetOutDirectly = packetOutDirectly;
    }
}
