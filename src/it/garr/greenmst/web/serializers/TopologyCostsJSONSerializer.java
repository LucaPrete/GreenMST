


package it.garr.greenmst.web.serializers;

import it.garr.greenmst.types.TopologyCosts;

import java.io.IOException;
import java.util.Map.Entry;
import java.util.Properties;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;

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
        
        Properties prop = costs.getProp();
        for (Entry<Object, Object> curProp : prop.entrySet()) {
        	jGen.writeNumberField(curProp.getKey().toString(), Integer.parseInt(curProp.getValue().toString())); 
        }
        
        jGen.writeEndObject();
    }

    @Override
    public Class<TopologyCosts> handledType() {
        return TopologyCosts.class;
    }

}