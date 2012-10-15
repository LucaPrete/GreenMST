/**
 * Copyright 2011, Stanford University. This file is licensed under GPL v2 plus
 * a special exception, as described in included LICENSE_EXCEPTION.txt.
 */
package net.beaconcontroller.topology.internal;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import net.beaconcontroller.core.IBeaconProvider;
import net.beaconcontroller.core.IOFMessageListener;
import net.beaconcontroller.core.IOFSwitch;
import net.beaconcontroller.core.IOFSwitchListener;
import net.beaconcontroller.packet.Ethernet;
import net.beaconcontroller.packet.LLDP;
import net.beaconcontroller.packet.LLDPTLV;
import net.beaconcontroller.topology.ITopology;
import net.beaconcontroller.topology.ITopologyAware;
import net.beaconcontroller.topology.LinkTuple;
import net.beaconcontroller.topology.SwitchPortTuple;

import org.openflow.protocol.OFMessage;
import org.openflow.protocol.OFPacketIn;
import org.openflow.protocol.OFPacketOut;
import org.openflow.protocol.OFPhysicalPort;
import org.openflow.protocol.OFPhysicalPort.OFPortConfig;
import org.openflow.protocol.OFPhysicalPort.OFPortState;
import org.openflow.protocol.OFPort;
import org.openflow.protocol.OFPortStatus;
import org.openflow.protocol.OFPortStatus.OFPortReason;
import org.openflow.protocol.OFType;
import org.openflow.protocol.action.OFAction;
import org.openflow.protocol.action.OFActionOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class sends out LLDP messages containing the sending switch's datapath
 * id as well as the outgoing port number.  Received LLDP messages that match
 * a known switch cause a new LinkTuple to be created according to the
 * invariant rules listed below.  This new LinkTuple is also passed to routing
 * if it exists to trigger updates.
 *
 * This class also handles removing links that are associated to switch ports
 * that go down, and switches that are disconnected.
 *
 * Invariants:
 *  -portLinks and switchLinks will not contain empty Sets outside of critical sections
 *  -portLinks contains LinkTuples where one of the src or dst SwitchPortTuple matches the map key
 *  -switchLinks contains LinkTuples where one of the src or dst SwitchPortTuple's id matches the switch id
 *  -Each LinkTuple will be indexed into switchLinks for both src.id and dst.id,
 *    and portLinks for each src and dst
 *  -The updates queue is only added to from within a held write lock
 *
 * @author David Erickson (daviderickson@cs.stanford.edu)
 */
public class TopologyImpl implements IOFMessageListener, IOFSwitchListener, ITopology {
    protected static Logger log = LoggerFactory.getLogger(TopologyImpl.class);

    protected IBeaconProvider beaconProvider;

    /**
     * Map from link to the most recent time it was verified functioning
     */
    protected Map<LinkTuple, Long> links;
    protected Timer lldpSendTimer;
    protected Long lldpFrequency = 15L * 1000; // sending frequency
    protected Long lldpTimeout = 35L * 1000; // timeout
    protected ReentrantReadWriteLock lock;

    /**
     * Map from a id:port to the set of links containing it as an endpoint
     */
    protected Map<SwitchPortTuple, Set<LinkTuple>> portLinks;
    protected volatile boolean shuttingDown = false;

    /**
     * Map from switch id to a set of all links with it as an endpoint
     */
    protected Map<IOFSwitch, Set<LinkTuple>> switchLinks;
    protected Timer timeoutLinksTimer;
    protected Set<ITopologyAware> topologyAware;
    protected BlockingQueue<Update> updates;
    protected Thread updatesThread;

    protected class Update {
        public IOFSwitch src;
        public short srcPort;
        public IOFSwitch dst;
        public short dstPort;
        public boolean added;

        public Update(IOFSwitch src, short srcPort, IOFSwitch dst,
                short dstPort, boolean added) {
            this.src = src;
            this.srcPort = srcPort;
            this.dst = dst;
            this.dstPort = dstPort;
            this.added = added;
        }

        public Update(LinkTuple lt, boolean added) {
            this.src = lt.getSrc().getSw();
            this.srcPort = lt.getSrc().getPort();
            this.dst = lt.getDst().getSw();
            this.dstPort = lt.getDst().getPort();
            this.added = added;
        }
    }

    public TopologyImpl() {
        this.lock = new ReentrantReadWriteLock();
        this.updates = new LinkedBlockingQueue<Update>();
    }

