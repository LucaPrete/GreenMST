/**
 * Copyright 2011, Stanford University. This file is licensed under GPL v2 plus
 * a special exception, as described in included LICENSE_EXCEPTION.txt.
 */
package net.beaconcontroller.core.web;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import net.beaconcontroller.core.IBeaconProvider;
import net.beaconcontroller.core.IOFMessageListener;
import net.beaconcontroller.core.IOFSwitch;
import net.beaconcontroller.util.BundleAction;
import net.beaconcontroller.web.IWebManageable;
import net.beaconcontroller.web.view.BeaconJsonView;
import net.beaconcontroller.web.view.BeaconViewResolver;
import net.beaconcontroller.web.view.Tab;
import net.beaconcontroller.web.view.json.DataTableJsonView;
import net.beaconcontroller.web.view.json.OFFlowStatisticsReplyDataTableFormatCallback;
import net.beaconcontroller.web.view.layout.Layout;
import net.beaconcontroller.web.view.layout.OneColumnLayout;
import net.beaconcontroller.web.view.layout.TwoColumnLayout;
import net.beaconcontroller.web.view.section.JspSection;
import net.beaconcontroller.web.view.section.StringSection;
import net.beaconcontroller.web.view.section.TableSection;

import org.openflow.protocol.OFMatch;
import org.openflow.protocol.OFPort;
import org.openflow.protocol.OFStatisticsRequest;
import org.openflow.protocol.OFType;
import org.openflow.protocol.statistics.OFFlowStatisticsReply;
import org.openflow.protocol.statistics.OFFlowStatisticsRequest;
import org.openflow.protocol.statistics.OFPortStatisticsRequest;
import org.openflow.protocol.statistics.OFStatistics;
import org.openflow.protocol.statistics.OFStatisticsType;
import org.openflow.util.HexString;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.service.packageadmin.PackageAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.osgi.context.BundleContextAware;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.View;

/**
 * This class sets up the web UI component for the structures in
 * net.beaconcontroller.core and related "platform" related input/output.
 * 
 * It uses the net.beaconcontroller.web mgmt framework /
 * 
 * @author Kyle Forster (kyle.forster@bigswitch.com)
 * @author David Erickson (daviderickson@cs.stanford.edu)
 * 
 */
@Controller
@RequestMapping("/core")
public class CoreWebManageable implements BundleContextAware, IWebManageable {
    /**
     * Used to retrieve the OpenFlow request objects for the statistics request
     *
     */
    protected interface OFSRCallback {
        OFStatisticsRequest getRequest();
    }

    protected static Logger log = LoggerFactory.getLogger(CoreWebManageable.class);
    protected IBeaconProvider beaconProvider;
    protected BundleContext bundleContext;
    protected PackageAdmin packageAdmin;
    protected List<Tab> tabs;

    public CoreWebManageable() {
        tabs = new ArrayList<Tab>();
        tabs.add(new Tab("Overview", "/wm/core/overview.do"));
        tabs.add(new Tab("OSGi", "/wm/core/osgi.do"));
    }

