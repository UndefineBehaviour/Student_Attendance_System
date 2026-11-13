package com.group5.attendance.model;

public class Subject {
    private int id;
    private String code;   // e.g. MCA12
    private String name;   // e.g. Advanced Java

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}