/**
 * Copyright 2011, Stanford University. This file is licensed under GPL v2 plus
 * a special exception, as described in included LICENSE_EXCEPTION.txt.
 */
package net.beaconcontroller.web.view.section;

import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author David Erickson (daviderickson@cs.stanford.edu)
 */
public class JspSection extends Section {
    protected String jspFileName;
    protected Map<String, Object> model;
    protected String resourcePath = "/WEB-INF/jsp/view/section/";

    /**
     * 
     */
    public JspSection() {
    }

    /**
     * @param jspFileName
     * @param model
     */
    public JspSection(String jspFileName, Map<String, Object> model) {
        this.jspFileName = jspFileName;
        this.model = model;
    }

    @Override
    public void render(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        if (model != null)
            for (Entry<String,Object> entry : model.entrySet())
                request.setAttribute(entry.getKey(), entry.getValue());
        if (jspFileName != null)
            request.getRequestDispatcher(resourcePath + jspFileName).include(request, response);
    }

    /**
     * @return the jspFileName
     */
    public String getJspFileName() {
        return jspFileName;
    }

    /**
     * @param jspFileName the jspFileName to set
     */
    public void setJspFileName(String jspFileName) {
        this.jspFileName = jspFileName;
    }

    /**
     * @return the model
     */
    public Map<String, Object> getModel() {
        return model;
    }

    /**
     * @param model the model to set
     */
    public void setModel(Map<String, Object> model) {
        this.model = model;
    }

    /**
     * @return the resourcePath
     */
    public String getResourcePath() {
        return resourcePath;
    }

    /**
     * @param resourcePath the resourcePath to set
     */
    public void setResourcePath(String resourcePath) {
        this.resourcePath = resourcePath;
    }
}