    protected void startUp() {
        beaconProvider.addOFMessageListener(OFType.PACKET_IN, this);
        beaconProvider.addOFMessageListener(OFType.PORT_STATUS, this);
        beaconProvider.addOFSwitchListener(this);
        links = new HashMap<LinkTuple, Long>();
        portLinks = new HashMap<SwitchPortTuple, Set<LinkTuple>>();
        switchLinks = new HashMap<IOFSwitch, Set<LinkTuple>>();

        lldpSendTimer = new Timer();
        lldpSendTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                sendLLDPs();
            }}, 1000, lldpFrequency);

        timeoutLinksTimer = new Timer();
        timeoutLinksTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                timeoutLinks();
            }}, 1000, lldpTimeout);

        updatesThread = new Thread(new Runnable () {
            @Override
            public void run() {
                while (true) {
                    try {
                        Update update = updates.take();
                        if (topologyAware != null) {
                            for (ITopologyAware ta : topologyAware) {
                                try {
                                    ta.linkUpdate(update.src, update.srcPort,
                                            update.dst, update.dstPort,
                                            update.added);
                                } catch (Exception e) {
                                    log.error("Exception on callback", e);
                                }
                            }
                        }
                    } catch (InterruptedException e) {
                        log.warn("Topology Updates thread interupted", e);
                        if (shuttingDown)
                            return;
                    }
                }
            }}, "Topology Updates");
        updatesThread.start();
    }

    protected void shutDown() {
        shuttingDown = true;
        lldpSendTimer.cancel();
        beaconProvider.removeOFSwitchListener(this);
        beaconProvider.removeOFMessageListener(OFType.PACKET_IN, this);
        beaconProvider.removeOFMessageListener(OFType.PORT_STATUS, this);
        updatesThread.interrupt();
    }

    protected void sendLLDP(IOFSwitch sw) {
        Ethernet ethernet = new Ethernet()
        .setSourceMACAddress(new byte[6])
        .setDestinationMACAddress("01:80:c2:00:00:0e")
        .setEtherType(Ethernet.TYPE_LLDP);

        LLDP lldp = new LLDP();
        ethernet.setPayload(lldp);
        byte[] chassisId = new byte[] {4, 0, 0, 0, 0, 0, 0}; // filled in later
        byte[] portId = new byte[] {2, 0, 0}; // filled in later
        lldp.setChassisId(new LLDPTLV().setType((byte) 1).setLength((short) 7).setValue(chassisId));
        lldp.setPortId(new LLDPTLV().setType((byte) 2).setLength((short) 3).setValue(portId));
        lldp.setTtl(new LLDPTLV().setType((byte) 3).setLength((short) 2).setValue(new byte[] {0, 0x78}));

        // OpenFlow OUI - 00-26-E1
        byte[] dpidTLVValue = new byte[] {0x0, 0x26, (byte) 0xe1, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        LLDPTLV dpidTLV = new LLDPTLV().setType((byte) 127).setLength((short) 12).setValue(dpidTLVValue);
        lldp.setOptionalTLVList(new ArrayList<LLDPTLV>());
        lldp.getOptionalTLVList().add(dpidTLV);

        byte[] dpidArray = new byte[8];
        ByteBuffer dpidBB = ByteBuffer.wrap(dpidArray);
        ByteBuffer portBB = ByteBuffer.wrap(portId, 1, 2);
        Long dpid = sw.getId();
        dpidBB.putLong(dpid);

        // set the ethernet source mac to last 6 bytes of dpid
        System.arraycopy(dpidArray, 2, ethernet.getSourceMACAddress(), 0, 6);
        // set the chassis id's value to last 6 bytes of dpid
        System.arraycopy(dpidArray, 2, chassisId, 1, 6);
        // set the optional tlv to the full dpid
        System.arraycopy(dpidArray, 0, dpidTLVValue, 4, 8);
        for (OFPhysicalPort port : sw.getFeaturesReply().getPorts()) {
            if (port.getPortNumber() == OFPort.OFPP_LOCAL.getValue())
                continue;

            // set the portId to the outgoing port
            portBB.putShort(port.getPortNumber());

            // serialize and wrap in a packet out
            byte[] data = ethernet.serialize();
            OFPacketOut po = new OFPacketOut();
            po.setBufferId(OFPacketOut.BUFFER_ID_NONE);
            po.setInPort(OFPort.OFPP_NONE);

            // set actions
            List<OFAction> actions = new ArrayList<OFAction>();
            actions.add(new OFActionOutput(port.getPortNumber(), (short) 0));
            po.setActions(actions);
            po.setActionsLength((short) OFActionOutput.MINIMUM_LENGTH);

            // set data
            po.setLengthU(OFPacketOut.MINIMUM_LENGTH + po.getActionsLength() + data.length);
            po.setPacketData(data);

            // send
            try {
                sw.getOutputStream().write(po);
            } catch (IOException e) {
                log.error("Failure sending LLDP", e);
            }

            // rewind for next pass
            portBB.position(1);
        }
    }

    protected void sendLLDPs() {
        Map<Long, IOFSwitch> switches = beaconProvider.getSwitches();
        for (Map.Entry<Long, IOFSwitch> entry : switches.entrySet())
            sendLLDP(entry.getValue());
    }

    @Override
    public String getName() {
        return "topology";
    }

    @Override
    public Command receive(IOFSwitch sw, OFMessage msg) {
        if (msg instanceof OFPacketIn)
            return handlePacketIn(sw, (OFPacketIn) msg);
        else
            return handlePortStatus(sw, (OFPortStatus)msg);
    }

    protected Command handlePacketIn(IOFSwitch sw, OFPacketIn pi) {
        Ethernet eth = new Ethernet();
        eth.deserialize(pi.getPacketData(), 0, pi.getPacketData().length);

        if (!(eth.getPayload() instanceof LLDP))
            return Command.CONTINUE;

        LLDP lldp = (LLDP) eth.getPayload();

        // If this is a malformed lldp, or not from us, exit
        if (lldp.getPortId() == null || lldp.getPortId().getLength() != 3)
            return Command.CONTINUE;

        ByteBuffer portBB = ByteBuffer.wrap(lldp.getPortId().getValue());
        portBB.position(1);
        Short remotePort = portBB.getShort();
        Long remoteDpid = 0L;
        boolean remoteDpidSet = false;

        // Verify this LLDP packet matches what we're looking for
        for (LLDPTLV lldptlv : lldp.getOptionalTLVList()) {
            if (lldptlv.getType() == 127 && lldptlv.getLength() == 12 &&
                    lldptlv.getValue()[0] == 0x0 && lldptlv.getValue()[1] == 0x26 &&
                    lldptlv.getValue()[2] == (byte)0xe1 && lldptlv.getValue()[3] == 0x0) {
                ByteBuffer dpidBB = ByteBuffer.wrap(lldptlv.getValue());
                remoteDpid = dpidBB.getLong(4);
                remoteDpidSet = true;
                break;
            }
        }

        if (!remoteDpidSet) {
            log.error("Failed to determine remote switch DPID from received LLDP");
            return Command.STOP;
        }

        IOFSwitch remoteSwitch = beaconProvider.getSwitches().get(remoteDpid);

        if (remoteSwitch == null) {
            log.error("Failed to locate remote switch with DPID: {}", remoteDpid);
            return Command.STOP;
        }
        
        // Store the time of update to this link, and push it out to routingEngine
        LinkTuple lt = new LinkTuple(new SwitchPortTuple(remoteSwitch, remotePort),
                new SwitchPortTuple(sw, pi.getInPort()));

        if (addOrUpdateLink(lt)) {
            /**
             * If a new link was discovered, trigger an LLDP from the destination
             * switch so the reverse direction can also be discovered if available.
             */
            sendLLDP(lt.getDst().getSw());
        }

        // Consume this message
        return Command.STOP;
    }

    /**
     * Updates the state of Topology.  Returns true of the link was added,
     * or false if it was an update to an already known link.
     * @param lt
     * @return
     */
    protected boolean addOrUpdateLink(LinkTuple lt) {
        lock.writeLock().lock();
        try {
            if (links.put(lt, System.currentTimeMillis()) == null) {
                // index it by switch source
                if (!switchLinks.containsKey(lt.getSrc().getSw()))
                    switchLinks.put(lt.getSrc().getSw(), new HashSet<LinkTuple>());
                switchLinks.get(lt.getSrc().getSw()).add(lt);

                // index it by switch dest
                if (!switchLinks.containsKey(lt.getDst().getSw()))
                    switchLinks.put(lt.getDst().getSw(), new HashSet<LinkTuple>());
                switchLinks.get(lt.getDst().getSw()).add(lt);

                // index both ends by switch:port
                if (!portLinks.containsKey(lt.getSrc()))
                    portLinks.put(lt.getSrc(), new HashSet<LinkTuple>());
                portLinks.get(lt.getSrc()).add(lt);

                if (!portLinks.containsKey(lt.getDst()))
                    portLinks.put(lt.getDst(), new HashSet<LinkTuple>());
                portLinks.get(lt.getDst()).add(lt);

                updates.add(new Update(lt, true));
                log.debug("Added link {}", lt);
                return true;
            }
            return false;
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     *
     * @param links
     */
    protected void deleteLinks(List<LinkTuple> links) {
        lock.writeLock().lock();
        try {
            for (LinkTuple lt : links) {
                this.switchLinks.get(lt.getSrc().getSw()).remove(lt);
                this.switchLinks.get(lt.getDst().getSw()).remove(lt);
                if (this.switchLinks.containsKey(lt.getSrc().getSw()) &&
                        this.switchLinks.get(lt.getSrc().getSw()).isEmpty())
                    this.switchLinks.remove(lt.getSrc().getSw());
                if (this.switchLinks.containsKey(lt.getDst().getSw()) &&
                        this.switchLinks.get(lt.getDst().getSw()).isEmpty())
                    this.switchLinks.remove(lt.getDst().getSw());

                this.portLinks.get(lt.getSrc()).remove(lt);
                this.portLinks.get(lt.getDst()).remove(lt);
                if (this.portLinks.get(lt.getSrc()).isEmpty())
                    this.portLinks.remove(lt.getSrc());
                if (this.portLinks.get(lt.getDst()).isEmpty())
                    this.portLinks.remove(lt.getDst());

                this.links.remove(lt);
                updates.add(new Update(lt, false));
                log.debug("Deleted link {}", lt);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    protected Command handlePortStatus(IOFSwitch sw, OFPortStatus ps) {
        // if ps is a delete, or a modify where the port is down or configured down
        if ((byte)OFPortReason.OFPPR_DELETE.ordinal() == ps.getReason() ||
            ((byte)OFPortReason.OFPPR_MODIFY.ordinal() == ps.getReason() &&
                        (((OFPortConfig.OFPPC_PORT_DOWN.getValue() & ps.getDesc().getConfig()) > 0) ||
                                ((OFPortState.OFPPS_LINK_DOWN.getValue() & ps.getDesc().getState()) > 0)))) {
            SwitchPortTuple tuple = new SwitchPortTuple(sw, ps.getDesc().getPortNumber());

            List<LinkTuple> eraseList = new ArrayList<LinkTuple>();
            lock.writeLock().lock();
            try {
                if (this.portLinks.containsKey(tuple)) {
                    eraseList.addAll(this.portLinks.get(tuple));
                    deleteLinks(eraseList);
                }
            } finally {
                lock.writeLock().unlock();
            }
        }
        return Command.CONTINUE;
    }

    @Override
    public void addedSwitch(IOFSwitch sw) {
        sendLLDP(sw);
    }

    @Override
    public void removedSwitch(IOFSwitch sw) {
        List<LinkTuple> eraseList = new ArrayList<LinkTuple>();
        lock.writeLock().lock();
        try {
            if (switchLinks.containsKey(sw)) {
                // add all tuples with an endpoint on this switch to erase list
                eraseList.addAll(switchLinks.get(sw));
                deleteLinks(eraseList);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    protected void timeoutLinks() {
        List<LinkTuple> eraseList = new ArrayList<LinkTuple>();
        Long curTime = System.currentTimeMillis();

        // reentrant required here because deleteLink also write locks
        lock.writeLock().lock();
        try {
            Iterator<Entry<LinkTuple, Long>> it = this.links.entrySet().iterator();
            while (it.hasNext()) {
                Entry<LinkTuple, Long> entry = it.next();
                if (entry.getValue() + this.lldpTimeout < curTime) {
                    eraseList.add(entry.getKey());
                }
            }
    
            deleteLinks(eraseList);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * @param beaconProvider the beaconProvider to set
     */
    public void setBeaconProvider(IBeaconProvider beaconProvider) {
        this.beaconProvider = beaconProvider;
    }

    @Override
    public boolean isInternal(SwitchPortTuple idPort) {
        lock.readLock().lock();
        boolean result;
        try {
            result = this.portLinks.containsKey(idPort);
        } finally {
            lock.readLock().unlock();
        }
        return result;
    }

    @Override
    public Map<LinkTuple, Long> getLinks() {
        return new HashMap<LinkTuple, Long>(links);
    }

    /**
     * @param topologyAware the topologyAware to set
     */
    public void setTopologyAware(Set<ITopologyAware> topologyAware) {
        // TODO make this a copy on write set or lock it somehow
        this.topologyAware = topologyAware;
    }
}
