package it.garr.greenmst.web;

import it.garr.greenmst.IGreenMSTService;
import it.garr.greenmst.types.ComparableLink;

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
 * @see it.garr.greenmst.types.ComparableLink
 * @see it.garr.greenmst.IGreenMSTService
 *
 */

public class TopoEdgesResource extends ServerResource {
	
    @Get("json")
    public List<ComparableLink> retrieve() {
        IGreenMSTService service = (IGreenMSTService) getContext().getAttributes().get(IGreenMSTService.class.getCanonicalName());
        List<ComparableLink> l = new ArrayList<ComparableLink>();
        l.addAll(service.getTopoEdges());
        return l;
    }
    
}