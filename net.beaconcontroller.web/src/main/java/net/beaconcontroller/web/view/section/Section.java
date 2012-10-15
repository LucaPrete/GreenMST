/**
 * Copyright 2011, Stanford University. This file is licensed under GPL v2 plus
 * a special exception, as described in included LICENSE_EXCEPTION.txt.
 */
package net.beaconcontroller.web.view.section;

import net.beaconcontroller.web.view.Renderable;

/**
 *
 *
 * @author David Erickson (daviderickson@cs.stanford.edu)
 * @author Kyle Forster (kyle.forster@bigswitch.com)
 */
public abstract class Section implements Renderable {
    protected String title;

    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param title the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }
}
