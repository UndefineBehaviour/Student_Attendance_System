package com.group5.attendance.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** One lecture. The live QR (later) and the manual marking page both belong to a session. */
public class AttendanceSession {
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd MMM yyyy");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("hh:mm a");

    private int id;
    private int subjectId;
    private String subjectCode;
    private String subjectName;
    private int teacherId;
    private String teacherName;
    private String className;
    private LocalDate sessionDate;
    private LocalDateTime startTime;
    private LocalDateTime endTime;      // null while the lecture is open
    private String status;              // OPEN or CLOSED
    private int presentCount;
    private int classSize;
    private boolean deletable;          // set by the service for the logged-in user

    public boolean isOpen() { return "OPEN".equals(status); }
    public String getDateText() { return sessionDate.format(DATE_FMT); }
    public String getStartText() { return startTime.format(TIME_FMT); }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getSubjectId() { return subjectId; }
    public void setSubjectId(int subjectId) { this.subjectId = subjectId; }
    public String getSubjectCode() { return subjectCode; }
    public void setSubjectCode(String subjectCode) { this.subjectCode = subjectCode; }
    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }
    public int getTeacherId() { return teacherId; }
    public void setTeacherId(int teacherId) { this.teacherId = teacherId; }
    public String getTeacherName() { return teacherName; }
    public void setTeacherName(String teacherName) { this.teacherName = teacherName; }
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }
    public LocalDate getSessionDate() { return sessionDate; }
    public void setSessionDate(LocalDate sessionDate) { this.sessionDate = sessionDate; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public int getPresentCount() { return presentCount; }
    public void setPresentCount(int presentCount) { this.presentCount = presentCount; }
    public int getClassSize() { return classSize; }
    public void setClassSize(int classSize) { this.classSize = classSize; }
    public boolean isDeletable() { return deletable; }
    public void setDeletable(boolean deletable) { this.deletable = deletable; }
}