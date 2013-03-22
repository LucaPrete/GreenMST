package it.garr.greenmst.web;

import it.garr.greenmst.IGreenMSTService;
import it.garr.greenmst.types.LinkWithCost;

import java.util.ArrayList;
import java.util.List;

import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

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

public class MSTEdgesResource extends ServerResource {
	
    @Get("json")
    public List<LinkWithCost> retrieve() {
        IGreenMSTService service = (IGreenMSTService) getContext().getAttributes().get(IGreenMSTService.class.getCanonicalName());
        List<LinkWithCost> l = new ArrayList<LinkWithCost>();
        l.addAll(service.getMSTEdges());
        return l;
    }
    
}