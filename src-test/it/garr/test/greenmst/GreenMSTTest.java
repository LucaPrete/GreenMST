package it.garr.test.greenmst;

import it.garr.greenmst.GreenMST;

import java.util.ArrayList;
import java.util.Collection;

import net.floodlightcontroller.core.module.FloodlightTestModuleLoader;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.test.MockFloodlightProvider;
import net.floodlightcontroller.test.FloodlightTestCase;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GreenMSTTest extends FloodlightTestCase {
	protected static Logger log = LoggerFactory.getLogger(GreenMSTTest.class);
	
	protected GreenMST greenMST = null;
	protected MockFloodlightProvider mockFloodlightProvider = null;
	
	@Before
    public void SetUp() throws Exception {
		super.setUp();
		
		/*
		FloodlightModuleContext fmc = new FloodlightModuleContext();
		mockFloodlightProvider = new MockFloodlightProvider();
		fmc.addService(IFloodlightProviderService.class, mockFloodlightProvider);
		
		topologyManager  = new TopologyManager();
		topologyManager.init(fmc);
		fmc.addService(ITopologyService.class, topologyManager);
		topology = fmc.getServiceImpl(ITopologyService.class);
		*/
		
		FloodlightTestModuleLoader fml = new FloodlightTestModuleLoader();
        Collection<Class<? extends IFloodlightModule>> mods = new ArrayList<Class<? extends IFloodlightModule>>();
        mods.add(GreenMST.class);
        fml.setupModules(mods, null);
        greenMST = (GreenMST) fml.getModuleByName(GreenMST.class);
        mockFloodlightProvider = (MockFloodlightProvider) fml.getModuleByName(MockFloodlightProvider.class);
    }
	
	@Test
    public void testMST4SwitchesFullMashed() throws Exception {
		/*
		TopologyManager tm = getTopologyManager();
        {
            int [][] linkArray = {
                                  {1, 1, 2, 1, DIRECT_LINK},
                                  {2, 2, 3, 2, DIRECT_LINK},
                                  {3, 1, 1, 2, DIRECT_LINK},
                                  {2, 3, 4, 2, DIRECT_LINK},
                                  {3, 3, 4, 1, DIRECT_LINK}
            };
            int [][] expectedClusters = {
                                         {1,2,3},
                                         {4}
            };
            //tm.recompute();
            createTopologyFromLinks(linkArray);
            verifyClusters(expectedClusters);
        }
        */
	}

}
