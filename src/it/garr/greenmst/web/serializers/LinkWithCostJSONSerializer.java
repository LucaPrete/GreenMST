package it.garr.greenmst.web.serializers;

import it.garr.greenmst.types.LinkWithCost;

import java.io.IOException;

import org.openflow.util.HexString;

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

public class LinkWithCostJSONSerializer extends JsonSerializer<LinkWithCost> {

    @Override
    public void serialize(LinkWithCost link, JsonGenerator jGen, SerializerProvider sProvider)
    		throws IOException, JsonProcessingException {
    	
        jGen.writeStartObject();
        
        jGen.writeStringField("sourceSwitch", HexString.toHexString(link.getSrc()));
        jGen.writeNumberField("sourcePort", link.getSrcPort());
        jGen.writeStringField("destinationSwitch", HexString.toHexString(link.getDst()));
        jGen.writeNumberField("destinationPort", link.getDstPort());
        jGen.writeNumberField("cost", link.getCost());
        
        jGen.writeEndObject();
    }

    @Override
    public Class<LinkWithCost> handledType() {
        return LinkWithCost.class;
    }

}