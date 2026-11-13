package com.group5.attendance.dao;

import com.group5.attendance.model.Student;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** SQL used only by the QR check-in (tables student, attendance). */
@Repository
public class QrDao {

    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("hh:mm:ss a");

    private final JdbcTemplate jdbc;

    public QrDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /** The active student that belongs to this login (found from the login, never from the URL). */
    public Optional<Student> studentOfUser(int userId) {
        return jdbc.query(
            "SELECT s.id, s.user_id, s.student_code, s.roll_no, s.class_name, u.username, u.full_name, u.active "
          + "FROM student s JOIN users u ON u.id = s.user_id WHERE s.user_id = ? AND u.active = TRUE",
            (rs, n) -> {
                Student s = new Student();
                s.setId(rs.getInt("id"));
                s.setUserId(rs.getInt("user_id"));
                s.setStudentCode(rs.getString("student_code"));
                s.setRollNo(rs.getString("roll_no"));
                s.setClassName(rs.getString("class_name"));
                s.setUsername(rs.getString("username"));
                s.setFullName(rs.getString("full_name"));
                s.setActive(rs.getBoolean("active"));
                return s;
            }, userId).stream().findFirst();
    }

    /** PRESENT / ABSENT, or empty if this student has no row for the lecture yet. */
    public Optional<String> statusOf(int sessionId, int studentId) {
        return jdbc.queryForList(
            "SELECT status FROM attendance WHERE session_id = ? AND student_id = ?",
            String.class, sessionId, studentId).stream().findFirst();
    }

    /** True if this phone (cookie) already marked a DIFFERENT student in this lecture. */
    public boolean deviceUsedByOther(int sessionId, String deviceId, int studentId) {
        Integer n = jdbc.queryForObject(
            "SELECT COUNT(*) FROM attendance WHERE session_id = ? AND device_id = ? AND student_id <> ?",
            Integer.class, sessionId, deviceId, studentId);
        return n != null && n > 0;
    }

    public void insertQrPresent(int sessionId, int studentId, int markedBy, String deviceId) {
        jdbc.update(
            "INSERT INTO attendance (session_id, student_id, status, method, marked_by, device_id) "
          + "VALUES (?, ?, 'PRESENT', 'QR', ?, ?)", sessionId, studentId, markedBy, deviceId);
    }

    /** The teacher had saved this student as ABSENT earlier; the scan turns it into PRESENT. */
    public void upgradeToQrPresent(int sessionId, int studentId, int markedBy, String deviceId) {
        jdbc.update(
            "UPDATE attendance SET status = 'PRESENT', method = 'QR', marked_by = ?, marked_at = NOW(), device_id = ? "
          + "WHERE session_id = ? AND student_id = ?", markedBy, deviceId, sessionId, studentId);
    }

    /** Latest students who checked in by QR (for the teacher's live screen). */
    public List<Map<String, String>> recentScans(int sessionId, int limit) {
        return jdbc.query(
            "SELECT u.full_name, s.roll_no, a.marked_at "
          + "FROM attendance a JOIN student s ON s.id = a.student_id JOIN users u ON u.id = s.user_id "
          + "WHERE a.session_id = ? AND a.method = 'QR' AND a.status = 'PRESENT' "
          + "ORDER BY a.marked_at DESC LIMIT ?",
            (rs, n) -> {
                Map<String, String> m = new LinkedHashMap<>();
                m.put("name", rs.getString("full_name"));
                m.put("roll", rs.getString("roll_no"));
                m.put("time", rs.getTimestamp("marked_at").toLocalDateTime().format(TIME));
                return m;
            }, sessionId, limit);
    }
}
