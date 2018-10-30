package com.diyandroid.eazycampus;

import android.support.annotation.Keep;

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
