package com.sathvikks.epolitics;

import java.net.URL;
import java.util.Calendar;

public class Post {
    public String postText, postDescription, postAuthorName, postAuthorEmail;
    public URL postImageUrl;
    public String postDateTime;

    public Post(String postText, String postDescription, String postAuthorName, String postAuthorEmail) {
        this.postText = postText;
        this.postDescription = postDescription;
        this.postAuthorName = postAuthorName;
        this.postImageUrl = null;
        this.postAuthorEmail = postAuthorEmail;
        this.postDateTime = Calendar.getInstance().getTime().toString();
    }

    @Override
    public String toString() {
        return "Post{" +
                "postText='" + postText + '\'' +
                ", postDescription='" + postDescription + '\'' +
                ", postAuthorName='" + postAuthorName + '\'' +
                ", postAuthorEmail='" + postAuthorEmail + '\'' +
                ", postImageUrl=" + postImageUrl +
                ", postDateTime='" + postDateTime + '\'' +
                '}';
    }
}
