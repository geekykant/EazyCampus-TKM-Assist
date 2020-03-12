package com.diyandroid.eazycampus.service;

import androidx.annotation.Keep;

import com.diyandroid.eazycampus.model.AssignmentMarks;
import com.diyandroid.eazycampus.model.SubjectAttendance;
import com.diyandroid.eazycampus.model.User;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

@Keep
public class APIResponse {
    private String message;
    private User user;

    @SerializedName("attendance_data")
    private ArrayList<SubjectAttendance> attendance_data;

    @SerializedName("assignment_data")
    private ArrayList<AssignmentMarks> assignment_data;

    public String getMessage() {
        return message;
    }

    public User getUser() {
        return user;
    }

    public ArrayList<SubjectAttendance> getAttendanceList() {
        return attendance_data;
    }

    public ArrayList<AssignmentMarks> getAssignments() {
        return assignment_data;
    }
}
