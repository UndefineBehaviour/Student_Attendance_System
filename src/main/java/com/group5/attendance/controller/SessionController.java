package com.group5.attendance.controller;

import com.group5.attendance.model.AttendanceRow;
import com.group5.attendance.model.AttendanceSession;
import com.group5.attendance.model.SessionForm;
import com.group5.attendance.model.User;
import com.group5.attendance.service.AttendanceService;
import com.group5.attendance.service.ForbiddenException;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/sessions")
public class SessionController {

    private final AttendanceService service;

    public SessionController(AttendanceService service) {
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

    private List<String> messages(BindingResult result) {
        return result.getAllErrors().stream().map(e -> e.getDefaultMessage()).toList();
    }

    private void fillHome(User user, Model model) {
        model.addAttribute("subjects", service.subjectsFor(user));
        model.addAttribute("classes", service.classNames());
        model.addAttribute("recent", service.recent(user));
        model.addAttribute("today", LocalDate.now().toString());
    }

    // ---------- Start a lecture + list of recent lectures ----------

    @GetMapping
    public String home(HttpSession session, Model model) {
        User user = staff(session);
        SessionForm form = new SessionForm();
        form.setDate(LocalDate.now().toString());
        model.addAttribute("form", form);
        fillHome(user, model);
        return "sessions";
    }

    @PostMapping
    public String start(@Valid @ModelAttribute("form") SessionForm form, BindingResult result,
                        HttpSession session, Model model) {
        User user = staff(session);
        List<String> errors = new ArrayList<>(messages(result));
        if (errors.isEmpty()) {
            try {
                int id = service.start(user, form);
                return "redirect:/sessions/" + id;                 // POST then redirect
            } catch (IllegalArgumentException e) {
                errors.add(e.getMessage());
            }
        }
        model.addAttribute("errors", errors);
        fillHome(user, model);
        return "sessions";
    }

    // ---------- Marking page of one lecture ----------

    @GetMapping("/{id}")
    public String mark(@PathVariable int id, HttpSession session, Model model) {
        User user = staff(session);
        AttendanceSession lecture = service.load(user, id);
        List<AttendanceRow> rows = service.rows(lecture);
        model.addAttribute("lecture", lecture);
        model.addAttribute("rows", rows);
        model.addAttribute("canEdit", service.canEdit(user, lecture));
        model.addAttribute("canDelete", service.canDelete(user, lecture));
        return "mark";
    }

    @PostMapping("/{id}/save")
    public String save(@PathVariable int id, @RequestParam(required = false) List<Integer> present,
                       HttpSession session, RedirectAttributes redirect) {
        service.save(staff(session), id, present);
        redirect.addFlashAttribute("msg", "Attendance saved.");
        return "redirect:/sessions/" + id;
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable int id, HttpSession session, RedirectAttributes redirect) {
        service.delete(staff(session), id);
        redirect.addFlashAttribute("msg", "Lecture deleted.");
        return "redirect:/sessions";
    }

    @PostMapping("/{id}/close")
    public String close(@PathVariable int id, HttpSession session, RedirectAttributes redirect) {
        service.close(staff(session), id);
        redirect.addFlashAttribute("msg", "Lecture closed. Students who were not marked present are now absent.");
        return "redirect:/sessions/" + id;
    }
}