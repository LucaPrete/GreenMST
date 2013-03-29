package it.garr.greenmst.algorithms;

import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KruskalAlgorithmTest extends GenericAlgorithmTest {
	
	protected static Logger logger = LoggerFactory.getLogger(KruskalAlgorithmTest.class);

	@Before
    public void setUp() throws Exception { 
		algorithm = new KruskalAlgorithm();
        logger.info("Ended startUp.");
    }
	
}
