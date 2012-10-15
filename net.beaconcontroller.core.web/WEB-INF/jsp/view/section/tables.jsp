<%@page import="org.openflow.protocol.statistics.OFTableStatistics"%>
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
          <th>ID</th>
          <th>Name</th>
          <th>Wildcards</th>
          <th>Max Entries</th>
          <th>Active Count</th>
          <th>Lookup Count</th>
          <th>Matched Count</th>
        </tr>
      </thead>
      <tbody>
        <c:forEach items="${tables}" var="table" varStatus="status">
          <%  OFTableStatistics table = (OFTableStatistics)pageContext.findAttribute("table"); 
              pageContext.setAttribute("wildcards", HexString.toHexString(table.getWildcards()));
          %>
          <tr>
            <td><c:out value="${u:byteUnsigned(table.tableId)}"/></td>
            <td><c:out value="${table.name}"/></td>            
            <td><c:out value="${wildcards}"/></td>
            <td><c:out value="${u:intUnsigned(table.maximumEntries)}"/></td>
            <td><c:out value="${u:intUnsigned(table.activeCount)}"/></td>
            <td><c:out value="${u:longUnsigned(table.lookupCount)}"/></td>
            <td><c:out value="${u:longUnsigned(table.matchedCount)}"/></td>
          </tr>
        </c:forEach>
      </tbody>
    </table>
  </div>
</div>