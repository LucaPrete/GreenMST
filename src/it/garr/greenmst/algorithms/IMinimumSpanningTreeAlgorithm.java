package it.garr.greenmst.algorithms;

import it.garr.greenmst.types.ComparableLink;

import java.util.TreeSet;
import java.util.Vector;

public interface IMinimumSpanningTreeAlgorithm {
	
	public Vector<ComparableLink> perform(TreeSet<ComparableLink> topoEdges) throws Exception;

}