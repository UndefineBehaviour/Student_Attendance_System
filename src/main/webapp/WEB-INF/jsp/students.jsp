<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<jsp:include page="/WEB-INF/jsp/layout/header.jsp">
  <jsp:param name="title" value="Students" />
  <jsp:param name="active" value="students" />
</jsp:include>

<%-- Success message (flash attribute from the controller) --%>
<c:if test="${not empty msg}">
  <div class="alert alert-success alert-dismissible fade show" role="alert">
    <c:out value="${msg}" />
    <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
  </div>
</c:if>

<%-- Login details of a new student / reset password: shown only once --%>
<c:if test="${not empty newPassword}">
  <div class="alert alert-warning">
    <div class="fw-semibold mb-1"><i class="bi bi-key-fill me-1"></i> Login details (shown only once, copy them now)</div>
    Username: <code><c:out value="${newUsername}" /></code>
    &nbsp;&nbsp; Password: <code><c:out value="${newPassword}" /></code>
    <div class="small text-muted mt-1">Give these to the student privately and ask them to change the password after the first login.</div>
  </div>
</c:if>

<%-- Search bar and Add button --%>
<div class="card stat-card mb-3">
  <div class="card-body d-flex flex-wrap gap-2 align-items-center">
    <form method="get" action="${ctx}/students" class="d-flex flex-grow-1 gap-2">
      <div class="input-group">
        <span class="input-group-text"><i class="bi bi-search"></i></span>
        <input type="text" class="form-control" name="q" value="<c:out value='${q}' />"
               placeholder="Search by name, student ID, username or roll number">
      </div>
      <button type="submit" class="btn btn-brand">Search</button>
      <c:if test="${not empty q}">
        <a href="${ctx}/students" class="btn btn-outline-secondary">Clear</a>
      </c:if>
    </form>
    <c:if test="${canEdit}">
      <a href="${ctx}/students/new" class="btn btn-success"><i class="bi bi-plus-lg me-1"></i> Add student</a>
    </c:if>
  </div>
</div>

<%-- Student table --%>
<div class="card stat-card">
  <div class="card-header bg-white fw-semibold">
    Students <span class="badge bg-primary-subtle text-primary-emphasis ms-1">${students.size()}</span>
  </div>
  <div class="table-responsive">
    <table class="table table-hover align-middle mb-0">
      <thead class="table-light">
        <tr>
          <th>Roll</th>
          <th>Student ID</th>
          <th>Name</th>
          <th>Username</th>
          <th>Class</th>
          <th>Status</th>
          <c:if test="${canEdit}"><th class="text-end">Actions</th></c:if>
        </tr>
      </thead>
      <tbody>
        <c:forEach var="s" items="${students}">
          <tr class="${s.active ? '' : 'table-secondary text-muted'}">
            <td><c:out value="${s.rollNo}" /></td>
            <td><c:out value="${s.studentCode}" /></td>
            <td><c:out value="${s.fullName}" /></td>
            <td><c:out value="${s.username}" /></td>
            <td><c:out value="${s.className}" /></td>
            <td>
              <c:choose>
                <c:when test="${s.active}"><span class="badge text-bg-success">Active</span></c:when>
                <c:otherwise><span class="badge text-bg-secondary">Inactive</span></c:otherwise>
              </c:choose>
            </td>
            <c:if test="${canEdit}">
              <td class="text-end">
                <div class="d-inline-flex gap-1">
                  <a class="btn btn-sm btn-outline-primary" href="${ctx}/students/${s.id}/edit">Edit</a>

                  <form method="post" action="${ctx}/students/${s.id}/reset-password"
                        onsubmit="return confirm('Reset the password of this student?');">
                    <button type="submit" class="btn btn-sm btn-outline-warning">Reset password</button>
                  </form>

                  <c:choose>
                    <c:when test="${s.active}">
                      <form method="post" action="${ctx}/students/${s.id}/deactivate"
                            onsubmit="return confirm('Deactivate this student? They will not be able to log in.');">
                        <button type="submit" class="btn btn-sm btn-outline-danger">Deactivate</button>
                      </form>
                    </c:when>
                    <c:otherwise>
                      <form method="post" action="${ctx}/students/${s.id}/activate">
                        <button type="submit" class="btn btn-sm btn-outline-success">Activate</button>
                      </form>
                    </c:otherwise>
                  </c:choose>
                </div>
              </td>
            </c:if>
          </tr>
        </c:forEach>

        <c:if test="${empty students}">
          <tr><td colspan="7" class="text-center text-muted py-4">No students found.</td></tr>
        </c:if>
      </tbody>
    </table>
  </div>
</div>

<jsp:include page="/WEB-INF/jsp/layout/footer.jsp" />