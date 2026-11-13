package com.group5.attendance.dao;

import com.group5.attendance.model.AttendanceRow;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

/** SQL for attendance marks (table attendance). */
@Repository
public class AttendanceDao {

    private final JdbcTemplate jdbc;

    public AttendanceDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<AttendanceRow> mapper = (rs, n) -> {
        AttendanceRow r = new AttendanceRow();
        r.setStudentId(rs.getInt("student_id"));
        r.setRollNo(rs.getString("roll_no"));
        r.setStudentCode(rs.getString("student_code"));
        r.setFullName(rs.getString("full_name"));
        r.setStatus(rs.getString("status"));
        r.setMethod(rs.getString("method"));
        return r;
    };

    /** Every active student of the class, with their mark for this lecture (null if not marked yet). */
    public List<AttendanceRow> roster(int sessionId, String className) {
        return jdbc.query(
            "SELECT s.id AS student_id, s.roll_no, s.student_code, u.full_name, a.status, a.method "
          + "FROM student s JOIN users u ON u.id = s.user_id "
          + "LEFT JOIN attendance a ON a.student_id = s.id AND a.session_id = ? "
          + "WHERE s.class_name = ? AND u.active = TRUE "
          + "ORDER BY CAST(s.roll_no AS UNSIGNED)", mapper, sessionId, className);
    }

    /** Saves the teacher's ticks. Batch insert; updates the row if the student already has one. */
    public void upsertManual(int sessionId, int markedBy, List<AttendanceRow> changes, Set<Integer> present) {
        List<Object[]> batch = changes.stream()
            .map(r -> new Object[] { sessionId, r.getStudentId(),
                                     present.contains(r.getStudentId()) ? "PRESENT" : "ABSENT", markedBy })
            .toList();
        jdbc.batchUpdate(
            "INSERT INTO attendance (session_id, student_id, status, method, marked_by) VALUES (?, ?, ?, 'MANUAL', ?) "
          + "ON DUPLICATE KEY UPDATE status = VALUES(status), method = 'MANUAL', "
          + "marked_by = VALUES(marked_by), marked_at = NOW()", batch);
    }

    /** Removes every mark and manual request of a lecture (they must go before the lecture itself). */
    public void deleteBySession(int sessionId) {
        jdbc.update("DELETE FROM attendance_request WHERE session_id = ?", sessionId);
        jdbc.update("DELETE FROM attendance WHERE session_id = ?", sessionId);
    }

    /** When a lecture is closed, everyone without a mark becomes ABSENT. */
    public void markRemainingAbsent(int sessionId, int markedBy, String className) {
        jdbc.update(
            "INSERT INTO attendance (session_id, student_id, status, method, marked_by) "
          + "SELECT ?, s.id, 'ABSENT', 'MANUAL', ? FROM student s JOIN users u ON u.id = s.user_id "
          + "WHERE s.class_name = ? AND u.active = TRUE "
          + "AND NOT EXISTS (SELECT 1 FROM attendance a WHERE a.session_id = ? AND a.student_id = s.id)",
            sessionId, markedBy, className, sessionId);
    }
}