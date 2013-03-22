package it.garr.greenmst.web;

import net.floodlightcontroller.restserver.RestletRoutable;

import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.routing.Router;

/**
 * Class to describe REST routes for the GreenMST service.
 *
 * @author Luca Prete <luca.prete@garr.it>
 * @author Andrea Biancini <andrea.biancini@garr.it>
 * @author Fabio Farina <fabio.farina@garr.it>
 * 
 * @version 0.8
 * @see net.floodlightcontroller.restserver.RestletRoutable
 * @see it.garr.greenmst.web.TopoEdgesResource
 * @see it.garr.greenmst.web.RedundantEdgesResource
 *
 */

public class GreenMSTWebRoutable implements RestletRoutable {
    @Override
    public Restlet getRestlet(Context context) {
        Router router = new Router(context);
        router.attach("/topocosts/json", TopoCostsResource.class);
        router.attach("/mstedges/json", MSTEdgesResource.class);
        router.attach("/topoedges/json", TopoEdgesResource.class);
        router.attach("/redundantedges/json", RedundantEdgesResource.class);
        return router;
    }

    @Override
    public String basePath() {
        return "/wm/greenmst";
    }
}
