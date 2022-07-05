package com.sathvikks.epolitics;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class MyProfile extends AppCompatActivity {
    ProgressDialog dialog;
    Bitmap selectedImageBitmap;
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
                dialog.show();
            }
        });
        userObj = Configs.fetchUserInfo(MyProfile.this, false);
        String userName, userEmail, userPhone, userRegion, userType, userGender, userPic;
        try {
            userName = (String) userObj.get("name");
            userEmail = (String) userObj.get("email");
            userGender = (String) userObj.get("gender");
            userPhone = String.valueOf(userObj.get("phone"));
            userRegion = (String) userObj.get("region");
            userType = (String) userObj.get("accType");
            userPic = (String) userObj.get("profilePic");
        } catch (Exception e) {
            Log.i("sksLog", "unable to parse json:\n"+e.toString());
            return;
        }
        profileUserName.setText(userName);
        profileUserEmail.setText(userEmail);
        profileUserGender.setText(userGender);
        profileUserPhone.setText(userPhone);
        profileUserRegion.setText(userRegion);
        Log.i("sksLog", "userType "+userType);
        if (userType.equals("MLAC")) {
            profileUserAuthorityLayout.setVisibility(View.GONE);
        } else {
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
        if (userPic != null) {
            profileImage.setImageBitmap(Configs.StringToBitMap(userPic));
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
                    profileImage.setImageBitmap(selectedImageBitmap);
                    dbRef.child("users").child(Configs.generateEmail(userEmail)).child("profilePic").setValue(Configs.BitMapToString(selectedImageBitmap)).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(MyProfile.this, "Profile picture has been uploaded", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(MyProfile.this, "Failed to upload profile picture", Toast.LENGTH_SHORT).show();
                                Log.i("sksLog", "failed to upload the image: "+task.getException().toString());
                            }
                            dialog.dismiss();
                            Configs.fetchUserInfo(MyProfile.this, true);
                        }
                    });
                }
            }
        });
    }
}