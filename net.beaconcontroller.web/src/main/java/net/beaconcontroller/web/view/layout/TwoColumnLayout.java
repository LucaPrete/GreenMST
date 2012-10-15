/**
 * Copyright 2011, Stanford University. This file is licensed under GPL v2 plus
 * a special exception, as described in included LICENSE_EXCEPTION.txt.
 */
package net.beaconcontroller.web.view.layout;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.beaconcontroller.web.view.section.Section;

/**
 *
 *
 * @author David Erickson (daviderickson@cs.stanford.edu)
 * @author Kyle Forster (kyle.forster@bigswitch.com)
 */
public class TwoColumnLayout extends Layout {
    public static final String COLUMN1 = "column1";
    public static final String COLUMN2 = "column2";

    protected List<Section> columnOne = new ArrayList<Section>();
    protected List<Section> columnTwo = new ArrayList<Section>();

    /**
     * Picks which column each section should be rendered in
     */
    protected void sortColumns() {
        for (Entry<Section, Object> entry : sections.entrySet()) {
            if (entry.getValue() != null && COLUMN2.equals(entry.getValue()))
                columnTwo.add(entry.getKey());
            else
                columnOne.add(entry.getKey());
        }
    }

    @Override
    public void render(HttpServletRequest request, HttpServletResponse response) throws Exception {
        sortColumns();
        request.getRequestDispatcher(RESOURCE_PATH + "twocolumn-header.jsp").include(request, response);
        request.getRequestDispatcher(RESOURCE_PATH + "twocolumn-colheader.jsp").include(request, response);
        for (Section section : columnOne)
            section.render(request, response);
        request.getRequestDispatcher(RESOURCE_PATH + "twocolumn-colfooter.jsp").include(request, response);
        request.getRequestDispatcher(RESOURCE_PATH + "twocolumn-colheader.jsp").include(request, response);
        for (Section section : columnTwo)
            section.render(request, response);
        request.getRequestDispatcher(RESOURCE_PATH + "twocolumn-colfooter.jsp").include(request, response);
        request.getRequestDispatcher(RESOURCE_PATH + "twocolumn-footer.jsp").include(request, response);
    }
}
