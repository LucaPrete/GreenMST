package it.garr.greenmst;

import it.garr.greenmst.algorithms.IMinimumSpanningTreeAlgorithm;
import it.garr.greenmst.algorithms.KruskalAlgorithm;
import it.garr.greenmst.types.ComparableLink;
import it.garr.greenmst.web.GreenMSTWebRoutable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeSet;
import java.util.Vector;
import java.util.concurrent.ExecutionException;

import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.linkdiscovery.ILinkDiscovery;
import net.floodlightcontroller.linkdiscovery.ILinkDiscovery.LDUpdate;
import net.floodlightcontroller.restserver.IRestApiService;
import net.floodlightcontroller.topology.ITopologyListener;
import net.floodlightcontroller.topology.ITopologyService;

import org.openflow.protocol.OFPhysicalPort;
import org.openflow.protocol.OFPhysicalPort.OFPortConfig;
import org.openflow.protocol.OFPortMod;
import org.openflow.util.HexString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of the Floodlight GreenMST service.
 *
 * @author Luca Prete <luca.prete@garr.it>
 * @author Andrea Biancini <andrea.biancini@garr.it>
 * @author Fabio Farina <fabio.farina@garr.it>
 * 
 * @version 0.8
 * @see net.floodlightcontroller.core.module.IFloodlightModule
 * @see net.floodlightcontroller.topology.ITopologyListener
 * @see it.garr.greenmst.types.ComparableLink
 * @see it.garr.greenmst.IGreenMSTService
 *
 */

public class GreenMST implements IFloodlightModule, IGreenMSTService, ITopologyListener {
	
	protected IRestApiService restApi = null;
	protected IFloodlightProviderService floodlightProvider = null;
	protected ITopologyService topology = null;
	protected static Logger logger = null;
	
	// Data structures for caching algorithm results
	protected TreeSet<ComparableLink> topoEdges = new TreeSet<ComparableLink>();
	protected HashSet<ComparableLink> redundantEdges = new HashSet<ComparableLink>();
	
	private IMinimumSpanningTreeAlgorithm algorithm = new KruskalAlgorithm();
	
	@Override
	public void topologyChanged() {		
		for (LDUpdate update : topology.getLastLinkUpdates()) {
			ComparableLink link = new ComparableLink(update.getSrc(), update.getSrcPort(), update.getDst(), update.getDstPort());
			
			if (update.getOperation().equals(ILinkDiscovery.UpdateOperation.LINK_REMOVED)) { 
	            if (!topoEdges.contains(link)) {
	            	logger.debug("Link removed: {}.", new Object[] { link });
	            	topoEdges.remove(link);
	            	updateLinks();
	            }
			} else if(update.getOperation().equals(ILinkDiscovery.UpdateOperation.LINK_UPDATED)) {
				if (!topoEdges.contains(link)) {
					logger.debug("Link added: {}.", new Object[] { link });
	                topoEdges.add(link);
	                updateLinks();
	            }
			}
		}
	}
	
	private void updateLinks() {
		logger.debug("Updating MST because of topology change...");
		
        // Perform Kruskal
		HashSet<ComparableLink> oldRedundantEdges = this.redundantEdges,
								newRedundantEdges = null;
		
        try {
        	Vector<ComparableLink> mstEdges = algorithm.perform(topoEdges);
            // In mstEdges we now have all edges of the MST
            // topoEdges still contains a list of all edges of the known physical network   
        	newRedundantEdges = removeRedundantEdges(mstEdges);
            // redundantEdges contains edges to be closed according to Kruskal
            // (ie edges in topoEdges but not present in mstEdges, edges not in MSP and not already closed)
        } catch (Exception e) {
            logger.error("Error calculating MST with Kruskal ", e);
        }
        
        if (newRedundantEdges != null) {
            // Close edges in redundantEdges
            for (ComparableLink s : newRedundantEdges) {
                if (!oldRedundantEdges.contains(s)) {
                	try {
                		modPort(s.getSrc(), s.getSrcPort(), false);
                	} catch (Exception e) {
                		logger.error("Error while closing port {} on switch {}.", new Object[] { s.getSrcPort(), s.getSrc() }, e);
                	}
                }
            }
            
            // Re-open ports in MSP which were closed in previous iterations
            // (ie edges in the redundantEdges, from previous execution, and not in the current execution)
            for (ComparableLink s : oldRedundantEdges) {
                if (!newRedundantEdges.contains(s)) {
                	try {
                		modPort(s.getSrc(), s.getSrcPort(), true);
                	} catch (Exception e) {
                		logger.error("Error while opening port {} on switch {}.", new Object[] { s.getSrcPort(), s.getSrc() }, e);
                	}
                }
            }

            // Clone redundantEdges in redundantEdges for future iterations
            this.redundantEdges = newRedundantEdges;
        }
        
        printFinalEdges();
    }
	
