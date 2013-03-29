package it.garr.greenmst.algorithms;

import it.garr.greenmst.types.LinkWithCost;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Vector;

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class GenericAlgorithmTest extends TestCase {
	
	protected static Logger logger = LoggerFactory.getLogger(GenericAlgorithmTest.class);
	
	IMinimumSpanningTreeAlgorithm algorithm = null;

	@Before
    public abstract void setUp() throws Exception;
	
	@Test
	public void testPerform() throws Exception {
		ArrayList<LinkWithCost> allTopology = new ArrayList<LinkWithCost>();
		
		addLinkToCollection(allTopology, 1L, 1, 2L, 1, 1);
		addLinkToCollection(allTopology, 1L, 2, 3L, 1, 4);
		addLinkToCollection(allTopology, 1L, 3, 4L, 1, 2);
		addLinkToCollection(allTopology, 2L, 2, 3L, 2, 3);
		addLinkToCollection(allTopology, 2L, 3, 4L, 2, 4);
		addLinkToCollection(allTopology, 3L, 3, 4L, 3, 1);
		
		Vector<LinkWithCost> mstEdges = algorithm.perform(allTopology, false);
		Vector<LinkWithCost> reverseEdges = new Vector<LinkWithCost>();
		for (LinkWithCost link : mstEdges) {
			reverseEdges.add(link.getInverse());
		}
		mstEdges.addAll(reverseEdges);
		
		HashSet<LinkWithCost> expectedMSTEdges = new HashSet<LinkWithCost>();
		addLinkToCollection(expectedMSTEdges, 1L, 1, 2L, 1, 1);
		addLinkToCollection(expectedMSTEdges, 1L, 3, 4L, 1, 2);
		addLinkToCollection(expectedMSTEdges, 3L, 3, 4L, 3, 1);
			
		assertEquals("MST edges computed are right in number.", expectedMSTEdges.size(), mstEdges.size());
		
		for (LinkWithCost expectedLink : expectedMSTEdges) {
			assertTrue("MST edges computed contains the expected link " + expectedLink + ".", mstEdges.contains(expectedLink));
			
			for (LinkWithCost curLink : mstEdges) {
				if (curLink.equals(expectedLink)) {
					assertEquals("The cost for the rendundant edge link is correct.", expectedLink.getCost(), curLink.getCost());
				}
			}
		}
		
		logger.info("Ended testPerform.");
	}
	
	@Test
	public void testPerformReverse() throws Exception {
		ArrayList<LinkWithCost> allTopology = new ArrayList<LinkWithCost>();
		
		addLinkToCollection(allTopology, 1L, 1, 2L, 1, 1);
		addLinkToCollection(allTopology, 1L, 2, 3L, 1, 4);
		addLinkToCollection(allTopology, 1L, 3, 4L, 1, 2);
		addLinkToCollection(allTopology, 2L, 2, 3L, 2, 3);
		addLinkToCollection(allTopology, 2L, 3, 4L, 2, 4);
		addLinkToCollection(allTopology, 3L, 3, 4L, 3, 1);
		
		Vector<LinkWithCost> mstEdges = algorithm.perform(allTopology, true);
		Vector<LinkWithCost> reverseEdges = new Vector<LinkWithCost>();
		for (LinkWithCost link : mstEdges) {
			reverseEdges.add(link.getInverse());
		}
		mstEdges.addAll(reverseEdges);
		
		HashSet<LinkWithCost> expectedMSTEdges = new HashSet<LinkWithCost>();
		addLinkToCollection(expectedMSTEdges, 1L, 2, 3L, 1, 4);
		addLinkToCollection(expectedMSTEdges, 2L, 3, 4L, 2, 4);
		addLinkToCollection(expectedMSTEdges, 2L, 2, 3L, 2, 3);
			
		assertEquals("MST edges computed are right in number.", expectedMSTEdges.size(), mstEdges.size());
		
		for (LinkWithCost expectedLink : expectedMSTEdges) {
			assertTrue("MST edges computed contains the expected link " + expectedLink + ".", mstEdges.contains(expectedLink));
			
			for (LinkWithCost curLink : mstEdges) {
				if (curLink.equals(expectedLink)) {
					assertEquals("The cost for the rendundant edge link is correct.", expectedLink.getCost(), curLink.getCost());
				}
			}
		}
		
		logger.info("Ended testPerform.");
	}
	
	protected void addLinkToCollection(Collection<LinkWithCost> topoEdges, long switchFrom, int portFrom, long switchTo, int portTo, int cost) {
		LinkWithCost link = new LinkWithCost(switchFrom, portFrom, switchTo, portTo, cost);
		
		logger.debug("Link added: {}.", new Object[] { link });
        topoEdges.add(link);
        
        logger.debug("Link added: {}.", new Object[] { link.getInverse() });
        topoEdges.add(link.getInverse());
	}
}
