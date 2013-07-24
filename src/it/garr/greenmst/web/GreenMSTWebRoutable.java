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
 * Class to describe REST routes for the GreenMST service.
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
 * @see it.garr.greenmst.web.MSTEdgesResource
 * @see it.garr.greenmst.web.RedundantEdgesResource
 * @see it.garr.greenmst.web.TopoCostsResource
 * @see it.garr.greenmst.web.TopoEdgesResource
 * @see it.garr.greenmst.web.serializers.LinkWithCostJSONSerializer
 * @see it.garr.greenmst.web.serializers.TopologyCostsJSONDeserializer
 * @see it.garr.greenmst.web.serializers.TopologyCostsJSONSerializer
 * 
 */

package it.garr.greenmst.web;

import net.floodlightcontroller.restserver.RestletRoutable;

import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.routing.Router;

/**
 * 
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
