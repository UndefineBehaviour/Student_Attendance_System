package com.group5.attendance.dao;

import com.group5.attendance.model.User;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class UserDao {

    private final JdbcTemplate jdbc;

    public UserDao(JdbcTemplate jdbc) {          // constructor injection
        this.jdbc = jdbc;
    }

    private final RowMapper<User> mapper = (rs, rowNum) -> {
        User u = new User();
        u.setId(rs.getInt("id"));
        u.setUsername(rs.getString("username"));
        u.setPasswordHash(rs.getString("password_hash"));
        u.setRole(rs.getString("role"));
        return u;
    };

    public Optional<User> findByUsername(String username) {
        List<User> list = jdbc.query(
            "SELECT id, username, password_hash, role FROM users WHERE username = ? AND active = TRUE",
            mapper, username);
        return list.stream().findFirst();
    }
}