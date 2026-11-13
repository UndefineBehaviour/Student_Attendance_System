<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<c:set var="editing" value="${not empty studentId}" />
<c:choose>
  <c:when test="${editing}"><c:set var="action" value="${ctx}/students/${studentId}" /></c:when>
  <c:otherwise><c:set var="action" value="${ctx}/students" /></c:otherwise>
</c:choose>
<jsp:include page="/WEB-INF/jsp/layout/header.jsp">
  <jsp:param name="title" value="Students" />
  <jsp:param name="active" value="students" />
</jsp:include>

<div class="row justify-content-center">
  <div class="col-12 col-lg-7">
    <div class="card stat-card">
      <div class="card-header bg-white fw-semibold">
        <c:choose>
          <c:when test="${editing}">Edit student</c:when>
          <c:otherwise>Add student</c:otherwise>
        </c:choose>
      </div>
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

        <form method="post" action="${action}">
          <div class="mb-3">
            <label class="form-label" for="fullName">Full name</label>
            <input type="text" class="form-control" id="fullName" name="fullName" maxlength="100"
                   value="<c:out value='${form.fullName}' />" required>
          </div>

          <div class="row">
            <div class="col-sm-6 mb-3">
              <label class="form-label" for="studentCode">Student ID</label>
              <input type="text" class="form-control" id="studentCode" name="studentCode" maxlength="20"
                     value="<c:out value='${form.studentCode}' />" placeholder="e.g. 26MCA61" required>
            </div>
            <div class="col-sm-6 mb-3">
              <label class="form-label" for="rollNo">Roll number</label>
              <input type="text" class="form-control" id="rollNo" name="rollNo" maxlength="3"
                     value="<c:out value='${form.rollNo}' />" required>
            </div>
          </div>

          <div class="row">
            <div class="col-sm-6 mb-3">
              <label class="form-label" for="username">Username (used to log in)</label>
              <input type="text" class="form-control" id="username" name="username" maxlength="100"
                     value="<c:out value='${form.username}' />" ${editing ? 'readonly' : 'required'}>
              <c:if test="${editing}"><div class="form-text">The username cannot be changed.</div></c:if>
            </div>
            <div class="col-sm-6 mb-3">
              <label class="form-label" for="className">Class</label>
              <input type="text" class="form-control" id="className" name="className" maxlength="30"
                     value="<c:out value='${form.className}' />" required>
            </div>
          </div>

          <c:if test="${not editing}">
            <div class="alert alert-info py-2 small">
              A random password is created automatically and shown once after you save.
            </div>
          </c:if>

          <div class="d-flex gap-2">
            <button type="submit" class="btn btn-brand"><i class="bi bi-check-lg me-1"></i> Save</button>
            <a href="${ctx}/students" class="btn btn-outline-secondary">Cancel</a>
          </div>
        </form>
      </div>
    </div>
  </div>
</div>

<jsp:include page="/WEB-INF/jsp/layout/footer.jsp" />