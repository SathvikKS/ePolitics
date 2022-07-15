package com.sathvikks.epolitics;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements PostView{
    RecyclerView recyclerView;
    ArrayList<Post> posts;
    public static UpdateAccType uat;
    public static UpdateUserRegion uar;
    DatabaseReference dbRef;
    FirebaseAuth mAuth;
    Intent siIntent, npIntent, viewPostIntent;
    HashMap userObj;
    FloatingActionButton newPostButton;
    String accType, region;
    TextView postHeading;
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
                    }
                    else {
                        newPostButton.setVisibility(View.GONE);
                    }
                }
                @Override
                public void onAccRegionUpdate(String region) {

                }

            });
            if (accType == null) {}
            else if (accType.equals("MLAC")) {
                newPostButton.setVisibility(View.VISIBLE);
            } else if (accType.equals("LU")) {
                newPostButton.setVisibility(View.GONE);
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
        region = Configs.getAccRegion(this);
        posts = new ArrayList<>();
        recyclerView = findViewById(R.id.recyclerView);
        postHeading = findViewById(R.id.postHeading);
        postHeading.setText("What's happening nearby");
        uat = new UpdateAccType();
        uar = new UpdateUserRegion();
        mAuth = Configs.getmAuth();
        siIntent = new Intent(getApplicationContext(), SignIn.class);
        npIntent = new Intent(getApplicationContext(), NewPost.class);
        viewPostIntent = new Intent(getApplicationContext(), ViewPost.class);
        dbRef = Configs.getDbRef();
        newPostButton = findViewById(R.id.newPostButton);
        newPostButton.setVisibility(View.GONE);
        newPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(npIntent);
            }
        });
        uar.setListener(new myListener() {
            @Override
            public void onAccTypeUpdate(String accType) {

            }

            @Override
            public void onAccRegionUpdate(String region) {
                createPostView(region);
            }
        });
        if (region == null) {}
        else {
            createPostView(region);
        }

    }

    public void createPostView(String region) {
        DatabaseReference postsRef = Configs.getDbRef().child("posts").child(region);
        PostAdapter listAdapter = new PostAdapter(MainActivity.this,posts, MainActivity.this);
        recyclerView.setAdapter(listAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                Post post = dataSnapshot.getValue(Post.class);
                posts.add(post);
                listAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }


            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("sksLog", "postComments:onCancelled", databaseError.toException());

            }
        };
        postsRef.addChildEventListener(childEventListener);
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
                Configs.removeProfilePic(this);
                Configs.delAccRegion(this);
                startActivity(this.siIntent);
                finish();
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onPostClick(Post post) {
        Gson gson = new Gson();
        startActivity(viewPostIntent);
    }
}

