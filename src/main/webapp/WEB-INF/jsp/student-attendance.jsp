<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<jsp:include page="/WEB-INF/jsp/layout/header.jsp">
  <jsp:param name="title" value="${own ? 'My Attendance' : 'Student Attendance'}" />
  <jsp:param name="active" value="${own ? 'my' : 'percentage'}" />
</jsp:include>

<%-- Who --%>
<div class="card stat-card mb-3">
  <div class="card-body d-flex flex-wrap gap-3 align-items-center justify-content-between">
    <div>
      <div class="fs-5 fw-semibold"><c:out value="${student.fullName}" /></div>
      <div class="text-muted">
        Roll <c:out value="${student.rollNo}" /> &middot; <c:out value="${student.studentCode}" />
        &middot; <c:out value="${student.className}" />
      </div>
    </div>
    <c:if test="${not own}">
      <a href="${ctx}/attendance/percentage" class="btn btn-outline-secondary no-print">Back</a>
    </c:if>
  </div>
</div>

<c:choose>
  <c:when test="${overall.total == 0}">
    <div class="alert alert-info">No lecture has been closed yet, so there is no attendance to show.</div>
  </c:when>
  <c:otherwise>

    <%-- Overall --%>
    <div class="card stat-card mb-3">
      <div class="card-body">
        <div class="d-flex flex-wrap justify-content-between align-items-end mb-2">
          <div>
            <div class="text-muted small">Overall attendance</div>
            <div class="display-6 fw-bold ${overall.textClass}">${overall.percentText}</div>
          </div>
          <div class="text-muted">${overall.attended} of ${overall.total} lectures attended</div>
        </div>
        <div class="progress" style="height:10px"><div class="progress-bar ${overall.barClass}" style="width:${overall.barWidth}%"></div></div>
        <c:if test="${overall.belowThreshold}">
          <div class="text-danger small mt-2">
            <c:choose>
              <c:when test="${own}">Your overall attendance is below 75%.</c:when>
              <c:otherwise>Overall attendance is below 75%.</c:otherwise>
            </c:choose>
          </div>
        </c:if>
      </div>
    </div>

    <%-- Subject-wise --%>
    <div class="card stat-card">
      <div class="card-header bg-white fw-semibold">Subject-wise attendance</div>
      <div class="table-responsive">
        <table class="table table-hover align-middle mb-0">
          <thead class="table-light">
            <tr>
              <th>Subject</th>
              <th>Present</th>
              <th>Lectures</th>
              <th style="min-width:160px">Percentage</th>
              <th>Status</th>
              <th>To reach 75%</th>
            </tr>
          </thead>
          <tbody>
            <c:forEach var="s" items="${stats}">
              <tr>
                <td><c:out value="${s.subjectCode}" /> <span class="text-muted small"><c:out value="${s.subjectName}" /></span></td>
                <td>${s.attended}</td>
                <td>${s.total}</td>
                <td>
                  <div class="${s.textClass}">${s.percentText}</div>
                  <div class="progress" style="height:6px"><div class="progress-bar ${s.barClass}" style="width:${s.barWidth}%"></div></div>
                </td>
                <td>
                  <c:choose>
                    <c:when test="${s.belowThreshold}"><span class="badge text-bg-danger">Below 75%</span></c:when>
                    <c:otherwise><span class="badge text-bg-success">OK</span></c:otherwise>
                  </c:choose>
                </td>
                <td>
                  <c:if test="${s.belowThreshold}">Attend the next ${s.needed} lectures</c:if>
                </td>
              </tr>
            </c:forEach>
          </tbody>
        </table>
      </div>
    </div>

  </c:otherwise>
</c:choose>

<jsp:include page="/WEB-INF/jsp/layout/footer.jsp" />