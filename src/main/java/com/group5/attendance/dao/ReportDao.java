package com.group5.attendance.dao;

import com.group5.attendance.model.AttendanceStat;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * SQL for attendance reports.
 * Only CLOSED lectures count. total = lectures held for the subject and class,
 * attended = lectures where the student was PRESENT.
 */
@Repository
public class ReportDao {

    private static final String STATS =
        "SELECT s.id AS student_id, s.roll_no, s.student_code, u.full_name, "
      + "       sub.id AS subject_id, sub.subject_code, sub.subject_name, "
      + "       (SELECT COUNT(*) FROM attendance_session ss "
      + "         WHERE ss.subject_id = sub.id AND ss.class_name = s.class_name AND ss.status = 'CLOSED') AS total, "
      + "       (SELECT COUNT(*) FROM attendance a JOIN attendance_session ss2 ON ss2.id = a.session_id "
      + "         WHERE a.student_id = s.id AND ss2.subject_id = sub.id AND ss2.status = 'CLOSED' "
      + "           AND a.status = 'PRESENT') AS attended "
      + "FROM student s JOIN users u ON u.id = s.user_id CROSS JOIN subject sub ";

    private final JdbcTemplate jdbc;

    public ReportDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<AttendanceStat> mapper = (rs, n) -> {
        AttendanceStat st = new AttendanceStat();
        st.setStudentId(rs.getInt("student_id"));
        st.setRollNo(rs.getString("roll_no"));
        st.setStudentCode(rs.getString("student_code"));
        st.setFullName(rs.getString("full_name"));
        st.setSubjectId(rs.getInt("subject_id"));
        st.setSubjectCode(rs.getString("subject_code"));
        st.setSubjectName(rs.getString("subject_name"));
        st.setAttended(rs.getInt("attended"));
        st.setTotal(rs.getInt("total"));
        return st;
    };

    /** Every active student of a class, for each of the given subjects. */
    public List<AttendanceStat> classStats(String className, List<Integer> subjectIds) {
        if (subjectIds.isEmpty()) {
            return List.of();
        }
        String marks = subjectIds.stream().map(id -> "?").collect(Collectors.joining(","));
        List<Object> args = new ArrayList<>();
        args.add(className);
        args.addAll(subjectIds);
        return jdbc.query(
            STATS + "WHERE s.class_name = ? AND u.active = TRUE AND sub.id IN (" + marks + ") "
                  + "ORDER BY sub.subject_code, CAST(s.roll_no AS UNSIGNED)",
            mapper, args.toArray());
    }

    /** One student, every subject in which their class has had at least one closed lecture. */
    public List<AttendanceStat> studentStats(int studentId) {
        return jdbc.query(
            STATS + "WHERE s.id = ? AND sub.id IN (SELECT ss3.subject_id FROM attendance_session ss3 "
                  + "WHERE ss3.class_name = s.class_name AND ss3.status = 'CLOSED') "
                  + "ORDER BY sub.subject_code",
            mapper, studentId);
    }

    /** The student row that belongs to a login (empty for teachers). */
    public Optional<Integer> studentIdForUser(int userId) {
        return jdbc.queryForList("SELECT id FROM student WHERE user_id = ?", Integer.class, userId)
                   .stream().findFirst();
    }
}