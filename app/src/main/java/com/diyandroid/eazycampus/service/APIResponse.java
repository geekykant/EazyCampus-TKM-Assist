package com.diyandroid.eazycampus.service;

import com.diyandroid.eazycampus.model.SubjectAttendance;
import com.diyandroid.eazycampus.model.User;

import java.util.ArrayList;

public class APIResponse {
    private String message;

    private User user;
    private ArrayList<SubjectAttendance> list;

    public String getMessage() {
        return message;
    }

    public User getUser() {
        return user;
    }

    public ArrayList<SubjectAttendance> getList() {
        return list;
    }
}
