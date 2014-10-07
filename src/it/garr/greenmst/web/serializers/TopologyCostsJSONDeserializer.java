package it.garr.greenmst.web.serializers;

import it.garr.greenmst.types.TopologyCosts;

import java.io.IOException;
import java.util.HashMap;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

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

public class TopologyCostsJSONDeserializer extends JsonDeserializer<TopologyCosts> {

	@Override
	public TopologyCosts deserialize(JsonParser jParser, DeserializationContext context)
			throws IOException, JsonProcessingException {
		
		HashMap<String, Integer> map = new HashMap<String, Integer>();
		jParser.nextToken();
        
        if (jParser.getCurrentToken() != JsonToken.START_OBJECT) throw new IOException("Expected START_OBJECT");

        while (jParser.nextToken() != JsonToken.END_OBJECT) {
            if (jParser.getCurrentToken() != JsonToken.FIELD_NAME) throw new IOException("Expected FIELD_NAME");

            String name = jParser.getCurrentName();
            jParser.nextToken();
            if (jParser.getText().equals("")) continue;
            Integer value = Integer.parseInt(jParser.getText());
            
            map.put(name, value);
        }
    	
        TopologyCosts costs = new TopologyCosts();
		costs.setCostsValues(map);
		
		return costs;
	}

}