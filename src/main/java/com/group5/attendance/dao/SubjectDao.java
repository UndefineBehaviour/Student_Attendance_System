package com.group5.attendance.dao;

import com.group5.attendance.model.Subject;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/** Read-only for now. Student 2 can add insert/update/delete here for Subject CRUD. */
@Repository
public class SubjectDao implements CrudDao<Subject, Integer> {

    private final JdbcTemplate jdbc;

    public SubjectDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private final RowMapper<Subject> mapper = (rs, n) -> {
        Subject s = new Subject();
        s.setId(rs.getInt("id"));
        s.setCode(rs.getString("subject_code"));
        s.setName(rs.getString("subject_name"));
        return s;
    };

    @Override
    public Optional<Subject> findById(Integer id) {
        return jdbc.query("SELECT id, subject_code, subject_name FROM subject WHERE id = ?", mapper, id)
                   .stream().findFirst();
    }

    @Override
    public List<Subject> findAll() {
        return jdbc.query("SELECT id, subject_code, subject_name FROM subject ORDER BY subject_code", mapper);
    }

    /** Subjects a teacher teaches (from the teacher_subject table). */
    public List<Subject> findByTeacher(int teacherId) {
        return jdbc.query(
            "SELECT s.id, s.subject_code, s.subject_name FROM subject s "
          + "JOIN teacher_subject ts ON ts.subject_id = s.id WHERE ts.teacher_id = ? ORDER BY s.subject_code",
            mapper, teacherId);
    }
}