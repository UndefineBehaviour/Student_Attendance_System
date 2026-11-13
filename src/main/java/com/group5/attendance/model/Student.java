package com.group5.attendance.model;

/** One student = one row in `student` joined with its login row in `users`. */
public class Student {
    private int id;              // student.id
    private int userId;          // users.id
    private String studentCode;  // e.g. 26MCA01
    private String rollNo;
    private String className;    // e.g. FYMCA-A
    private String username;     // what the student types to log in
    private String fullName;
    private boolean active;      // false = deactivated ("deleted"), cannot log in

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getStudentCode() { return studentCode; }
    public void setStudentCode(String studentCode) { this.studentCode = studentCode; }
    public String getRollNo() { return rollNo; }
    public void setRollNo(String rollNo) { this.rollNo = rollNo; }
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}