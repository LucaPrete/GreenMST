/**
 * Copyright (C) 2013 Luca Prete, Andrea Biancini, Fabio Farina - www.garr.it - Consortium GARR
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * Extends the Link class from net.floodlightcontroller.routing to
 * implement Comparable and Comparator interfaces.
 * 
 * @author Luca Prete <luca.prete@garr.it>
 * @author Andrea Biancini <andrea.biancini@garr.it>
 * @author Fabio Farina <fabio.farina@garr.it>
 * 
 * @version 0.90
 * @see it.garr.greenmst.GreenMST
 * @see it.garr.greenmst.TopologyCostsLoader
 * @see it.garr.greenmst.algorithms.IMinimumSpanningTreeAlgorithm
 * @see it.garr.greenmst.algorithms.KruskalAlgorithm
 * @see it.garr.greenmst.IGreenMSTService
 * @see it.garr.greenmst.types.TopologyCosts
 * @see it.garr.greenmst.web.GreenMSTWebRoutable
 * @see it.garr.greenmst.web.MSTEdgesResource
 * @see it.garr.greenmst.web.RedundantEdgesResource
 * @see it.garr.greenmst.web.TopoCostsResource
 * @see it.garr.greenmst.web.TopoEdgesResource
 * @see it.garr.greenmst.web.serializers.LinkWithCostJSONSerializer
 * @see it.garr.greenmst.web.serializers.TopologyCostsJSONDeserializer
 * @see it.garr.greenmst.web.serializers.TopologyCostsJSONSerializer
 * 
 */

package it.garr.greenmst.types;

import it.garr.greenmst.IGreenMSTService;
import it.garr.greenmst.TopologyCostsLoader;
import it.garr.greenmst.web.serializers.LinkWithCostJSONSerializer;
import net.floodlightcontroller.routing.Link;

import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@JsonSerialize(using=LinkWithCostJSONSerializer.class)
public class LinkWithCost extends Link {
    
	protected Logger logger = LoggerFactory.getLogger(LinkWithCost.class);
	
	public LinkWithCost(long srcId, int srcPort, long dstId, int dstPort) {
		super(srcId, srcPort, dstId, dstPort);
	}
	
	public LinkWithCost(long srcId, int srcPort, long dstId, int dstPort, int cost) {
		super(srcId, srcPort, dstId, dstPort);
		TopologyCosts costs = TopologyCostsLoader.getTopologyCosts();
		costs.getCost(srcId, dstId);
	}
	
	public LinkWithCost(IGreenMSTService greenMST, Link l) {
		super(l.getSrc(), l.getSrcPort(), l.getDst(), l.getDstPort());
	}
	
	public int getCost() {
		TopologyCosts costs = TopologyCostsLoader.getTopologyCosts();
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
		return new LinkWithCost(this.getDst(), this.getDstPort(), this.getSrc(), this.getSrcPort());
	}
}