package it.garr.greenmst.algorithms;

import it.garr.greenmst.types.LinkWithCost;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KruskalAlgorithm implements IMinimumSpanningTreeAlgorithm {
	
	protected static Logger logger = LoggerFactory.getLogger(KruskalAlgorithm.class);

	@Override
	public Vector<LinkWithCost> perform(List<LinkWithCost> topoEdges) throws Exception {
		return perform(topoEdges, false);
	}
	
	@Override
	// KRUSKAL ALGORITHM -- COLUMBIA UNIV. IMPL.
    public Vector<LinkWithCost> perform(List<LinkWithCost> topoEdges, boolean reverse) throws Exception {
		logger.debug("Starting to perform Kruskal algorithm...");
		
		if (reverse) {
			Collections.sort(topoEdges, new Comparator<LinkWithCost>() {
				public int compare(LinkWithCost link1, LinkWithCost link2) {
					return new Integer(link2.getCost()).compareTo(link1.getCost());
				}
			});
		} else {
			Collections.sort(topoEdges, new Comparator<LinkWithCost>() {
				public int compare(LinkWithCost link1, LinkWithCost link2) {
					return new Integer(link1.getCost()).compareTo(link2.getCost());
				}
			});
		}
		
		logger.trace("Kruskal performed on the following topoEdges: " + printEdges(topoEdges));
		
		HashMap<Long, HashSet<Long>> nodes = new HashMap<Long, HashSet<Long>>();
		
        // Generates nodes Hashmap containing one entry for each switch
        for (LinkWithCost lt: topoEdges) {
            if (!nodes.containsKey(lt.getSrc())) {
            	// Create set of connect components [singleton] for this node
                nodes.put(lt.getSrc(), new HashSet<Long>());
                nodes.get(lt.getSrc()).add(lt.getSrc());
            }
            
            if (!nodes.containsKey(lt.getDst())) {
            	// Create set of connect components [singleton] for this node
                nodes.put(lt.getDst(), new HashSet<Long>());
                nodes.get(lt.getDst()).add(lt.getDst());
            }
        }
        
        logger.trace("Kruskal generated the following nodes structure: " + printNodes(nodes));
    	
    	Vector<LinkWithCost> mstEdges = new Vector<LinkWithCost>();
    	Vector<LinkWithCost> edgesDone = new Vector<LinkWithCost>();
    	
    	logger.trace("Entering Kruskal cycle...");
    	for (LinkWithCost curEdge: topoEdges) {
    		logger.trace("curEdge = {}", new Object[] { curEdge });
    		
	        if (edgesDone.contains(curEdge)) {
	        	logger.trace("Edge already computed by Kruskal. Not computing again!");
	        } else {
	        	edgesDone.add(curEdge); // This way the same edge will not be processed two times (if present two times in topoEdges)
	        	if (nodes.get(curEdge.getSrc()).equals(nodes.get(curEdge.getDst()))) {
	        		logger.trace("Edge has source set equal to destination set. Not considering for MST!");
	        	} else {
	        		HashSet<Long> src = null, dst = null;
	        		Long dstHashSetIndex = 0L;
	        		
	        		logger.trace("Comparing size of source and destination of curEdge: (src = {}, dst = {}).", new Object[] {nodes.get(curEdge.getSrc()).size(), nodes.get(curEdge.getDst()).size()});
	        		if (nodes.get(curEdge.getSrc()).size() > nodes.get(curEdge.getDst()).size()) {
	        			// have to transfer all nodes including curEdge.to
	        			src = nodes.get(curEdge.getDst());
	        			dst = nodes.get(dstHashSetIndex = curEdge.getSrc());
	        		} else {
	        			// have to transfer all nodes including curEdge.from
	        			src = nodes.get(curEdge.getSrc());
	        			dst = nodes.get(dstHashSetIndex = curEdge.getDst());
	        		}
	        		logger.trace("Set src = {}, dst = {}.", new Object[] {printHash(src), printHash(dst)});
	        		
	        		Object[] srcArray = src.toArray();
	        		int transferSize = srcArray.length;
	        		
	        		logger.trace("Moving each node from set: src into set: dst.");
	        		logger.trace("Updating appropriate index in array: nodes.");
	        		for (int j = 0; j < transferSize; j++) {
	        			if (src.remove(srcArray[j])) {
	        				dst.add((Long) srcArray[j]);
	        				nodes.put((Long) srcArray[j], nodes.get(dstHashSetIndex));
	        			} else {
	        				logger.error("Error while removing element {} from array {}.", new Object[] {srcArray[j], src});
	        				throw new Exception("Kruskal - Error performing Kruskal algorithm (set union).");
	        			}
	        		}
	        		logger.trace("Kruskal updated the nodes structure: " + printNodes(nodes));
	        		
	        		logger.trace("Kruskal add the edge {} to mstEdges.", new Object[] {curEdge});
	        		mstEdges.add(curEdge);
	        	}
	        }
    	}
    	logger.trace("End of Kruskal cycle.");
    	logger.debug("Computed MST by Kruskal: "  + printEdges(mstEdges));
    	logger.debug("End of Kruskal algorithm.");
    	
    	return mstEdges;
    }
	
	private static String printEdges(Iterable<LinkWithCost> edges) {
    	String s  = "\n";
    	for (LinkWithCost e: edges) {
    		s += e.toString() + "\n";
    	}
    	return s;
    }
	
	private static String printNodes(HashMap<Long, HashSet<Long>> nodes) {
		String s  = "\n";
    	for (Map.Entry<Long, HashSet<Long>> entry: nodes.entrySet()) {
    		s += "Node (" + entry.getKey() + "): " + printHash(entry.getValue()) + "\n";
    	}
    	return s;
	}
	
	private static String printHash(HashSet<Long> value) {
		String s  = "(";
		for (Long set : value) {
			s += set + ", ";
		}
		s += ")";
    	return s;
	}

}
