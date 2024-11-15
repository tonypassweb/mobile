package com.example.yoga_dodanhtuyen.Instance;

import java.io.Serializable;
import java.util.Date;

public class Instance implements Serializable {
    private String id;
    private String yogaId = "";
    private String teacherName;
    private Date date;
    private String comment;

    // Constructors
    public Instance() {}

    public Instance(String id, String teacherName, String comment, Date date, String yogaId) {
        this.id = id;
        this.yogaId = yogaId;
        this.teacherName = teacherName;
        this.date = date;
        this.comment = comment;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTeacherName() {
        return teacherName;
    }

    public void setTeacherName(String teacherName) {
        this.teacherName = teacherName;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    // Override toString for easier debugging
    @Override
    public String toString() {
        return "Instance{" +
                "id=" + id +
                ", yogaId=" + yogaId +
                ", teacherName='" + teacherName + '\'' +
                ", date=" + date +
                ", comment='" + comment + '\'' +
                '}';
    }

    // Optional validation method to check if the date aligns with yoga schedule rules
    public boolean isValidDateForYogaDay(int expectedDayOfWeek) {
        // Assuming expectedDayOfWeek uses Calendar.DAY_OF_WEEK constants
        if (date == null) return false;
        java.util.Calendar cal = java.util.Calendar.getInstance();
        cal.setTime(date);
        int dayOfWeek = cal.get(java.util.Calendar.DAY_OF_WEEK);
        return dayOfWeek == expectedDayOfWeek;
    }
}

