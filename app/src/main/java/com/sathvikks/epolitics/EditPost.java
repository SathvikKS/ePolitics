package com.sathvikks.epolitics;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
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
import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Objects;

public class EditPost extends AppCompatActivity {
    Boolean pictureChange = false;
    Intent homeIntent, thisIntent;
    EditText editPostDescription;
    Bitmap selectedImageBitmap;
    ImageView editPostImage;
    Button editPostUpload, editPostRemove, editPostEdit;
    ProgressDialog dialog;
    StorageReference storageRef;
    FirebaseUser myUser;
    DatabaseReference dbRef;
    HashMap userObj;
    Post post;
    ActivityResultLauncher<Intent> launchSomeActivity;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_post);
        thisIntent = getIntent();
        Gson gson = new Gson();
        post = gson.fromJson(thisIntent.getStringExtra("post"), Post.class);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Edit Post");
        editPostDescription = findViewById(R.id.editPostDescription);
        editPostImage = findViewById(R.id.editPostImage);
        editPostUpload = findViewById(R.id.editPostEditImage);
        editPostImage.setVisibility(View.GONE);
        homeIntent = new Intent(getApplicationContext(), MainActivity.class);
        editPostRemove = findViewById(R.id.editPostRemoveImage);
        storageRef = Configs.getStorageRef();
        dbRef = Configs.getDbRef();
        myUser = Configs.getUser();
        userObj = Configs.fetchUserInfo(this, false);
        editPostEdit = findViewById(R.id.editPostEdit);
        editPostDescription.setText(post.getPostDescription());
        try {
            new URL((String) post.getPostImage()).toURI();
            editPostImage.setVisibility(View.VISIBLE);
            Glide.with(EditPost.this)
                    .load(post.getPostImage())
                    .into(editPostImage);
        } catch (Exception ignored) {

        }
        editPostUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageChooser();
            }
        });
        editPostRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editPostImage.setImageBitmap(null);
                editPostImage.setVisibility(View.GONE);
            }
        });
        editPostEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editPostDescription.getText().toString().equals("")) {
                    Toast.makeText(EditPost.this, "Description cannot be empty!", Toast.LENGTH_SHORT).show();
                    return;
                }
                String postChild = post.postId;
                if (editPostImage.getVisibility() != View.GONE && pictureChange) {
                    dialog = Configs.showProcessDialogue(EditPost.this, "Uploading the image...");
                    dialog.setIndeterminate(false);
                    dialog.setProgress(0);
                    dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                    dialog.show();
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    selectedImageBitmap = Configs.getResizedBitmap(selectedImageBitmap, 70);
                    selectedImageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    byte[] bitmapData = baos.toByteArray();
                    StorageReference ref = storageRef.child("posts/"+userObj.get("region")+"/"+postChild);
                    UploadTask uploadTask = ref.putBytes(bitmapData);
                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            dialog.dismiss();
                            Toast.makeText(EditPost.this, "Failed to upload the image", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            dialog.dismiss();
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
                                editPost(postChild, downloadUri.toString());
                            }
                        }
                    });
                    uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            dialog.setMax((int) taskSnapshot.getTotalByteCount()/1024);
                            dialog.setProgress((int)taskSnapshot.getBytesTransferred()/1024);
                        }
                    });
                }
                else {
                    editPost(postChild);
                }
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
                    editPostImage.setVisibility(View.VISIBLE);
                    Glide.with(this).load(selectedImageUri).into(editPostImage);
                    pictureChange = true;
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
    private void editPost(String postChild, String uri) {
        dialog = Configs.showProcessDialogue(EditPost.this, "Updating the post...");
        dialog.show();
        Post editPost;
        String editPostDescriptionText;
        editPostDescriptionText = editPostDescription.getText().toString();
        editPost = new Post(editPostDescriptionText, (String) userObj.get("name"), myUser.getEmail(), uri, postChild);
        editPost.setEdited(true);
        dbRef.child("posts").child((String) Objects.requireNonNull(userObj.get("region"))).child(postChild).setValue(editPost).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(!task.isSuccessful()) {
                    Toast.makeText(EditPost.this, "Unable to update post at the moment", Toast.LENGTH_SHORT).show();
                    Log.i("sksLog", "update post failure: "+task.getException().toString());
                    dialog.dismiss();
                    return;
                }
                dialog.dismiss();
                Toast.makeText(EditPost.this, "The post has been updated", Toast.LENGTH_SHORT).show();
                startActivity(homeIntent);
                finish();
            }
        });
    }
    private void editPost(String postChild) {
        dialog = Configs.showProcessDialogue(EditPost.this, "Updating the post...");
        dialog.show();
        Post editPost;
        String editPostDescriptionText;
        editPostDescriptionText = editPostDescription.getText().toString();
        if (!pictureChange && editPostImage.getVisibility() == View.VISIBLE) {
            editPost = new Post(editPostDescriptionText, (String) userObj.get("name"), myUser.getEmail(), post.getPostImage() ,postChild);
        } else {
            editPost = new Post(editPostDescriptionText, (String) userObj.get("name"), myUser.getEmail(), postChild);
        }
        editPost.setEdited(true);
        dbRef.child("posts").child((String) Objects.requireNonNull(userObj.get("region"))).child(postChild).setValue(editPost).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(!task.isSuccessful()) {
                    Toast.makeText(EditPost.this, "Unable to add post at the moment", Toast.LENGTH_SHORT).show();
                    Log.i("sksLog", "add post failure: "+task.getException().toString());
                    dialog.dismiss();
                    return;
                }
                dialog.dismiss();
                Toast.makeText(EditPost.this, "New post has been created", Toast.LENGTH_SHORT).show();
                startActivity(homeIntent);
                finish();
            }
        });
    }
}