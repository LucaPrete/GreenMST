<%@page import="org.openflow.protocol.statistics.OFFlowStatisticsReply"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="org.openflow.util.*, org.openflow.protocol.*,
                 org.openflow.protocol.action.*, net.beaconcontroller.packet.*"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.beaconcontroller.net/tld/utils.tld" prefix="u" %>  
<div class="section">
  <div class="section-header">${title}</div>
  <div class="section-content">
    <table id="table-flows-${switchIdEsc}" class="tableSection">
      <thead>
        <tr>
          <th>In Port</th>
          <th>DL Src</th>
          <th>DL Dst</th>
          <th>DL Type</th>
          <th>NW Src</th>
          <th>NW Dst</th>
          <th>NW Protot</th>
          <th>TP Src</th>
          <th>TP Dst</th>
          <th>Wildcards</th>
          <th>Bytes</th>
          <th>Packets</th>
          <th>Time (s)</th>
          <th>Idle TO</th>
          <th>Hard TO</th>
          <th>Cookie</th>
          <th>Out Port(s)</th>
        </tr>
      </thead>
    </table>
  </div>
</div>

<script type="text/javascript" charset="utf-8">
    (function() {
        new DataTableWrapper('table-flows-${switchIdEsc}','/wm/core/switch/${switchId}/flows/dataTable',
            {
              "bFilter": true
            }); 
    })();
</script>
