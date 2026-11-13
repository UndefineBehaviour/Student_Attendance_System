package com.group5.attendance.controller;

import com.group5.attendance.model.AttendanceSession;
import com.group5.attendance.model.User;
import com.group5.attendance.service.AttendanceService;
import com.group5.attendance.service.ForbiddenException;
import com.group5.attendance.service.NotFoundException;
import com.group5.attendance.service.QrService;
import com.group5.attendance.service.QrToken;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Live QR.
 *   Teacher / coordinator:  /qr (open lectures), /qr/{id} (the live screen), /qr/{id}/code.png, /qr/{id}/status
 *   Student (after scanning): /scan (check), POST /scan (mark), /scan/result
 */
@Controller
public class QrController {

    private static final String TICKET = "scanTicket";          // session attribute: "lectureId:expiresAtMillis"
    private static final long TICKET_MS = 2 * 60 * 1000L;       // the student has 2 minutes to press the button
    private static final String DEVICE_COOKIE = "did";

    private final QrService qr;
    private final AttendanceService attendance;

    public QrController(QrService qr, AttendanceService attendance) {
        this.qr = qr;
        this.attendance = attendance;
    }

    /** Coordinator or teacher. Students get 403. */
    private User staff(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null || "STUDENT".equals(user.getRole())) {
            throw new ForbiddenException();
        }
        return user;
    }

    // ================= Teacher side =================

    /** Pick one of your open lectures. */
    @GetMapping("/qr")
    public String list(HttpSession session, Model model) {
        User user = staff(session);
        List<AttendanceSession> open = attendance.recent(user).stream().filter(AttendanceSession::isOpen).toList();
        model.addAttribute("openSessions", open);
        return "live-qr";
    }

    /** The screen the teacher shows on the projector. */
    @GetMapping("/qr/{id}")
    public String show(@PathVariable int id, HttpSession session, HttpServletRequest req, Model model) {
        User user = staff(session);
        AttendanceSession lecture = attendance.load(user, id);   // 403 if it is another teacher's lecture
        if (!lecture.isOpen()) {
            return "redirect:/sessions/" + id;
        }
        String base = qr.baseUrl(req);
        model.addAttribute("lecture", lecture);
        model.addAttribute("stepSeconds", QrToken.STEP_SECONDS);
        model.addAttribute("baseUrl", base);
        model.addAttribute("localOnly", qr.isLocalOnly(base));
        return "qr-live";
    }

    /** The QR picture. The page asks for a fresh one every few seconds. */
    @GetMapping(value = "/qr/{id}/code.png", produces = MediaType.IMAGE_PNG_VALUE)
    @ResponseBody
    public ResponseEntity<byte[]> code(@PathVariable int id, HttpSession session, HttpServletRequest req) {
        User user = staff(session);
        AttendanceSession lecture = attendance.load(user, id);
        if (!lecture.isOpen()) {
            throw new NotFoundException("This lecture is closed");
        }
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(qr.png(id, qr.baseUrl(req)));
    }

    /** Small JSON the live screen polls: counts and the latest scans. */
    @GetMapping(value = "/qr/{id}/status", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<Map<String, Object>> status(@PathVariable int id, HttpSession session) {
        User user = staff(session);
        AttendanceSession lecture = attendance.load(user, id);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("open", lecture.isOpen());
        body.put("present", lecture.getPresentCount());
        body.put("total", lecture.getClassSize());
        body.put("recent", qr.recentScans(id));
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(body);
    }

    // ================= Student side =================

    /** Opened by scanning the QR. Checks the code, then asks the student to confirm. */
    @GetMapping("/scan")
    public String scan(@RequestParam(required = false) Integer s, @RequestParam(required = false) String t,
                       @CookieValue(value = DEVICE_COOKIE, required = false) String deviceId,
                       HttpSession session, HttpServletRequest req, HttpServletResponse res, Model model) {
        User user = (User) session.getAttribute("user");
        if (s == null || t == null) {
            model.addAttribute("state", "ERROR");
            model.addAttribute("message", "This is not a valid QR link. Scan the code on the screen again.");
            return "scan";
        }
        if (deviceId == null || deviceId.isBlank()) {
            giveDeviceCookie(req, res);
        }
        QrService.Outcome o = qr.check(user, s, t);
        if (o.state == QrService.State.READY) {
            session.setAttribute(TICKET, s + ":" + (System.currentTimeMillis() + TICKET_MS));
        }
        fill(model, o);
        model.addAttribute("lectureId", s);
        return "scan";
    }

    /** Button pressed. Only works right after a successful check (the ticket in the session). */
    @PostMapping("/scan")
    public String mark(@RequestParam int s,
                       @CookieValue(value = DEVICE_COOKIE, required = false) String deviceId,
                       HttpSession session, RedirectAttributes redirect) {
        User user = (User) session.getAttribute("user");
        QrService.Outcome o;
        if (!ticketValid(session, s)) {
            o = new QrService.Outcome(QrService.State.ERROR, "This check-in has timed out. Scan the QR on the screen again.", null);
        } else {
            session.removeAttribute(TICKET);
            o = qr.mark(user, s, deviceId);
        }
        redirect.addFlashAttribute("state", o.state.name());
        redirect.addFlashAttribute("message", o.message);
        if (o.lecture != null) {                                          // plain strings only: safe to keep in the session
            redirect.addFlashAttribute("lectureTitle", title(o.lecture));
            redirect.addFlashAttribute("lectureMeta", meta(o.lecture));
        }
        return "redirect:/scan/result";                                  // POST then redirect
    }

    @GetMapping("/scan/result")
    public String result(Model model) {
        if (!model.containsAttribute("state")) {                         // page was refreshed: nothing to show
            return "redirect:/dashboard";
        }
        return "scan";
    }

    // ---------- helpers ----------

    private void fill(Model model, QrService.Outcome o) {
        model.addAttribute("state", o.state.name());
        model.addAttribute("message", o.message);
        if (o.lecture != null) {
            model.addAttribute("lectureTitle", title(o.lecture));
            model.addAttribute("lectureMeta", meta(o.lecture));
        }
    }

    private String title(AttendanceSession l) {
        return l.getSubjectCode() + " \u2013 " + l.getSubjectName();
    }

    private String meta(AttendanceSession l) {
        return l.getClassName() + " \u00b7 " + l.getDateText() + " \u00b7 " + l.getTeacherName();
    }

    private boolean ticketValid(HttpSession session, int lectureId) {
        Object raw = session.getAttribute(TICKET);
        if (!(raw instanceof String text)) {
            return false;
        }
        String[] parts = text.split(":");
        return parts.length == 2
                && parts[0].equals(String.valueOf(lectureId))
                && Long.parseLong(parts[1]) > System.currentTimeMillis();
    }

    /** A random id kept in this phone's browser. The database allows one student per phone per lecture. */
    private void giveDeviceCookie(HttpServletRequest req, HttpServletResponse res) {
        Cookie c = new Cookie(DEVICE_COOKIE, UUID.randomUUID().toString().replace("-", ""));
        c.setPath("/");
        c.setHttpOnly(true);
        c.setSecure(req.isSecure());
        c.setMaxAge(60 * 60 * 24 * 365);
        c.setAttribute("SameSite", "Lax");
        res.addCookie(c);
    }
}
