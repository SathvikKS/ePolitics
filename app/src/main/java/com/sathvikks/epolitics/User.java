package com.sathvikks.epolitics;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;

import java.net.URL;

public class User {
    public String accType;
    public String email;
    public String gender;
    public String name;
    public String phone;
    public String region;
    public URL profilePicUrl;
    public Bitmap profilePic;

    public User(String email2, String phone2, String name2, String accType2, String gender2, String region2) {
        email = email2;
        phone = phone2;
        name = name2;
        accType = accType2;
        gender = gender2;
        region = region2;
        profilePicUrl = null;
        profilePic = null;
    }

    @NonNull
    @Override
    public String toString() {
        return "User{" +
                "accType='" + accType + '\'' +
                ", email='" + email + '\'' +
                ", gender='" + gender + '\'' +
                ", name='" + name + '\'' +
                ", phone='" + phone + '\'' +
                ", region='" + region + '\'' +
                ", profilePicUrl=" + profilePicUrl +
                '}';
    }
}
