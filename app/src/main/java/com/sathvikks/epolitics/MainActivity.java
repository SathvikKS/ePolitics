package com.sathvikks.epolitics;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

import java.util.HashMap;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    public static UpdateAccType uat;
    DatabaseReference dbRef;
    FirebaseAuth mAuth;
    Intent siIntent, npIntent;
    HashMap userObj;
    FloatingActionButton newPostButton;
    String accType;

    @Override
    public void onStart() {
        super.onStart();
        if (mAuth.getCurrentUser() == null) {
            startActivity(this.siIntent);
            finish();
            return;
        }
        if (!Configs.getUser().isEmailVerified()) {
            new AlertDialog.Builder(this)
                    .setTitle("Verify Email")
                    .setMessage("You need to verify your E-mail ID inorder to use this application. Please check your inbox or spam for verification link")
                    .setNegativeButton("Okay", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent intent = new Intent("android.intent.action.MAIN");
                            intent.addCategory("android.intent.category.HOME");
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        }
                    })
                    .setPositiveButton("Resend verification link", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Configs.getUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(getApplicationContext(), "Verification link has been sent again", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent("android.intent.action.MAIN");
                                        intent.addCategory("android.intent.category.HOME");
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        startActivity(intent);
                                        Configs.getmAuth().signOut();
                                        finish();
                                        return;
                                    }
                                    Toast.makeText(getApplicationContext(), "Unable to send a verification link", Toast.LENGTH_SHORT).show();
                                    Log.i("sksLog", "resend verification email error: " + Objects.requireNonNull(task.getException()));
                                }
                            });
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        } else {
            accType = Configs.getAccountType(this);
            userObj = Configs.fetchUserInfo(this, false);
            uat.setListener(new myListener() {
                @Override
                public void onAccTypeUpdate(String accType) {
                    if (accType.equals("MLAC")) {
                        newPostButton.setVisibility(View.VISIBLE);
                        Toast.makeText(MainActivity.this, "Welcome MLAC", Toast.LENGTH_SHORT).show();
                    }
                    else if (accType.equals("LU")) {

                        Toast.makeText(MainActivity.this, "Welcome LU", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            if (accType == null) {}
            else if (accType.equals("MLAC")) {
                newPostButton.setVisibility(View.VISIBLE);
                Toast.makeText(MainActivity.this, "Welcome MLAC stored", Toast.LENGTH_SHORT).show();
            } else if (accType.equals("LU")) {
                newPostButton.setVisibility(View.GONE);
                Toast.makeText(MainActivity.this, "Welcome LU stored", Toast.LENGTH_SHORT).show();
            }
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView((int) R.layout.activity_main);
        uat = new UpdateAccType();
        mAuth = Configs.getmAuth();
        siIntent = new Intent(getApplicationContext(), SignIn.class);
        npIntent = new Intent(getApplicationContext(), NewPost.class);
        dbRef = Configs.getDbRef();
        newPostButton = findViewById(R.id.newPostButton);
        newPostButton.setVisibility(View.GONE);
        newPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(npIntent);
            }
        });
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case R.id.menuMyProfile:
                startActivity(new Intent(getApplicationContext(), MyProfile.class));
                return true;
            case R.id.menuSignOut:
                mAuth.signOut();
                Configs.userObj.clear();
                Configs.delAccountType(this);
                startActivity(this.siIntent);
                finish();
                return true;
            default:
                return false;
        }
    }

}
