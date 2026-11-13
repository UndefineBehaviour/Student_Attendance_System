package com.group5.attendance.controller;

import com.group5.attendance.model.Student;
import com.group5.attendance.model.StudentForm;
import com.group5.attendance.model.User;
import com.group5.attendance.service.StudentService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/students")
public class StudentController {

    /** false = only the coordinator can add/edit/deactivate; true = teachers can too. */
    private static final boolean TEACHERS_CAN_EDIT = false;

    private final StudentService service;

    public StudentController(StudentService service) {
        this.service = service;
    }

    // ---------- Who is allowed? (checked on the server, not just by hiding buttons) ----------

    /** Coordinator or teacher. Students get 403. */
    private User staff(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null || "STUDENT".equals(user.getRole())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not allowed");
        }
        return user;
    }

    private boolean canEdit(User user) {
        return "COORDINATOR".equals(user.getRole())
                || (TEACHERS_CAN_EDIT && "TEACHER".equals(user.getRole()));
    }

    /** Staff who are allowed to change data. */
    private void manager(HttpSession session) {
        if (!canEdit(staff(session))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not allowed");
        }
    }

    private Student studentOr404(int id) {
        return service.find(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found"));
    }

    private List<String> messages(BindingResult result) {
        return result.getAllErrors().stream()
                .map(e -> e.getDefaultMessage())
                .toList();
    }

    // ---------- List and search ----------

    @GetMapping
    public String list(@RequestParam(required = false) String q, HttpSession session, Model model) {
        User user = staff(session);
        model.addAttribute("students", service.search(q));
        model.addAttribute("q", q == null ? "" : q);
        model.addAttribute("canEdit", canEdit(user));
        return "students";
    }

    // ---------- Add ----------

    @GetMapping("/new")
    public String newForm(HttpSession session, Model model) {
        manager(session);
        StudentForm form = new StudentForm();
        form.setClassName("FYMCA-A");                  // default; change it in the form if needed
        model.addAttribute("form", form);
        return "student-form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("form") StudentForm form, BindingResult result,
                         HttpSession session, Model model, RedirectAttributes redirect) {
        manager(session);
        List<String> errors = new ArrayList<>(messages(result));
        if (errors.isEmpty()) {
            try {
                String password = service.create(form);
                redirect.addFlashAttribute("msg", "Student added.");
                redirect.addFlashAttribute("newUsername", form.getUsername().trim());
                redirect.addFlashAttribute("newPassword", password);
                return "redirect:/students";                       // POST then redirect
            } catch (IllegalArgumentException e) {
                errors.add(e.getMessage());
            } catch (DuplicateKeyException e) {
                errors.add("That username, student ID or roll number already exists");
            }
        }
        model.addAttribute("errors", errors);
        return "student-form";
    }

    // ---------- Edit ----------

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable int id, HttpSession session, Model model) {
        manager(session);
        Student s = studentOr404(id);
        StudentForm form = new StudentForm();
        form.setFullName(s.getFullName());
        form.setUsername(s.getUsername());
        form.setStudentCode(s.getStudentCode());
        form.setRollNo(s.getRollNo());
        form.setClassName(s.getClassName());
        model.addAttribute("form", form);
        model.addAttribute("studentId", id);
        return "student-form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable int id, @Valid @ModelAttribute("form") StudentForm form,
                         BindingResult result, HttpSession session, Model model,
                         RedirectAttributes redirect) {
        manager(session);
        studentOr404(id);
        List<String> errors = new ArrayList<>(messages(result));
        if (errors.isEmpty()) {
            try {
                service.update(id, form);
                redirect.addFlashAttribute("msg", "Student updated.");
                return "redirect:/students";
            } catch (IllegalArgumentException e) {
                errors.add(e.getMessage());
            } catch (DuplicateKeyException e) {
                errors.add("That student ID or roll number already exists");
            }
        }
        model.addAttribute("errors", errors);
        model.addAttribute("studentId", id);
        return "student-form";
    }

    // ---------- Deactivate / activate / reset password (all POST, then redirect) ----------

    @PostMapping("/{id}/deactivate")
    public String deactivate(@PathVariable int id, HttpSession session, RedirectAttributes redirect) {
        manager(session);
        studentOr404(id);
        service.setActive(id, false);
        redirect.addFlashAttribute("msg", "Student deactivated. They can no longer log in; their attendance is kept.");
        return "redirect:/students";
    }

    @PostMapping("/{id}/activate")
    public String activate(@PathVariable int id, HttpSession session, RedirectAttributes redirect) {
        manager(session);
        studentOr404(id);
        service.setActive(id, true);
        redirect.addFlashAttribute("msg", "Student activated again.");
        return "redirect:/students";
    }

    @PostMapping("/{id}/reset-password")
    public String resetPassword(@PathVariable int id, HttpSession session, RedirectAttributes redirect) {
        manager(session);
        Student s = studentOr404(id);
        String password = service.resetPassword(id);
        redirect.addFlashAttribute("msg", "Password reset.");
        redirect.addFlashAttribute("newUsername", s.getUsername());
        redirect.addFlashAttribute("newPassword", password);
        return "redirect:/students";
    }
}