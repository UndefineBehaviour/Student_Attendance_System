<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<jsp:include page="/WEB-INF/jsp/layout/header.jsp">
  <jsp:param name="title" value="Live QR" />
  <jsp:param name="active" value="qr" />
</jsp:include>

<p class="text-muted mb-3">Pick an open lecture and show its QR on the projector. Students scan it with their phone camera.</p>

<div class="card stat-card">
  <div class="card-header bg-white d-flex flex-wrap gap-2 align-items-center justify-content-between">
    <span class="fw-semibold">Open lectures</span>
    <a href="${ctx}/sessions" class="btn btn-sm btn-brand"><i class="bi bi-play-fill me-1"></i> Start a lecture</a>
  </div>
  <div class="table-responsive">
    <table class="table table-hover align-middle mb-0">
      <thead class="table-light">
        <tr>
          <th>Date</th>
          <th>Subject</th>
          <th>Class</th>
          <th>Teacher</th>
          <th>Present</th>
          <th class="text-end"></th>
        </tr>
      </thead>
      <tbody>
        <c:forEach var="l" items="${openSessions}">
          <tr>
            <td><c:out value="${l.dateText}" /></td>
            <td><c:out value="${l.subjectCode}" /> &ndash; <c:out value="${l.subjectName}" /></td>
            <td><c:out value="${l.className}" /></td>
            <td><c:out value="${l.teacherName}" /></td>
            <td>${l.presentCount} / ${l.classSize}</td>
            <td class="text-end text-nowrap">
              <a class="btn btn-sm btn-brand" href="${ctx}/qr/${l.id}"><i class="bi bi-qr-code me-1"></i> Show QR</a>
              <a class="btn btn-sm btn-outline-secondary" href="${ctx}/sessions/${l.id}">Marking sheet</a>
            </td>
          </tr>
        </c:forEach>
        <c:if test="${empty openSessions}">
          <tr><td colspan="6" class="text-center text-muted py-4">No open lectures. Start a lecture first, then come back here.</td></tr>
        </c:if>
      </tbody>
    </table>
  </div>
</div>

<jsp:include page="/WEB-INF/jsp/layout/footer.jsp" />
