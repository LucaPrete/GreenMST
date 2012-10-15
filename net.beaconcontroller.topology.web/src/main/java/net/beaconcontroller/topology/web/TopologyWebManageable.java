/**
 * Copyright 2011, Stanford University. This file is licensed under GPL v2 plus
 * a special exception, as described in included LICENSE_EXCEPTION.txt.
 */
package net.beaconcontroller.topology.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.beaconcontroller.topology.ITopology;
import net.beaconcontroller.topology.LinkTuple;
import net.beaconcontroller.web.IWebManageable;
import net.beaconcontroller.web.view.BeaconViewResolver;
import net.beaconcontroller.web.view.Tab;
import net.beaconcontroller.web.view.layout.Layout;
import net.beaconcontroller.web.view.layout.TwoColumnLayout;
import net.beaconcontroller.web.view.section.TableSection;

import org.openflow.util.HexString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * This class sets up the web UI component for the structures in
 * net.beaconcontroller.topology
 * 
 * @author David Erickson (daviderickson@cs.stanford.edu)
 */
@Controller
@RequestMapping("/topology")
public class TopologyWebManageable implements IWebManageable {
    protected static Logger log = LoggerFactory.getLogger(TopologyWebManageable.class);
    protected List<Tab> tabs;
    protected ITopology topology;

    public TopologyWebManageable() {
        tabs = new ArrayList<Tab>();
        tabs.add(new Tab("Overview", "/wm/topology/overview.do"));
    }

    @Override
    public String getName() {
        return "Topology";
    }

    @Override
    public String getDescription() {
        return "View the discovered topology.";
    }

    @Override
    public List<Tab> getTabs() {
        return tabs;
    }

    @RequestMapping("/overview")
    public String overview(Map<String, Object> model) {
        Layout layout = new TwoColumnLayout();
        model.put("layout", layout);

        // Listener List Table
        List<String> columnNames = new ArrayList<String>();
        List<List<String>> cells = new ArrayList<List<String>>();
        columnNames = new ArrayList<String>();
        columnNames.add("Src Id");
        columnNames.add("Src Port");
        columnNames.add("Dst Id");
        columnNames.add("Dst Port");
        cells = new ArrayList<List<String>>();
        for (LinkTuple lt : topology.getLinks().keySet()) {
            List<String> row = new ArrayList<String>();
            row.add(HexString.toHexString(lt.getSrc().getSw().getId()));
            row.add(lt.getSrc().getPort().toString());
            row.add(HexString.toHexString(lt.getDst().getSw().getId()));
            row.add(lt.getDst().getPort().toString());
            cells.add(row);
        }
        Map<String,String> tableOptions = new HashMap<String, String>();
        tableOptions.put("\"bFilter\"", "true");
        TableSection tableSection = new TableSection("Discovered Links", columnNames, cells, "table-links", tableOptions);
        layout.addSection(tableSection, TwoColumnLayout.COLUMN1);

        return BeaconViewResolver.SIMPLE_VIEW;
    }


    /**
     * @param topology the topology to set
     */
    @Autowired
    public void setTopology(ITopology topology) {
        this.topology = topology;
    }
}
