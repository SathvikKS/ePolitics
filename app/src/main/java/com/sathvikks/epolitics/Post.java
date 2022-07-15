package com.sathvikks.epolitics;

import java.util.Calendar;

public class Post {
    public String postDescription, postDateTime, postUserName, postImage, postUserEmail, postId;

    public Post() {

    }

    public Post(String postDescription, String postUserName, String postUserEmail, String postId) {
        this.postDescription = postDescription;
        this.postDateTime = Calendar.getInstance().getTime().toString();
        this.postUserName = postUserName;
        this.postUserEmail = postUserEmail;
        this.postId = postId;
    }

    public Post(String postDescription, String postUserName, String postUserEmail, String postImage, String postId) {
        this.postDescription = postDescription;
        this.postDateTime = Calendar.getInstance().getTime().toString();
        this.postUserName = postUserName;
        this.postImage = postImage;
        this.postUserEmail = postUserEmail;
        this.postId = postId;
    }

    public Post(String postDescription, String postDateTime, String postUserName, String postImage, String postUserEmail, String postId) {
        this.postDescription = postDescription;
        this.postDateTime = postDateTime;
        this.postUserName = postUserName;
        this.postImage = postImage;
        this.postUserEmail = postUserEmail;
        this.postId = postId;
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


    public String getPostId() { return postId; }

    public void setPostId(String postId) { this.postId = postId; }

    public String getPostUserEmail() {
        return postUserEmail;
    }

    public void setPostUserEmail(String postUserEmail) {
        this.postUserEmail = postUserEmail;
    }

    public String getPostDescription() {
        return postDescription;
    }

    public void setPostDescription(String postDescription) { this.postDescription = postDescription; }

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
