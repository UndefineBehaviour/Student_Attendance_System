package com.group5.attendance.dao;

import com.group5.attendance.model.AttendanceSession;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/** SQL for lecture sessions (table attendance_session). */
@Repository
public class SessionDao {

    private static final String SELECT =
        "SELECT ss.id, ss.subject_id, sub.subject_code, sub.subject_name, ss.teacher_id, "
      + "       t.full_name AS teacher_name, ss.class_name, ss.session_date, ss.start_time, "
      + "       ss.end_time, ss.status, "
      + "       (SELECT COUNT(*) FROM attendance a WHERE a.session_id = ss.id AND a.status = 'PRESENT') AS present_count, "
      + "       (SELECT COUNT(*) FROM student st JOIN users su ON su.id = st.user_id "
      + "         WHERE st.class_name = ss.class_name AND su.active = TRUE) AS class_size "
      + "FROM attendance_session ss "
      + "JOIN subject sub ON sub.id = ss.subject_id "
      + "JOIN users t ON t.id = ss.teacher_id ";

    private final JdbcTemplate jdbc;

    public SessionDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<AttendanceSession> mapper = (rs, n) -> {
        AttendanceSession s = new AttendanceSession();
        s.setId(rs.getInt("id"));
        s.setSubjectId(rs.getInt("subject_id"));
        s.setSubjectCode(rs.getString("subject_code"));
        s.setSubjectName(rs.getString("subject_name"));
        s.setTeacherId(rs.getInt("teacher_id"));
        s.setTeacherName(rs.getString("teacher_name"));
        s.setClassName(rs.getString("class_name"));
        s.setSessionDate(rs.getDate("session_date").toLocalDate());
        s.setStartTime(rs.getTimestamp("start_time").toLocalDateTime());
        Timestamp end = rs.getTimestamp("end_time");
        s.setEndTime(end == null ? null : end.toLocalDateTime());
        s.setStatus(rs.getString("status"));
        s.setPresentCount(rs.getInt("present_count"));
        s.setClassSize(rs.getInt("class_size"));
        return s;
    };

    public Optional<AttendanceSession> findById(int id) {
        return jdbc.query(SELECT + "WHERE ss.id = ?", mapper, id).stream().findFirst();
    }

    /** An already-open lecture for the same subject, class, date and teacher (avoids double-clicks). */
    public Optional<AttendanceSession> findOpen(int subjectId, String className, LocalDate date, int teacherId) {
        return jdbc.query(
            SELECT + "WHERE ss.subject_id = ? AND ss.class_name = ? AND ss.session_date = ? "
                   + "AND ss.teacher_id = ? AND ss.status = 'OPEN'",
            mapper, subjectId, className, java.sql.Date.valueOf(date), teacherId).stream().findFirst();
    }

    /** Latest 20 lectures. teacherId = 0 means everyone's (coordinator). */
    public List<AttendanceSession> recent(int teacherId) {
        if (teacherId == 0) {
            return jdbc.query(SELECT + "ORDER BY ss.start_time DESC LIMIT 20", mapper);
        }
        return jdbc.query(SELECT + "WHERE ss.teacher_id = ? ORDER BY ss.start_time DESC LIMIT 20", mapper, teacherId);
    }

    public List<String> classNames() {
        return jdbc.queryForList("SELECT DISTINCT class_name FROM student ORDER BY class_name", String.class);
    }

    public int insert(int subjectId, int teacherId, String className, LocalDate date, LocalDateTime start) {
        KeyHolder keys = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                "INSERT INTO attendance_session (subject_id, teacher_id, class_name, session_date, start_time) "
              + "VALUES (?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, subjectId);
            ps.setInt(2, teacherId);
            ps.setString(3, className);
            ps.setDate(4, java.sql.Date.valueOf(date));
            ps.setTimestamp(5, Timestamp.valueOf(start));
            return ps;
        }, keys);
        return keys.getKey().intValue();
    }
        /** All lectures on one date. teacherId = 0 means everyone's (coordinator). */
    public List<AttendanceSession> findByDate(LocalDate date, int teacherId) {
        String order = "ORDER BY ss.start_time DESC";
        if (teacherId == 0) {
            return jdbc.query(SELECT + "WHERE ss.session_date = ? " + order,
                              mapper, java.sql.Date.valueOf(date));
        }
        return jdbc.query(SELECT + "WHERE ss.session_date = ? AND ss.teacher_id = ? " + order,
                          mapper, java.sql.Date.valueOf(date), teacherId);
    }

    public void delete(int sessionId) {
        jdbc.update("DELETE FROM attendance_session WHERE id = ?", sessionId);
    }

    public void close(int sessionId) {
        jdbc.update("UPDATE attendance_session SET status = 'CLOSED', end_time = ? WHERE id = ? AND status = 'OPEN'",
                    Timestamp.valueOf(LocalDateTime.now()), sessionId);
    }
}