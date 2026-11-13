package com.group5.attendance.service;

import com.group5.attendance.dao.UserDao;
import com.group5.attendance.model.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserDao userDao;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public UserService(UserDao userDao) {
        this.userDao = userDao;
    }

    public Optional<User> authenticate(String username, String rawPassword) {
        return userDao.findByUsername(username)
                .filter(u -> encoder.matches(rawPassword, u.getPasswordHash()));
    }
}