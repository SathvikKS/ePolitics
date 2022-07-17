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

public class EditReport extends AppCompatActivity {
    Boolean pictureChange = false;
    Intent homeIntent, thisIntent;
    EditText editReportDescription;
    Bitmap selectedImageBitmap;
    ImageView editReportImage;
    Button editReportEditImage, editReportRemoveImage, editReportEdit;
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
        setContentView(R.layout.activity_edit_report);
        thisIntent = getIntent();
        Gson gson = new Gson();
        post = gson.fromJson(thisIntent.getStringExtra("report"), Post.class);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Edit issue reported");
        editReportDescription = findViewById(R.id.editReportDescription);
        editReportImage = findViewById(R.id.editReportImage);
        editReportEditImage = findViewById(R.id.editReportEditImage);
        editReportImage.setVisibility(View.GONE);
        homeIntent = new Intent(getApplicationContext(), MainActivity.class);
        editReportRemoveImage = findViewById(R.id.editReportRemoveImage);
        storageRef = Configs.getStorageRef();
        dbRef = Configs.getDbRef();
        myUser = Configs.getUser();
        userObj = Configs.fetchUserInfo(this, false);
        editReportEdit = findViewById(R.id.editReportEdit);
        editReportDescription.setText(post.getPostDescription());
        try {
            new URL((String) post.getPostImage()).toURI();
            editReportImage.setVisibility(View.VISIBLE);
            Glide.with(EditReport.this)
                    .load(post.getPostImage())
                    .into(editReportImage);
        } catch (Exception ignored) {

        }
        editReportEditImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageChooser();
            }
        });
        editReportRemoveImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editReportImage.setImageBitmap(null);
                editReportImage.setVisibility(View.GONE);
            }
        });
        editReportEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editReportDescription.getText().toString().equals("")) {
                    Toast.makeText(EditReport.this, "Description cannot be empty!", Toast.LENGTH_SHORT).show();
                    return;
                }
                String postChild = post.postId;
                if (editReportImage.getVisibility() != View.GONE && pictureChange) {
                    dialog = Configs.showProcessDialogue(EditReport.this, "Uploading the image...");
                    dialog.setIndeterminate(false);
                    dialog.setProgress(0);
                    dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                    dialog.show();
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    selectedImageBitmap = Configs.getResizedBitmap(selectedImageBitmap, 70);
                    selectedImageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    byte[] bitmapData = baos.toByteArray();
                    StorageReference ref = storageRef.child("reports/"+userObj.get("region")+"/"+postChild);
                    UploadTask uploadTask = ref.putBytes(bitmapData);
                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            dialog.dismiss();
                            Toast.makeText(EditReport.this, "Failed to upload the image", Toast.LENGTH_SHORT).show();
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
                    editReportImage.setVisibility(View.VISIBLE);
                    Glide.with(this).load(selectedImageUri).into(editReportImage);
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
        dialog = Configs.showProcessDialogue(EditReport.this, "Editing ...");
        dialog.show();
        Post editPost;
        String editPostDescriptionText;
        editPostDescriptionText = editReportDescription.getText().toString();
        editPost = new Post(editPostDescriptionText, (String) userObj.get("name"), myUser.getEmail(), uri, postChild);
        editPost.setEdited(true);
        dbRef.child("reports").child((String) Objects.requireNonNull(userObj.get("region"))).child(postChild).setValue(editPost).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(!task.isSuccessful()) {
                    Toast.makeText(EditReport.this, "Unable to edit the issue report ath the moment", Toast.LENGTH_SHORT).show();
                    Log.i("sksLog", "update issue failure: "+task.getException().toString());
                    dialog.dismiss();
                    return;
                }
                dialog.dismiss();
                Toast.makeText(EditReport.this, "The issue report has been updated", Toast.LENGTH_SHORT).show();
                startActivity(homeIntent);
                finish();
            }
        });
    }
    private void editPost(String postChild) {
        dialog = Configs.showProcessDialogue(EditReport.this, "Updating the post...");
        dialog.show();
        Post editPost;
        String editPostDescriptionText;
        editPostDescriptionText = editReportDescription.getText().toString();
        if (!pictureChange && editReportImage.getVisibility() == View.VISIBLE) {
            editPost = new Post(editPostDescriptionText, (String) userObj.get("name"), myUser.getEmail(), post.getPostImage() ,postChild);
        } else {
            editPost = new Post(editPostDescriptionText, (String) userObj.get("name"), myUser.getEmail(), postChild);
        }
        editPost.setEdited(true);
        dbRef.child("reports").child((String) Objects.requireNonNull(userObj.get("region"))).child(postChild).setValue(editPost).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(!task.isSuccessful()) {
                    Toast.makeText(EditReport.this, "Unable to edit the issue report ath the moment", Toast.LENGTH_SHORT).show();
                    Log.i("sksLog", "update issue failure: "+task.getException().toString());
                    dialog.dismiss();
                    return;
                }
                dialog.dismiss();
                Toast.makeText(EditReport.this, "The issue report has been updated", Toast.LENGTH_SHORT).show();
                startActivity(homeIntent);
                finish();
            }
        });
    }
}