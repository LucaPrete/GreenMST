/**
 * Copyright 2011, Stanford University. This file is licensed under GPL v2 plus
 * a special exception, as described in included LICENSE_EXCEPTION.txt.
 */
package net.beaconcontroller.web.view.section;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 
 *
 * @author David Erickson (daviderickson@cs.stanford.edu)
 */
public class TableSection extends JspSection {
    protected List<String> columnNames;
    protected List<List<String>> cells;
    protected String tableId;
    protected Map<String,String> tableOptions;

    public TableSection(String title, List<String> columnNames,
            List<List<String>> cells, String tableId) {
        this.title = title;
        this.columnNames = columnNames;
        this.cells = cells;
        this.jspFileName = "table.jsp";
        this.tableId = tableId;
    }

    public TableSection(String title, List<String> columnNames,
            List<List<String>> cells, String tableId, Map<String,String> tableOptions) {
        this.title = title;
        this.columnNames = columnNames;
        this.cells = cells;
        this.jspFileName = "table.jsp";
        this.tableId = tableId;
        this.tableOptions = tableOptions;
    }

    @Override
    public void render(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        request.setAttribute("columnNames", columnNames);
        request.setAttribute("cells", cells);
        request.setAttribute("tableId", tableId);
        request.setAttribute("tableOptions", tableOptions);
        request.setAttribute("title", title);
        super.render(request, response);
    }
}
