/* Light/dark toggle with an ink-blot reveal that spreads from the button.
   - Uses the View Transitions API and animates a clip-path on the new page snapshot.
   - Falls back to an instant switch when unsupported or when the user prefers reduced motion. */
(function () {
  'use strict';
  var root = document.documentElement;
  var DURATION = 1300;        // ms
  var FRAMES   = 44;          // keyframes in the animation
  var POINTS   = 96;          // vertices on the main blob edge
  var DROPS    = 16;          // satellite droplets thrown ahead of the front

  function currentTheme() { return root.getAttribute('data-bs-theme') === 'dark' ? 'dark' : 'light'; }

  function applyTheme(t) {
    root.setAttribute('data-bs-theme', t);
    try { localStorage.setItem('theme', t); } catch (e) {}
    document.querySelectorAll('.theme-toggle').forEach(function (b) {
      b.setAttribute('aria-pressed', String(t === 'dark'));
      b.setAttribute('aria-label', t === 'dark' ? 'Switch to light theme' : 'Switch to dark theme');
      b.setAttribute('title', t === 'dark' ? 'Switch to light theme' : 'Switch to dark theme');
    });
  }

  /* ----- ink blot geometry ----- */
  function rand(seedRef) { // small deterministic PRNG so each blot is stable across its frames
    seedRef.v = (seedRef.v * 1664525 + 1013904223) % 4294967296;
    return seedRef.v / 4294967296;
  }
  function easeOut(t) { return 1 - Math.pow(1 - t, 3); }

  function makeBlot(cx, cy, maxR) {
    var seed = { v: (Math.random() * 4294967296) >>> 0 };
    // Edge character: a handful of sine "lobes" with random phase, plus sharper tendrils.
    var lobes = [];
    for (var k = 0; k < 6; k++) {
      lobes.push({ f: 2 + Math.floor(rand(seed) * 9), p: rand(seed) * Math.PI * 2, a: .05 + rand(seed) * .11, s: (rand(seed) - .5) * 2 });
    }
    var tendrils = [];
    for (var j = 0; j < 9; j++) {
      tendrils.push({ ang: rand(seed) * Math.PI * 2, w: .05 + rand(seed) * .09, h: .18 + rand(seed) * .4 });
    }
    var drops = [];
    for (var d = 0; d < DROPS; d++) {
      drops.push({ ang: rand(seed) * Math.PI * 2, lead: 1.04 + rand(seed) * .32, size: .012 + rand(seed) * .035, birth: .08 + rand(seed) * .5 });
    }

    function edgeRadius(ang, prog) {
      var wob = 0;
      for (var i = 0; i < lobes.length; i++) {
        var L = lobes[i];
        wob += L.a * Math.sin(L.f * ang + L.p + L.s * prog * 3);
      }
      var tend = 0;
      for (var t = 0; t < tendrils.length; t++) {
        var T = tendrils[t];
        var da = Math.atan2(Math.sin(ang - T.ang), Math.cos(ang - T.ang));
        tend += T.h * Math.exp(-(da * da) / (2 * T.w * T.w));
      }
      // organic roughness fades as the blot nears full coverage so the screen always ends fully covered
      var rough = Math.sin(Math.min(prog, 1) * Math.PI) * .85 + .15;
      return 1 + (wob + tend) * rough;
    }

    function frame(prog, over) {
      var base = maxR * easeOut(prog) * (over || 1);
      var path = '';
      var i, a;
      for (i = 0; i < POINTS; i++) {
        a = (i / POINTS) * Math.PI * 2;
        var r = Math.max(0, base * edgeRadius(a, prog));
        path += (i ? 'L' : 'M') + (cx + Math.cos(a) * r).toFixed(1) + ' ' + (cy + Math.sin(a) * r).toFixed(1);
      }
      path += 'Z';
      // droplets: always emit the same number of sub-paths/points so frames interpolate
      for (var d = 0; d < drops.length; d++) {
        var D = drops[d];
        var life = Math.max(0, Math.min(1, (prog - D.birth) / .35));
        var dr = maxR * D.size * Math.sin(life * Math.PI * .5) * (1 - Math.max(0, prog - .85) / .15);
        var dist = base * D.lead * edgeRadius(D.ang, prog);
        var dx = cx + Math.cos(D.ang) * dist, dy = cy + Math.sin(D.ang) * dist;
        for (i = 0; i < 12; i++) {
          a = (i / 12) * Math.PI * 2;
          var rr = dr * (1 + .18 * Math.sin(a * 3 + d));
          path += (i ? 'L' : 'M') + (dx + Math.cos(a) * rr).toFixed(1) + ' ' + (dy + Math.sin(a) * rr).toFixed(1);
        }
        path += 'Z';
      }
      return path;
    }

    var keyframes = [];
    for (var f = 0; f <= FRAMES; f++) {
      keyframes.push({ clipPath: "path('" + frame(f / FRAMES) + "')" });
    }
    // Guarantee the last frame covers the whole viewport (same path structure, so it still interpolates).
    keyframes[keyframes.length - 1] = { clipPath: "path('" + frame(1, 1.8) + "')" };
    return keyframes;
  }

  /* ----- toggle ----- */
  var busy = false;
  function toggle(originEl) {
    var next = currentTheme() === 'dark' ? 'light' : 'dark';
    var reduce = window.matchMedia && window.matchMedia('(prefers-reduced-motion: reduce)').matches;

    if (busy) return;
    if (!document.startViewTransition || reduce) { applyTheme(next); return; }

    var rect = originEl.getBoundingClientRect();
    var cx = rect.left + rect.width / 2, cy = rect.top + rect.height / 2;
    var w = window.innerWidth, h = window.innerHeight;
    var maxR = Math.hypot(Math.max(cx, w - cx), Math.max(cy, h - cy)) * 1.05;

    busy = true;
    var vt = document.startViewTransition(function () { applyTheme(next); });
    vt.ready.then(function () {
      var anim = root.animate(makeBlot(cx, cy, maxR), {
        duration: DURATION, easing: 'linear', fill: 'forwards', pseudoElement: '::view-transition-new(root)'
      });
      return anim.finished;
    }).catch(function () {}).then(function () { /* ready can reject if skipped */ });
    vt.finished.catch(function () {}).then(function () { busy = false; });
  }

  document.addEventListener('DOMContentLoaded', function () {
    applyTheme(currentTheme()); // sync aria state with the theme chosen by theme-init.js
    document.querySelectorAll('.theme-toggle').forEach(function (btn) {
      btn.addEventListener('click', function (e) { e.preventDefault(); toggle(btn); });
    });
  });
})();
