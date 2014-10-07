


package it.garr.greenmst.web.serializers;

import it.garr.greenmst.types.TopologyCosts;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

/**
 * Class that serializes the ComparableLink type of the GreenMST service.
 *
 * @author Luca Prete <luca.prete@garr.it>
 * @author Andrea Biancini <andrea.biancini@garr.it>
 * @author Fabio Farina <fabio.farina@garr.it>
 * 
 * @version 0.8
 * @see org.codehaus.jackson.map.JsonSerializer
 * @see it.garr.greenmst.types.LinkWithCost
 *
 */

public class TopologyCostsJSONSerializer extends JsonSerializer<TopologyCosts> {

    @Override
    public void serialize(TopologyCosts costs, JsonGenerator jGen, SerializerProvider sProvider)
    		throws IOException, JsonProcessingException {
    	
        jGen.writeStartObject();
        
        HashMap<String, Integer> prop = costs.getCosts();
        for (Entry<String, Integer> curProp : prop.entrySet()) {
        	jGen.writeNumberField(curProp.getKey(), curProp.getValue()); 
        }
        
        jGen.writeEndObject();
    }

    @Override
    public Class<TopologyCosts> handledType() {
        return TopologyCosts.class;
    }

}