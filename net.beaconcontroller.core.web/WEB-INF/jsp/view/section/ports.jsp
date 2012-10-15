<%@page import="org.openflow.protocol.statistics.OFPortStatisticsReply"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page import="org.openflow.util.*, org.openflow.protocol.*,
                 org.openflow.protocol.action.*, net.beaconcontroller.packet.*"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.beaconcontroller.net/tld/utils.tld" prefix="u" %>
<div class="section">
  <div class="section-header">${title}</div>
  <div class="section-content">
    <table class="beaconTable">
      <thead>
        <tr>
          <th>Port Num</th>
          <th>RX (pkts)</th>
          <th>TX (pkts)</th>
          <th>RX (bytes)</th>
          <th>TX (bytes)</th>
          <th>RX Drops</th>
          <th>TX Drops</th>
          <th>RX Err</th>
          <th>TX Err</th>
          <th>RX Frame Err</th>
          <th>RX Overrun Err</th>
          <th>RX CRC Err</th>
          <th>Collisions</th>
        </tr>
      </thead>
      <tbody>
        <c:forEach items="${ports}" var="port" varStatus="status">
          <%  OFPortStatisticsReply port = (OFPortStatisticsReply)pageContext.findAttribute("port");
          %>
          <tr>
            <td><c:out value="${u:shortUnsigned(port.portNumber)}"/></td>
            <td><c:out value="${u:longUnsigned(port.receivePackets)}"/></td>
            <td><c:out value="${u:longUnsigned(port.transmitPackets)}"/></td>
            <td><c:out value="${u:longUnsigned(port.receiveBytes)}"/></td>
            <td><c:out value="${u:longUnsigned(port.transmitBytes)}"/></td>
            <td><c:out value="${u:longUnsigned(port.receiveDropped)}"/></td>
            <td><c:out value="${u:longUnsigned(port.transmitDropped)}"/></td>
            <td><c:out value="${u:longUnsigned(port.receiveErrors)}"/></td>
            <td><c:out value="${u:longUnsigned(port.transmitErrors)}"/></td>
            <td><c:out value="${u:longUnsigned(port.receiveFrameErrors)}"/></td>
            <td><c:out value="${u:longUnsigned(port.receiveOverrunErrors)}"/></td>
            <td><c:out value="${u:longUnsigned(port.receiveCRCErrors)}"/></td>
            <td><c:out value="${u:longUnsigned(port.collisions)}"/></td>
          </tr>
        </c:forEach>
      </tbody>
    </table>
  </div>
</div>