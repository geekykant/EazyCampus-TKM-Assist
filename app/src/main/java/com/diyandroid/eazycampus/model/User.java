package com.diyandroid.eazycampus.model;

public class User {
    private int username;
    private String password;

    private String login_name, rollno;

    public User(int username, String password) {
        this.username = username;
        this.password = password;
    }

    public User(int username, String password, String login_name, String rollno) {
        this.username = username;
        this.password = password;
        this.login_name = login_name;
        this.rollno = rollno;
    }

    public int getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getLogin_name() {
        return login_name;
    }

    public String getRoll_no() {
        return rollno;
    }
}
