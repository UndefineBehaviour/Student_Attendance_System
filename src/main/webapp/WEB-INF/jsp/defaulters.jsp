<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<jsp:include page="/WEB-INF/jsp/layout/header.jsp">
  <jsp:param name="title" value="Defaulters" />
  <jsp:param name="active" value="defaulters" />
</jsp:include>

<%-- Filter: subject (or all) and class --%>
<div class="card stat-card mb-3 no-print">
  <div class="card-header bg-white fw-semibold">Defaulter list (below 75%)</div>
  <div class="card-body">
    <c:choose>
      <c:when test="${empty subjects}">
        <div class="alert alert-warning mb-0">No subjects are assigned to you yet. Ask the coordinator to link your subjects.</div>
      </c:when>
      <c:otherwise>
        <form method="get" action="${ctx}/attendance/defaulters" class="row g-3 align-items-end">
          <div class="col-12 col-md-5">
            <label class="form-label" for="subjectId">Subject</label>
            <select class="form-select" id="subjectId" name="subjectId">
              <option value="">All subjects</option>
              <c:forEach var="sub" items="${subjects}">
                <option value="${sub.id}" ${subjectId == sub.id ? 'selected' : ''}><c:out value="${sub.code} - ${sub.name}" /></option>
              </c:forEach>
            </select>
          </div>
          <div class="col-6 col-md-3">
            <label class="form-label" for="className">Class</label>
            <select class="form-select" id="className" name="className">
              <c:forEach var="cls" items="${classes}">
                <option value="${cls}" ${className == cls ? 'selected' : ''}><c:out value="${cls}" /></option>
              </c:forEach>
            </select>
          </div>
          <div class="col-6 col-md-2">
            <button type="submit" class="btn btn-brand w-100">Show</button>
          </div>
          <div class="col-12 col-md-2">
            <button type="button" class="btn btn-outline-secondary w-100" onclick="window.print()"><i class="bi bi-printer me-1"></i> Print</button>
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

<c:if test="${empty errors && not empty subjects}">
  <c:choose>
    <c:when test="${empty groups}">
      <div class="alert alert-success">No student is below 75% (subjects without a closed lecture are not counted).</div>
    </c:when>
    <c:otherwise>
      <div class="mb-3 fw-semibold">
        <c:out value="${className}" />: <span class="badge text-bg-danger">${total}</span> defaulter<c:if test="${total != 1}">s</c:if>
      </div>

      <c:forEach var="g" items="${groups}">
        <div class="card stat-card mb-3">
          <div class="card-header bg-white fw-semibold d-flex justify-content-between align-items-center">
            <span><c:out value="${g.key}" /></span>
            <span class="badge text-bg-danger">${g.value.size()}</span>
          </div>
          <div class="table-responsive">
            <table class="table table-hover align-middle mb-0">
              <thead class="table-light">
                <tr>
                  <th>Roll</th>
                  <th>Student ID</th>
                  <th>Name</th>
                  <th>Present</th>
                  <th>Lectures</th>
                  <th>Percentage</th>
                  <th>To reach 75%</th>
                </tr>
              </thead>
              <tbody>
                <c:forEach var="s" items="${g.value}">
                  <tr>
                    <td><c:out value="${s.rollNo}" /></td>
                    <td><c:out value="${s.studentCode}" /></td>
                    <td><a href="${ctx}/attendance/student/${s.studentId}"><c:out value="${s.fullName}" /></a></td>
                    <td>${s.attended}</td>
                    <td>${s.total}</td>
                    <td><span class="${s.textClass}">${s.percentText}</span></td>
                    <td>Attend next ${s.needed}</td>
                  </tr>
                </c:forEach>
              </tbody>
            </table>
          </div>
        </div>
      </c:forEach>
    </c:otherwise>
  </c:choose>
</c:if>

<jsp:include page="/WEB-INF/jsp/layout/footer.jsp" />