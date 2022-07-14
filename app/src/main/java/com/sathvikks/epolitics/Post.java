package com.sathvikks.epolitics;

import java.net.URL;
import java.util.Calendar;

public class Post {
    public String postTitle, postDescription, postDateTime, postUserName, postUserPic, postImage;

    public Post() {

    }

    public Post(String postTitle, String postDescription, String postUserName, String postUserPic) {
        this.postTitle = postTitle;
        this.postDescription = postDescription;
        this.postDateTime = Calendar.getInstance().getTime().toString();
        this.postUserName = postUserName;
        this.postUserPic = postUserPic;
    }
    public Post(String postTitle, String postDescription, String postUserName) {
        this.postTitle = postTitle;
        this.postDescription = postDescription;
        this.postDateTime = Calendar.getInstance().getTime().toString();
        this.postUserName = postUserName;
        this.postUserPic = null;
    }

    public Post(String postTitle, String postDescription, String postDateTime, String postUserName, String postUserPic, String postImage) {
        this.postTitle = postTitle;
        this.postDescription = postDescription;
        this.postDateTime = postDateTime;
        this.postUserName = postUserName;
        this.postUserPic = postUserPic;
        this.postImage = postImage;
    }

    @Override
    public String toString() {
        return "Post{" +
                "postTitle='" + postTitle + '\'' +
                ", postDescription='" + postDescription + '\'' +
                ", postDateTime='" + postDateTime + '\'' +
                ", postUserName='" + postUserName + '\'' +
                ", postUserPic='" + postUserPic + '\'' +
                ", postImage='" + postImage + '\'' +
                '}';
    }

    public String getPostTitle() {
        return postTitle;
    }

    public void setPostTitle(String postTitle) {
        this.postTitle = postTitle;
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
