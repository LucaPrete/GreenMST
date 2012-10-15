/**
 * This file is licensed under GPL v2 plus.
 * 
 * A special exception, as described in included LICENSE_EXCEPTION.txt.
 * 
 */

package net.beaconcontroller.garrRecovery;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import net.beaconcontroller.core.IBeaconProvider;
import net.beaconcontroller.core.IOFMessageListener;
import net.beaconcontroller.core.IOFSwitch;
import net.beaconcontroller.topology.ITopology;
import net.beaconcontroller.topology.ITopologyAware;
import net.beaconcontroller.topology.LinkTuple;
import net.beaconcontroller.topology.SwitchPortTuple;

import org.openflow.protocol.OFMessage;
import org.openflow.protocol.OFPhysicalPort;
import org.openflow.protocol.OFPhysicalPort.OFPortConfig;
import org.openflow.protocol.OFPortMod;
import org.openflow.protocol.OFType;
import org.openflow.util.HexString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
*
* @author Luca Prete (luca.prete@garr.it) - 15/10/12
* @author Andrea Biancini (andrea.biancini@mib.infn.it) - 15/10/12
* @author Fabio Farina (fabio.farina@garr.it) - 15/10/12
*
*/

public class GarrRecovery implements IOFMessageListener, ITopologyAware {
    
    protected static Logger logger = LoggerFactory.getLogger(GarrRecovery.class);
    protected IBeaconProvider beaconProvider;
    protected ITopology topology;
    protected Set<ITopologyAware> topologyAware;
    
    // Data structures for Kruskal
    private HashMap<IOFSwitch, HashSet<IOFSwitch>> nodes = new HashMap<IOFSwitch, HashSet<IOFSwitch>>();           // Array of connected components
    private TreeSet<LinkTuple> topoEdges = new TreeSet<LinkTuple>();    // Priority queue of Edge objects
    private Vector<LinkTuple> mstEdges = new Vector<LinkTuple>();        // Edges in Minimal-Spanning Tree
    private TreeSet<LinkTuple> allEdges;
    private HashSet<SwitchPortTuple> redundantSPTuples = new HashSet<SwitchPortTuple>();
    private HashSet<SwitchPortTuple> redundantSPTuplesOld = new HashSet<SwitchPortTuple>();
    
    public IBeaconProvider getBeaconProvider() {
        return beaconProvider;
    }
    
    public void setBeaconProvider(IBeaconProvider beaconProvider){
        this.beaconProvider = beaconProvider;
    }
    
    public void startUp(){
        logger.info("Starting " + getName() + "...");
    	beaconProvider.addOFMessageListener(OFType.PORT_STATUS, this);
    	logger.info(getName() + " started succesfuly...");
    }
    
    public void shutDown(){
        beaconProvider.removeOFMessageListener(OFType.PORT_STATUS, this);
    }
    
    @Override
    public String getName() {
        return "GARR GreenMST";
    }
    
    @Override
    public void linkUpdate(IOFSwitch src, short srcPort, IOFSwitch dst, short dstPort, boolean added) {
        boolean executeUpdate = false;
        // Manages new link update and generates topoEdges containing ALL edges known in the physical network
        if (added==true){
            if (!topoEdges.contains(new LinkTuple(dst, dstPort, src, srcPort))) {
                topoEdges.add(new LinkTuple(src, srcPort, dst, dstPort));
                executeUpdate = true;
                logger.info("New LinkTuple added");
            }
        }else {
            executeUpdate = topoEdges.remove(new LinkTuple(src, srcPort, dst, dstPort));
            executeUpdate |= topoEdges.remove(new LinkTuple(dst, dstPort, src, srcPort));
            logger.info("Link removed");
        }
        
//        logger.debug("topoEdges[size = " + topoEdges.size() + "] = " + topoEdges);
        if (executeUpdate) {
//            long tempoIniziale = System.nanoTime();
//            logger.debug("\n################ GARR - TEMPO INIZIALE: " + tempoIniziale+"\n");
            updateLinks();
//            long tempoFinale = System.nanoTime();
//            logger.debug("\n################ GARR - TEMPO FINALE: " + tempoFinale+"\n");
//            long delta = tempoFinale - tempoIniziale;
//            logger.debug("\n################ GARR - CI HA MESSO: " + delta+"\n");
        }
    }

