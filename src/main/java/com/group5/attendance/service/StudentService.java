package com.group5.attendance.service;

import com.group5.attendance.dao.StudentDao;
import com.group5.attendance.model.Student;
import com.group5.attendance.model.StudentForm;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.List;
import java.util.Optional;

/** Business rules for students: duplicate checks, password generation, hashing. */
@Service
public class StudentService {

    // letters and digits without look-alikes (no 0/O, 1/l/I)
    private static final String ALPHABET = "abcdefghjkmnpqrstuvwxyzABCDEFGHJKLMNPQRSTUVWXYZ23456789";

    private final StudentDao dao;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    private final SecureRandom random = new SecureRandom();

    public StudentService(StudentDao dao) {
        this.dao = dao;
    }

    public List<Student> search(String text) {
        if (text == null || text.isBlank()) {
            return dao.findAll();
        }
        return dao.search(text.trim());
    }

    public Optional<Student> find(int id) {
        return dao.findById(id);
    }

    /** Adds a student with a login. Returns the generated first password (shown once, never stored). */
    @Transactional
    public String create(StudentForm form) {
        String username = form.getUsername().trim();
        String code = form.getStudentCode().trim();
        String roll = form.getRollNo().trim();
        String cls = form.getClassName().trim();

        if (dao.usernameExists(username)) {
            throw new IllegalArgumentException("Username '" + username + "' is already taken");
        }
        if (dao.codeExists(code, 0)) {
            throw new IllegalArgumentException("Student ID '" + code + "' already exists");
        }
        if (dao.rollExists(cls, roll, 0)) {
            throw new IllegalArgumentException("Roll number " + roll + " already exists in " + cls);
        }

        String password = randomPassword();
        int userId = dao.insertUser(username, encoder.encode(password), form.getFullName().trim());
        dao.insertStudent(userId, code, roll, cls);
        return password;
    }

    @Transactional
    public void update(int id, StudentForm form) {
        Student existing = dao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));
        String code = form.getStudentCode().trim();
        String roll = form.getRollNo().trim();
        String cls = form.getClassName().trim();

        if (dao.codeExists(code, id)) {
            throw new IllegalArgumentException("Student ID '" + code + "' already belongs to another student");
        }
        if (dao.rollExists(cls, roll, id)) {
            throw new IllegalArgumentException("Roll number " + roll + " already belongs to another student in " + cls);
        }

        dao.updateName(existing.getUserId(), form.getFullName().trim());
        dao.updateStudent(id, code, roll, cls);
    }

    public int countActive() {
        return dao.countActive();
    }

    public void setActive(int id, boolean active) {
        Student s = dao.findById(id).orElseThrow(() -> new IllegalArgumentException("Student not found"));
        dao.setActive(s.getUserId(), active);
    }

    /** Gives the student a new random password and forces a change at next login. */
    public String resetPassword(int id) {
        Student s = dao.findById(id).orElseThrow(() -> new IllegalArgumentException("Student not found"));
        String password = randomPassword();
        dao.resetPassword(s.getUserId(), encoder.encode(password));
        return password;
    }

    private String randomPassword() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            sb.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
        }
        return sb.toString();
    }
}