package com.diyandroid.eazycampus;

public class SubjectAttendance {
    private String subjectName;
    private String totalClasses;
    private String totalAttended;
    private String attendancePercent;

    public SubjectAttendance(String subjectName, String totalClasses, String totalAttended, String attendancePercent) {
        this.subjectName = subjectName;
        this.totalClasses = totalClasses;
        this.totalAttended = totalAttended;
        this.attendancePercent = attendancePercent;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public String getTotalClasses() {
        return totalClasses;
    }

    public String getTotalAttended() {
        return totalAttended;
    }

    public String getAttendancePercent() {
        return attendancePercent;
    }
}