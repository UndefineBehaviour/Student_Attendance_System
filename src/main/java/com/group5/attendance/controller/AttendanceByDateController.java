package com.group5.attendance.controller;

import com.group5.attendance.model.AttendanceSession;
import com.group5.attendance.model.User;
import com.group5.attendance.service.AttendanceService;
import com.group5.attendance.service.ForbiddenException;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Controller
public class AttendanceByDateController {

    private static final DateTimeFormatter SHOW = DateTimeFormatter.ofPattern("dd MMM yyyy");

    private final AttendanceService service;

    public AttendanceByDateController(AttendanceService service) {
        this.service = service;
    }

    /** Coordinator or teacher. Students get 403. */
    private User staff(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null || "STUDENT".equals(user.getRole())) {
            throw new ForbiddenException();
        }
        return user;
    }

    @GetMapping("/attendance/by-date")
    public String byDate(@RequestParam(required = false) String date,
                         HttpSession session, Model model) {
        User user = staff(session);

        LocalDate selected = LocalDate.now();
        List<String> errors = new ArrayList<>();
        if (date != null && !date.isBlank()) {
            try {
                selected = LocalDate.parse(date.trim());
            } catch (DateTimeParseException e) {
                errors.add("That date is not valid. Showing today instead.");
            }
        }

        List<AttendanceSession> records = service.byDate(user, selected);

        model.addAttribute("selectedDate", selected.toString());   // for the date box
        model.addAttribute("dateText", selected.format(SHOW));     // for display
        model.addAttribute("today", LocalDate.now().toString());
        model.addAttribute("records", records);
        model.addAttribute("errors", errors);
        return "attendance-by-date";
    }
}