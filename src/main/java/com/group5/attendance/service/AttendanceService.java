package com.group5.attendance.service;


import com.group5.attendance.dao.AttendanceDao;
import com.group5.attendance.dao.SessionDao;
import com.group5.attendance.dao.SubjectDao;
import com.group5.attendance.model.AttendanceRow;
import com.group5.attendance.model.AttendanceSession;
import com.group5.attendance.model.SessionForm;
import com.group5.attendance.model.Subject;
import com.group5.attendance.model.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.group5.attendance.dao.AuditDao;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/** Rules for lectures and manual marking. */
@Service
public class AttendanceService {

    private final SubjectDao subjects;
    private final SessionDao sessions;
    private final AttendanceDao attendance;
    private final AuditDao audit;

    public AttendanceService(SubjectDao subjects, SessionDao sessions, AttendanceDao attendance, AuditDao audit){
        this.subjects = subjects;
        this.sessions = sessions;
        this.attendance = attendance;
        this.audit = audit;
    }

    private boolean isCoordinator(User user) {
        return "COORDINATOR".equals(user.getRole());
    }

    /** Coordinator: every subject. Teacher: only the subjects linked to them. */
    public List<Subject> subjectsFor(User user) {
        return isCoordinator(user) ? subjects.findAll() : subjects.findByTeacher(user.getId());
    }

    public List<String> classNames() {
        return sessions.classNames();
    }

    public List<AttendanceSession> recent(User user) {
         List<AttendanceSession> list = sessions.recent(isCoordinator(user) ? 0 : user.getId());
        list.forEach(l -> l.setDeletable(canDelete(user, l)));      // tells the page which rows get a Delete button
        return list;
    }

    

    /** Starts a lecture (or returns the id of the one already open). */
    @Transactional
    public int start(User user, SessionForm form) {
        LocalDate date;
        try {
            date = LocalDate.parse(form.getDate().trim());
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("The date is not valid");
        }
        if (date.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("The date cannot be in the future");
        }

        int subjectId = form.getSubjectId();
        boolean allowed = subjectsFor(user).stream().anyMatch(s -> s.getId() == subjectId);
        if (!allowed) {
            throw new ForbiddenException();            // a teacher chose a subject that is not theirs
        }

        String className = form.getClassName().trim();
        if (!sessions.classNames().contains(className)) {
            throw new IllegalArgumentException("That class does not exist");
        }

        Optional<AttendanceSession> open = sessions.findOpen(subjectId, className, date, user.getId());
        if (open.isPresent()) {
            return open.get().getId();
        }
        return sessions.insert(subjectId, user.getId(), className, date, LocalDateTime.of(date, LocalTime.now()));
    }

    /** Loads a lecture if it exists and this user may use it (owner teacher or coordinator). */
    public AttendanceSession load(User user, int id) {
        AttendanceSession lecture = sessions.findById(id)
                .orElseThrow(() -> new NotFoundException("Lecture not found"));
        if (!isCoordinator(user) && lecture.getTeacherId() != user.getId()) {
            throw new ForbiddenException();
        }
        return lecture;
    }

    public List<AttendanceRow> rows(AttendanceSession lecture) {
        return attendance.roster(lecture.getId(), lecture.getClassName());
    }

    /** Open lectures can be edited by their teacher; closed ones only by the coordinator. */
    public boolean canEdit(User user, AttendanceSession lecture) {
        return lecture.isOpen() || isCoordinator(user);
    }

    public boolean canDelete(User user, AttendanceSession lecture) {
        return isCoordinator(user) || (lecture.isOpen() && lecture.getTeacherId() == user.getId());
    }

    /** Deletes a lecture with all its marks and writes the audit log. */
    @Transactional
    public void delete(User user, int sessionId) {
        AttendanceSession lecture = load(user, sessionId);
        if (!canDelete(user, lecture)) {
            throw new ForbiddenException();
        }
        String summary = lecture.getSubjectCode() + " " + lecture.getClassName() + ", " + lecture.getDateText()
                + ", " + lecture.getStatus() + ", " + lecture.getPresentCount() + "/" + lecture.getClassSize()
                + " present, teacher " + lecture.getTeacherName();
        attendance.deleteBySession(sessionId);
        sessions.delete(sessionId);
        audit.log(user.getId(), "DELETE_LECTURE", "attendance_session", sessionId, summary, null, "Deleted from the lecture list");
    }

    /** Saves the ticked students as PRESENT and everyone else as ABSENT (only rows that changed). */
    @Transactional
    public void save(User user, int sessionId, Collection<Integer> presentIds) {
        AttendanceSession lecture = load(user, sessionId);
        if (!canEdit(user, lecture)) {
            throw new ForbiddenException();
        }
        Set<Integer> present = presentIds == null ? Set.of() : new HashSet<>(presentIds);
        List<AttendanceRow> changes = rows(lecture).stream()
                .filter(r -> !(present.contains(r.getStudentId()) ? "PRESENT" : "ABSENT").equals(r.getStatus()))
                .toList();
        if (!changes.isEmpty()) {
            attendance.upsertManual(sessionId, user.getId(), changes, present);
        }
    }

        /** Lectures on one date. A teacher sees only their own; the coordinator sees all. */
    public List<AttendanceSession> byDate(User user, LocalDate date) {
        return sessions.findByDate(date, isCoordinator(user) ? 0 : user.getId());
    }

    /** Closes the lecture; students without a mark become ABSENT. */
    @Transactional
    public void close(User user, int sessionId) {
        AttendanceSession lecture = load(user, sessionId);
        if (!lecture.isOpen()) {
            return;
        }
        attendance.markRemainingAbsent(sessionId, user.getId(), lecture.getClassName());
        sessions.close(sessionId);
    }
}