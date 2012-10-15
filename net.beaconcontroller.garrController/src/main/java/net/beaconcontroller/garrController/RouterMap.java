/**
 * This file is licensed under GPL v2 plus.
 * 
 * A special exception, as described in included LICENSE_EXCEPTION.txt.
 * 
 */

package net.beaconcontroller.garrController;

import java.util.ArrayList;
import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
* 
* @author David Erickson (daviderickson@cs.stanford.edu) - 04/04/10
* @author Luca Prete (luca.prete@garr.it) - 15/10/12
* @author Andrea Biancini (andrea.biancini@mib.infn.it) - 15/10/12
* @author Fabio Farina (fabio.farina@garr.it) - 15/10/12
* 
*/

public class RouterMap {

    protected static Logger logger = LoggerFactory.getLogger(RouterMap.class);
    private ArrayList<GarrRouter> routerMap = new ArrayList<GarrRouter>();
        
    // Says if router exists in routerMap, if exists return index of the element, otherwise it returns -1
    public int routerExists(GarrRouter router){
        for(int i=0;i<routerMap.size();i++){
            if(routerMap.get(i).equals(router)){
                return i;
            }
        }
        return -1;
    }
    
    // Get master router (first of the ordered routerMap)
    public GarrRouter getMasterRouter(){
        if(routerMap.size()>0){
            return routerMap.get(0);
        }
        logger.error("GARR - Return master router impossible. Al least two routers must be configured");
        return null;
    }
    
    // Get slave router (first of the ordered routerMap)
    public GarrRouter getSlaveRouter(){
        if(routerMap.size()>1){
            return routerMap.get(1);
        }
        logger.error("GARR - Return slave router is impossible. Al least two routers must be configured");
        return null;
    }
    
    // If exists returns the name of a given router with mac
    public String getNameByMac(String mac){
        for(int i=0;i<routerMap.size();i++){
            if(routerMap.get(i).getMac().equals(mac)){
                return routerMap.get(i).getName();
            }
        }
        return "";
    }
    
    // Says if a router with mac exists. If exists returns true, otherwise it returns false
    public boolean macExists(String mac){
        for(int i=0;i<routerMap.size();i++){
            if(routerMap.get(i).getMac().equals(mac.toLowerCase())){
                return true;
            }
        }
        return false;
    }
    
    // Adds a new router to the routerMap
    public boolean addRouter(GarrRouter router){
        int i = routerExists(router);
        if(i!=-1){
            routerMap.set(i, router);
            logger.debug("GARR - Router " + router.getName() + " ("+router.getMac() + ") already exists: Router informations will be overwritten.");
        }else{
            routerMap.add(router);
            logger.debug("GARR - Router " + router.getName() + " ("+router.getMac() + ") succesfully added to the routerMap.");
        }
        Collections.sort(routerMap);
        return true;
    }
    
}
