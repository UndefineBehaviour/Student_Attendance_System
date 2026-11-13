<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1">
  <title>Login | Attendance System</title>
  <script src="${ctx}/js/theme-init.js"></script>
  <link rel="preconnect" href="https://fonts.googleapis.com">
  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
  <link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700;800&display=swap">
  <link rel="stylesheet" href="${ctx}/vendor/bootstrap/css/bootstrap.min.css">
  <link rel="stylesheet" href="${ctx}/vendor/bootstrap-icons/bootstrap-icons.min.css">
  <link rel="stylesheet" href="${ctx}/css/app.css">
</head>
<body>
<div class="login-page">
  <button type="button" class="theme-toggle" aria-pressed="false" aria-label="Switch to dark theme" title="Switch to dark theme">
    <i class="bi bi-moon-stars-fill" aria-hidden="true"></i><i class="bi bi-sun-fill" aria-hidden="true"></i>
  </button>
  <div class="card login-card">
    <div class="card-body p-4 p-sm-5">
      <div class="login-logo"><i class="bi bi-calendar2-check-fill"></i></div>
      <h3 class="text-center mb-1">Student <em class="accent">Attendance</em> System</h3>
      <p class="text-center text-muted mb-4">Sign in to continue</p>

      <c:if test="${not empty error}">
        <div class="alert alert-danger d-flex align-items-center py-2" role="alert">
          <i class="bi bi-exclamation-triangle-fill me-2"></i>
          <div><c:out value="${error}" /></div>
        </div>
      </c:if>

      <form method="post" action="${ctx}/login">
        <div class="mb-3">
          <label class="form-label" for="username">Username</label>
          <div class="input-group">
            <span class="input-group-text"><i class="bi bi-person"></i></span>
            <input type="text" class="form-control" id="username" name="username" placeholder="Enter username" required autofocus>
          </div>
        </div>

        <div class="mb-4">
          <label class="form-label" for="password">Password</label>
          <div class="input-group">
            <span class="input-group-text"><i class="bi bi-lock"></i></span>
            <input type="password" class="form-control" id="password" name="password" placeholder="Enter password" required>
            <button class="btn btn-outline-secondary" type="button" id="togglePwd" aria-label="Show password">
              <i class="bi bi-eye" id="togglePwdIcon"></i>
            </button>
          </div>
        </div>

        <button type="submit" class="btn btn-brand w-100 py-2 fw-semibold">
          <i class="bi bi-box-arrow-in-right me-1"></i> Login
        </button>
      </form>
    </div>
    <div class="card-footer text-center text-muted small bg-transparent border-0 pb-4">
      Group 5 &middot; Advance Java Mini Project
    </div>
  </div>
</div>

<script src="${ctx}/js/theme.js"></script>
<script>
  document.getElementById('togglePwd').addEventListener('click', function () {
    var f = document.getElementById('password');
    var show = f.type === 'password';
    f.type = show ? 'text' : 'password';
    document.getElementById('togglePwdIcon').className = show ? 'bi bi-eye-slash' : 'bi bi-eye';
  });
</script>
</body>
</html>



<!-- <%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html>
<head>
  <meta charset="UTF-8">
  <title>Login - Attendance System</title>
</head>
<body>
  <h2>Student Attendance System</h2>
  <c:if test="${not empty error}">
    <p style="color:red"><c:out value="${error}"/></p>
  </c:if>
  <form method="post" action="${pageContext.request.contextPath}/login">
    <label>Username <input type="text" name="username" required></label><br>
    <label>Password <input type="password" name="password" required></label><br>
    <button type="submit">Login</button>
  </form>
</body>
</html> -->