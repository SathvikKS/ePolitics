package com.sathvikks.epolitics;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements PostView {
    View header;
    ImageView navProfilePicture;
    Menu menu;
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    ActionBarDrawerToggle drawerToggle;
    TextView textView, navReport, navProfileName, navProfileRegion, postHeading;
    RecyclerView recyclerView;
    ArrayList<Post> posts;
    public static UpdateAccType uat;
    public static UpdateUserRegion uar;
    DatabaseReference dbRef;
    FirebaseAuth mAuth;
    Intent siIntent, npIntent, viewPostIntent, newReportIntent, myReportsIntent;
    HashMap userObj;
    FloatingActionButton newPostButton;
    String accType, region;
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
                        menu.findItem(R.id.navReport).setVisible(false);
                        menu.findItem(R.id.navReportList).setVisible(false);
                    }
                    else {
                        newPostButton.setVisibility(View.GONE);
                        menu.findItem(R.id.navCheckReports).setVisible(false);
                    }
                }
                @Override
                public void onAccRegionUpdate(String region) {

                }

            });
            if (accType == null) {}
            else if (accType.equals("MLAC")) {
                newPostButton.setVisibility(View.VISIBLE);
                menu.findItem(R.id.navReport).setVisible(false);
                menu.findItem(R.id.navReportList).setVisible(false);
            } else if (accType.equals("LU")) {
                newPostButton.setVisibility(View.GONE);
                menu.findItem(R.id.navCheckReports).setVisible(false);
            }
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView((int) R.layout.activity_main);
        drawerLayout=findViewById(R.id.drawer_layout);
        navigationView=findViewById(R.id.nav_view);
        menu = navigationView.getMenu();
        drawerToggle = new ActionBarDrawerToggle(this,drawerLayout,R.string.nav_open,R.string.nav_close);
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();
        navigationView.bringToFront();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        header = navigationView.getHeaderView(0);
        navProfileName = header.findViewById(R.id.navProfileName);
        navProfileRegion = header.findViewById(R.id.navProfileRegion);
        navProfilePicture = header.findViewById(R.id.navProfilePicture);
        textView=findViewById(R.id.textView);
        navReport = findViewById(R.id.navReport);
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
        newReportIntent = new Intent(getApplicationContext(), ReportIssue.class);
        myReportsIntent = new Intent(getApplicationContext(), MyReports.class);
        dbRef = Configs.getDbRef();
        newPostButton = findViewById(R.id.newPostButton);
        newPostButton.setVisibility(View.GONE);
        newPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!Configs.isNetworkAvailable(MainActivity.this)) {
                    Toast.makeText(MainActivity.this, "You cannot create a post while you are offline!", Toast.LENGTH_SHORT).show();
                } else {
                    startActivity(npIntent);
                }
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
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menuMyProfile:
                        if (!Configs.isNetworkAvailable(MainActivity.this)) {
                            Toast.makeText(MainActivity.this, "You must be connected to internet for this", Toast.LENGTH_SHORT).show();
                        } else {
                            startActivity(new Intent(getApplicationContext(), MyProfile.class));
                        }
                        return true;
                    case R.id.menuSignOut:
                        mAuth.signOut();
                        Configs.userObj.clear();
                        Configs.delAccountType(MainActivity.this);
                        Configs.removeProfilePic(MainActivity.this);
                        Configs.delAccRegion(MainActivity.this);
                        startActivity(MainActivity.this.siIntent);
                        finish();
                        return true;
                    case R.id.navReport:
                        startActivity(newReportIntent);
                        return true;
                    case R.id.navReportList:
                        startActivity(myReportsIntent);
                        return true;
                    default:
                        return false;
                }
            }
        });
        if (Configs.getUser() == null ) {
            return;
        }
        dbRef.child("users").child(Configs.generateEmail(Objects.requireNonNull(Configs.getUser().getEmail()))).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.isSuccessful()) {
                    HashMap userObj = (HashMap) task.getResult().getValue();
                    if (userObj.get("profilePicUrl") != null ) {
                        Glide.with(MainActivity.this)
                                .load(userObj.get("profilePicUrl"))
                                .into(navProfilePicture);
                    }
                    navProfileName.setText((String) userObj.get("name"));
                    navProfileRegion.setText((String) userObj.get("region"));
                }
            }
        });
    }

    public void createPostView(String region) {
        DatabaseReference postsRef = Configs.getDbRef().child("posts").child(region);
        postsRef.keepSynced(true);
        PostAdapter listAdapter = new PostAdapter(MainActivity.this,posts, MainActivity.this);
        recyclerView.setAdapter(listAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                Post post = dataSnapshot.getValue(Post.class);
                posts.add(0, post);
                listAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                try {
                    posts.set(Configs.getPostIndex(posts, snapshot.getKey()), snapshot.getValue(Post.class));
                    listAdapter.notifyDataSetChanged();
                } catch (ArrayIndexOutOfBoundsException unused) {}
            }


            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                try {
                    posts.remove(Configs.getPostIndex(posts, dataSnapshot.getKey()));
                    listAdapter.notifyDataSetChanged();
                } catch (ArrayIndexOutOfBoundsException unused) {}
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
    public void onPostClick(Post post) {
        Gson gson = new Gson();
        viewPostIntent.putExtra("post", gson.toJson(post));
        startActivity(viewPostIntent);
    }
    @Override
    public void onBackPressed(){
        if(drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        else {
            super.onBackPressed();
        }
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

