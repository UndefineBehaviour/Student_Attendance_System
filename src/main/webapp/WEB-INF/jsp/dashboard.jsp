<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fmt" uri="jakarta.tags.fmt" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<jsp:include page="/WEB-INF/jsp/layout/header.jsp">
  <jsp:param name="title" value="Dashboard" />
  <jsp:param name="active" value="dashboard" />
</jsp:include>

<%-- Today's date, e.g. "Wednesday, 11 November 2026" --%>
<jsp:useBean id="now" class="java.util.Date" />
<fmt:setLocale value="en" />

<%-- 1. Hero: date, welcome heading, subtitle (left) + two buttons (right) --%>
<section class="dash-hero">
  <div>
    <div class="dash-date"><fmt:formatDate value="${now}" pattern="EEEE, d MMMM yyyy" /></div>
    <h1 class="dash-title">Welcome, <c:out value="${sessionScope.user.username}" /></h1>
    <p class="dash-sub">
      <c:choose>
        <c:when test="${sessionScope.user.role == 'COORDINATOR'}">Manage students, subjects and view attendance reports.</c:when>
        <c:otherwise>Mark attendance and check each student's percentage.</c:otherwise>
      </c:choose>
    </p>
  </div>
  <div class="dash-hero-actions">
    <a href="${ctx}/sessions" class="btn btn-brand">Start a lecture</a>
    <a href="${ctx}/qr" class="btn btn-white">Open Live QR</a>
  </div>
</section>

<%-- 2. One card with four numbers (model attributes: totalStudents, totalSubjects, todayMarked, belowThreshold) --%>
<section class="stats-strip">
  <div class="stat-cell">
    <div class="stat-label">Total Students</div>
    <div class="stat-value"><c:out value="${empty totalStudents ? '—' : totalStudents}" /></div>
  </div>
  <div class="stat-cell">
    <div class="stat-label">Subjects</div>
    <div class="stat-value"><c:out value="${empty totalSubjects ? '—' : totalSubjects}" /></div>
  </div>
  <div class="stat-cell">
    <div class="stat-label">Marked Today</div>
    <div class="stat-value"><c:out value="${empty todayMarked ? '—' : todayMarked}" /></div>
  </div>
  <div class="stat-cell">
    <div class="stat-label">Below 75%</div>
    <div class="stat-value stat-red"><c:out value="${empty belowThreshold ? '—' : belowThreshold}" /></div>
  </div>
</section>

<%-- 3. Two columns: Quick actions (wide) + Needs attention (narrow) --%>
<div class="dash-grid">

  <section class="panel">
    <h2 class="panel-title">Quick actions</h2>
    <div class="panel-divider"></div>
    <div class="qa-grid">
      <a href="${ctx}/sessions" class="qa-card">
        <i class="bi bi-check2-square"></i>
        <h3>Mark Attendance</h3>
        <p>Pick a subject and date, then mark Present or Absent.</p>
      </a>
      <a href="${ctx}/attendance/by-date" class="qa-card">
        <i class="bi bi-calendar3"></i>
        <h3>View Records</h3>
        <p>See attendance of the whole class for any date.</p>
      </a>
      <a href="${ctx}/attendance/percentage" class="qa-card">
        <i class="bi bi-percent"></i>
        <h3>Percentage</h3>
        <p>Check each student's percentage and who is below 75%.</p>
      </a>
      <a href="${ctx}/qr" class="qa-card">
        <i class="bi bi-qr-code"></i>
        <h3>Live QR</h3>
        <p>Students scan a QR that changes every 5 seconds.</p>
      </a>
    </div>
  </section>

  <aside class="panel attention-panel">

    <%-- Link to the full Defaulters page (adds the chosen subject when there is one) --%>
    <c:set var="defaultersLink" value="${ctx}/attendance/defaulters" />
    <c:if test="${not empty subjectId}">
      <c:set var="defaultersLink" value="${ctx}/attendance/defaulters?subjectId=${subjectId}" />
    </c:if>

    <div class="d-flex justify-content-between align-items-baseline">
      <h2 class="panel-title">Needs attention</h2>
      <a href="${defaultersLink}" class="panel-link">Defaulters</a>
    </div>
    <div class="panel-divider"></div>

    <c:choose>
      <c:when test="${empty subjects}">
        <p class="attention-text">No subjects are assigned to you yet. Ask the coordinator to link your subjects.</p>
      </c:when>
      <c:otherwise>

        <%-- Subject dropdown: choosing one reloads the dashboard --%>
        <form method="get" action="${ctx}/dashboard" class="mb-3">
          <label class="form-label small text-muted mb-1" for="attnSubject">Subject</label>
          <select class="form-select form-select-sm" id="attnSubject" name="subjectId" onchange="this.form.submit()">
            <c:forEach var="sub" items="${subjects}">
              <option value="${sub.id}" ${subjectId == sub.id ? 'selected' : ''}><c:out value="${sub.code} – ${sub.name}" /></option>
            </c:forEach>
          </select>
          <noscript><button type="submit" class="btn btn-sm btn-brand mt-2">Show</button></noscript>
        </form>

        <c:choose>
          <c:when test="${lectures == 0}">
            <p class="attention-text">No lecture has been closed for this subject yet, so there is no attendance to show.</p>
          </c:when>
          <c:when test="${defaulterCount == 0}">
            <p class="attention-text"><c:out value="${className}" /> &middot; No student is below 75%.</p>
          </c:when>
          <c:otherwise>
            <p class="fw-semibold mb-2">
              <c:out value="${className}" /> &middot; ${defaulterCount} student${defaulterCount == 1 ? '' : 's'} below 75%
            </p>

            <%-- Lowest attendance first --%>
            <table class="table table-sm align-middle mb-2">
              <thead class="text-muted small">
                <tr>
                  <th style="width:3.5rem">Roll</th>
                  <th>Name</th>
                  <th class="text-end">Attendance</th>
                </tr>
              </thead>
              <tbody>
                <c:forEach var="s" items="${defaulters}">
                  <tr>
                    <td><c:out value="${s.rollNo}" /></td>
                    <td><c:out value="${s.fullName}" /></td>
                    <td class="text-end ${s.textClass}">${s.percentText}</td>
                  </tr>
                </c:forEach>
              </tbody>
            </table>

            <p class="attention-text">
              Showing lowest ${shown} &middot;
              <a class="panel-link" href="${defaultersLink}">View full list &rarr;</a>
            </p>
          </c:otherwise>
        </c:choose>

      </c:otherwise>
    </c:choose>
  </aside>

</div>

<jsp:include page="/WEB-INF/jsp/layout/footer.jsp" />