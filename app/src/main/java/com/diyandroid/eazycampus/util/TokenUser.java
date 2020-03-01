package com.diyandroid.eazycampus.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import com.diyandroid.eazycampus.model.User;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;

public class TokenUser {

    private static final String TAG = TokenUser.class.getSimpleName();
    private SharedPreferences pref;

    public TokenUser(@NonNull Context context) {
        pref = getDefaultSharedPreferences(context);
    }

    public void storeToken(User user) {
        pref.edit().clear().apply();
        pref.edit()
                .putInt("username", user.getUsername())
                .putString("password", user.getPassword())
                .putString("roll_no", user.getBranch_roll_no()).apply();

        Log.i(TAG, "storeToken: " + user.getUsername());
        Log.i(TAG, "storeToken: " + user.getPassword());
    }

    public void removeTokens() {
        pref.edit().clear().apply();
    }

    public User getUser() {
        int username = getPrefUsename();
        String password = getPrefPassword();
        String login_name = getPrefLoginName();
        String roll_no = getPrefRollNo();

        Log.i(TAG, "getUser: " + username);
        Log.i(TAG, "getUser: " + password);

        if (username == -1 || TextUtils.isEmpty(password) || TextUtils.isEmpty(login_name)
                || TextUtils.isEmpty(roll_no)) {
            return null;
        }

        return new User(username, password, login_name, roll_no);
    }

    public int getPrefUsename() {
        return pref.getInt("username", -1);
    }

    public String getPrefPassword() {
        return pref.getString("password", null);
    }

    public String getPrefRollNo() {
        return pref.getString("rollno", null);
    }

    public String getPrefLoginName() {
        return pref.getString("login_name", null);
    }

    public void setFirstTime(boolean bool) {
        pref.edit().putBoolean("FIRST_RUN", bool).apply();
    }

    public Boolean isFirstTime() {
        return pref.getBoolean("FIRST_RUN", true);
    }

}
