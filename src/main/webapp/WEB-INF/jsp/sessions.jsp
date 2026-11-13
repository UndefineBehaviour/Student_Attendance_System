<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<jsp:include page="/WEB-INF/jsp/layout/header.jsp">
  <jsp:param name="title" value="Mark Attendance" />
  <jsp:param name="active" value="mark" />
</jsp:include>

<c:if test="${not empty msg}">
  <div class="alert alert-success alert-dismissible fade show" role="alert">
    <c:out value="${msg}" />
    <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
  </div>
</c:if>

<%-- Start a lecture --%>
<div class="card stat-card mb-3">
  <div class="card-header bg-white fw-semibold">Start a lecture</div>
  <div class="card-body">

    <c:if test="${not empty errors}">
      <div class="alert alert-danger">
        <ul class="mb-0">
          <c:forEach var="e" items="${errors}">
            <li><c:out value="${e}" /></li>
          </c:forEach>
        </ul>
      </div>
    </c:if>

    <c:choose>
      <c:when test="${empty subjects}">
        <div class="alert alert-warning mb-0">No subjects are assigned to you yet. Ask the coordinator to link your subjects.</div>
      </c:when>
      <c:otherwise>
        <form method="post" action="${ctx}/sessions" class="row g-3 align-items-end">
          <div class="col-12 col-md-5">
            <label class="form-label" for="subjectId">Subject</label>
            <select class="form-select" id="subjectId" name="subjectId" required>
              <option value="">Choose a subject</option>
              <c:forEach var="sub" items="${subjects}">
                <option value="${sub.id}" ${form.subjectId == sub.id ? 'selected' : ''}><c:out value="${sub.code} - ${sub.name}" /></option>
              </c:forEach>
            </select>
          </div>
          <div class="col-6 col-md-3">
            <label class="form-label" for="className">Class</label>
            <select class="form-select" id="className" name="className" required>
              <c:forEach var="cls" items="${classes}">
                <option value="${cls}" ${form.className == cls ? 'selected' : ''}><c:out value="${cls}" /></option>
              </c:forEach>
            </select>
          </div>
          <div class="col-6 col-md-2">
            <label class="form-label" for="date">Date</label>
            <input type="date" class="form-control" id="date" name="date" max="${today}"
                   value="<c:out value='${form.date}' />" required>
          </div>
          <div class="col-12 col-md-2">
            <button type="submit" class="btn btn-brand w-100"><i class="bi bi-play-fill me-1"></i> Start</button>
          </div>
        </form>
      </c:otherwise>
    </c:choose>
  </div>
</div>

<%-- Recent lectures --%>
<div class="card stat-card">
  <div class="card-header bg-white fw-semibold">Recent lectures</div>
  <div class="table-responsive">
    <table class="table table-hover align-middle mb-0">
      <thead class="table-light">
        <tr>
          <th>Date</th>
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
        <c:forEach var="l" items="${recent}">
          <tr>
            <td class="text-nowrap"><c:out value="${l.dateText}" /></td>
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
              <div class="d-inline-flex gap-1">
                <a class="btn btn-sm btn-outline-primary" href="${ctx}/sessions/${l.id}">${l.open ? 'Mark' : 'View'}</a>
                <c:if test="${l.deletable}">
                  <form method="post" action="${ctx}/sessions/${l.id}/delete"
                        onsubmit="return confirm('Delete this lecture and all its attendance marks? This cannot be undone.');">
                    <button type="submit" class="btn btn-sm btn-outline-danger">Delete</button>
                  </form>
                </c:if>
              </div>
            </td>
          </tr>
        </c:forEach>
        <c:if test="${empty recent}">
          <tr><td colspan="8" class="text-center text-muted py-4">No lectures yet. Start one above.</td></tr>
        </c:if>
      </tbody>
    </table>
  </div>
</div>

<jsp:include page="/WEB-INF/jsp/layout/footer.jsp" />