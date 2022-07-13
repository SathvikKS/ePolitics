package com.sathvikks.epolitics;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Objects;

public class NewPost extends AppCompatActivity {
    EditText newPostTitle, newPostDescription;
    Bitmap selectedImageBitmap;
    ImageView newPostImage;
    Button newPostUpload, newPostRemove, newPostAddPost;
    ProgressDialog dialog;
    StorageReference storageRef;
    FirebaseUser myUser;
    DatabaseReference dbRef;
    HashMap userObj;
    ActivityResultLauncher<Intent> launchSomeActivity;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);
        Objects.requireNonNull(getSupportActionBar()).setTitle("New Post");
        newPostTitle= findViewById(R.id.newPostTitle);
        newPostDescription = findViewById(R.id.newPostDescription);
        newPostImage = findViewById(R.id.newPostImage);
        newPostUpload = findViewById(R.id.newPostUpload);
        newPostImage.setVisibility(View.GONE);
        newPostRemove = findViewById(R.id.newPostRemove);
        storageRef = Configs.getStorageRef();
        dbRef = Configs.getDbRef();
        myUser = Configs.getUser();
        userObj = Configs.fetchUserInfo(this, false);
        newPostAddPost = findViewById(R.id.newPostAddPost);
        newPostUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newPostImage.setVisibility(View.VISIBLE);
                imageChooser();
            }
        });
        newPostRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newPostImage.setImageBitmap(null);
                newPostImage.setVisibility(View.GONE);
            }
        });
        newPostAddPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog = Configs.showProcessDialogue(NewPost.this, "Adding post...");
                dialog.show();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                String newPostTitleText, newPostDescriptionText;
                newPostTitleText = newPostTitle.getText().toString();
                newPostDescriptionText = newPostDescription.getText().toString();
                String postChild = (Calendar.getInstance().getTime().toString()+"myCustomSplit"+myUser.getEmail()).replace(".", ",").replaceAll("\\s", "");
                Post newPost = new Post(newPostTitleText, newPostDescriptionText, (String) userObj.get("name"), myUser.getEmail());
                dbRef.child("posts").child((String) Objects.requireNonNull(userObj.get("region"))).child(postChild).setValue(newPost).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(!task.isSuccessful()) {
                            Toast.makeText(NewPost.this, "Unable to add post at the moment", Toast.LENGTH_SHORT).show();
                            Log.i("sksLog", "add post failure: "+task.getException().toString());
                            dialog.dismiss();
                            return;
                        }
                        if (newPostImage.getVisibility() == View.GONE) {
                            dialog.dismiss();
                            Toast.makeText(NewPost.this, "New post has been created", Toast.LENGTH_SHORT).show();
                            NewPost.super.onBackPressed();
                            return;
                        }
                        dialog.dismiss();
                        dialog = Configs.showProcessDialogue(NewPost.this, "Uploading the image...");
                        dialog.setIndeterminate(false);
                        dialog.setProgress(0);
                        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                        dialog.show();
                        selectedImageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                        byte[] bitmapData = baos.toByteArray();
                        StorageReference ref = storageRef.child("posts/"+userObj.get("region")+"/"+postChild);
                        UploadTask uploadTask = ref.putBytes(bitmapData);
                        uploadTask.addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                dialog.dismiss();
                                Toast.makeText(NewPost.this, "Failed to upload the image", Toast.LENGTH_SHORT).show();
                                dbRef.child("posts").child((String) Objects.requireNonNull(userObj.get("region"))).child(postChild).removeValue();
                            }
                        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                dialog.dismiss();
                                Toast.makeText(NewPost.this, "New post has been created", Toast.LENGTH_SHORT).show();
                                NewPost.super.onBackPressed();
                            }
                        });
                        Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                            @Override
                            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                if (!task.isSuccessful()) {
                                    throw task.getException();
                                }
                                return ref.getDownloadUrl();
                            }
                        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                if (task.isSuccessful()) {
                                    Uri downloadUri = task.getResult();
                                    dbRef.child("posts").child((String) Objects.requireNonNull(userObj.get("region"))).child(postChild).child("postImageUrl").setValue(downloadUri.toString());
                                }
                            }
                        });
                        uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                dialog.setMax((int) taskSnapshot.getTotalByteCount()/1024);
                                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                                dialog.setProgress((int)taskSnapshot.getBytesTransferred()/1024);
                                Log.d("sksLog", "Upload is " + progress + "% done");
                            }
                        });
                    }
                });
            }
        });
        launchSomeActivity =  registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {
                Intent data = result.getData();
                if (data != null && data.getData() != null) {
                    Uri selectedImageUri = data.getData();
                    try {
                        selectedImageBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(),selectedImageUri);
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }
                    //dialog.show();
                    newPostImage.setImageBitmap(selectedImageBitmap);

                }
            }
        });
    }
    private void imageChooser() {
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);
        launchSomeActivity.launch(i);
    }
}