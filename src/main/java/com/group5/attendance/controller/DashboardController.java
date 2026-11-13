package com.group5.attendance.controller;

import com.group5.attendance.model.AttendanceStat;
import com.group5.attendance.model.Subject;
import com.group5.attendance.model.User;
import com.group5.attendance.service.AttendanceService;
import com.group5.attendance.service.ReportService;
import com.group5.attendance.service.StudentService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

@Controller
public class DashboardController {

    private static final int SHOW = 5;                 // rows in the "Needs attention" panel

    private final StudentService students;
    private final AttendanceService attendance;
    private final ReportService reports;

    public DashboardController(StudentService students, AttendanceService attendance,
                               ReportService reports) {
        this.students = students;
        this.attendance = attendance;
        this.reports = reports;
    }

    @GetMapping("/dashboard")
    public String dashboard(@RequestParam(required = false) Integer subjectId,
                            HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user != null && "STUDENT".equals(user.getRole())) {
            return "redirect:/my-attendance";      // students go straight to their own attendance
        }

        // ----- The four numbers at the top (unchanged) -----
        model.addAttribute("totalStudents", students.countActive());
        model.addAttribute("totalSubjects", attendance.subjectsFor(user).size());
        model.addAttribute("todayMarked", attendance.byDate(user, LocalDate.now()).size());
        model.addAttribute("belowThreshold", reports.countBelowThreshold(user));

        // ----- "Needs attention" panel: one subject, lowest attendance first -----
        List<Subject> subjects = reports.subjectsFor(user);      // coordinator: all, teacher: only theirs
        List<String> classes = reports.classNames();
        model.addAttribute("subjects", subjects);

        if (!subjects.isEmpty() && !classes.isEmpty()) {
            int chosen = (subjectId != null) ? subjectId : subjects.get(0).getId();
            String className = classes.get(0);                   // only one class today (FYMCA-A)

            // lowest first; throws 403 if a teacher asks for a subject that is not theirs
            List<AttendanceStat> all = reports.classReport(user, chosen, className, true);
            List<AttendanceStat> below = all.stream().filter(AttendanceStat::isBelowThreshold).toList();

            model.addAttribute("subjectId", chosen);
            model.addAttribute("className", className);
            model.addAttribute("lectures", all.isEmpty() ? 0 : all.get(0).getTotal());
            model.addAttribute("defaulterCount", below.size());
            model.addAttribute("defaulters", below.stream().limit(SHOW).toList());
            model.addAttribute("shown", Math.min(SHOW, below.size()));
        }
        return "dashboard";
    }
}