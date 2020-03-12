package com.diyandroid.eazycampus.model;

import androidx.annotation.Keep;

@Keep
public class AssignmentMarks {
    private String assignment_no, max_mark, subjects, marks;

    public String getAssignment_no() {
        return assignment_no;
    }

    public String getMax_mark() {
        return max_mark;
    }

    public String getSubjects() {
        return subjects;
    }

    public String getMarks() {
        return marks;
    }
}
