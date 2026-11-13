<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<jsp:include page="/WEB-INF/jsp/layout/header.jsp">
  <jsp:param name="title" value="QR check-in" />
  <jsp:param name="active" value="my" />
</jsp:include>
<link rel="stylesheet" href="${ctx}/css/qr.css">

<div class="scan-wrap">
  <section class="panel scan-card">

    <c:choose>
      <c:when test="${state == 'READY'}"><div class="scan-icon scan-icon-brand"><i class="bi bi-qr-code-scan"></i></div></c:when>
      <c:when test="${state == 'DONE'}"><div class="scan-icon scan-icon-good"><i class="bi bi-check-lg"></i></div></c:when>
      <c:when test="${state == 'ALREADY'}"><div class="scan-icon scan-icon-brand"><i class="bi bi-check2-all"></i></div></c:when>
      <c:otherwise><div class="scan-icon scan-icon-low"><i class="bi bi-x-lg"></i></div></c:otherwise>
    </c:choose>

    <c:if test="${not empty lectureTitle}">
      <h2 class="scan-title"><c:out value="${lectureTitle}" /></h2>
      <p class="text-muted"><c:out value="${lectureMeta}" /></p>
    </c:if>

    <c:choose>
      <c:when test="${state == 'READY'}">
        <p class="scan-text">Confirm that you are in this lecture.</p>
        <form method="post" action="${ctx}/scan">
          <input type="hidden" name="s" value="${lectureId}">
          <button type="submit" class="btn btn-brand btn-lg w-100"><i class="bi bi-check2-circle me-1"></i> Mark me present</button>
        </form>
      </c:when>
      <c:when test="${state == 'DONE'}">
        <p class="scan-text scan-good"><c:out value="${message}" /></p>
        <a href="${ctx}/my-attendance" class="btn btn-outline-secondary w-100">My attendance</a>
      </c:when>
      <c:when test="${state == 'ALREADY'}">
        <p class="scan-text"><c:out value="${message}" /></p>
        <a href="${ctx}/my-attendance" class="btn btn-outline-secondary w-100">My attendance</a>
      </c:when>
      <c:otherwise>
        <p class="scan-text scan-low"><c:out value="${message}" /></p>
        <a href="${ctx}/dashboard" class="btn btn-outline-secondary w-100">Back</a>
      </c:otherwise>
    </c:choose>

  </section>
</div>

<jsp:include page="/WEB-INF/jsp/layout/footer.jsp" />
