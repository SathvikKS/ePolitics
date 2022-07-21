package com.sathvikks.epolitics;

import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.HashMap;

public class MyReports extends AppCompatActivity implements PostView {
    FirebaseUser myUser;
    RecyclerView myReportsView;
    HashMap userObj;
    ArrayList<Post> reports;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_reports);
        getSupportActionBar().setTitle("Issues reported by you");
        reports = new ArrayList<>();
        myUser = Configs.getUser();
        userObj = Configs.fetchUserInfo(MyReports.this, false);
        myReportsView = findViewById(R.id.myReportsView);
        DatabaseReference postsRef = Configs.getDbRef().child("reports").child((String) userObj.get("region"));
        postsRef.keepSynced(true);
        ReportsAdapter listAdapter = new ReportsAdapter(MyReports.this,reports, MyReports.this);
        myReportsView.setAdapter(listAdapter);
        myReportsView.setLayoutManager(new LinearLayoutManager(MyReports.this));
        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                Post post = dataSnapshot.getValue(Post.class);
                assert post != null;
                if (post.getPostUserEmail().equals(myUser.getEmail())) {
                    reports.add(0, post);
                    listAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                try {
                    Post post = snapshot.getValue(Post.class);
                    assert post != null;
                    if (post.getPostUserEmail().equals(myUser.getEmail())) {
                        reports.set(Configs.getPostIndex(reports, snapshot.getKey()), snapshot.getValue(Post.class));
                        listAdapter.notifyDataSetChanged();
                    }
                } catch (ArrayIndexOutOfBoundsException unused) {}
            }


            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                try {
                    reports.remove(Configs.getPostIndex(reports, dataSnapshot.getKey()));
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

    }
}