    /**
     * The bundleContext to set (platform level stuff)
     */
    @Autowired
    @Override
    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }

    /**
     * 
     */
    @Autowired
    public void setBeaconProvider(IBeaconProvider beaconProvider) {
        this.beaconProvider = beaconProvider;
    }

    @Override
    public String getName() {
        return "Core";
    }

    @Override
    public String getDescription() {
        return "Controls the core components of Beacon.";
    }

    @Override
    public List<Tab> getTabs() {
        return tabs;
    }

    @RequestMapping("/overview")
    public String overview(Map<String, Object> model) {
        Layout layout = new TwoColumnLayout();
        model.put("layout", layout);

        // Description
        layout.addSection(
                new StringSection("Welcome",
                        "Thanks for using Beacon!"),
                TwoColumnLayout.COLUMN1);

        // Switch List Table
        model.put("title", "Switches");
        model.put("switches", beaconProvider.getSwitches().values());
        layout.addSection(new JspSection("switches.jsp", model), TwoColumnLayout.COLUMN2);

        // Listener List Table
        List<String> columnNames = new ArrayList<String>();
        List<List<String>> cells = new ArrayList<List<String>>();
        columnNames = new ArrayList<String>();
        columnNames.add("OpenFlow Packet Type");
        columnNames.add("Listeners");
        cells = new ArrayList<List<String>>();
        for (Entry<OFType, List<IOFMessageListener>> entry : beaconProvider.getListeners().entrySet()) {
            List<String> row = new ArrayList<String>();
            row.add(entry.getKey().toString());
            StringBuffer sb = new StringBuffer();
            for (IOFMessageListener listener : entry.getValue()) {
                sb.append(listener.getName() + " ");
            }
            row.add(sb.toString());
            cells.add(row);
        }
        layout.addSection(new TableSection("OpenFlow Packet Listeners", columnNames, cells, "table-listeners"), TwoColumnLayout.COLUMN1);

        return BeaconViewResolver.SIMPLE_VIEW;
    }

    @RequestMapping("/osgi")
    public String osgi(Map<String, Object> model) {
        Layout layout = new OneColumnLayout();
        model.put("layout", layout);

        // Bundle Form
        model.put("title", "Add Bundle");
        layout.addSection(new JspSection("addBundle.jsp", new HashMap<String, Object>(model)), TwoColumnLayout.COLUMN1);

        // Bundle List Table
        model.put("bundles", Arrays.asList(this.bundleContext.getBundles()));
        model.put("title", "OSGi Bundles");
        layout.addSection(new JspSection("bundles.jsp", model), TwoColumnLayout.COLUMN1);

        return BeaconViewResolver.SIMPLE_VIEW;
    }

    @RequestMapping("/bundle/{bundleId}/{action}")
    @ResponseBody
    public String osgiAction(@PathVariable Long bundleId, @PathVariable String action) {
        final Bundle bundle = this.bundleContext.getBundle(bundleId);
        if (action != null) {
            try {
                if (BundleAction.START.toString().equals(action)) {
                    bundle.start();
                } else if (BundleAction.STOP.toString().equals(action)) {
                    bundle.stop();
                } else if (BundleAction.UNINSTALL.toString().equals(action)) {
                    bundle.uninstall();
                } else if (BundleAction.REFRESH.toString().equals(action)) {
                    packageAdmin.refreshPackages(new Bundle[] {bundle});
                }
            } catch (BundleException e) {
                log.error("Failure performing action " + action + " on bundle " + bundle.getSymbolicName(), e);
            }
        }
        return "";
    }

    @RequestMapping(value = "/bundle/add", method = RequestMethod.POST)
    public View osgiBundleAdd(@RequestParam("file") MultipartFile file, Map<String, Object> model) throws Exception {
        BeaconJsonView view = new BeaconJsonView();

        File tempFile = null;
        Bundle newBundle = null;
        try {
            tempFile = File.createTempFile("beacon", ".jar");
            file.transferTo(tempFile);
            tempFile.deleteOnExit();
            newBundle = bundleContext.installBundle("file:"+tempFile.getCanonicalPath());
            model.put(BeaconJsonView.ROOT_OBJECT_KEY,
                    "Successfully installed: " + newBundle.getSymbolicName()
                            + "_" + newBundle.getVersion());
        } catch (IOException e) {
            log.error("Failure to create temporary file", e);
            model.put(BeaconJsonView.ROOT_OBJECT_KEY, "Failed to install bundle.");
        } catch (BundleException e) {
            log.error("Failure installing bundle", e);
            model.put(BeaconJsonView.ROOT_OBJECT_KEY, "Failed to install bundle.");
        }
        view.setContentType("text/javascript");
        return view;
    }

    @RequestMapping("/refreshWeb")
    @ResponseBody
    public String refreshWebBundle() {
        for (Bundle bundle : this.bundleContext.getBundles()) {
            if (bundle.getSymbolicName().equalsIgnoreCase("net.beaconcontroller.web")) {
                packageAdmin.refreshPackages(new Bundle[] {bundle});
                try {
                    Thread.sleep(1000);
                    while (bundle.getState() != Bundle.ACTIVE) {
                        Thread.sleep(100);
                    }
                } catch (InterruptedException e) {
                    log.error("Interupted waiting for refresh", e);
                }
            }
        }
        return "";
    }

    protected List<OFStatistics> getSwitchStats(OFSRCallback f, String switchId, String statsType) {
        IOFSwitch sw = beaconProvider.getSwitches().get(HexString.toLong(switchId));
        Future<List<OFStatistics>> future;
        List<OFStatistics> values = null;
        if (sw != null) {
            try {
                future = sw.getStatistics(f.getRequest());
                values = future.get(10, TimeUnit.SECONDS);
            } catch (Exception e) {
                log.error("Failure retrieving " + statsType, e);
            }
        }
        return values;
    }

    protected class makeFlowStatsRequest implements OFSRCallback {
        public OFStatisticsRequest getRequest() {
            OFStatisticsRequest req = new OFStatisticsRequest();
            OFFlowStatisticsRequest fsr = new OFFlowStatisticsRequest();
            OFMatch match = new OFMatch();
            match.setWildcards(0xffffffff);
            fsr.setMatch(match);
            fsr.setOutPort(OFPort.OFPP_NONE.getValue());
            fsr.setTableId((byte) 0xff);
            req.setStatisticType(OFStatisticsType.FLOW);
            req.setStatistics(Collections.singletonList((OFStatistics)fsr));
            req.setLengthU(req.getLengthU() + fsr.getLength());
            return req;
        };
    }

    public String addStatsSection(String statsType, @PathVariable String switchId, Map<String,Object> model, List<OFStatistics> stats) {
        OneColumnLayout layout = new OneColumnLayout();
        model.put("title", statsType + " for switch: " + switchId);
        model.put("layout", layout);
        model.put(statsType, stats);
        layout.addSection(new JspSection(statsType + ".jsp", model), null);
        return BeaconViewResolver.SIMPLE_VIEW;
    }

    protected List<OFStatistics> getSwitchFlows(String switchId) {
        return getSwitchStats(new makeFlowStatsRequest(), switchId, "flows");
    }

    // FLOWS

    @RequestMapping("/switch/{switchId}/flows/json")
    public View getSwitchFlowsJson(@PathVariable String switchId, Map<String,Object> model) {
        BeaconJsonView view = new BeaconJsonView();
        model.put(BeaconJsonView.ROOT_OBJECT_KEY, getSwitchFlows(switchId));
        return view;
    }

    @SuppressWarnings("unchecked")
    @RequestMapping("/switch/{switchId}/flows/dataTable")
    public View getSwitchFlowsDataTable(@PathVariable String switchId, Map<String,Object> model) {
        List<OFFlowStatisticsReply> data = new ArrayList<OFFlowStatisticsReply>();
        List<OFStatistics> stats = getSwitchFlows(switchId);
        if (stats != null) {
            // Ugly cast.. sigh
            data.addAll((Collection<? extends OFFlowStatisticsReply>) stats);
        }
        DataTableJsonView<OFFlowStatisticsReply> view = new DataTableJsonView<OFFlowStatisticsReply>(
                data, new OFFlowStatisticsReplyDataTableFormatCallback());
        return view;
    }

    @RequestMapping("/switch/{switchId}/flows")
    public String getSwitchFlows(@PathVariable String switchId, Map<String,Object> model) {
        OneColumnLayout layout = new OneColumnLayout();
        model.put("title", "Flows for switch: " + switchId);
        model.put("layout", layout);
        model.put("switchId", switchId);
        model.put("switchIdEsc", switchId.replaceAll(":", ""));
        layout.addSection(new JspSection("flows.jsp", model), null);
        return BeaconViewResolver.SIMPLE_VIEW;
    }

    protected List<OFStatistics> getSwitchTables(String switchId) {
        return getSwitchStats(new OFSRCallback() {
            @Override
            public OFStatisticsRequest getRequest() {
                OFStatisticsRequest req = new OFStatisticsRequest();
                req.setStatisticType(OFStatisticsType.TABLE);
                req.setLengthU(req.getLengthU());
                return req;
            }
        }, switchId, "tables");
    }

    // TABLES

    @RequestMapping("/switch/{switchId}/tables/json")
    public View getSwitchTablesJson(@PathVariable String switchId, Map<String,Object> model) {
        BeaconJsonView view = new BeaconJsonView();
        model.put(BeaconJsonView.ROOT_OBJECT_KEY, getSwitchTables(switchId));
        return view;
    }

    @RequestMapping("/switch/{switchId}/tables")
    public String getSwitchTables(@PathVariable String switchId, Map<String,Object> model) {
        List<OFStatistics> ports = getSwitchTables(switchId);
        return addStatsSection("tables", switchId, model, ports);
    }

    protected List<OFStatistics> getSwitchPorts(String switchId) {
        return getSwitchStats(new OFSRCallback() {
            @Override
            public OFStatisticsRequest getRequest() {
                OFStatisticsRequest req = new OFStatisticsRequest();
                OFPortStatisticsRequest psr = new OFPortStatisticsRequest();
                psr.setPortNumber(OFPort.OFPP_NONE.getValue());
                req.setStatisticType(OFStatisticsType.PORT);
                req.setStatistics(Collections.singletonList((OFStatistics)psr));
                req.setLengthU(req.getLengthU() + psr.getLength());
                return req;
            }
        }, switchId, "ports");
    }

    // PORTS

    @RequestMapping("/switch/{switchId}/ports/json")
    public View getSwitchPortsJson(@PathVariable String switchId, Map<String,Object> model) {
        BeaconJsonView view = new BeaconJsonView();
        model.put(BeaconJsonView.ROOT_OBJECT_KEY, getSwitchPorts(switchId));
        return view;
    }

    @RequestMapping("/switch/{switchId}/ports")
    public String getSwitchPorts(@PathVariable String switchId, Map<String,Object> model) {
        List<OFStatistics> ports = getSwitchPorts(switchId);
        return addStatsSection("ports", switchId, model, ports);
    }

    /**
     * @param packageAdmin the packageAdmin to set
     */
    @Autowired
    public void setPackageAdmin(PackageAdmin packageAdmin) {
        this.packageAdmin = packageAdmin;
    }
}
