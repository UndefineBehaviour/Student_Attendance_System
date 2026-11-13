package com.group5.attendance.dao;

import com.group5.attendance.model.Student;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

/** All SQL for students lives here (and only here). */
@Repository
public class StudentDao implements CrudDao<Student, Integer> {

    private static final String SELECT =
        "SELECT s.id, s.user_id, s.student_code, s.roll_no, s.class_name, "
      + "       u.username, u.full_name, u.active "
      + "FROM student s JOIN users u ON u.id = s.user_id ";

    private static final String ORDER = " ORDER BY s.class_name, CAST(s.roll_no AS UNSIGNED)";

    private final JdbcTemplate jdbc;

    public StudentDao(JdbcTemplate jdbc) {                 // constructor injection
        this.jdbc = jdbc;
    }

    // RowMapper (lambda): turns one database row into one Student object
    private final RowMapper<Student> mapper = (rs, rowNum) -> {
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
    };

    // ---------- Read ----------

    @Override
    public Optional<Student> findById(Integer id) {
        List<Student> list = jdbc.query(SELECT + "WHERE s.id = ?", mapper, id);
        return list.stream().findFirst();
    }

    @Override
    public List<Student> findAll() {
        return jdbc.query(SELECT + ORDER, mapper);
    }

    /** Search by name, student ID, username (partial match) or roll number (exact). */
    public List<Student> search(String text) {
        String like = "%" + text + "%";
        return jdbc.query(
            SELECT + "WHERE u.full_name LIKE ? OR s.student_code LIKE ? OR u.username LIKE ? OR s.roll_no = ?" + ORDER,
            mapper, like, like, like, text);
    }

    // ---------- Duplicate checks (excludeId = 0 when adding a new student) ----------

    public boolean usernameExists(String username) {
        return count("SELECT COUNT(*) FROM users WHERE username = ?", username) > 0;
    }

    public boolean codeExists(String code, int excludeStudentId) {
        return count("SELECT COUNT(*) FROM student WHERE student_code = ? AND id <> ?", code, excludeStudentId) > 0;
    }

    public boolean rollExists(String className, String rollNo, int excludeStudentId) {
        return count("SELECT COUNT(*) FROM student WHERE class_name = ? AND roll_no = ? AND id <> ?",
                     className, rollNo, excludeStudentId) > 0;
    }

    private int count(String sql, Object... args) {
        Integer n = jdbc.queryForObject(sql, Integer.class, args);
        return n == null ? 0 : n;
    }

        /** Active students (same ones the Students page lists as Active). */
    public int countActive() {
        return count("SELECT COUNT(*) FROM student s JOIN users u ON u.id = s.user_id WHERE u.active = TRUE");
    }

    // ---------- Create ----------

    /** Inserts the login row and returns its new id (PreparedStatement + generated key). */
    public int insertUser(String username, String passwordHash, String fullName) {
        KeyHolder keys = new GeneratedKeyHolder();
        jdbc.update(con -> {
            PreparedStatement ps = con.prepareStatement(
                "INSERT INTO users (username, password_hash, full_name, role) VALUES (?, ?, ?, 'STUDENT')",
                Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, username);
            ps.setString(2, passwordHash);
            ps.setString(3, fullName);
            return ps;
        }, keys);
        return keys.getKey().intValue();
    }

    public void insertStudent(int userId, String code, String rollNo, String className) {
        jdbc.update("INSERT INTO student (user_id, student_code, roll_no, class_name) VALUES (?, ?, ?, ?)",
                    userId, code, rollNo, className);
    }

    // ---------- Update ----------

    public void updateName(int userId, String fullName) {
        jdbc.update("UPDATE users SET full_name = ? WHERE id = ?", fullName, userId);
    }

    public void updateStudent(int studentId, String code, String rollNo, String className) {
        jdbc.update("UPDATE student SET student_code = ?, roll_no = ?, class_name = ? WHERE id = ?",
                    code, rollNo, className, studentId);
    }

    /** "Delete" = deactivate, so old attendance records stay intact. */
    public void setActive(int userId, boolean active) {
        jdbc.update("UPDATE users SET active = ? WHERE id = ?", active, userId);
    }

    public void resetPassword(int userId, String passwordHash) {
        jdbc.update("UPDATE users SET password_hash = ?, must_change_password = TRUE, "
                  + "failed_attempts = 0, locked_until = NULL WHERE id = ?", passwordHash, userId);
    }
}