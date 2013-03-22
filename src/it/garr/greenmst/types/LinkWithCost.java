package it.garr.greenmst.types;

import it.garr.greenmst.IGreenMSTService;
import it.garr.greenmst.web.serializers.LinkWithCostJSONSerializer;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.routing.Link;

import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extends the Link class from net.floodlightcontroller.routing to implement Comparable and Comparator interfaces.
 * 
 * @author Luca Prete <luca.prete@garr.it>
 * @author Andrea Biancini <andrea.biancini@garr.it>
 * @author Fabio Farina <fabio.farina@garr.it>
 * 
 * @version 0.8
 * @see net.floodlightcontroller.routing.Link
 * @see java.util.Comparator
 * @see java.lang.Comparable
 * @see it.garr.greenmst.GreenMST
 *
 */

@JsonSerialize(using=LinkWithCostJSONSerializer.class)
public class LinkWithCost extends Link {
    
	private FloodlightModuleContext context = null;
	private TopologyCosts costs = null; 
	protected Logger logger = LoggerFactory.getLogger(LinkWithCost.class);
	
	public LinkWithCost(FloodlightModuleContext ctx, long srcId, int srcPort, long dstId, int dstPort) {
		super(srcId, srcPort, dstId, dstPort);
		context = ctx;
		costs = context.getServiceImpl(IGreenMSTService.class).getCosts();
	}
	
	public LinkWithCost(FloodlightModuleContext ctx, Link l) {
		super(l.getSrc(), l.getSrcPort(), l.getDst(), l.getDstPort());
		context = ctx;
		costs = context.getServiceImpl(IGreenMSTService.class).getCosts();
	}
	
	public int getCost() {
		return costs.getCost(this.getSrc(), this.getDst());
	}
	
	@Override
	public boolean equals(Object obj) {
		return super.equals(obj);
	}

	@Override
	public String toString() {
		return "Link (" + this.getSrc() + ", " + this.getDst() + ") with cost: " + this.getCost();
	}
	
	public LinkWithCost getInverse() {
		return new LinkWithCost(context, this.getDst(), this.getDstPort(), this.getSrc(), this.getSrcPort());
	}
}