package com.diyandroid.eazycampus;

import android.support.annotation.Keep;

@Keep
public class Story {
    private String Title, ImageURL, IntentURL;
    private boolean is_present;

    public void Story() {
    }

    public String getTitle() {
        return Title;
    }

    public String getImageURL() {
        return ImageURL;
    }

    public String getIntentURL() {
        return IntentURL;
    }

    public boolean isIs_present() {
        return is_present;
    }
}