package com.group5.attendance.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Data typed into the Add / Edit student form, with validation rules. */
public class StudentForm {

    @NotBlank(message = "Full name is required")
    @Size(max = 100, message = "Full name can be at most 100 characters")
    private String fullName;

    @NotBlank(message = "Username is required")
    @Size(max = 100, message = "Username can be at most 100 characters")
    private String username;

    @NotBlank(message = "Student ID is required")
    @Size(max = 20, message = "Student ID can be at most 20 characters")
    @Pattern(regexp = "[A-Za-z0-9_-]*", message = "Student ID may contain only letters, digits, - and _")
    private String studentCode;

    @NotBlank(message = "Roll number is required")
    @Pattern(regexp = "[0-9]{0,3}", message = "Roll number must be 1 to 3 digits")
    private String rollNo;

    @NotBlank(message = "Class is required")
    @Size(max = 30, message = "Class can be at most 30 characters")
    private String className;

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getStudentCode() { return studentCode; }
    public void setStudentCode(String studentCode) { this.studentCode = studentCode; }
    public String getRollNo() { return rollNo; }
    public void setRollNo(String rollNo) { this.rollNo = rollNo; }
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }
}