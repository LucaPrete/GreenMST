package it.garr.greenmst.tests;

import it.garr.greenmst.GreenMSTTest;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunWith(Suite.class)
@SuiteClasses({ GreenMSTTest.class, GreenMSTAlgorithmSuite.class })
public class GreenMSTTestSuite {
	protected static Logger logger = LoggerFactory.getLogger(GreenMSTTestSuite.class);

    @BeforeClass 
    public static void setUpClass() {
    	logger.info("Test suite setup.");

    }

    @AfterClass
    public static void tearDownClass() {
    	logger.info("Test suite end.");
    }

}