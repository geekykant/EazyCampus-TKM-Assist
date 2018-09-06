package com.diyandroid.eazycampus;

public class Notification {
    String title, message;
    String link1, link2, link3, link1_title, link2_title, link3_title;
    String date, timestamp, month_year;

    public Notification() {
        //required for firebase
    }

    public Notification(String title, String message, String link1, String link2, String link3, String link1_title, String link2_title, String link3_title, String date, String timestamp, String month_year) {
        this.title = title;
        this.message = message;
        this.link1 = link1;
        this.link2 = link2;
        this.link3 = link3;
        this.link1_title = link1_title;
        this.link2_title = link2_title;
        this.link3_title = link3_title;
        this.date = date;
        this.timestamp = timestamp;
        this.month_year = month_year;
    }

    public String getMonth_year() {
        return month_year;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public String getLink1() {
        return link1;
    }

    public String getLink2() {
        return link2;
    }

    public String getLink3() {
        return link3;
    }

    public String getLink1_title() {
        return link1_title;
    }

    public String getLink2_title() {
        return link2_title;
    }

    public String getLink3_title() {
        return link3_title;
    }

    public String getDate() {
        return date;
    }

    public String getTimestamp() {
        return timestamp;
    }

}
