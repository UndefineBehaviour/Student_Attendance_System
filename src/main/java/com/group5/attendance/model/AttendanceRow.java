package com.group5.attendance.model;

/** One line of the marking page: a student of the class and their mark for this lecture (if any). */
public class AttendanceRow {
    private int studentId;
    private String rollNo;
    private String studentCode;
    private String fullName;
    private String status;   // PRESENT, ABSENT, or null if not marked yet
    private String method;   // QR, MANUAL, IMPORT, or null

    public boolean isPresent() { return "PRESENT".equals(status); }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }
    public String getRollNo() { return rollNo; }
    public void setRollNo(String rollNo) { this.rollNo = rollNo; }
    public String getStudentCode() { return studentCode; }
    public void setStudentCode(String studentCode) { this.studentCode = studentCode; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }
}