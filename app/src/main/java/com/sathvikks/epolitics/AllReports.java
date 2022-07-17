package com.sathvikks.epolitics;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;

public class AllReports extends AppCompatActivity implements PostView{
    RecyclerView recyclerView;
    ArrayList<Post> reports;
    Intent viewReportIntent;
    HashMap userObj;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_reports);
        viewReportIntent = new Intent(getApplicationContext(), ViewPost.class);
        recyclerView = findViewById(R.id.allReports);
        reports = new ArrayList<>();
        userObj = Configs.fetchUserInfo(AllReports.this, false);
        DatabaseReference postsRef = Configs.getDbRef().child("reports").child((String) userObj.get("region"));
        postsRef.keepSynced(true);
        PostAdapter listAdapter = new PostAdapter(AllReports.this,reports, AllReports.this);
        recyclerView.setAdapter(listAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(AllReports.this));
        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                Post post = dataSnapshot.getValue(Post.class);
                reports.add(0, post);
                listAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                try {
                    reports.set(Configs.getPostIndex(reports, snapshot.getKey()), snapshot.getValue(Post.class));
                    listAdapter.notifyDataSetChanged();
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
    public void onPostClick(Post report) {
        Gson gson = new Gson();
        viewReportIntent.putExtra("post", gson.toJson(report));
        startActivity(viewReportIntent);
    }
}