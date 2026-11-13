<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<jsp:include page="/WEB-INF/jsp/layout/header.jsp">
  <jsp:param name="title" value="Percentage" />
  <jsp:param name="active" value="percentage" />
</jsp:include>

<%-- Filter: subject, class, sort order --%>
<div class="card stat-card mb-3 no-print">
  <div class="card-header bg-white fw-semibold">Attendance percentage</div>
  <div class="card-body">
    <c:choose>
      <c:when test="${empty subjects}">
        <div class="alert alert-warning mb-0">No subjects are assigned to you yet. Ask the coordinator to link your subjects.</div>
      </c:when>
      <c:otherwise>
        <form method="get" action="${ctx}/attendance/percentage" class="row g-3 align-items-end">
          <div class="col-12 col-md-4">
            <label class="form-label" for="subjectId">Subject</label>
            <select class="form-select" id="subjectId" name="subjectId" required>
              <option value="">Choose a subject</option>
              <c:forEach var="sub" items="${subjects}">
                <option value="${sub.id}" ${subjectId == sub.id ? 'selected' : ''}><c:out value="${sub.code} - ${sub.name}" /></option>
              </c:forEach>
            </select>
          </div>
          <div class="col-6 col-md-2">
            <label class="form-label" for="className">Class</label>
            <select class="form-select" id="className" name="className">
              <c:forEach var="cls" items="${classes}">
                <option value="${cls}" ${className == cls ? 'selected' : ''}><c:out value="${cls}" /></option>
              </c:forEach>
            </select>
          </div>
          <div class="col-6 col-md-4">
            <label class="form-label" for="sort">Sort</label>
            <select class="form-select" id="sort" name="sort">
              <option value="roll" ${sort == 'roll' ? 'selected' : ''}>By roll number</option>
              <option value="percent" ${sort == 'percent' ? 'selected' : ''}>Lowest attendance first</option>
            </select>
          </div>
          <div class="col-12 col-md-2">
            <button type="submit" class="btn btn-brand w-100">Show</button>
          </div>
        </form>
      </c:otherwise>
    </c:choose>
  </div>
</div>

<c:if test="${not empty errors}">
  <div class="alert alert-danger">
    <ul class="mb-0">
      <c:forEach var="e" items="${errors}">
        <li><c:out value="${e}" /></li>
      </c:forEach>
    </ul>
  </div>
</c:if>

<c:if test="${not empty subjectId && empty errors}">
  <c:choose>
    <c:when test="${lectures == 0}">
      <div class="alert alert-info">No lecture has been closed yet for this subject, so there is no percentage to show. Close a lecture on the Mark Attendance page first.</div>
    </c:when>
    <c:otherwise>

      <%-- Summary --%>
      <div class="row g-3 mb-3">
        <div class="col-6 col-lg-3">
          <div class="card stat-card"><div class="card-body">
            <div class="text-muted small">Lectures held</div><div class="fs-4 fw-bold">${lectures}</div>
          </div></div>
        </div>
        <div class="col-6 col-lg-3">
          <div class="card stat-card"><div class="card-body">
            <div class="text-muted small">Students</div><div class="fs-4 fw-bold">${stats.size()}</div>
          </div></div>
        </div>
        <div class="col-6 col-lg-3">
          <div class="card stat-card"><div class="card-body">
            <div class="text-muted small">Class average</div><div class="fs-4 fw-bold">${average}</div>
          </div></div>
        </div>
        <div class="col-6 col-lg-3">
          <div class="card stat-card"><div class="card-body">
            <div class="text-muted small">Below 75%</div><div class="fs-4 fw-bold text-danger">${belowCount}</div>
          </div></div>
        </div>
      </div>

      <%-- Table --%>
      <div class="card stat-card">
        <div class="table-responsive">
          <table class="table table-hover align-middle mb-0">
            <thead class="table-light">
              <tr>
                <th>Roll</th>
                <th>Student ID</th>
                <th>Name</th>
                <th>Present</th>
                <th>Lectures</th>
                <th style="min-width:150px">Percentage</th>
                <th>Status</th>
                <th>To reach 75%</th>
              </tr>
            </thead>
            <tbody>
              <c:forEach var="s" items="${stats}">
                <tr>
                  <td><c:out value="${s.rollNo}" /></td>
                  <td><c:out value="${s.studentCode}" /></td>
                  <td><a href="${ctx}/attendance/student/${s.studentId}"><c:out value="${s.fullName}" /></a></td>
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
                    <c:if test="${s.belowThreshold}">Attend next ${s.needed}</c:if>
                  </td>
                </tr>
              </c:forEach>
              <c:if test="${empty stats}">
                <tr><td colspan="8" class="text-center text-muted py-4">No active students in this class.</td></tr>
              </c:if>
            </tbody>
          </table>
        </div>
      </div>

    </c:otherwise>
  </c:choose>
</c:if>

<jsp:include page="/WEB-INF/jsp/layout/footer.jsp" />