/**
 * InfoLink 3D Effects Engine - Clean Edition
 * Safe effects only: no perspective on containers, no sidebar rotation,
 * no transforms on layout-critical elements during page load.
 */
(function () {
  "use strict";

  /* â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
     1. GENTLE CARD TILT  (per-element perspective â€” never touches layout)
  â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€ */
  function initTilt() {
    /* Tilt disabled â€” no rotation/tilt on any box */
  }

  /* â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
     2. FLOATING PARTICLE NETWORK
  â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€ */
  function initParticles() {
    if (document.getElementById("il-particles")) return;
    var canvas = document.createElement("canvas");
    canvas.id = "il-particles";
    canvas.style.cssText = "position:fixed;top:0;left:0;width:100%;height:100%;pointer-events:none;z-index:0;opacity:0.45;";
    document.body.prepend(canvas);

    var ctx = canvas.getContext("2d");
    var W, H, pts = [], mX = -999, mY = -999;
    var COLS = ["rgba(124,58,237,","rgba(167,139,250,","rgba(79,70,229,","rgba(139,92,246,","rgba(59,130,246,"];

    function resize() { W = canvas.width = window.innerWidth; H = canvas.height = window.innerHeight; }
    resize(); window.addEventListener("resize", resize);
    document.addEventListener("mousemove", function (e) { mX = e.clientX; mY = e.clientY; });

    function mkPt(rY) {
      return { x: Math.random() * (W || window.innerWidth),
               y: rY ? Math.random() * (H || window.innerHeight) : (H || window.innerHeight) + 10,
               r: Math.random() * 2 + 0.4, a: Math.random() * 0.6 + 0.1,
               sp: Math.random() * 0.35 + 0.12, dx: (Math.random() - 0.5) * 0.25,
               c: COLS[Math.floor(Math.random() * COLS.length)],
               ph: Math.random() * Math.PI * 2, ps: Math.random() * 0.018 + 0.006 };
    }
    for (var i = 0; i < 50; i++) pts.push(mkPt(true));

    function frame() {
      ctx.clearRect(0, 0, W, H);
      for (var i = 0; i < pts.length; i++) {
        for (var j = i + 1; j < pts.length; j++) {
          var d = Math.hypot(pts[i].x - pts[j].x, pts[i].y - pts[j].y);
          if (d < 100) {
            ctx.beginPath(); ctx.moveTo(pts[i].x, pts[i].y); ctx.lineTo(pts[j].x, pts[j].y);
            ctx.strokeStyle = "rgba(124,58,237," + ((1 - d/100) * 0.14) + ")";
            ctx.lineWidth = 0.7; ctx.stroke();
          }
        }
      }
      pts.forEach(function (p, idx) {
        p.ph += p.ps;
        var r = p.r * (0.85 + Math.sin(p.ph) * 0.15);
        var mdx = p.x - mX, mdy = p.y - mY, md = Math.hypot(mdx, mdy);
        if (md < 80 && md > 0) { var f = (80 - md) / 80; p.x += mdx/md*f*1.2; p.y += mdy/md*f*1.2; }
        p.y -= p.sp; p.x += p.dx;
        if (p.y + r < 0) { pts[idx] = mkPt(false); return; }
        if (p.x < -r) p.x = W + r; if (p.x > W + r) p.x = -r;
        var g = ctx.createRadialGradient(p.x, p.y, 0, p.x, p.y, r * 3);
        g.addColorStop(0, p.c + p.a + ")"); g.addColorStop(1, p.c + "0)");
        ctx.beginPath(); ctx.arc(p.x, p.y, r*3, 0, Math.PI*2); ctx.fillStyle = g; ctx.fill();
        ctx.beginPath(); ctx.arc(p.x, p.y, r, 0, Math.PI*2); ctx.fillStyle = p.c + p.a + ")"; ctx.fill();
      });
      requestAnimationFrame(frame);
    }
    requestAnimationFrame(frame);
  }

  /* â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
     3. BLOB PARALLAX  (blobs only â€” sidebar is NOT touched)
  â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€ */
  function initParallax() {
    var mx = 0, my = 0, cx = 0, cy = 0;
    var blobs = document.querySelectorAll(".blob");
    if (!blobs.length) return;
    document.addEventListener("mousemove", function (e) {
      mx = (e.clientX / window.innerWidth  - 0.5) * 2;
      my = (e.clientY / window.innerHeight - 0.5) * 2;
    });
    function tick() {
      cx += (mx - cx) * 0.05; cy += (my - cy) * 0.05;
      blobs.forEach(function (b, i) {
        var f = (i + 1) * 6;
        b.style.transform = "translate(" + (cx*f) + "px," + (cy*f) + "px)";
      });
      requestAnimationFrame(tick);
    }
    requestAnimationFrame(tick);
  }

  /* â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
     4. ANIMATED 3D PERSPECTIVE GRID
  â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€ */
  function init3DGrid() {
    if (document.getElementById("il-grid")) return;
    var canvas = document.createElement("canvas");
    canvas.id = "il-grid";
    canvas.style.cssText = "position:fixed;top:0;left:0;width:100%;height:100%;pointer-events:none;z-index:0;opacity:0.06;";
    document.body.prepend(canvas);
    var ctx = canvas.getContext("2d"), W, H, t = 0;
    function resize() { W = canvas.width = window.innerWidth; H = canvas.height = window.innerHeight; }
    resize(); window.addEventListener("resize", resize);
    function draw() {
      ctx.clearRect(0, 0, W, H); t += 0.004;
      var vx = W/2, vy = H*0.38, cols = 14, rows = 10;
      ctx.strokeStyle = "#7c3aed"; ctx.lineWidth = 0.8;
      for (var r = 0; r <= rows; r++) {
        var pr = (r/rows + t*0.1) % 1;
        ctx.globalAlpha = pr * 0.8;
        var y = vy + (H - vy) * pr;
        ctx.beginPath(); ctx.moveTo(0, y); ctx.lineTo(W, y); ctx.stroke();
      }
      ctx.globalAlpha = 1;
      for (var c = 0; c <= cols; c++) {
        ctx.globalAlpha = (0.35 + Math.sin(c*0.5 + t)*0.2) * 0.7;
        ctx.beginPath(); ctx.moveTo(vx, vy); ctx.lineTo(c*(W/cols), H); ctx.stroke();
      }
      ctx.globalAlpha = 1;
      requestAnimationFrame(draw);
    }
    requestAnimationFrame(draw);
  }

  /* â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
     5. LOGO 3D SPIN
  â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€ */
  function initLogoSpin() {
    document.querySelectorAll(".sidebar-logo img, .logo-icon img, .logo img").forEach(function (logo) {
      logo.style.transition = "transform 0.65s cubic-bezier(0.34,1.56,0.64,1), filter 0.3s ease";
      var spinning = false;
      var trigger = logo.closest(".sidebar-logo, .logo, .logo-icon");
      if (!trigger) return;
      trigger.addEventListener("mouseenter", function () {
        if (spinning) return; spinning = true;
        logo.style.transform = "rotateY(360deg) scale(1.12)";
        logo.style.filter = "drop-shadow(0 0 8px rgba(124,58,237,0.8))";
        setTimeout(function () {
          logo.style.transform = "rotateY(0deg) scale(1)"; logo.style.filter = "";
          setTimeout(function () { spinning = false; }, 700);
        }, 680);
      });
    });
  }

  /* â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
     6. STAT COUNT-UP
  â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€ */
  function initStatCards() {
    document.querySelectorAll(".stat-value").forEach(function (el) {
      var target = parseInt(el.textContent, 10);
      if (isNaN(target) || target === 0) return;
      el.textContent = "0";
      var start = performance.now(), dur = 1100;
      function upd(now) {
        var p = Math.min((now - start) / dur, 1);
        var e = 1 - Math.pow(1 - p, 3);
        el.textContent = Math.round(e * target);
        el.style.transform = "scale(" + (1 + (1-p)*0.07) + ")";
        if (p < 1) requestAnimationFrame(upd); else el.style.transform = "";
      }
      requestAnimationFrame(upd);
    });
  }

  /* â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
     7. NAV ITEM 3D SLIDE-IN
  â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€ */
  function initNavAnimations() {
    document.querySelectorAll(".nav-item").forEach(function (item, i) {
      item.style.opacity = "0";
      item.style.transform = "translateX(-16px)";
      item.style.transition = "opacity 0.38s ease " + (i*0.055) + "s, transform 0.38s ease " + (i*0.055) + "s";
      setTimeout(function () {
        item.style.opacity = "1"; item.style.transform = "translateX(0)";
      }, 40 + i * 55);
      item.addEventListener("mouseenter", function () { item.style.transform = "translateX(4px)"; });
      item.addEventListener("mouseleave", function () {
        item.style.transform = item.classList.contains("active") ? "translateX(2px)" : "translateX(0)";
      });
    });
    var active = document.querySelector(".nav-item.active");
    if (active) setTimeout(function () { active.style.transform = "translateX(2px)"; }, 450);
  }

  /* â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
     8. CURSOR GLOW TRAIL
  â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€ */
  function initCursorGlow() {
    var glow = document.createElement("div");
    glow.id = "il-cursor-glow";
    glow.style.cssText = "position:fixed;width:300px;height:300px;border-radius:50%;"
      + "background:radial-gradient(circle,rgba(124,58,237,0.1) 0%,transparent 70%);"
      + "pointer-events:none;z-index:1;transform:translate(-50%,-50%);transition:opacity 0.35s ease;opacity:0;";
    document.body.appendChild(glow);
    var gx = -999, gy = -999, tx = -999, ty = -999;
    document.addEventListener("mousemove", function (e) { tx = e.clientX; ty = e.clientY; glow.style.opacity = "1"; });
    document.addEventListener("mouseleave", function () { glow.style.opacity = "0"; });
    function loop() {
      gx += (tx-gx)*0.07; gy += (ty-gy)*0.07;
      glow.style.left = gx + "px"; glow.style.top = gy + "px";
      requestAnimationFrame(loop);
    }
    requestAnimationFrame(loop);
  }

  /* â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
     9. PAGE FADE-IN  (opacity only â€” NO layout transform)
  â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€ */
  function initPageEntrance() {
    var main = document.querySelector(".page-content") || document.querySelector(".card");
    if (!main) return;
    main.style.opacity = "0";
    main.style.transition = "opacity 0.6s ease";
    requestAnimationFrame(function () {
      setTimeout(function () { main.style.opacity = "1"; }, 60);
    });
  }

  /* â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
     10. TABLE ROW LIFT ON HOVER
  â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€ */
  function initTableRows() {
    function apply() {
      document.querySelectorAll(".data-table tbody tr:not(.il-tr)").forEach(function (row) {
        row.classList.add("il-tr");
        row.style.transition = "transform 0.2s ease, box-shadow 0.2s ease";
        row.addEventListener("mouseenter", function () {
          row.style.transform  = "translateX(3px)";
          row.style.boxShadow  = "0 3px 16px rgba(124,58,237,0.1)";
        });
        row.addEventListener("mouseleave", function () {
          row.style.transform = ""; row.style.boxShadow = "";
        });
      });
    }
    apply();
    var tb = document.querySelector(".data-table tbody");
    if (tb) new MutationObserver(apply).observe(tb, { childList: true });
  }

  /* â”€â”€â”€ INIT â”€â”€â”€ */
  function init() {
    init3DGrid();
    initParticles();
    initTilt();
    initParallax();
    initLogoSpin();
    initStatCards();
    initNavAnimations();
    initCursorGlow();
    initPageEntrance();
    initTableRows();
  }

  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", init);
  } else {
    init();
  }
})();
