/**
 * Copyright 2011, Stanford University. This file is licensed under GPL v2 plus
 * a special exception, as described in included LICENSE_EXCEPTION.txt.
 */
/**
 * 
 */
package net.beaconcontroller.web.view.json;

import java.io.IOException;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;
import org.openflow.util.U32;

/**
 * @author David Erickson (derickso@cs.stanford.edu)
 *
 */
public class UnsignedIntegerJsonSerializer extends JsonSerializer<Integer> {

    @Override
    public void serialize(Integer value, JsonGenerator jgen,
            SerializerProvider provider) throws IOException,
            JsonProcessingException {
        jgen.writeNumber(U32.f(value));
    }
}
