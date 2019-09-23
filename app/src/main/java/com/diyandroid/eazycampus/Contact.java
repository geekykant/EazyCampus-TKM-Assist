package com.diyandroid.eazycampus;

import androidx.annotation.Keep;

@Keep
public class Contact {
    String name;
    String image;
    String mobile_no;
    String office_phone;
    String email;
    String designation;
    String department;
    String address;

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
