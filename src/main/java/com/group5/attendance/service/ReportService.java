package com.group5.attendance.service;

import com.group5.attendance.dao.ReportDao;
import com.group5.attendance.dao.StudentDao;
import com.group5.attendance.model.AttendanceStat;
import com.group5.attendance.model.Student;
import com.group5.attendance.model.Subject;
import com.group5.attendance.model.User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.HashSet;

/** Percentage and defaulter reports. Teachers see only their own subjects; the coordinator sees all. */
@Service
public class ReportService {

    private final AttendanceService attendanceService;
    private final ReportDao dao;
    private final StudentDao studentDao;

    public ReportService(AttendanceService attendanceService, ReportDao dao, StudentDao studentDao) {
        this.attendanceService = attendanceService;
        this.dao = dao;
        this.studentDao = studentDao;
    }

    private boolean isCoordinator(User user) {
        return "COORDINATOR".equals(user.getRole());
    }

    public List<Subject> subjectsFor(User user) {
        return attendanceService.subjectsFor(user);
    }

    public List<String> classNames() {
        return attendanceService.classNames();
    }

    /** The subject ids this user may look at: the chosen one (if allowed) or all they are allowed. */
    private List<Integer> resolveSubjects(User user, Integer subjectId) {
        List<Integer> allowed = subjectsFor(user).stream().map(Subject::getId).toList();
        if (subjectId == null) {
            return allowed;
        }
        if (!allowed.contains(subjectId)) {
            throw new ForbiddenException();
        }
        return List.of(subjectId);
    }

    private void checkClass(String className) {
        if (!classNames().contains(className)) {
            throw new IllegalArgumentException("That class does not exist");
        }
    }

        /** Students (counted once) who are below 75% in at least one subject the user may see. */
    public int countBelowThreshold(User user) {
        Set<Integer> ids = new HashSet<>();
        for (String cls : classNames()) {
            defaulters(user, null, cls).values()
                    .forEach(list -> list.forEach(s -> ids.add(s.getStudentId())));
        }
        return ids.size();
    }

    /** Percentage of every student of a class in one subject. */
    public List<AttendanceStat> classReport(User user, int subjectId, String className, boolean lowestFirst) {
        checkClass(className);
        List<AttendanceStat> stats = dao.classStats(className, resolveSubjects(user, subjectId));
        return lowestFirst ? ReportCalc.sortByPercent(stats) : stats;
    }

    /** Defaulters of a class, grouped by subject (subjectId null = every subject the user may see). */
    public Map<String, List<AttendanceStat>> defaulters(User user, Integer subjectId, String className) {
        checkClass(className);
        return ReportCalc.defaulters(dao.classStats(className, resolveSubjects(user, subjectId)));
    }

    public Student student(int studentId) {
        return studentDao.findById(studentId).orElseThrow(() -> new NotFoundException("Student not found"));
    }

    /** Subject-wise attendance of one student, as seen by staff (teachers: their subjects only). */
    public List<AttendanceStat> studentReport(User user, int studentId) {
        List<AttendanceStat> stats = dao.studentStats(studentId);
        if (isCoordinator(user)) {
            return stats;
        }
        Set<Integer> allowed = subjectsFor(user).stream().map(Subject::getId).collect(Collectors.toSet());
        return stats.stream().filter(s -> allowed.contains(s.getSubjectId())).toList();
    }

    /** The student record that belongs to the logged-in student. */
    public int studentIdFor(User user) {
        return dao.studentIdForUser(user.getId()).orElseThrow(ForbiddenException::new);
    }

    public List<AttendanceStat> ownReport(User user) {
        return dao.studentStats(studentIdFor(user));
    }
}