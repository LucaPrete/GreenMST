/**
 * Copyright 2011, Stanford University. This file is licensed under GPL v2 plus
 * a special exception, as described in included LICENSE_EXCEPTION.txt.
 */
package net.beaconcontroller.web.view;

import java.util.Map;

import net.beaconcontroller.web.view.json.OFFlowStatisticsReplyMixin;
import net.beaconcontroller.web.view.json.OFMatchMixin;

import org.codehaus.jackson.map.ObjectMapper;
import org.openflow.protocol.OFMatch;
import org.openflow.protocol.statistics.OFFlowStatisticsReply;
import org.springframework.web.servlet.view.json.MappingJacksonJsonView;

/**
 * Extends {@link MappingJacksonJsonView} and adds the capability to set the
 * root object of the serialization to be something other than the default
 * Map. If an object exists in the model with key {@link ROOT_OBJECT_KEY} then
 * it is set as the root of the Json serialization, otherwise the Map is used.
 *
 * @author David Erickson (daviderickson@cs.stanford.edu)
 */
public class BeaconJsonView extends MappingJacksonJsonView {
    public static String ROOT_OBJECT_KEY = BeaconJsonView.class.getName() + "_ROOT";

    protected ObjectMapper objectMapper;

    /**
     * 
     */
    public BeaconJsonView() {
        super();
        objectMapper = new ObjectMapper();
        // Terrible API design to not let me touch the parent object
        super.setObjectMapper(objectMapper);

        // Add our known mixins
        addSerializeMixin(OFFlowStatisticsReply.class, OFFlowStatisticsReplyMixin.class);
        addSerializeMixin(OFMatch.class, OFMatchMixin.class);
    }

    @Override
    @SuppressWarnings("unchecked")
    protected Object filterModel(Map<String, Object> model) {
        Map<String, Object> map = (Map<String, Object>) super.filterModel(model);
        if (map.containsKey(ROOT_OBJECT_KEY))
            return map.get(ROOT_OBJECT_KEY);
        else
            return map;
    }

    public void addSerializeMixin(Class<? extends Object> source, Class<? extends Object> mixin) {
        this.objectMapper.getSerializationConfig().addMixInAnnotations(source, mixin);
    }
}
