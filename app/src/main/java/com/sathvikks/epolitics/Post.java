package com.sathvikks.epolitics;

import java.net.URL;
import java.util.Calendar;

public class Post {
    public String postDescription, postDateTime, postUserName, postUserPic, postImage, postUserEmail;

    public Post() {

    }

    public Post(String postDescription, String postUserName, String postUserPic, String postUserEmail) {
        this.postDescription = postDescription;
        this.postDateTime = Calendar.getInstance().getTime().toString();
        this.postUserName = postUserName;
        this.postUserPic = postUserPic;
        this.postUserEmail = postUserEmail;
    }

    public Post(String postDescription, String postUserName, String postUserPic, String postImage, String postUserEmail) {
        this.postDescription = postDescription;
        this.postDateTime = Calendar.getInstance().getTime().toString();
        this.postUserName = postUserName;
        this.postUserPic = postUserPic;
        this.postImage = postImage;
        this.postUserEmail = postUserEmail;
    }

    public Post(String postDescription, String postUserName, String postUserEmail) {
        this.postDescription = postDescription;
        this.postDateTime = Calendar.getInstance().getTime().toString();
        this.postUserName = postUserName;
        this.postUserPic = null;
        this.postUserEmail = postUserEmail;
    }

    public Post(String postDescription, String postDateTime, String postUserName, String postUserPic, String postImage, String postUserEmail) {
        this.postDescription = postDescription;
        this.postDateTime = postDateTime;
        this.postUserName = postUserName;
        this.postUserPic = postUserPic;
        this.postImage = postImage;
        this.postUserEmail = postUserEmail;
    }

    @Override
    public String toString() {
        return "Post{" +
                ", postDescription='" + postDescription + '\'' +
                ", postDateTime='" + postDateTime + '\'' +
                ", postUserName='" + postUserName + '\'' +
                ", postUserPic='" + postUserPic + '\'' +
                ", postImage='" + postImage + '\'' +
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

    public String getPostUserPic() {
        return postUserPic;
    }

    public void setPostUserPic(String postUserPic) {
        this.postUserPic = postUserPic;
    }

    public String getPostImage() {
        return postImage;
    }

    public void setPostImage(String postImage) {
        this.postImage = postImage;
    }
}
