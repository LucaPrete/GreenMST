/**
 * This file is licensed under GPL v2 plus.
 * 
 * A special exception, as described in included LICENSE_EXCEPTION.txt.
 *
 */

package net.beaconcontroller.garrController;

import java.util.ArrayList;
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

public class SwitchMap {

    protected static Logger logger = LoggerFactory.getLogger(RouterMap.class);
    private ArrayList<GarrSwitch> switchMap = new ArrayList<GarrSwitch>();
    
    // Returns the switchMap
    public ArrayList<GarrSwitch> getRouterMap() {
        return switchMap;
    }
        
    // Says if newSwitch exists in switchMap, if exists return index of the element, otherwise it returns -1
    private int switchExists(GarrSwitch newSwitch){
        for(int i=0;i<switchMap.size();i++){
            if(switchMap.get(i).equals(newSwitch)){
                return i;
            }
        }
        return -1;
    }
    
    // If exists returns the name of a given switch with id
    public String getNameById(Long id){
        for(int i=0;i<switchMap.size();i++){
            if(switchMap.get(i).getId().equals(id)){
                return switchMap.get(i).getName();
            }
        }
        return "";
    }
    
    public String getNameById(long id){
        for(int i=0;i<switchMap.size();i++){
            if(switchMap.get(i).getId() == id){
                return switchMap.get(i).getName();
            }
        }
        return "";
    }
    
    // Adds newSwitch to the switchMap. If it's added returns true, otherwise false.
    public boolean addSwitch(GarrSwitch newSwitch){
        int i = switchExists(newSwitch);
        if(i!=-1){
            switchMap.set(i, newSwitch);
            logger.warn("GARR - Switch " + newSwitch.getName() + " ("+ newSwitch.getId() + ") already exists: Router informations will be overwritten.");
        }else{
            switchMap.add(newSwitch);
            logger.debug("GARR - Switch " + newSwitch.getName() + " (" + newSwitch.getId() + ") succesfully added to the routerMap.");
        }
        return true;
    }
    
}