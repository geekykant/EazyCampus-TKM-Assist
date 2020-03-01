package com.diyandroid.eazycampus.model;

import androidx.annotation.Keep;

@Keep
public class Contact {
    public String name;
    public String image;
    public String mobile_no;
    public String office_phone;
    public String email;
    public String designation;
    public String department;
    public String address;

    public Contact() {
    }

    public String getName() {
        return name;
    }

    public String getDesignation() {
        return designation;
    }

    public String getImage() {
        return image;

    }

    public String getPhone() {
        return mobile_no;
    }

    public String getDepartment() {
        return department;
    }

    public String getOffice_phone() {
        return office_phone;
    }

    public String getEmail() {
        return email;
    }

    public String getAddress() {
        return address;
    }
}
