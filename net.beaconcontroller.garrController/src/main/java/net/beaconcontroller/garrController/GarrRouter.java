/**
 * This file is licensed under GPL v2 plus.
 * 
 * A special exception, as described in included LICENSE_EXCEPTION.txt.
 * 
 */

package net.beaconcontroller.garrController;

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

public class GarrRouter implements Comparable<GarrRouter>{
    
    private String name;
    private String mac;
    private int priority;
    protected static Logger logger = LoggerFactory.getLogger(GarrRouter.class);
    
    public GarrRouter(String name, String mac, int priority){
        this.name = name;
        this.mac = mac;
        if(priority<0){
            logger.error("GARR - Error setting priority for router " + this.getName() + " (" + this.getMac() + "). Priority value set to 0.");
            this.priority = 0;
        }
        this.priority = priority;
    }
    
    public int compareTo(GarrRouter router){
        if(this.priority<router.priority){
            return 1;
        }else if((this.priority==router.priority)){
            return 0;
        }else{
            return -1;
        }
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public int getPriority() {
        return priority;
    }

    @Override
    public String toString() {
        return "GarrRouter [name=" + name + ", mac=" + mac + ", priority="
                + priority + "]";
    }
    
}
