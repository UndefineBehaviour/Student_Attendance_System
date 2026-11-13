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

<%-- Lecture details --%>
<div class="card stat-card mb-3">
  <div class="card-body d-flex flex-wrap gap-3 align-items-center justify-content-between">
    <div>
      <div class="fs-5 fw-semibold">
        <c:out value="${lecture.subjectCode}" /> &ndash; <c:out value="${lecture.subjectName}" />
      </div>
      <div class="text-muted">
        <c:out value="${lecture.className}" /> &middot; <c:out value="${lecture.dateText}" />
        &middot; started <c:out value="${lecture.startText}" /> &middot; <c:out value="${lecture.teacherName}" />
      </div>
    </div>
    <div>
      <c:choose>
        <c:when test="${lecture.open}"><span class="badge text-bg-warning fs-6">Open</span></c:when>
        <c:otherwise><span class="badge text-bg-secondary fs-6">Closed</span></c:otherwise>
      </c:choose>
    </div>
  </div>
</div>

<c:if test="${not canEdit}">
  <div class="alert alert-secondary">This lecture is closed. Only the coordinator can change it now.</div>
</c:if>

<%-- Roster with tick boxes --%>
<form method="post" action="${ctx}/sessions/${lecture.id}/save">
  <div class="card stat-card mb-3">
    <div class="card-header bg-white d-flex flex-wrap gap-2 align-items-center justify-content-between">
      <div class="fw-semibold">
        Present: <span id="presentCount" class="badge bg-primary-subtle text-primary-emphasis">0 / 0</span>
      </div>
      <c:if test="${canEdit}">
        <div class="d-flex gap-2">
          <button type="button" class="btn btn-sm btn-outline-success" onclick="setAll(true)">Mark all present</button>
          <button type="button" class="btn btn-sm btn-outline-secondary" onclick="setAll(false)">Clear all</button>
        </div>
      </c:if>
    </div>
    <div class="table-responsive">
      <table class="table table-hover align-middle mb-0">
        <thead class="table-light">
          <tr>
            <th style="width:90px">Present</th>
            <th>Roll</th>
            <th>Student ID</th>
            <th>Name</th>
            <th>Marked by</th>
          </tr>
        </thead>
        <tbody>
          <c:forEach var="row" items="${rows}">
            <tr>
              <td>
                <input type="checkbox" class="form-check-input present-box" name="present" value="${row.studentId}"
                       ${row.present ? 'checked' : ''} ${canEdit ? '' : 'disabled'}>
              </td>
              <td><c:out value="${row.rollNo}" /></td>
              <td><c:out value="${row.studentCode}" /></td>
              <td><c:out value="${row.fullName}" /></td>
              <td>
                <c:choose>
                  <c:when test="${row.method == 'QR'}"><span class="badge text-bg-info">QR scan</span></c:when>
                  <c:when test="${row.method == 'MANUAL'}"><span class="badge text-bg-secondary">Teacher</span></c:when>
                  <c:otherwise><span class="text-muted">&ndash;</span></c:otherwise>
                </c:choose>
              </td>
            </tr>
          </c:forEach>
          <c:if test="${empty rows}">
            <tr><td colspan="5" class="text-center text-muted py-4">No active students in this class.</td></tr>
          </c:if>
        </tbody>
      </table>
    </div>
  </div>

  <div class="d-flex gap-2 mb-3">
    <c:if test="${canEdit}">
      <button type="submit" class="btn btn-brand"><i class="bi bi-check-lg me-1"></i> Save attendance</button>
    </c:if>
    <a href="${ctx}/sessions" class="btn btn-outline-secondary">Back</a>
  </div>
</form>

<%-- Close the lecture (separate form) --%>
<c:if test="${lecture.open}">
  <form method="post" action="${ctx}/sessions/${lecture.id}/close"
        onsubmit="return confirm('Close this lecture? Students who are not marked present will be marked absent.');">
    <button type="submit" class="btn btn-outline-danger"><i class="bi bi-lock me-1"></i> Close lecture</button>
    <span class="text-muted small ms-2">Save first, then close. Unmarked students become absent.</span>
  </form>
</c:if>

<%-- Delete the whole lecture (coordinator, or the teacher while it is still open) --%>
<c:if test="${canDelete}">
  <form method="post" action="${ctx}/sessions/${lecture.id}/delete" class="mt-3"
        onsubmit="return confirm('Delete this lecture and all its attendance marks? This cannot be undone.');">
    <button type="submit" class="btn btn-sm btn-outline-danger"><i class="bi bi-trash me-1"></i> Delete lecture</button>
  </form>
</c:if>

<script>
  function count() {
    var boxes = document.querySelectorAll('input.present-box');
    var n = 0;
    boxes.forEach(function (b) { if (b.checked) { n++; } });
    document.getElementById('presentCount').textContent = n + ' / ' + boxes.length;
  }
  function setAll(value) {
    document.querySelectorAll('input.present-box').forEach(function (b) { b.checked = value; });
    count();
  }
  document.querySelectorAll('input.present-box').forEach(function (b) { b.addEventListener('change', count); });
  count();
</script>

<jsp:include page="/WEB-INF/jsp/layout/footer.jsp" />