<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<%@ taglib prefix="fn" uri="jakarta.tags.functions" %>
<%-- 
  SHARED HEADER (top navigation bar). Every page after login starts with:

    <jsp:include page="/WEB-INF/jsp/layout/header.jsp">
      <jsp:param name="title"  value="Students" />
      <jsp:param name="active" value="students" />   (one of: dashboard, students, subjects, mark, view, search, percentage, qr)
    </jsp:include>

  and ends with:  <jsp:include page="/WEB-INF/jsp/layout/footer.jsp" />
--%>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title><c:out value="${param.title}" /> | Attendance System</title>
  <script src="${ctx}/js/theme-init.js"></script>
  <link rel="preconnect" href="https://fonts.googleapis.com">
  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
  <link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap">
  <link rel="stylesheet" href="${ctx}/vendor/bootstrap/css/bootstrap.min.css">
  <link rel="stylesheet" href="${ctx}/vendor/bootstrap-icons/bootstrap-icons.min.css">
  <link rel="stylesheet" href="${ctx}/css/app.css">
</head>
<body>

<%-- ===== Top navigation bar (brand | page links | user menu) ===== --%>
<header class="app-topnav sticky-top">
  <div class="topnav-inner">

    <a class="topnav-brand" href="${ctx}/dashboard">
      <span class="brand-logo"><i class="bi bi-calendar2-check"></i></span>
      <span class="brand-name">Attendance System</span>
    </a>

    <%-- Hamburger: only visible on phones/tablets --%>
    <button class="topnav-toggle d-lg-none" type="button" data-bs-toggle="collapse" data-bs-target="#topnav-links" aria-controls="topnav-links" aria-label="Open menu">
      <i class="bi bi-list fs-4"></i>
    </button>

    <nav class="topnav-links collapse" id="topnav-links">
      <a class="${param.active == 'dashboard' ? 'active' : ''}" href="${ctx}/dashboard">Dashboard</a>

      <c:if test="${sessionScope.user.role == 'COORDINATOR' || sessionScope.user.role == 'TEACHER'}">
        <a class="${param.active == 'students' ? 'active' : ''}" href="${ctx}/students">Students</a>
        <a class="${param.active == 'subjects' ? 'active' : ''}" href="${ctx}/subjects">Subjects</a>
      </c:if>

      <c:if test="${sessionScope.user.role != 'STUDENT'}">
        <a class="${param.active == 'mark' ? 'active' : ''}" href="${ctx}/sessions">Mark Attendance</a>
        <a class="${param.active == 'qr' ? 'active' : ''}" href="${ctx}/qr">Live QR</a>
        <a class="${param.active == 'view' ? 'active' : ''}" href="${ctx}/attendance/by-date">View by Date</a>
        <a class="${param.active == 'percentage' ? 'active' : ''}" href="${ctx}/attendance/percentage">Percentage</a>
        <a class="${param.active == 'defaulters' ? 'active' : ''}" href="${ctx}/attendance/defaulters">Defaulters</a>
      </c:if>

      <c:if test="${sessionScope.user.role == 'STUDENT'}">
        <a class="${param.active == 'my' ? 'active' : ''}" href="${ctx}/my-attendance">My Attendance</a>
      </c:if>
    </nav>

    <%-- User menu: avatar (first letter) + name + role badge + dropdown --%>
    <div class="dropdown user-menu">
      <a href="#" class="user-toggle dropdown-toggle" data-bs-toggle="dropdown" aria-expanded="false">
        <span class="user-avatar"><c:out value="${fn:toUpperCase(fn:substring(sessionScope.user.username, 0, 1))}" /></span>
        <span class="user-name d-none d-sm-inline"><c:out value="${sessionScope.user.username}" /></span>
        <span class="role-badge d-none d-sm-inline"><c:out value="${sessionScope.user.role}" /></span>
      </a>
      <ul class="dropdown-menu dropdown-menu-end shadow-sm">
        <li>
          <%-- Theme switch moved here (theme.js still finds it by class) --%>
          <button type="button" class="dropdown-item theme-toggle-item theme-toggle" aria-pressed="false" aria-label="Switch to dark theme">
            <i class="bi bi-moon-stars-fill me-2" aria-hidden="true"></i><i class="bi bi-sun-fill me-2" aria-hidden="true"></i>Switch theme
          </button>
        </li>
        <li><hr class="dropdown-divider"></li>
        <li><a class="dropdown-item text-danger" href="${ctx}/logout"><i class="bi bi-box-arrow-right me-2"></i>Logout</a></li>
      </ul>
    </div>

  </div>
</header>

<main class="app-content">
  <%-- Pages other than the dashboard get their title here (the dashboard has its own big welcome heading) --%>
  <c:if test="${param.active != 'dashboard'}">
    <h1 class="page-heading"><c:out value="${param.title}" /></h1>
  </c:if>
