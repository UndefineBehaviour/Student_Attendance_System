<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<jsp:include page="/WEB-INF/jsp/layout/header.jsp">
  <jsp:param name="title" value="Subjects" />
  <jsp:param name="active" value="subjects" />
</jsp:include>

<div class="card stat-card">
  <div class="card-header bg-white fw-semibold d-flex justify-content-between">
    <span>Subjects</span>
    <span class="text-muted fw-normal">
      ${subjects.size()} subjects &middot; ${coordinator ? 'Coordinator view' : 'Teacher view'}
    </span>
  </div>
  <div class="table-responsive">
    <table class="table table-hover align-middle mb-0">
      <thead class="table-light">
        <tr>
          <th style="width: 80px;">#</th>
          <th>Subject code</th>
          <th>Subject name</th>
        </tr>
      </thead>
      <tbody>
        <c:forEach var="s" items="${subjects}" varStatus="st">
          <tr>
            <td class="text-muted">${st.count}</td>
            <td class="fw-semibold"><c:out value="${s.code}" /></td>
            <td><c:out value="${s.name}" /></td>
          </tr>
        </c:forEach>
        <c:if test="${empty subjects}">
          <tr><td colspan="3" class="text-center text-muted py-4">
            No subjects are assigned to you yet. Ask the coordinator to link your subjects.
          </td></tr>
        </c:if>
      </tbody>
    </table>
  </div>
</div>

<jsp:include page="/WEB-INF/jsp/layout/footer.jsp" />