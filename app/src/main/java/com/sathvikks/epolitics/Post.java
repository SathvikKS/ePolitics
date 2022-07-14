package com.sathvikks.epolitics;

import java.net.URL;
import java.util.Calendar;

public class Post {
    public String postDescription, postDateTime, postUserName, postImage, postUserEmail;

    public Post() {

    }

    public Post(String postDescription, String postUserName, String postUserEmail) {
        this.postDescription = postDescription;
        this.postDateTime = Calendar.getInstance().getTime().toString();
        this.postUserName = postUserName;
        this.postUserEmail = postUserEmail;
    }

    public Post(String postDescription, String postUserName, String postUserEmail, String postImage) {
        this.postDescription = postDescription;
        this.postDateTime = Calendar.getInstance().getTime().toString();
        this.postUserName = postUserName;
        this.postImage = postImage;
        this.postUserEmail = postUserEmail;
    }

    public Post(String postDescription, String postDateTime, String postUserName, String postImage, String postUserEmail) {
        this.postDescription = postDescription;
        this.postDateTime = postDateTime;
        this.postUserName = postUserName;
        this.postImage = postImage;
        this.postUserEmail = postUserEmail;
    }

    @Override
    public String toString() {
        return "Post{" +
                "postDescription='" + postDescription + '\'' +
                ", postDateTime='" + postDateTime + '\'' +
                ", postUserName='" + postUserName + '\'' +
                ", postImage='" + postImage + '\'' +
                ", postUserEmail='" + postUserEmail + '\'' +
                '}';
    }

    public String getPostUserEmail() {
        return postUserEmail;
    }

    public void setPostUserEmail(String postUserEmail) {
        this.postUserEmail = postUserEmail;
    }

    public String getPostDescription() {
        return postDescription;
    }

    public void setPostDescription(String postDescription) {
        this.postDescription = postDescription;
    }

    public String getPostDateTime() {
        return postDateTime;
    }

    public void setPostDateTime(String postDateTime) {
        this.postDateTime = postDateTime;
    }

    public String getPostUserName() {
        return postUserName;
    }

    public void setPostUserName(String postUserName) {
        this.postUserName = postUserName;
    }

    public String getPostImage() {
        return postImage;
    }

    public void setPostImage(String postImage) {
        this.postImage = postImage;
    }
}
