package com.diyandroid.eazycampus.model;

import androidx.annotation.Keep;

import java.util.ArrayList;

@Keep
public class Marks {
    ArrayList<String> marksList;

    public Marks(ArrayList<String> marksList) {
        this.marksList = marksList;
    }

    public ArrayList<String> getMarksList() {
        return marksList;
    }

    public void setMarksList(ArrayList<String> marksList) {
        this.marksList = marksList;
    }
}
