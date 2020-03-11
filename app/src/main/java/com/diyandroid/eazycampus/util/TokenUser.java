package com.diyandroid.eazycampus.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import com.diyandroid.eazycampus.model.User;

public class TokenUser {

    private static final String TAG = TokenUser.class.getSimpleName();
    private SharedPreferences pref;

    public TokenUser(@NonNull Context context) {
        pref = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public void storeToken(User user, int username, String password) {
        pref.edit().clear().apply();
        pref.edit()
                .putInt("int_username", username)
                .putString("nice_password", password)
                .putString("login_name", user.getLogin_name())
                .putString("rollno", user.getRoll_no()).apply();

        Log.i(TAG, "storeToken: " + user.getUsername());
        Log.i(TAG, "storeToken: " + user.getPassword());
        Log.i(TAG, "storeToken: " + user.getLogin_name());
    }

    public void removeTokens() {
        pref.edit().clear().apply();
    }

    public User getUser() {
        int username = getPrefUsername();
        String password = getPrefPassword();
        String login_name = getPrefLoginName();
        String roll_no = getPrefRollNo();

        Log.i(TAG, "getUser: " + username);
        Log.i(TAG, "getUser: " + password);
        Log.i(TAG, "getUser: " + login_name);
        Log.i(TAG, "getUser: " + roll_no);

        if (username == -1 || TextUtils.isEmpty(password) || TextUtils.isEmpty(login_name)
                || TextUtils.isEmpty(roll_no)) {
            return null;
        }

        return new User(username, password, login_name, roll_no);
    }

    private int getPrefUsername() {
        return pref.getInt("int_username", -1);
    }

    private String getPrefPassword() {
        return pref.getString("nice_password", null);
    }

    public String getPrefRollNo() {
        return pref.getString("rollno", null);
    }

    public String getPrefLoginName() {
        return pref.getString("login_name", null);
    }

    public void setFirstTime(boolean bool) {
        pref.edit().putBoolean("YES_FIRST_RUN", bool).apply();
    }

    public Boolean isFirstTime() {
        return pref.getBoolean("YES_FIRST_RUN", true);
    }

}
