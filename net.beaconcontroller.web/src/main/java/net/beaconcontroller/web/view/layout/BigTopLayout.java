/**
 * Copyright 2011, Stanford University. This file is licensed under GPL v2 plus
 * a special exception, as described in included LICENSE_EXCEPTION.txt.
 */
package net.beaconcontroller.web.view.layout;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.beaconcontroller.web.view.section.Section;

/**
 * A big top area (e.g. for a chart) with a two column underneath
 *
 * @author Kyle Forster (kyle.forster@bigswitch.com)
 * @author David Erickson (daviderickson@cs.stanford.edu)
 *
 */
public class BigTopLayout extends TwoColumnLayout {
    protected Section topSection;

    @Override
    public void render(HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        // borrow the first section for ourselves
        for (Section s : sections.keySet()) {
            topSection = s;
            sections.remove(s);
            break;
        }

        request.getRequestDispatcher(RESOURCE_PATH+"bigtop-header.jsp").include(request, response);
        if (topSection != null)
            topSection.render(request, response);
        request.getRequestDispatcher(RESOURCE_PATH+"bigtop-footer.jsp").include(request, response);
        super.render(request, response);
    }
}
