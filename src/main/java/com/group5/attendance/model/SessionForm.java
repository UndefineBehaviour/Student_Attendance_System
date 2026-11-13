package com.group5.attendance.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** Data from the "Start a lecture" form. */
public class SessionForm {

    @NotNull(message = "Choose a subject")
    private Integer subjectId;

    @NotBlank(message = "Choose a class")
    private String className;

    @NotBlank(message = "Choose a date")
    private String date;          // yyyy-MM-dd, from <input type="date">

    public Integer getSubjectId() { return subjectId; }
    public void setSubjectId(Integer subjectId) { this.subjectId = subjectId; }
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
}