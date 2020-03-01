package com.diyandroid.eazycampus.model;

import androidx.annotation.Keep;

import com.google.gson.annotations.SerializedName;

@Keep
public class SubjectAttendance {
    @SerializedName("subject_name")
    private String subjectName;
    @SerializedName("total_classes")
    private String totalClasses;
    @SerializedName("total_attended")
    private String totalAttended;
    @SerializedName("attendance_percent")
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