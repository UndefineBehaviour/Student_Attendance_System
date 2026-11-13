<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<jsp:include page="/WEB-INF/jsp/layout/header.jsp">
  <jsp:param name="title" value="View by Date" />
  <jsp:param name="active" value="view" />
</jsp:include>

<c:if test="${not empty errors}">
  <div class="alert alert-warning">
    <ul class="mb-0">
      <c:forEach var="e" items="${errors}"><li><c:out value="${e}" /></li></c:forEach>
    </ul>
  </div>
</c:if>

<%-- Date filter --%>
<div class="card stat-card mb-3">
  <div class="card-header bg-white fw-semibold">View by date</div>
  <div class="card-body">
    <form method="get" action="${ctx}/attendance/by-date" class="row g-3 align-items-end">
      <div class="col-12 col-md-4">
        <label class="form-label" for="date">Choose date</label>
        <input type="date" class="form-control" id="date" name="date"
               max="${today}" value="${selectedDate}" required>
      </div>
      <div class="col-6 col-md-2">
        <button type="submit" class="btn btn-brand w-100">Show</button>
      </div>
      <div class="col-6 col-md-2">
        <a class="btn btn-outline-secondary w-100" href="${ctx}/attendance/by-date">Today</a>
      </div>
    </form>
  </div>
</div>

<%-- Lectures on that date --%>
<div class="card stat-card">
  <div class="card-header bg-white fw-semibold d-flex justify-content-between">
    <span>Lectures on <c:out value="${dateText}" /></span>
    <span class="text-muted fw-normal">${records.size()} found</span>
  </div>
  <div class="table-responsive">
    <table class="table table-hover align-middle mb-0">
      <thead class="table-light">
        <tr>
          <th>Subject</th>
          <th>Class</th>
          <th>Teacher</th>
          <th>Started</th>
          <th>Present</th>
          <th>Status</th>
          <th class="text-end"></th>
        </tr>
      </thead>
      <tbody>
        <c:forEach var="l" items="${records}">
          <tr>
            <td><c:out value="${l.subjectCode}" /> <span class="text-muted small"><c:out value="${l.subjectName}" /></span></td>
            <td class="text-nowrap"><c:out value="${l.className}" /></td>
            <td><c:out value="${l.teacherName}" /></td>
            <td class="text-nowrap"><c:out value="${l.startText}" /></td>
            <td class="text-nowrap">${l.presentCount} / ${l.classSize}</td>
            <td>
              <c:choose>
                <c:when test="${l.open}"><span class="badge text-bg-warning">Open</span></c:when>
                <c:otherwise><span class="badge text-bg-secondary">Closed</span></c:otherwise>
              </c:choose>
            </td>
            <td class="text-end">
              <a class="btn btn-sm btn-outline-primary" href="${ctx}/sessions/${l.id}">${l.open ? 'Mark' : 'View'}</a>
            </td>
          </tr>
        </c:forEach>
        <c:if test="${empty records}">
          <tr><td colspan="7" class="text-center text-muted py-4">No lectures on this date.</td></tr>
        </c:if>
      </tbody>
    </table>
  </div>
</div>

<jsp:include page="/WEB-INF/jsp/layout/footer.jsp" />