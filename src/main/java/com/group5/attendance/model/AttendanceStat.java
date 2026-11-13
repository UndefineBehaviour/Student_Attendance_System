package com.group5.attendance.model;

import java.util.Locale;

/**
 * One student's attendance in one subject: how many closed lectures they were present in,
 * out of how many closed lectures were held. All percentage rules live here.
 */
public class AttendanceStat {

    /** Minimum attendance required, in percent. */
    public static final int THRESHOLD = 75;

    private int studentId;
    private String rollNo;
    private String studentCode;
    private String fullName;
    private int subjectId;
    private String subjectCode;
    private String subjectName;
    private int attended;   // lectures present
    private int total;      // lectures held (closed)

    public boolean isHasLectures() {
        return total > 0;
    }

    /** Percentage with one decimal, rounded DOWN so 74.96 never shows as 75.0. */
    public double getPercent() {
        return total == 0 ? 0 : (attended * 1000 / total) / 10.0;
    }

    public String getPercentText() {
        return total == 0 ? "-" : String.format(Locale.ROOT, "%.1f%%", getPercent());
    }

    /** True when attendance is below 75% (exact integer check, no rounding). */
    public boolean isBelowThreshold() {
        return total > 0 && attended * 100 < THRESHOLD * total;
    }

    /** How many lectures in a row the student must attend to reach 75% (0 if already there). */
    public int getNeeded() {
        int numerator = THRESHOLD * total - 100 * attended;
        if (numerator <= 0) {
            return 0;
        }
        int denominator = 100 - THRESHOLD;
        return (numerator + denominator - 1) / denominator;       // ceiling division
    }

    /** Colour class for the text: green 75+, amber 65 to 74.9, red below 65. */
    public String getTextClass() {
        if (total == 0) return "text-muted";
        if (!isBelowThreshold()) return "pct-good";
        return attended * 100 >= 65 * total ? "pct-warn" : "pct-low";
    }

    /** Colour class for the progress bar. */
    public String getBarClass() {
        if (total == 0) return "bg-secondary";
        if (!isBelowThreshold()) return "bg-success";
        return attended * 100 >= 65 * total ? "bg-warning" : "bg-danger";
    }

    public int getBarWidth() {
        return (int) getPercent();
    }

    public String getSubjectLabel() {
        return subjectCode + " - " + subjectName;
    }

    /** Roll number as a number, for sorting (0 if it is not a number). */
    public int getRollNumber() {
        try {
            return Integer.parseInt(rollNo.trim());
        } catch (NumberFormatException | NullPointerException e) {
            return 0;
        }
    }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }
    public String getRollNo() { return rollNo; }
    public void setRollNo(String rollNo) { this.rollNo = rollNo; }
    public String getStudentCode() { return studentCode; }
    public void setStudentCode(String studentCode) { this.studentCode = studentCode; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public int getSubjectId() { return subjectId; }
    public void setSubjectId(int subjectId) { this.subjectId = subjectId; }
    public String getSubjectCode() { return subjectCode; }
    public void setSubjectCode(String subjectCode) { this.subjectCode = subjectCode; }
    public String getSubjectName() { return subjectName; }
    public void setSubjectName(String subjectName) { this.subjectName = subjectName; }
    public int getAttended() { return attended; }
    public void setAttended(int attended) { this.attended = attended; }
    public int getTotal() { return total; }
    public void setTotal(int total) { this.total = total; }
}