    private HashSet<ComparableLink> removeRedundantEdges(Vector<ComparableLink> mstEdges) {
    	HashSet<ComparableLink> redundantEdges = new HashSet<ComparableLink>();
    	
    	for (ComparableLink lt: topoEdges) {
    		ComparableLink ltInverse = lt.getInverse();
    		if (!mstEdges.contains(lt) && !mstEdges.contains(ltInverse)) {
                redundantEdges.add(lt);
            }
        }
    	
    	return redundantEdges;
    }
    
	private void modPort(long switchId, short portNum, boolean open) throws InterruptedException, ExecutionException, IOException {
    	OFPortMod portMod = new OFPortMod();

    	// Search ports for finding hardware address
    	for (OFPhysicalPort curPort : floodlightProvider.getSwitches().get(switchId).getFeaturesReplyFromSwitch().get().getPorts()) {
    		if (curPort.getPortNumber() == portNum) portMod.setHardwareAddress(curPort.getHardwareAddress());
    	}

    	//portMod.setHardwareAddress(switchObj.getPort(portNum).getHardwareAddress());
    	portMod.setPortNumber(portNum);
    	portMod.setMask(OFPortConfig.OFPPC_PORT_DOWN.getValue());
    	portMod.setConfig((open == true) ? 0 : 1);
    	
    	if (portMod.getHardwareAddress() != null) logger.info("Sending ModPort command to switch {} - {} port {} (hw address {}).", new Object[] { switchId, ((open == true) ? "opening" : "closing"), portNum, HexString.toHexString(portMod.getHardwareAddress())});
    	else logger.info("Sending ModPort command to switch {} - {} port {}.", new Object[] { switchId, ((open == true) ? "opening" : "closing"), portNum});
    	
		floodlightProvider.getSwitches().get(switchId).write(portMod, null);
    }
    
	protected void printFinalEdges() {
    	String s  = "\n\n";
    	s += "################################################\n" +
             "#####     New MST generated by Kruskal     #####\n" +
             "################################################\n";
    	s += "The minimal spanning tree generated by Kruskal's algorithm is: \n";
    	for (ComparableLink e: topoEdges) {
    		s += "Nodes: (" + e.getDst() + ", " + e.getSrc() + ") with cost: " + e.getCost() + "\n";
    	}
    	s += "Following ports will be closed: \n";
    	for (ComparableLink spt : redundantEdges){
    		s += "Port "+ spt + "\n";
    	}
    	s += "##############################################\n";
    	
    	logger.trace(s);
    }
    
	@Override
	public Collection<Class<? extends IFloodlightService>> getModuleServices() {
		Collection<Class<? extends IFloodlightService>> l = new ArrayList<Class<? extends IFloodlightService>>();
	    l.add(IGreenMSTService.class);
	    return l;
	}

	@Override
	public Map<Class<? extends IFloodlightService>, IFloodlightService> getServiceImpls() {
		Map<Class<? extends IFloodlightService>, IFloodlightService> m = new HashMap<Class<? extends IFloodlightService>, IFloodlightService>();
	    m.put(IGreenMSTService.class, this);
	    return m;
	}

	@Override
	public Collection<Class<? extends IFloodlightService>> getModuleDependencies() {
		Collection<Class<? extends IFloodlightService>> l = new ArrayList<Class<? extends IFloodlightService>>();
		l.add(IFloodlightProviderService.class);
		l.add(ITopologyService.class);
		l.add(IRestApiService.class);
		return l;
	}

	@Override
	public void init(FloodlightModuleContext context) throws FloodlightModuleException {
		floodlightProvider = context.getServiceImpl(IFloodlightProviderService.class);
		restApi = context.getServiceImpl(IRestApiService.class);
		topology = context.getServiceImpl(ITopologyService.class);
		logger = LoggerFactory.getLogger(GreenMST.class);
	}

	@Override
	public void startUp(FloodlightModuleContext context) {
		if (topology != null) topology.addListener(this);
		restApi.addRestletRoutable(new GreenMSTWebRoutable());
	}
	
	@Override
	public TreeSet<ComparableLink> getTopoEdges() {
		return topoEdges;
	}
	
	@Override
    public HashSet<ComparableLink> getRedundantEdges(){
    	return redundantEdges;
    }
}