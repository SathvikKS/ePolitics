package com.sathvikks.epolitics;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class ForgotPassword extends AppCompatActivity {
    FirebaseAuth mAuth;
    EditText resetEmail;
    Button resetLogin;
    Button resetPassword;
    Intent signIn;

    /* access modifiers changed from: protected */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Forgot Password");
        this.signIn = new Intent(getApplicationContext(), SignIn.class);
        this.resetEmail = findViewById(R.id.resetEmail);
        this.resetPassword = findViewById(R.id.resetPassword);
        this.resetLogin = findViewById(R.id.resetLogin);
        this.mAuth = Configs.getmAuth();
        this.resetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String resetEmailText = resetEmail.getText().toString();
                if (resetEmailText.matches("")) {
                    Toast.makeText(getApplicationContext(), "Please enter the E-mail ID", Toast.LENGTH_SHORT).show();
                } else {
                    mAuth.sendPasswordResetEmail(resetEmailText).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(getApplicationContext(), "A link has been sent to your E-mail ID", Toast.LENGTH_LONG).show();
                                return;
                            }
                            Log.i("sksLog", "resetPassword:failure", task.getException());
                            Toast.makeText(getApplicationContext(), "The E-mail entered is not registered", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
        this.resetLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(signIn);
                finish();
            }
        });
    }
}
