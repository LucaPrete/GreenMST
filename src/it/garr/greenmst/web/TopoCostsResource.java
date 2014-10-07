package it.garr.greenmst.web;

import it.garr.greenmst.IGreenMSTService;
import it.garr.greenmst.types.TopologyCosts;

import java.util.ArrayList;
import java.util.List;

import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Class describing the topoEdges resource for the GreenMST REST api.
 * 
 * @author Luca Prete <luca.prete@garr.it>
 * @author Andrea Biancini <andrea.biancini@garr.it>
 * @author Fabio Farina <fabio.farina@garr.it>
 * 
 * @version 0.8
 * @see org.restlet.resource.ServerResource
 * @see it.garr.greenmst.types.LinkWithCost
 * @see it.garr.greenmst.IGreenMSTService
 *
 */

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