    @SuppressWarnings("unchecked")
	private void updateLinks() {
        IOFSwitch src;
        IOFSwitch dst;
        // Generates nodes Hashmap containing one entry for each switch
        nodes.clear();
        for (LinkTuple lt: topoEdges){
            src = lt.getSrc().getSw();
            dst = lt.getDst().getSw();
            
            if (!nodes.containsKey(src)) {
                // Create set of connect components [singleton] for this node
                nodes.put(src, new HashSet<IOFSwitch>());
                nodes.get(src).add(src);
            }
    
            if (!nodes.containsKey(dst)) {
                // Create set of connect components [singleton] for this node
                nodes.put(dst, new HashSet<IOFSwitch>());
                nodes.get(dst).add(dst);
            }
        }
        
        // Perform Kruskal
        try {
            mstEdges.clear();
            allEdges = (TreeSet<LinkTuple>) topoEdges.clone();   
            performKruskal();
            // In mstEdges we now have all edges of the MST
            // topoEdges still contains a list of all edges of the known physical network
            
            redundantSPTuples.clear();
            removeRedundantEdges();
            // redundantSPTuples contains closed edges 
            
            // Close edges not in MSP and not already closed
            for (SwitchPortTuple s : redundantSPTuples){
                if (!redundantSPTuplesOld.contains(s)) modPort(s.getSw(),s.getPort(),false);
            }

            // Re-open ports in MSP which were closed in previous iterations
            for (SwitchPortTuple s : redundantSPTuplesOld){
                if (!redundantSPTuples.contains(s)) modPort(s.getSw(),s.getPort(),true);
            }
            
            // Clone redundantSPTuples for future iterations
            redundantSPTuplesOld = (HashSet<SwitchPortTuple>) redundantSPTuples.clone();
            
            printFinalEdges();
        }catch(Exception e){
            logger.error("Error calculating MST with Kruskal ", e);
        }
    }
    
    private void modPort(IOFSwitch s, short port, boolean open){
        OFPortMod pM = new OFPortMod();
        String action; 
        for (OFPhysicalPort curPort : s.getFeaturesReply().getPorts()) {
            if (curPort.getPortNumber() == port)
                pM.setHardwareAddress(curPort.getHardwareAddress());
        }
        pM.setPortNumber(port);
        pM.setMask(OFPortConfig.OFPPC_PORT_DOWN.getValue());
        if(open == true){
            action = "opening";
            pM.setConfig(0 << 0);
        }else{
            pM.setConfig(OFPortConfig.OFPPC_PORT_DOWN.getValue());
            action = "closing";
        }
        try {
            logger.info("Sending ModPort command to switch " + HexString.toHexString(s.getId()) + " - " + action + " port " + port);
            s.getOutputStream().write(pM);
        } catch (Exception e) {
            logger.error("Error while " + action + " port " + port + " on switch " + HexString.toHexString(s.getId()), e);
        }
    }
    
    private void removeRedundantEdges() {
        // Find edges not present in MST: to be closed
        TreeSet<LinkTuple> linkToBeRemoved = new TreeSet<LinkTuple>(); 
        for (LinkTuple lt: topoEdges) {
//            logger.debug("examining tuple " + lt);
//            logger.debug("mstEdges[size=" + mstEdges.size() + "] = " + mstEdges);
            if (!mstEdges.contains(lt) && !mstEdges.contains(new LinkTuple(lt.getDst(), lt.getSrc()))) {
                linkToBeRemoved.add(lt);
                linkToBeRemoved.add(new LinkTuple(lt.getDst(), lt.getSrc()));
                redundantSPTuples.add(lt.getSrc());
                redundantSPTuples.add(lt.getDst());
            }
        }
    }

