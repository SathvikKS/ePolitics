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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Objects;

public class ReportIssue extends AppCompatActivity {
    Intent homeIntent;
    EditText reportIssueDescription;
    Bitmap selectedImageBitmap, compressedBitMap;
    ImageView reportIssueImage;
    Button reportIssueUpload, reportIssueRemove, reportIssueReport;
    ProgressDialog dialog;
    StorageReference storageRef;
    FirebaseUser myUser;
    DatabaseReference dbRef;
    HashMap userObj;
    ActivityResultLauncher<Intent> launchSomeActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report_issue);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Report an Issue");
        reportIssueDescription = findViewById(R.id.reportIssueDescription);
        reportIssueImage = findViewById(R.id.reportIssueImage);
        reportIssueUpload = findViewById(R.id.reportIssueUpload);
        reportIssueImage.setVisibility(View.GONE);
        homeIntent = new Intent(getApplicationContext(), MainActivity.class);
        reportIssueRemove = findViewById(R.id.reportIssueRemove);
        storageRef = Configs.getStorageRef();
        dbRef = Configs.getDbRef();
        myUser = Configs.getUser();
        userObj = Configs.fetchUserInfo(this, false);
        reportIssueReport = findViewById(R.id.reportIssueReport);
        reportIssueUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageChooser();
            }
        });
        reportIssueRemove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reportIssueImage.setImageBitmap(null);
                reportIssueImage.setVisibility(View.GONE);
            }
        });
        reportIssueReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (reportIssueDescription.getText().toString().equals("")) {
                    Toast.makeText(ReportIssue.this, "Description cannot be empty!", Toast.LENGTH_SHORT).show();
                    return;
                }
                String postChild = (Calendar.getInstance().getTime().toString()+"myCustomSplit"+myUser.getEmail()).replace(".", ",").replaceAll("\\s", "");
                if (reportIssueImage.getVisibility() != View.GONE) {
                    dialog = Configs.showProcessDialogue(ReportIssue.this, "Uploading the image...");
                    dialog.setIndeterminate(false);
                    dialog.setProgress(0);
                    dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                    dialog.show();
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    compressedBitMap = Configs.getResizedBitmap(selectedImageBitmap, 50);
                    compressedBitMap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    byte[] bitmapData = baos.toByteArray();
                    StorageReference ref = storageRef.child("reports/"+userObj.get("region")+"/"+postChild);
                    UploadTask uploadTask = ref.putBytes(bitmapData);
                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            dialog.dismiss();
                            Toast.makeText(ReportIssue.this, "Failed to upload the image", Toast.LENGTH_SHORT).show();
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
                                newReport(postChild, downloadUri.toString());
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
                    newReport(postChild);
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
                    reportIssueImage.setVisibility(View.VISIBLE);
                    Glide.with(this).load(selectedImageUri).into(reportIssueImage);
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
    private void newReport(String postChild, String uri) {
        dialog = Configs.showProcessDialogue(ReportIssue.this, "Reporting ...");
        dialog.show();
        Post newPost;
        String newPostDescriptionText;
        newPostDescriptionText = reportIssueDescription.getText().toString();
        newPost = new Post(newPostDescriptionText, (String) userObj.get("name"), myUser.getEmail(), uri, postChild);
        dbRef.child("reports").child((String) Objects.requireNonNull(userObj.get("region"))).child(postChild).setValue(newPost).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(!task.isSuccessful()) {
                    Toast.makeText(ReportIssue.this, "Unable to report issue at the moment", Toast.LENGTH_SHORT).show();
                    Log.i("sksLog", "add post failure: "+task.getException().toString());
                    dialog.dismiss();
                    return;
                }
                dialog.dismiss();
                Toast.makeText(ReportIssue.this, "Your issue has been reported", Toast.LENGTH_SHORT).show();
                startActivity(homeIntent);
                finish();
            }
        });
    }
    private void newReport(String postChild) {
        dialog = Configs.showProcessDialogue(ReportIssue.this, "Reporting ...");
        dialog.show();
        Post newPost;
        String newPostDescriptionText;
        newPostDescriptionText = reportIssueDescription.getText().toString();
        newPost = new Post(newPostDescriptionText, (String) userObj.get("name"), myUser.getEmail(), postChild);
        dbRef.child("reports").child((String) Objects.requireNonNull(userObj.get("region"))).child(postChild).setValue(newPost).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(!task.isSuccessful()) {
                    Toast.makeText(ReportIssue.this, "Unable to report issue at the moment", Toast.LENGTH_SHORT).show();
                    Log.i("sksLog", "add post failure: "+task.getException().toString());
                    dialog.dismiss();
                    return;
                }
                dialog.dismiss();
                Toast.makeText(ReportIssue.this, "Your issue has been reported", Toast.LENGTH_SHORT).show();
                startActivity(homeIntent);
                finish();
            }
        });
    }
}