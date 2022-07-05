package com.sathvikks.epolitics;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import java.util.Objects;

public class SignUp extends AppCompatActivity {
    ProgressDialog dialog;
    Intent signIn;
    Button suButton;
    LinearLayout suChooseLayout;
    EditText suEmail;
    RadioGroup suGender;
    LinearLayout suLayout;
    EditText suName;
    EditText suPassword1;
    EditText suPassword2;
    EditText suPhone;
    Spinner suRegion;
    String suType;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        dialog = new ProgressDialog(SignUp.this);
        suRegion = findViewById(R.id.suRegion);
        suGender = findViewById(R.id.suGenderGroup);
        suName = findViewById(R.id.suName);
        suEmail = findViewById(R.id.suEmail);
        suPhone = findViewById(R.id.suPhone);
        suPassword1 = findViewById(R.id.suPassword1);
        suPassword2 = findViewById(R.id.suPassword2);
        signIn = new Intent(getApplicationContext(), SignIn.class);
        suChooseLayout = findViewById(R.id.suChooseLayout);
        LinearLayout linearLayout = findViewById(R.id.suLayout);
        suLayout = linearLayout;
        linearLayout.setVisibility(View.GONE);
        suChooseLayout.setVisibility(View.VISIBLE);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Sign Up");
        suButton = findViewById(R.id.suButton);
        suButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int selectedId;
                String suNameText = suName.getText().toString();
                String suEmailText = suEmail.getText().toString();
                String suPhoneText = suPhone.getText().toString();
                String suPassword1Text = suPassword1.getText().toString();
                String suPassword2Text = suPassword2.getText().toString();
                String suRegionText = suRegion.getSelectedItem().toString();
                selectedId = suGender.getCheckedRadioButtonId();
                if (suNameText.matches("") || suEmailText.matches("") || suPhoneText.matches("") || suPassword1Text.matches("") || suPassword2Text.matches("") || selectedId == -1 || suRegionText.matches("")) {
                    Toast.makeText(getApplicationContext(), "Please enter all the details", Toast.LENGTH_SHORT).show();
                    return;
                }
                String suGenderText = ((RadioButton) findViewById(selectedId)).getText().toString();
                if (suPassword1Text.length() < 6) {
                    Toast.makeText(getApplicationContext(), "Password should be at least 6 characters", Toast.LENGTH_SHORT).show();
                    return;
                }
                DatabaseReference userDb = Configs.getDbRef();
                User myUser = new User(suEmailText, suPhoneText, suNameText, suType, suGenderText, suRegionText);
                if (!suPassword1Text.equals(suPassword2Text)) {
                    Toast.makeText(getApplicationContext(), "The passwords do not match", Toast.LENGTH_SHORT).show();
                    return;
                }
                suButton.setClickable(false);
                Configs.getmAuth().createUserWithEmailAndPassword(suEmailText, suPassword1Text).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Configs.getUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Log.i("sksLog", "storing object: "+myUser);
                                        userDb.child("users").child(Configs.generateEmail(suEmailText)).setValue(myUser).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    Toast.makeText(getApplicationContext(), "Sign Up successful. Please verify your E-mail to proceed", Toast.LENGTH_LONG).show();
                                                    Configs.getmAuth().signOut();
                                                    startActivity(signIn);
                                                    finish();
                                                    return;
                                                }
                                                if (Objects.requireNonNull(task.getException()).toString().matches(".*Permission denied")) {
                                                    Toast.makeText(getApplicationContext(), "This E-mail ID is already registered", Toast.LENGTH_SHORT).show();
                                                } else {
                                                    Toast.makeText(getApplicationContext(), "Sign Up failed!", Toast.LENGTH_SHORT).show();
                                                }
                                                Log.i("sksLog", task.getException().toString());
                                                suButton.setClickable(true);
                                            }
                                        });
                                    } else {
                                        Log.i("sksLog", "email verify error: " + Objects.requireNonNull(task.getException()));
                                    }
                                    suButton.setClickable(true);
                                }
                            });
                            return;
                        }
                        Log.i("sksLog", "SignUP firebase failure: " + Objects.requireNonNull(task.getException()));
                        if (task.getException().toString().matches(".*The email address is already in use by another account.*")) {
                            Toast.makeText(getApplicationContext(), "Sign Up failed! E-mail ID is already registered", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getApplicationContext(), "Sign Up failed!", Toast.LENGTH_SHORT).show();
                        }
                        suButton.setClickable(true);
                    }
                });
            }
        });
    }
    @Override
    public void onBackPressed() {
        if (this.suChooseLayout.getVisibility() == View.VISIBLE) {
            super.onBackPressed();
            return;
        }
        this.suChooseLayout.setVisibility(View.VISIBLE);
        this.suLayout.setVisibility(View.GONE);
    }

    public void hideChoice() {
        this.suChooseLayout.setVisibility(View.INVISIBLE);
    }

    public void su(View view) {
        hideChoice();
        this.suLayout.setVisibility(View.VISIBLE);
        this.suType = view.getTag().toString();
        dialog.setMessage("Fetching regions");
        dialog.setCancelable(false);
        dialog.setInverseBackgroundForced(false);
        dialog.show();
        Configs.getDbRef().child("regions").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DataSnapshot> task) {
                        if (task.isSuccessful()) {
                            suRegion.setAdapter(new ArrayAdapter<>(getApplication(), android.R.layout.simple_spinner_item, String.valueOf(task.getResult().getValue()).split(", ")));
                            Log.i("sksLog", "fetched the regions: " + task.getResult().toString());
                            dialog.dismiss();
                            return;
                        }
                        Log.i("sksLog", "retrieve regions error: " + Objects.requireNonNull(task.getException()));
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i("sksLog", "retrieve regions error: " + Objects.requireNonNull(e));
                    }
                });
    }

    public void siIntent(View view) {
        startActivity(this.signIn);
        finish();
    }
}
