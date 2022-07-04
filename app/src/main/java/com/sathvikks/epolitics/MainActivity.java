package com.sathvikks.epolitics;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    static DatabaseReference dbRef;
    static FirebaseAuth mAuth;
    Intent siIntent;
    ProgressDialog dialog;
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
            Configs.fetchUserInfo(this, false);
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
        mAuth = Configs.getmAuth();
        this.siIntent = new Intent(getApplicationContext(), SignIn.class);
        dbRef = Configs.getDbRef();
        dialog = new ProgressDialog(this);
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
                startActivity(this.siIntent);
                finish();
                return true;
            default:
                return false;
        }
    }
}