    public void setTopologyAware(Set<ITopologyAware> topologyAware) {
        this.topologyAware = topologyAware;
    }
    
    /**
     * @param topology the topology to set
     */
    public void setTopology(ITopology topology) {
        this.topology = topology;
    }
    
    @Override
    public Command receive(IOFSwitch sw, OFMessage msg) {        
        return null;
    }
    
    // KRUSKAL ALGORIThM -- COLUMBIA UNIV. IMPL.

      private void performKruskal() throws Exception {
//        logger.debug("Perform Kruskal starts...");
        
        int size = allEdges.size();
//        logger.debug("topoEdges size: " + size);
        for (int i=0; i<size; i++) {
          
          LinkTuple curEdge = (LinkTuple) allEdges.first();
          if (allEdges.remove(curEdge)) {
          //successful removal from priority queue: topoEdges

            if (nodesAreInDifferentSets(curEdge.getSrc().getSw(), curEdge.getDst().getSw())) {
//              logger.debug("Nodes are in different sets ...");
              HashSet<IOFSwitch> src, dst;
              IOFSwitch dstHashSetIndex;

              if (nodes.get(curEdge.getSrc().getSw()).size() > nodes.get(curEdge.getDst().getSw()).size()) {
                // have to transfer all nodes including curEdge.to
                src = nodes.get(curEdge.getDst().getSw());
                dst = nodes.get(dstHashSetIndex = curEdge.getSrc().getSw());
              } else {
                // have to transfer all nodes including curEdge.from
                src = nodes.get(curEdge.getSrc().getSw());
                dst = nodes.get(dstHashSetIndex = curEdge.getDst().getSw());
              }

              Object[] srcArray = src.toArray();
              int transferSize = srcArray.length;
              for (int j=0; j<transferSize; j++) {
                // move each node from set: src into set: dst
                // and update appropriate index in array: nodes
                if (src.remove(srcArray[j])) {
                	dst.add((IOFSwitch) srcArray[j]);
                	nodes.put((IOFSwitch) srcArray[j], nodes.get(dstHashSetIndex));
                } else {
//              	This is a serious problem
                	logger.error("Kruskal - Error performing Kruskal algorithm (set union)");
                	throw new Exception("Kruskal - Error performing Kruskal algorithm (set union)");
                }
              }

              mstEdges.add(curEdge);
              // add new edge to MST edge vector
            } else {
//                logger.debug("Kruskal - Nodes are in the same set ... nothing to do here");
            }

          } else {
            // This is a serious problem
            logger.error("Kruskal - Error TreeSet should have contained this element!!");
            throw new Exception("Kruskal - Error TreeSet should have contained this element!!");
          }
        }
      }

      private boolean nodesAreInDifferentSets(IOFSwitch a, IOFSwitch b) {
        // returns true if graph nodes (a,b) are in different
        // connected components, ie the set for 'a' is different
        // from that for 'b'
        return(!nodes.get(a).equals(nodes.get(b)));
      }

      private void printFinalEdges() {
    	String s = "\n\n################################################\n" +
                   "#####      New MST generated by Kruskal    #####\n" +
                   "################################################\n";
    	s += "The minimal spanning tree generated by Kruskal's algorithm is: \n";
        for (LinkTuple e: mstEdges) {
          // for each edge in Vector of MST edges
          s += "Nodes: (" + e.getDst() + ", " + e.getSrc() +
            ") with cost: " + e.getCost() + "\n";
        }
        s += "Following ports will be closed: \n";
        for (SwitchPortTuple spt : redundantSPTuples){
          s += "Port "+ spt + "\n";
        }
        s += "##############################################\n";
        logger.debug(s);
      }
}