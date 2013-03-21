package it.garr.greenmst.algorithms;

import it.garr.greenmst.types.ComparableLink;

import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeSet;
import java.util.Vector;

public class KruskalAlgorithm implements IMinimumSpanningTreeAlgorithm {

	@Override
	// KRUSKAL ALGORITHM -- COLUMBIA UNIV. IMPL.
    public Vector<ComparableLink> perform(TreeSet<ComparableLink> topoEdges) throws Exception {
    	HashMap<Long, HashSet<Long>> nodes = new HashMap<Long, HashSet<Long>>();
		
        // Generates nodes Hashmap containing one entry for each switch
        for (ComparableLink lt: topoEdges) {
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
    	
    	Vector<ComparableLink> mstEdges = new Vector<ComparableLink>();
    	Vector<ComparableLink> edgesDone = new Vector<ComparableLink>();
    	
    	for (ComparableLink curEdge: topoEdges) {
	        if (!edgesDone.contains(curEdge)) {
	        	edgesDone.add(curEdge); // This way the same edge will not be processed two times (if present two times in topoEdges)
	        	if (!nodes.get(curEdge.getSrc()).equals(nodes.get(curEdge.getDst()))) {
	        		HashSet<Long> src = null, dst = null;
	        		Long dstHashSetIndex;
	        		if (nodes.get(curEdge.getSrc()).size() > nodes.get(curEdge.getDst()).size()) {
	        			// have to transfer all nodes including curEdge.to
	        			src = nodes.get(curEdge.getDst());
	        			dst = nodes.get(dstHashSetIndex = curEdge.getSrc());
	        		} else {
	        			// have to transfer all nodes including curEdge.from
	        			src = nodes.get(curEdge.getSrc());
	        			dst = nodes.get(dstHashSetIndex = curEdge.getDst());
	        		}
	        		Object[] srcArray = src.toArray();
	        		int transferSize = srcArray.length;
	        		for (int j = 0; j < transferSize; j++) {
	        			// move each node from set: src into set: dst
	        			// and update appropriate index in array: nodes
	        			if (src.remove(srcArray[j])) {
	        				dst.add((Long) srcArray[j]);
	        				nodes.put((Long) srcArray[j], nodes.get(dstHashSetIndex));
	        			} else {
	        				throw new Exception("Kruskal - Error performing Kruskal algorithm (set union)");
	        			}
	        		}
	        		mstEdges.add(curEdge);
	        	}
	        }
    	}
    	
    	return mstEdges;
    }

}
