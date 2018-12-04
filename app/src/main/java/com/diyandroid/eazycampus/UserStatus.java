package com.diyandroid.eazycampus;

import android.support.annotation.Keep;

@Keep
public class UserStatus {
    public Boolean paid;

    public UserStatus() {
        //firebase
    }

    public Boolean hasPaid() {
        return paid;
    }

}