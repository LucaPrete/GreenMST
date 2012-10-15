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

public class GarrSwitch {
    
    private long id;
    private String name;
    
    public GarrSwitch(String name, long id){
        this.id = id;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "GarrSwitch [id=" + id + ", name=" + name + "]";
    }
    
}
