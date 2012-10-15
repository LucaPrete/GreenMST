/**
 * This file is licensed under GPL v2 plus.
 * 
 * A special exception, as described in included LICENSE_EXCEPTION.txt.
 * 
 */

package net.beaconcontroller.garrController;

/**
* 
* @author David Erickson (daviderickson@cs.stanford.edu) - 04/04/10
* @author Luca Prete (luca.prete@garr.it) - 15/10/12
* @author Andrea Biancini (andrea.biancini@mib.infn.it) - 15/10/12
* @author Fabio Farina (fabio.farina@garr.it) - 15/10/12
* 
*/

public class GarrUtil {

    public static int shortToInt(short num){
        return  (int)(num & 0xffff);
    }
    
}
