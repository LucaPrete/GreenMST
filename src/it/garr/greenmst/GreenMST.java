package it.garr.greenmst;

import it.garr.greenmst.algorithms.IMinimumSpanningTreeAlgorithm;
import it.garr.greenmst.algorithms.KruskalAlgorithm;
import it.garr.greenmst.types.LinkWithCost;
import it.garr.greenmst.types.TopologyCosts;
import it.garr.greenmst.web.GreenMSTWebRoutable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.linkdiscovery.ILinkDiscovery;
import net.floodlightcontroller.linkdiscovery.ILinkDiscovery.LDUpdate;
import net.floodlightcontroller.restserver.IRestApiService;
import net.floodlightcontroller.topology.ITopologyListener;
import net.floodlightcontroller.topology.ITopologyService;

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
 * @see it.garr.greenmst.types.LinkWithCost
 * @see it.garr.greenmst.IGreenMSTService
 *
 */

public class GreenMST implements IFloodlightModule, IGreenMSTService, ITopologyListener {
	
	protected static Logger logger = LoggerFactory.getLogger(GreenMST.class);
	
	// Service references
	protected IRestApiService restApi = null;
	protected IFloodlightProviderService floodlightProvider = null;
	protected ITopologyService topology = null;
	
	// Data structures for caching algorithm results
	protected HashSet<LinkWithCost> topoEdges = new HashSet<LinkWithCost>();
	protected HashSet<LinkWithCost> redundantEdges = new HashSet<LinkWithCost>();
	
	private IMinimumSpanningTreeAlgorithm algorithm = new KruskalAlgorithm();
	
	@Override
	public void topologyChanged(List<LDUpdate> linkUpdates) {
		for (LDUpdate update : linkUpdates) {
			logger.trace("Received topology update event {}.", update);
			
			if (update.getOperation().equals(ILinkDiscovery.UpdateOperation.LINK_REMOVED) || update.getOperation().equals(ILinkDiscovery.UpdateOperation.LINK_UPDATED)) {
				LinkWithCost link = new LinkWithCost(update.getSrc(), update.getSrcPort(), update.getDst(), update.getDstPort());
				logger.trace("Considering link {}.", link);
				
				logger.trace("topoEdges = {}.", new Object[] { printEdges(topoEdges) });
				logger.trace("redundantEdges = {}.", new Object[] { printEdges(redundantEdges) });
				
				if (update.getOperation().equals(ILinkDiscovery.UpdateOperation.LINK_REMOVED)) {
		            if ((topoEdges.contains(link) || topoEdges.contains(link.getInverse())) && 
		            		!redundantEdges.contains(link) && !redundantEdges.contains(link.getInverse())) {	
		            	logger.debug("Link removed: {}.", new Object[] { link });
		            	topoEdges.remove(link);
		            	updateLinks();
		            }
				} else if(update.getOperation().equals(ILinkDiscovery.UpdateOperation.LINK_UPDATED)) {
					if (!topoEdges.contains(link) && !topoEdges.contains(link.getInverse())) {
						logger.debug("Link added: {}.", new Object[] { link });
		                topoEdges.add(link);
		                updateLinks();
		            }
				}
			}
		}
	}
	
	protected void updateLinks() {
		logger.debug("Updating MST because of topology change...");
		HashSet<LinkWithCost> oldRedundantEdges = this.redundantEdges,
							  newRedundantEdges = null;
		
        try {
        	ArrayList<LinkWithCost> allTopology = new ArrayList<LinkWithCost>(topoEdges);
        	//allTopology.addAll(oldRedundantEdges);
        	Vector<LinkWithCost> mstEdges = algorithm.perform(allTopology);
        	logger.trace("mstEdges = {}.", new Object[] { printEdges(mstEdges) });
            // In mstEdges we now have all edges of the MST
            // topoEdges still contains a list of all edges of the known physical network   
        	newRedundantEdges = findRedundantEdges(mstEdges);
        	logger.trace("newRedundantEdges = {}.", new Object[] { printEdges(newRedundantEdges) });
            // redundantEdges contains edges to be closed according to Kruskal
            // (ie edges in topoEdges but not present in mstEdges, edges not in MSP and not already closed)
        } catch (Exception e) {
            logger.error("Error calculating MST with Kruskal ", e);
        }
        
        if (newRedundantEdges != null && newRedundantEdges.size() > 0) {
            // Close edges in redundantEdges
            for (LinkWithCost s : newRedundantEdges) {
                if (!oldRedundantEdges.contains(s)) {
                	logger.trace("Closing edge {}.", new Object[] { s });

                	modPort(s.getSrc(), s.getSrcPort(), false);
                	modPort(s.getDst(), s.getDstPort(), false);
                }
            }
            
            // Re-open ports in MSP which were closed in previous iterations
            // (ie edges in the redundantEdges, from previous execution, and not in the current execution)
            for (LinkWithCost s : oldRedundantEdges) {
                if (!newRedundantEdges.contains(s)) {
                	logger.trace("Opening edge {}.", new Object[] { s });
                	
            		modPort(s.getSrc(), s.getSrcPort(), true);
            		modPort(s.getDst(), s.getDstPort(), true);
                }
            }

            // Clone redundantEdges in redundantEdges for future iterations
            this.redundantEdges = newRedundantEdges;
        }
        
        logger.trace("New topoEdges = {}.", new Object[] { printEdges(topoEdges) });
        logger.trace("New redundantEdges = {}.", new Object[] { printEdges(redundantEdges) });
    }
	
