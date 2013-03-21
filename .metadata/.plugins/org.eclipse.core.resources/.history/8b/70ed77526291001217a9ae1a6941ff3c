package it.garr.greenmst;


import it.garr.greenmst.types.ComparableLink;

import java.util.HashSet;
import java.util.TreeSet;

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
	
	public TreeSet<ComparableLink> getTopoEdges();
    public HashSet<ComparableLink> getRedundantEdges();
    
}