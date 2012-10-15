/**
 * Copyright 2011, Stanford University. This file is licensed under GPL v2 plus
 * a special exception, as described in included LICENSE_EXCEPTION.txt.
 */
package net.beaconcontroller.devicemanager.web;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import net.beaconcontroller.devicemanager.Device;
import net.beaconcontroller.devicemanager.IDeviceManager;
import net.beaconcontroller.packet.IPv4;
import net.beaconcontroller.web.IWebManageable;
import net.beaconcontroller.web.view.BeaconViewResolver;
import net.beaconcontroller.web.view.Tab;
import net.beaconcontroller.web.view.layout.Layout;
import net.beaconcontroller.web.view.layout.TwoColumnLayout;
import net.beaconcontroller.web.view.section.TableSection;

import org.openflow.util.HexString;
import org.openflow.util.U16;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * This class sets up the web UI component for the structures in
 * net.beaconcontroller.devicemanager
 * 
 * @author David Erickson (daviderickson@cs.stanford.edu)
 */
@Controller
@RequestMapping("/devicemanager")
public class DeviceManagerWebManageable implements IWebManageable {
    protected static Logger log = LoggerFactory.getLogger(DeviceManagerWebManageable.class);
    protected List<Tab> tabs;
    protected IDeviceManager deviceManager;

    public DeviceManagerWebManageable() {
        tabs = new ArrayList<Tab>();
        tabs.add(new Tab("Overview", "/wm/devicemanager/overview.do"));
    }

    @Override
    public String getName() {
        return "Device Manager";
    }

    @Override
    public String getDescription() {
        return "View devices.";
    }

    @Override
    public List<Tab> getTabs() {
        return tabs;
    }

    @RequestMapping("/overview")
    public String overview(Locale locale, Map<String, Object> model) {
        Layout layout = new TwoColumnLayout();
        model.put("layout", layout);
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd HH:mm:ss z", locale);

        // Listener List Table
        List<String> columnNames = new ArrayList<String>();
        List<List<String>> cells = new ArrayList<List<String>>();
        columnNames = new ArrayList<String>();
        columnNames.add("MAC");
        columnNames.add("IP");
        columnNames.add("Last Seen");
        columnNames.add("Switch");
        columnNames.add("Port");
        cells = new ArrayList<List<String>>();
        for (Device device : deviceManager.getDevices()) {
            List<String> row = new ArrayList<String>();
            row.add(HexString.toHexString(device.getDataLayerAddress()));
            StringBuffer sb = new StringBuffer();
            for (Integer nw : device.getNetworkAddresses()) {
                if (sb.length() > 0)
                    sb.append(" ");
                sb.append(IPv4.fromIPv4Address(nw) + " ");
            }
            row.add(sb.toString());
            row.add(sdf.format(new Date(device.getLastSeen())));
            row.add(HexString.toHexString(device.getSw().getId()));
            row.add(((Integer)U16.f(device.getSwPort())).toString());
            cells.add(row);
        }
        Map<String,String> tableOptions = new HashMap<String, String>();
        tableOptions.put("\"bFilter\"", "true");
        TableSection tableSection = new TableSection("Devices", columnNames, cells, "table-devices", tableOptions);
        layout.addSection(tableSection, TwoColumnLayout.COLUMN1);

        return BeaconViewResolver.SIMPLE_VIEW;
    }

    /**
     * @param deviceManager the deviceManager to set
     */
    @Autowired
    public void setDeviceManager(IDeviceManager deviceManager) {
        this.deviceManager = deviceManager;
    }
}
