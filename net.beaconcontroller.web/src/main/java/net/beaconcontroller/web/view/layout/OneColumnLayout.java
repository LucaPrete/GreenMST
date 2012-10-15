/**
 * Copyright 2011, Stanford University. This file is licensed under GPL v2 plus
 * a special exception, as described in included LICENSE_EXCEPTION.txt.
 */
package net.beaconcontroller.web.view.layout;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.beaconcontroller.web.view.section.Section;

/**
 *
 * @author David Erickson (daviderickson@cs.stanford.edu)
 */
public class OneColumnLayout extends Layout {

    @Override
    public void render(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        for (Section s : sections.keySet())
            s.render(request, response);
    }
}
