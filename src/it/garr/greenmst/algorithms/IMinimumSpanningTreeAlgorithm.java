package it.garr.greenmst.algorithms;

import it.garr.greenmst.types.LinkWithCost;

import java.util.List;
import java.util.Vector;

public interface IMinimumSpanningTreeAlgorithm {

	public Vector<LinkWithCost> perform(List<LinkWithCost> topoEdges) throws Exception;
	public Vector<LinkWithCost> perform(List<LinkWithCost> topoEdges, boolean reverse) throws Exception;

}
