package com.sathvikks.epolitics;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class PostAdapter extends ArrayAdapter<Post> {

    private Activity context;
    private String postUserNameText, postTimeText, postTileText, postDescriptionText;
    private Bitmap postUserPicBitmap, postImageBitmap;
    private TextView postUserName, postTime, postTitle, postDescription;
    private ImageView postUserPic, postImage;

//    public PostAdapter(Activity context, String postUserNameText, String postTimeText, String postTileText, String postDescriptionText, Bitmap postUserPicBitmap, Bitmap postImageBitmap) {
//        super(context, R.layout.post_layout);
//        this.context=context;
//        this.postUserNameText = postUserNameText;
//        this.postTimeText = postTimeText;
//        this.postTileText = postTileText;
//        this.postDescriptionText = postDescriptionText;
//        this.postUserPicBitmap = postUserPicBitmap;
//        this.postImageBitmap = postImageBitmap;
//
//    }
    public PostAdapter(Activity context, ArrayList<Post> posts) {
        super(context, R.layout.post_layout, posts);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Post post = getItem(position);
        if (convertView == null)
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.post_layout,parent,false);
        postUserName = convertView.findViewById(R.id.postUserName);
        postTime = convertView.findViewById(R.id.postTime);
        postTitle = convertView.findViewById(R.id.postTitle);
        postDescription = convertView.findViewById(R.id.postDescription);
        postUserPic = convertView.findViewById(R.id.postUserPic);
        postImage = convertView.findViewById(R.id.postImage);

        postUserName.setText(post.postUserName);
        postTime.setText(post.postDateTime.substring(0, 16));
        postTitle.setText(post.postTitle);
        postDescription.setText(post.postDescription);

        StorageReference dpRef;
        if (post.postUserPic != null) {
            dpRef = Configs.getStorageRef(post.postUserPic);
            try {
                File localFile = File.createTempFile("images", "jpg");
                dpRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        Bitmap bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                        postUserPic.setImageBitmap(bitmap);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Log.i("sksLog", "unable to download profile picture post: "+ exception);
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (post.postImage != null) {
            dpRef = Configs.getStorageRef(post.postImage);
            try {
                File localFile = File.createTempFile("images", "jpg");
                dpRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        Bitmap bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                        postImage.setImageBitmap(bitmap);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Log.i("sksLog", "unable to download post picture post: "+ exception);
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        return convertView;
    }
}
