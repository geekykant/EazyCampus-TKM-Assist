package com.diyandroid.eazycampus.model;

public class User {
    private int username;
    private String password;

    private String login_name, branch_roll_no;

    public User(int username, String password) {
        this.username = username;
        this.password = password;
    }

    public User(int username, String password, String login_name, String branch_roll_no) {
        this.username = username;
        this.password = password;
        this.login_name = login_name;
        this.branch_roll_no = branch_roll_no;
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

    public String getBranch_roll_no() {
        return branch_roll_no;
    }
}
