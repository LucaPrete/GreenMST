package it.garr.greenmst.types;

import it.garr.greenmst.web.serializers.TopologyCostsJSONDeserializer;
import it.garr.greenmst.web.serializers.TopologyCostsJSONSerializer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Properties;

import net.floodlightcontroller.core.module.FloodlightModuleContext;

import org.codehaus.jackson.map.annotate.JsonDeserialize;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@JsonSerialize(using=TopologyCostsJSONSerializer.class)
@JsonDeserialize(using=TopologyCostsJSONDeserializer.class)
public class TopologyCosts {
	
	protected FloodlightModuleContext context = null;
	protected static Logger logger = LoggerFactory.getLogger(TopologyCosts.class);
	private static Properties prop = null;
	public static final int DEFAULT_COST = 1;
	
	public TopologyCosts() {
		prop = new Properties(); 
		try {
			//load a properties file from class path, inside static method
			prop.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("nodecosts.properties"));
		}  catch (IOException ex) {
			logger.error("Error while reading nodecosts.properties file.", ex);
		}
	}
	
	public void setPropValues(HashMap<String, String> map) {
		prop = new Properties();
		for (Entry<String, String> entry : map.entrySet()) {
			prop.setProperty(entry.getKey(), entry.getValue());
		}
	}
	
	public Properties getProp() {
		return prop;
	}
	
	public int getCost(long source, long destination) {
		int cost = DEFAULT_COST;
		
		if (prop != null) {
			String sCost = prop.getProperty(source + "," + destination);
			if (sCost == null) sCost = prop.getProperty(destination + "," + source);
			if (sCost != null) cost = Integer.parseInt(sCost);
			return cost;
		}
		
		return cost;
	}
	
	public String toString() {
		if (prop == null) return "(null)";
		
		String s = "";
		for (Entry<Object, Object> curProp: prop.entrySet()) {
			if (!s.equals("")) s += "\n";
			s += curProp.getKey() + " => " + curProp.getValue();
		}
		return s;
	}
	
	
}
