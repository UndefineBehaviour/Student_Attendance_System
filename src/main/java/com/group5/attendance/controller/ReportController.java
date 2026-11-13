package com.group5.attendance.controller;

import com.group5.attendance.model.AttendanceStat;
import com.group5.attendance.model.Student;
import com.group5.attendance.model.User;
import com.group5.attendance.service.ForbiddenException;
import com.group5.attendance.service.ReportCalc;
import com.group5.attendance.service.ReportService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
public class ReportController {

    private final ReportService service;

    public ReportController(ReportService service) {
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

    /** The class chosen on the page, or the first class if none was chosen. */
    private String classOrDefault(String className) {
        if (className != null && !className.isBlank()) {
            return className;
        }
        List<String> classes = service.classNames();
        return classes.isEmpty() ? "" : classes.get(0);
    }

    // ---------- Percentage of a class in one subject ----------

    @GetMapping("/attendance/percentage")
    public String percentage(@RequestParam(required = false) Integer subjectId,
                             @RequestParam(required = false) String className,
                             @RequestParam(required = false) String sort,
                             HttpSession session, Model model) {
        User user = staff(session);
        String cls = classOrDefault(className);
        List<String> errors = new ArrayList<>();

        model.addAttribute("subjects", service.subjectsFor(user));
        model.addAttribute("classes", service.classNames());
        model.addAttribute("subjectId", subjectId);
        model.addAttribute("className", cls);
        model.addAttribute("sort", sort == null ? "roll" : sort);

        if (subjectId != null) {
            try {
                List<AttendanceStat> stats = service.classReport(user, subjectId, cls, "percent".equals(sort));
                model.addAttribute("stats", stats);
                model.addAttribute("lectures", stats.isEmpty() ? 0 : stats.get(0).getTotal());
                model.addAttribute("belowCount", ReportCalc.belowCount(stats));
                model.addAttribute("average", ReportCalc.average(stats));
            } catch (IllegalArgumentException e) {
                errors.add(e.getMessage());
            }
        }
        model.addAttribute("errors", errors);
        return "percentage";
    }

    // ---------- Defaulter list ----------

    @GetMapping("/attendance/defaulters")
    public String defaulters(@RequestParam(required = false) Integer subjectId,
                             @RequestParam(required = false) String className,
                             HttpSession session, Model model) {
        User user = staff(session);
        String cls = classOrDefault(className);
        List<String> errors = new ArrayList<>();

        model.addAttribute("subjects", service.subjectsFor(user));
        model.addAttribute("classes", service.classNames());
        model.addAttribute("subjectId", subjectId);
        model.addAttribute("className", cls);

        try {
            Map<String, List<AttendanceStat>> groups = service.defaulters(user, subjectId, cls);
            model.addAttribute("groups", groups);
            model.addAttribute("total", ReportCalc.countDefaulters(groups));
        } catch (IllegalArgumentException e) {
            errors.add(e.getMessage());
        }
        model.addAttribute("errors", errors);
        return "defaulters";
    }

    // ---------- One student's subject-wise attendance (staff view) ----------

    @GetMapping("/attendance/student/{id}")
    public String studentView(@PathVariable int id, HttpSession session, Model model) {
        User user = staff(session);
        Student student = service.student(id);
        List<AttendanceStat> stats = service.studentReport(user, id);
        model.addAttribute("student", student);
        model.addAttribute("stats", stats);
        model.addAttribute("overall", ReportCalc.overall(stats));
        model.addAttribute("own", false);
        return "student-attendance";
    }

    // ---------- A student's own attendance ----------

    @GetMapping("/my-attendance")
    public String myAttendance(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null || !"STUDENT".equals(user.getRole())) {
            return "redirect:/dashboard";
        }
        // the student is found from the LOGIN, never from a number in the URL
        Student student = service.student(service.studentIdFor(user));
        List<AttendanceStat> stats = service.ownReport(user);
        model.addAttribute("student", student);
        model.addAttribute("stats", stats);
        model.addAttribute("overall", ReportCalc.overall(stats));
        model.addAttribute("own", true);
        return "student-attendance";
    }
}