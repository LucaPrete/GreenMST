package it.garr.greenmst.types;

import it.garr.greenmst.web.serializers.TopologyCostsJSONDeserializer;
import it.garr.greenmst.web.serializers.TopologyCostsJSONSerializer;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(using=TopologyCostsJSONSerializer.class)
@JsonDeserialize(using=TopologyCostsJSONDeserializer.class)
public class TopologyCosts {
	
	protected static Logger logger = LoggerFactory.getLogger(TopologyCosts.class);
	private static HashMap<String, Integer> costs = new HashMap<String, Integer>();
	public static final int DEFAULT_COST = 1;
	
	public TopologyCosts() {
		try {
			//load a properties file from class path, inside static method
			Properties prop = new Properties(); 
			prop.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("nodecosts.properties"));
			 
			Enumeration<?> e = prop.propertyNames();
		    while (e.hasMoreElements()) {
		      String key = (String) e.nextElement();
		      Integer value = Integer.parseInt(prop.getProperty(key));
		      costs.put(key, value);
		    }
		}  catch (IOException ex) {
			logger.error("Error while reading nodecosts.properties file.", ex);
		}
	}
	
	public void setCostsValues(HashMap<String, Integer> map) {
		//costs.clear();
		costs.putAll(map);
	}
	
	public HashMap<String, Integer> getCosts() {
		return costs;
	}
	
	public void setCost(long source, long destination, int cost) {
		if (costs != null) {
			if (costs.get(source + "," + destination) != null) costs.put(source + "," + destination, cost);
			if (costs.get(destination + "," + source) != null) costs.put(destination + "," + source, cost);
		}
	}
	
	public int getCost(long source, long destination) {
		if (costs != null) {
			Integer sCost = costs.get(source + "," + destination);
			if (sCost == null) sCost = costs.get(destination + "," + source);
			if (sCost != null) return sCost;
		}
		
		return DEFAULT_COST;
	}
	
	public String toString() {
		if (costs == null) return "(null)";
		
		String s = "";
		for (Entry<String, Integer> curProp: costs.entrySet()) {
			if (!s.equals("")) s += "\n";
			s += curProp.getKey() + " => " + curProp.getValue();
		}
		return s;
	}
	
	
}
