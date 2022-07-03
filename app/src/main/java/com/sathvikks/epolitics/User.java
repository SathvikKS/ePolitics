package com.sathvikks.epolitics;

import android.graphics.Bitmap;

import com.google.firebase.database.Exclude;

public class User {
    private String accType;
    private String email;
    private String gender;
    private String name;
    private String phone;
    private String region;
    private Bitmap profilePic;

    public User(String email2, String phone2, String name2, String accType2, String gender2, String region2) {
        email = email2;
        phone = phone2;
        name = name2;
        accType = accType2;
        gender = gender2;
        region = region2;
        profilePic = null;
    }

    public String replaceEmail() {
        return email.replace('.', ',');
    }

    public long getPhone() {
        return Long.parseLong(this.phone);
    }
}
