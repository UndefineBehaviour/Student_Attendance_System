package com.group5.attendance.controller;

import com.group5.attendance.model.User;
import com.group5.attendance.service.AttendanceService;
import com.group5.attendance.service.ForbiddenException;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SubjectsController {

    private final AttendanceService service;

    public SubjectsController(AttendanceService service) {
        this.service = service;
    }

    @GetMapping("/subjects")
    public String subjects(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null || "STUDENT".equals(user.getRole())) {
            throw new ForbiddenException();
        }
        // Coordinator: all subjects. Teacher: only the subjects linked to them.
        model.addAttribute("subjects", service.subjectsFor(user));
        model.addAttribute("coordinator", "COORDINATOR".equals(user.getRole()));
        return "subjects";
    }
}