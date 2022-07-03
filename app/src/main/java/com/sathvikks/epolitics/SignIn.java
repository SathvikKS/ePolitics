package com.sathvikks.epolitics;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;

import java.util.Objects;

public class SignIn extends AppCompatActivity {
    Intent forgotPassword;
    Intent mainActivity;
    Button signIn;
    Intent signUp;
    EditText userEmail;
    EditText userPassword;

    public void goToForgotPassword(View view) {
        startActivity(this.forgotPassword);
    }

    public void goToSignUp(View view) {
        startActivity(this.signUp);
    }

    public void onBackPressed() {
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.HOME");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView((int) R.layout.activity_sign_in);
        ((ActionBar) Objects.requireNonNull(getSupportActionBar())).setTitle((CharSequence) "Sign In");
        this.forgotPassword = new Intent(getApplicationContext(), ForgotPassword.class);
        this.signUp = new Intent(getApplicationContext(), SignUp.class);
        this.mainActivity = new Intent(getApplicationContext(), MainActivity.class);
        this.userEmail = (EditText) findViewById(R.id.siEmail);
        this.userPassword = (EditText) findViewById(R.id.siPassword);
        Button button = (Button) findViewById(R.id.siButton);
        this.signIn = button;
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = userEmail.getText().toString();
                String password = userPassword.getText().toString();
                if (email.matches("") || password.matches("")) {
                    Toast.makeText(getApplicationContext(), "Please enter the credentials", Toast.LENGTH_SHORT).show();
                } else {
                    Configs.getmAuth().signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (!task.isSuccessful()) {
                                Log.i("sksLog", "signInWithEmail:failure", task.getException());
                                Toast.makeText(getApplicationContext(), "Authentication failed.", Toast.LENGTH_SHORT).show();
                            } else if (!Configs.getUser().isEmailVerified()) {
                                new AlertDialog.Builder(SignIn.this)
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
                                startActivity(mainActivity);
                                finish();
                            }
                        }
                    });
                }
            }
        });
    }
}
