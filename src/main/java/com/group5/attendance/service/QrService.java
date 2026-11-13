package com.group5.attendance.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.group5.attendance.dao.QrDao;
import com.group5.attendance.dao.SessionDao;
import com.group5.attendance.model.AttendanceSession;
import com.group5.attendance.model.Student;
import com.group5.attendance.model.User;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Rules for the live QR: make the code, check a scan, mark the student present. */
@Service
public class QrService {

    /** What the student's phone should show. */
    public enum State { READY, DONE, ALREADY, ERROR }

    /** Result of checking or marking a scan. */
    public static final class Outcome {
        public final State state;
        public final String message;
        public final AttendanceSession lecture;   // may be null for early errors

        public Outcome(State state, String message, AttendanceSession lecture) {
            this.state = state;
            this.message = message;
            this.lecture = lecture;
        }
    }

    private final SessionDao sessions;
    private final QrDao qrDao;
    private final QrToken token;
    private final String publicUrl;

    public QrService(SessionDao sessions, QrDao qrDao,
                     @Value("${app.qr.secret:}") String secret,
                     @Value("${app.public-url:}") String publicUrl) {
        this.sessions = sessions;
        this.qrDao = qrDao;
        byte[] key;
        if (secret == null || secret.isBlank()) {          // no secret configured: random one per start
            key = new byte[32];
            new SecureRandom().nextBytes(key);
        } else {
            key = secret.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        }
        this.token = new QrToken(key);
        this.publicUrl = publicUrl == null ? "" : publicUrl.trim().replaceAll("/+$", "");
    }

    // ---------- Teacher side ----------

    /** The address phones must be able to open, e.g. https://abc.example.app/attendance */
    public String baseUrl(HttpServletRequest req) {
        if (!publicUrl.isEmpty()) {
            return publicUrl;
        }
        int port = req.getServerPort();
        boolean defaultPort = ("http".equals(req.getScheme()) && port == 80)
                           || ("https".equals(req.getScheme()) && port == 443);
        return req.getScheme() + "://" + req.getServerName() + (defaultPort ? "" : ":" + port) + req.getContextPath();
    }

    public boolean isLocalOnly(String baseUrl) {
        return baseUrl.contains("://localhost") || baseUrl.contains("://127.0.0.1");
    }

    /** PNG of the QR for this lecture, with the token of the current moment inside. */
    public byte[] png(int sessionId, String baseUrl) {
        String text = baseUrl + "/scan?s=" + sessionId + "&t=" + token.create(sessionId, Instant.now().getEpochSecond());
        try {
            BitMatrix matrix = new QRCodeWriter().encode(text, BarcodeFormat.QR_CODE, 420, 420,
                    Map.of(EncodeHintType.MARGIN, 2, EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M));
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("Could not draw the QR code", e);
        }
    }

    public List<Map<String, String>> recentScans(int sessionId) {
        return qrDao.recentScans(sessionId, 8);
    }

    public Optional<AttendanceSession> lecture(int sessionId) {
        return sessions.findById(sessionId);
    }

    // ---------- Student side ----------

    /** Step 1 (the page opened by scanning): can this student mark this lecture? */
    public Outcome check(User user, int sessionId, String tokenText) {
        Outcome early = rules(user, sessionId);
        if (early.state != State.READY) {
            return early;
        }
        if (!token.verify(sessionId, tokenText, Instant.now().getEpochSecond())) {
            return new Outcome(State.ERROR, "This QR code has expired. Scan the screen again.", early.lecture);
        }
        return early;
    }

    /** Step 2 (button pressed): write the attendance row. */
    @Transactional
    public Outcome mark(User user, int sessionId, String deviceId) {
        Outcome o = rules(user, sessionId);
        if (o.state != State.READY) {
            return o;
        }
        Student student = qrDao.studentOfUser(user.getId()).orElseThrow(ForbiddenException::new);
        if (deviceId == null || deviceId.isBlank()) {
            return new Outcome(State.ERROR, "Your browser blocked cookies. Allow cookies and scan again.", o.lecture);
        }
        if (qrDao.deviceUsedByOther(sessionId, deviceId, student.getId())) {
            return new Outcome(State.ERROR, "This phone has already been used to mark another student in this lecture.", o.lecture);
        }
        try {
            Optional<String> existing = qrDao.statusOf(sessionId, student.getId());
            if (existing.isPresent()) {
                qrDao.upgradeToQrPresent(sessionId, student.getId(), user.getId(), deviceId);
            } else {
                qrDao.insertQrPresent(sessionId, student.getId(), user.getId(), deviceId);
            }
        } catch (DuplicateKeyException e) {                  // double tap or a race with another phone
            return qrDao.statusOf(sessionId, student.getId()).filter("PRESENT"::equals).isPresent()
                    ? new Outcome(State.ALREADY, "You are already marked present.", o.lecture)
                    : new Outcome(State.ERROR, "This phone has already been used to mark another student in this lecture.", o.lecture);
        }
        return new Outcome(State.DONE, "You are marked present.", o.lecture);
    }

    /** Checks shared by both steps. Returns READY when the student may continue. */
    private Outcome rules(User user, int sessionId) {
        if (!"STUDENT".equals(user.getRole())) {
            return new Outcome(State.ERROR, "Only students can check in with the QR. Log in with a student account.", null);
        }
        Optional<AttendanceSession> found = sessions.findById(sessionId);
        if (found.isEmpty()) {
            return new Outcome(State.ERROR, "This lecture does not exist.", null);
        }
        AttendanceSession lecture = found.get();
        if (!lecture.isOpen()) {
            return new Outcome(State.ERROR, "This lecture is closed. Ask your teacher to mark you.", lecture);
        }
        Optional<Student> student = qrDao.studentOfUser(user.getId());
        if (student.isEmpty()) {
            return new Outcome(State.ERROR, "No active student record was found for your login.", lecture);
        }
        if (!student.get().getClassName().equals(lecture.getClassName())) {
            return new Outcome(State.ERROR, "This lecture is for class " + lecture.getClassName() + ", not yours.", lecture);
        }
        Optional<String> status = qrDao.statusOf(sessionId, student.get().getId());
        if (status.isPresent() && "PRESENT".equals(status.get())) {
            return new Outcome(State.ALREADY, "You are already marked present.", lecture);
        }
        return new Outcome(State.READY, "", lecture);
    }
}
