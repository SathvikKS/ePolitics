package com.sathvikks.epolitics;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnPausedListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.time.temporal.TemporalAdjuster;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;


public class MyProfile extends AppCompatActivity {
    StorageReference storageRef;
    String userName, userEmail, userPhone, userRegion, userType, userGender, userPicUrl, reEnterPassword;
    ProgressDialog dialog;
    Bitmap selectedImageBitmap, profilePicBitmap;
    ImageView profileImage;
    TextView profileUserName, profileUserEmail, profileUserPhone, profileUserRegion, profileUserGender, profileUserAuthority;
    Button profileEditImage, profileRemoveImage, profileUserDelete;
    LinearLayout profileUserAuthorityLayout;
    FirebaseAuth mAuth;
    DatabaseReference dbRef;
    FirebaseUser myUser;
    Intent siIntent;
    HashMap userObj;

    private void imageChooser()
    {
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);
        launchSomeActivity.launch(i);
    }
    ActivityResultLauncher<Intent> launchSomeActivity;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_profile);
        ((ActionBar) Objects.requireNonNull(getSupportActionBar())).setTitle((CharSequence) "My Profile");
        storageRef = Configs.getStorageRef();
        dialog = new ProgressDialog(MyProfile.this);
        profileEditImage = findViewById(R.id.profileEditImage);
        profileRemoveImage = findViewById(R.id.profileRemoveImage);
        profileUserDelete = findViewById(R.id.profileUserDelete);
        profileImage = findViewById(R.id.profileImage);
        profileUserEmail = findViewById(R.id.profileUserEmail);
        profileUserAuthority = findViewById(R.id.profileUserAuthorities);
        profileUserGender = findViewById(R.id.profileUserGender);
        profileUserName = findViewById(R.id.profileUserName);
        profileUserPhone = findViewById(R.id.profileUserPhone);
        profileUserRegion = findViewById(R.id.profileUserRegion);
        profileUserAuthorityLayout = findViewById(R.id.profileUserAuthorityLayout);
        mAuth = Configs.getmAuth();
        dbRef = Configs.getDbRef();
        myUser = Configs.getUser();
        siIntent = new Intent(getApplicationContext(), SignIn.class);
        profileRemoveImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dbRef.child("users").child(Configs.generateEmail(mAuth.getCurrentUser().getEmail())).child("profilePic").setValue(null).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Configs.fetchUserInfo(MyProfile.this, true);
                            Toast.makeText(MyProfile.this, "Profile picture has been removed", Toast.LENGTH_SHORT).show();
                            profileImage.setImageDrawable(null);
                        } else {
                            Toast.makeText(MyProfile.this, "Unable to remove profile picture", Toast.LENGTH_SHORT).show();
                            Log.i("sksLog", "unable to remove profile pic "+task.getException().toString());
                        }
                    }
                });
            }
        });
        profileEditImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imageChooser();
                dialog = Configs.showProcessDialogue(MyProfile.this, "Uploading the image");
                dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                dialog.setIndeterminate(false);
                dialog.setProgress(0);
            }
        });
        userObj = Configs.fetchUserInfo(MyProfile.this, false);
        try {
            userName = (String) userObj.get("name");
            userEmail = (String) userObj.get("email");
            userGender = (String) userObj.get("gender");
            userPhone = String.valueOf(userObj.get("phone"));
            userRegion = (String) userObj.get("region");
            userType = (String) userObj.get("accType");
            userPicUrl = (String) userObj.get("profilePicUrl");
            profilePicBitmap = (Bitmap) ((BitmapDrawable) userObj.get("profilePic")).getBitmap();
        } catch (Exception e) {
            Log.i("sksLog", "unable to parse json:\n"+e.toString());
            return;
        }
        profileUserName.setText(userName);
        profileUserEmail.setText(userEmail);
        profileUserGender.setText(userGender);
        profileUserPhone.setText(userPhone);
        profileUserRegion.setText(userRegion);
        if (userType.equals("MLAC")) {
            profileUserAuthorityLayout.setVisibility(View.GONE);
        }
        else {
            ProgressDialog dl = Configs.showProcessDialogue(this, "Fetching the list of authorities");
            dl.show();
            dbRef.child("users").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                List authorities = new ArrayList();
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    if(!task.isSuccessful()) {
                        dl.dismiss();
                        return;
                    }
                    for (DataSnapshot ds: task.getResult().getChildren()) {
                        String accType = (String) ds.child("accType").getValue();
                        String region = (String) ds.child("region").getValue();
                        assert accType != null;
                        if (accType.equals("MLAC")) {
                            assert region != null;
                            if (region.equals(Configs.userObj.get("region"))) {
                                authorities.add(ds.child("name").getValue().toString());
                            }
                        }
                    }
                    String authoritiesString = authorities.toString().replace("[", "")
                            .replace("]", "");
                    TextView profileUserAuthorities = findViewById(R.id.profileUserAuthorities);
                    profileUserAuthorities.setText(authoritiesString);
                    dl.dismiss();
                }
            });
        }
        if (profilePicBitmap != null) {
            profileImage.setImageBitmap(profilePicBitmap);

        }
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
                    dialog.show();
                    profileImage.setImageBitmap(selectedImageBitmap);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    selectedImageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    byte[] bitmapData = baos.toByteArray();
                    StorageReference ref = storageRef.child("profile/"+myUser.getEmail());
                    UploadTask uploadTask = ref.putBytes(bitmapData);
                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            dialog.dismiss();
                            Toast.makeText(MyProfile.this, "Failed to upload profile picture", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            dialog.dismiss();
                            Toast.makeText(MyProfile.this, "Profile picture uploaded", Toast.LENGTH_SHORT).show();
                            Configs.userObj.put("profilePic", new BitmapDrawable(getResources(), selectedImageBitmap));
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
                                dbRef.child("users").child(Configs.generateEmail(userEmail)).child("profilePicUrl").setValue(downloadUri.toString());
                                Log.i("sksLog", "image url: "+downloadUri.toString());
                            } else {
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
            }
        });
        profileUserDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(MyProfile.this)
                        .setTitle("Confirm Delete")
                        .setMessage("Are you sure you want to delete your account?")
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                
                            }
                        })
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialog = Configs.showProcessDialogue(MyProfile.this, "Re Authenticating...");
                                ContextThemeWrapper ctx = new ContextThemeWrapper(MyProfile.this, com.google.android.material.R.style.Theme_MaterialComponents_DayNight_DarkActionBar);
                                AlertDialog.Builder builder = new AlertDialog.Builder(MyProfile.this);
                                builder.setTitle("Re enter the account password");
                                View viewInflated = LayoutInflater.from(ctx).inflate(R.layout.text_input_password, null);
                                final EditText input = (EditText) viewInflated.findViewById(R.id.input);
                                builder.setView(viewInflated);
                                builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogg, int which) {
                                        dialog.show();
                                        reEnterPassword = input.getText().toString();
                                        AuthCredential credential = EmailAuthProvider.getCredential(userEmail, reEnterPassword);
                                        mAuth.getCurrentUser().reauthenticate(credential)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            Log.i("sksLog", "User re-authenticated.");
                                                            mAuth.getCurrentUser().delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if (!task.isSuccessful()){
                                                                        Toast.makeText(MyProfile.this, "Unable to delete your account at the moment. Please try again later.", Toast.LENGTH_LONG).show();
                                                                        Log.e("sksLog", "user.delete() error: "+task.getException().toString());
                                                                        return;
                                                                    }
                                                                    dbRef.child("users").child(Configs.generateEmail(userEmail)).removeValue(new DatabaseReference.CompletionListener() {
                                                                        @Override
                                                                        public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                                                                            if(error!=null) {
                                                                                Toast.makeText(MyProfile.this, "Unable to delete your account at the moment. Please try again later.", Toast.LENGTH_LONG).show();
                                                                                Log.e("sksLog", "dbref.removeValure() error: "+error.toString());
                                                                                return;
                                                                            }
                                                                            Toast.makeText(MyProfile.this, "Your account has been deleted", Toast.LENGTH_SHORT).show();
                                                                            startActivity(siIntent);
                                                                            finish();
                                                                        }
                                                                    });
                                                                }
                                                            });
                                                        } else {
                                                            Log.e("sksLog", "unable to reauthenticate "+task.getException().toString());
                                                            Toast.makeText(MyProfile.this, "Wrong password", Toast.LENGTH_SHORT).show();
                                                        }
                                                        dialog.dismiss();
                                                    }
                                                });

                                    }
                                });
                                builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                                });

                                builder.show();
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
                
            }
        });
    }
}