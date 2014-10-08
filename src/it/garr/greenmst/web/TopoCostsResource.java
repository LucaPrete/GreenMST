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
 * Class describing the topoEdges resource for the GreenMST REST API.
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
 * @see it.garr.greenmst.types.LinkWithCost
 * @see it.garr.greenmst.web.GreenMSTWebRoutable
 * @see it.garr.greenmst.web.MSTEdgesResource
 * @see it.garr.greenmst.web.RedundantEdgesResource
 * @see it.garr.greenmst.web.TopoEdgesResource
 * @see it.garr.greenmst.web.serializers.LinkWithCostJSONSerializer
 * @see it.garr.greenmst.web.serializers.TopologyCostsJSONDeserializer
 * @see it.garr.greenmst.web.serializers.TopologyCostsJSONSerializer
 * 
 */

package it.garr.greenmst.web;

import it.garr.greenmst.IGreenMSTService;
import it.garr.greenmst.types.TopologyCosts;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.map.ObjectMapper;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TopoCostsResource extends ServerResource {
	
	protected Logger logger = LoggerFactory.getLogger(TopoCostsResource.class);
	
    @Get("json")
    public List<TopologyCosts> retrieve() {
        IGreenMSTService service = (IGreenMSTService) getContext().getAttributes().get(IGreenMSTService.class.getCanonicalName());
        List<TopologyCosts> l = new ArrayList<TopologyCosts>();
        l.add(service.getCosts());
        return l;
    }
    
    @Post
    public String handlePost(String fmJson) {
        try {
        	ObjectMapper mapper = new ObjectMapper();
        	TopologyCosts costs = mapper.readValue(fmJson, TopologyCosts.class);
        	
        	IGreenMSTService service = (IGreenMSTService) getContext().getAttributes().get(IGreenMSTService.class.getCanonicalName());
        	service.setCosts(costs);
    		
        	logger.debug("Loaded new node costs.\n{}", new Object[]{costs});
    		return ("{\"status\" : \"new topology costs set\"}");
        } catch (Exception e) {
            logger.error("Error parsing new topology costs.", e);
            return "{\"status\" : \"Error! Could not parse new topology costs, see log for details.\"}";
        }
        
    }
    
}