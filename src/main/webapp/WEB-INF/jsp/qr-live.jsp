<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<c:set var="ctx" value="${pageContext.request.contextPath}" />
<jsp:include page="/WEB-INF/jsp/layout/header.jsp">
  <jsp:param name="title" value="Live QR" />
  <jsp:param name="active" value="qr" />
</jsp:include>
<link rel="stylesheet" href="${ctx}/css/qr.css">

<c:if test="${localOnly}">
  <div class="alert alert-warning">
    You opened this page as <strong><c:out value="${baseUrl}" /></strong>, so students' phones cannot open the QR link.
    Open the app with your computer's Wi-Fi address (or the HTTPS link you use for the demo) and reload this page.
  </div>
</c:if>

<div class="row g-3">

  <%-- Left: the QR --%>
  <div class="col-12 col-lg-5">
    <section class="panel qr-panel" id="qrStage">
      <div class="qr-box">
        <img id="qrImg" src="${ctx}/qr/${lecture.id}/code.png" alt="QR code for attendance" width="420" height="420">
        <div id="qrClosed" class="qr-closed d-none">This lecture is closed.</div>
      </div>
      <div class="qr-timer" aria-hidden="true"><div id="qrBar" class="qr-timer-bar"></div></div>
      <p class="qr-hint">The code changes every ${stepSeconds} seconds. Students scan it with their phone camera and confirm.</p>
      <div class="d-flex flex-wrap gap-2 justify-content-center no-print">
        <button type="button" class="btn btn-outline-secondary btn-sm" id="btnFull"><i class="bi bi-arrows-fullscreen me-1"></i> Full screen</button>
      </div>
      <p class="qr-url text-muted">Phones open: <c:out value="${baseUrl}" /></p>
    </section>
  </div>

  <%-- Right: lecture + live count --%>
  <div class="col-12 col-lg-7">
    <div class="card stat-card mb-3">
      <div class="card-body d-flex flex-wrap gap-3 align-items-center justify-content-between">
        <div>
          <div class="fs-5 fw-semibold"><c:out value="${lecture.subjectCode}" /> &ndash; <c:out value="${lecture.subjectName}" /></div>
          <div class="text-muted">
            <c:out value="${lecture.className}" /> &middot; <c:out value="${lecture.dateText}" />
            &middot; started <c:out value="${lecture.startText}" /> &middot; <c:out value="${lecture.teacherName}" />
          </div>
        </div>
        <span class="badge text-bg-warning fs-6" id="openBadge">Open</span>
      </div>
    </div>

    <div class="stats-strip qr-stats mb-3">
      <div class="stat-cell">
        <div class="stat-label">Present</div>
        <div class="stat-value" id="cntPresent">${lecture.presentCount}</div>
      </div>
      <div class="stat-cell">
        <div class="stat-label">In class</div>
        <div class="stat-value" id="cntTotal">${lecture.classSize}</div>
      </div>
    </div>

    <div class="card stat-card mb-3">
      <div class="card-header bg-white fw-semibold d-flex justify-content-between align-items-center">
        <span>Latest check-ins</span>
        <span class="text-muted small" id="liveNote">updating&hellip;</span>
      </div>
      <ul class="list-group list-group-flush qr-recent" id="recentList">
        <li class="list-group-item text-muted">No QR check-ins yet.</li>
      </ul>
    </div>

    <div class="d-flex flex-wrap gap-2 align-items-center">
      <a href="${ctx}/sessions/${lecture.id}" class="btn btn-outline-secondary">Marking sheet</a>
      <form method="post" action="${ctx}/sessions/${lecture.id}/close"
            onsubmit="return confirm('Close this lecture? Students who are not marked present will be marked absent.');">
        <button type="submit" class="btn btn-outline-danger"><i class="bi bi-lock me-1"></i> Close lecture</button>
      </form>
      <a href="${ctx}/qr" class="btn btn-outline-secondary ms-auto">Back</a>
    </div>
  </div>
</div>

<script>
  (function () {
    var id = ${lecture.id};
    var step = ${stepSeconds} * 1000;
    var base = '${ctx}/qr/' + id;
    var img = document.getElementById('qrImg');
    var bar = document.getElementById('qrBar');
    var lastLoad = Date.now();
    var stopped = false;

    function reloadImage() {
      if (stopped) { return; }
      lastLoad = Date.now();
      img.src = base + '/code.png?ts=' + lastLoad;
    }
    function animateBar() {
      var left = Math.max(0, 1 - (Date.now() - lastLoad) / step);
      bar.style.width = (left * 100) + '%';
      if (!stopped) { requestAnimationFrame(animateBar); }
    }
    function markClosed() {
      stopped = true;
      document.getElementById('qrClosed').classList.remove('d-none');
      img.classList.add('qr-dim');
      var b = document.getElementById('openBadge');
      b.className = 'badge text-bg-secondary fs-6';
      b.textContent = 'Closed';
      bar.style.width = '0';
    }
    img.addEventListener('error', function () { markClosed(); });

    function renderRecent(list) {
      var ul = document.getElementById('recentList');
      ul.innerHTML = '';
      if (!list.length) {
        var li0 = document.createElement('li');
        li0.className = 'list-group-item text-muted';
        li0.textContent = 'No QR check-ins yet.';
        ul.appendChild(li0);
        return;
      }
      list.forEach(function (r) {
        var li = document.createElement('li');
        li.className = 'list-group-item d-flex justify-content-between align-items-center';
        var left = document.createElement('span');
        left.textContent = r.name;
        var roll = document.createElement('span');
        roll.className = 'text-muted ms-2 small';
        roll.textContent = 'Roll ' + r.roll;
        left.appendChild(roll);
        var t = document.createElement('span');
        t.className = 'text-muted small';
        t.textContent = r.time;
        li.appendChild(left);
        li.appendChild(t);
        ul.appendChild(li);
      });
    }

    function poll() {
      fetch(base + '/status', { credentials: 'same-origin', headers: { 'Accept': 'application/json' } })
        .then(function (r) { return r.json(); })
        .then(function (d) {
          document.getElementById('cntPresent').textContent = d.present;
          document.getElementById('cntTotal').textContent = d.total;
          renderRecent(d.recent);
          document.getElementById('liveNote').textContent = 'live';
          if (!d.open) { markClosed(); }
        })
        .catch(function () { document.getElementById('liveNote').textContent = 'connection lost, retrying\u2026'; });
    }

    document.getElementById('btnFull').addEventListener('click', function () {
      var el = document.getElementById('qrStage');
      if (el.requestFullscreen) { el.requestFullscreen(); }
    });

    setInterval(reloadImage, step);
    setInterval(poll, 3000);
    requestAnimationFrame(animateBar);
    poll();
  })();
</script>

<jsp:include page="/WEB-INF/jsp/layout/footer.jsp" />
