package it.garr.greenmst;


import it.garr.greenmst.types.LinkWithCost;
import it.garr.greenmst.types.TopologyCosts;

import java.util.Set;

import net.floodlightcontroller.core.module.IFloodlightService;

/**
 * Public interface for the Floodlight GreenMST service.
 *
 * @author Luca Prete <luca.prete@garr.it>
 * @author Andrea Biancini <andrea.biancini@garr.it>
 * @author Fabio Farina <fabio.farina@garr.it>
 * 
 * @version 0.8
 * @see net.floodlightcontroller.core.module.IFloodlightService
 *
 */

public interface IGreenMSTService extends IFloodlightService {
	
	public Set<LinkWithCost> getMSTEdges();
	public Set<LinkWithCost> getTopoEdges();
    public Set<LinkWithCost> getRedundantEdges();
	public TopologyCosts getCosts();
	public void setCosts(TopologyCosts costs);
    
}