	protected HashSet<LinkWithCost> findRedundantEdges(Vector<LinkWithCost> mstEdges) {
    	HashSet<LinkWithCost> redundantEdges = new HashSet<LinkWithCost>();
    	
    	for (LinkWithCost lt: topoEdges) {
    		LinkWithCost ltInverse = lt.getInverse();
    		if (!mstEdges.contains(lt) && !mstEdges.contains(ltInverse)) {
    			redundantEdges.add(lt);
    		}
        }
    	
    	return redundantEdges;
    }
    
	protected void modPort(long switchId, short portNum, boolean open) {
		try {
	    	OFPortMod portMod = new OFPortMod();
	    	IOFSwitch sw = floodlightProvider.getAllSwitchMap().get(switchId);
	
	    	// Search ports for finding hardware address
	    	// portMod.setHardwareAddress(sw.getPort(portNum).getHardwareAddress());
	    	try {
	    		portMod.setHardwareAddress(sw.getPort(portNum).getHardwareAddress());
	    	} catch (Exception e) {
	    		logger.error("Error while retrieving port hardware address from switch. Try using switch address.");
	    		throw e;
	    	}
	
	    	portMod.setPortNumber(portNum);
	    	//portMod.setMask(OFPortConfig.OFPPC_PORT_DOWN.getValue());
	    	portMod.setMask(OFPortConfig.OFPPC_NO_FLOOD.getValue());
	    	
	    	portMod.setConfig((open == true) ? 0 : 63);
	    	
	    	if (portMod.getHardwareAddress() != null) logger.info("Sending ModPort command to switch {} - {} port {} (hw address {}).", new Object[] { switchId, ((open == true) ? "opening" : "closing"), portNum, HexString.toHexString(portMod.getHardwareAddress())});
	    	else logger.info("Sending ModPort command to switch {} - {} port {}.", new Object[] { switchId, ((open == true) ? "opening" : "closing"), portNum});
	    	
	    	sw.write(portMod, null);
		}
		catch (Exception e) {
			logger.error("Error while {} port {} on switch {}.", new Object[] { (open) ? "opening" : "closing", switchId, portNum }, e);
		}
    }
    
	protected String printEdges(Iterable<LinkWithCost> edges) {
    	String s  = "";
    	for (LinkWithCost e: edges) {
    		if (!s.equals("")) s += "\n";
    		s += e.toString();
    	}
    	return s;
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
	}

	@Override
	public void startUp(FloodlightModuleContext context) {
		if (topology != null) topology.addListener(this);
		if (restApi != null) restApi.addRestletRoutable(new GreenMSTWebRoutable());
	}
	
	@Override
	public Set<LinkWithCost> getTopoEdges() {
		return topoEdges;
	}
	
	protected void setTopoEdges(HashSet<LinkWithCost> topoEdges) {
		this.topoEdges = topoEdges;
	}
	
	@SuppressWarnings("unchecked")
	@Override
    public Set<LinkWithCost> getMSTEdges(){
		HashSet<LinkWithCost> mstEdges = (HashSet<LinkWithCost>) topoEdges.clone();
		mstEdges.removeAll(redundantEdges);
    	return mstEdges;
    }
	
	@Override
    public Set<LinkWithCost> getRedundantEdges(){
    	return redundantEdges;
    }
	
	@Override
	public TopologyCosts getCosts() {
		return TopologyCostsLoader.getTopologyCosts();
	}
	
	@Override
	public void setCosts(TopologyCosts newCosts) {
		TopologyCosts costs = getCosts();
		//costs.getCosts().clear();
		costs.getCosts().putAll(newCosts.getCosts());
		
		updateLinks();
	}
}