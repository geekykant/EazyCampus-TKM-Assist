package com.diyandroid.eazycampus.service;

import com.diyandroid.eazycampus.model.SubjectAttendance;
import com.diyandroid.eazycampus.model.User;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class APIResponse {
    private String message;
    private User user;

    @SerializedName("data")
    private ArrayList<SubjectAttendance> attendance_data;

    public String getMessage() {
        return message;
    }

    public User getUser() {
        return user;
    }

    public ArrayList<SubjectAttendance> getList() {
        return attendance_data;
    }
}
