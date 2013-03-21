package it.garr.greenmst.web.serializers;

import it.garr.greenmst.types.ComparableLink;

import java.io.IOException;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;
import org.openflow.util.HexString;

/**
 * Class that serializes the ComparableLink type of the GreenMST service.
 *
 * @author Luca Prete <luca.prete@garr.it>
 * @author Andrea Biancini <andrea.biancini@garr.it>
 * @author Fabio Farina <fabio.farina@garr.it>
 * 
 * @version 0.8
 * @see org.codehaus.jackson.map.JsonSerializer
 * @see it.garr.greenmst.types.ComparableLink
 *
 */

public class ComparableLinkJSONSerializer extends JsonSerializer<ComparableLink> {

    @Override
    public void serialize(ComparableLink link, JsonGenerator jGen, SerializerProvider sProvider)
    		throws IOException, JsonProcessingException {
    	
        jGen.writeStartObject();
        
        jGen.writeStringField("sourceSwitch", HexString.toHexString(link.getSrc()));
        jGen.writeStringField("sourcePort", Short.toString(link.getSrcPort()));
        jGen.writeStringField("destinationSwitch", HexString.toHexString(link.getDst()));
        jGen.writeStringField("destinationPort", Short.toString(link.getDstPort()));
        jGen.writeStringField("cost", Integer.toString(link.getCost()));
        
        jGen.writeEndObject();
    }

    @Override
    public Class<ComparableLink> handledType() {
        return ComparableLink.class;
    }

}