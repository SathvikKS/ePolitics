package com.sathvikks.epolitics;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.gson.Gson;

public class ViewPost extends AppCompatActivity {
    ImageView vpProfilePic, vpImage;
    TextView vpUserName, vpTime, vpDescription;
    DatabaseReference dbRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_post);
        Intent intent = getIntent();
        Gson gson = new Gson();
        Post post = gson.fromJson(intent.getStringExtra("post"), Post.class);
        dbRef = Configs.getDbRef();
        vpProfilePic = findViewById(R.id.vpProfilePic);
        vpImage = findViewById(R.id.vpImage);
        vpUserName = findViewById(R.id.vpUserName);
        vpTime = findViewById(R.id.vpTime);
        vpDescription = findViewById(R.id.vpDescription);
        vpUserName.setText(post.getPostUserName());
        vpTime.setText(post.getPostDateTime().substring(0, 16));
        vpDescription.setText(post.getPostDescription());
        dbRef.child("users").child(Configs.generateEmail(post.postUserEmail)).child("profilePicUrl").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult().getValue() != null) {
                        Glide.with(ViewPost.this)
                                .load(task.getResult().getValue().toString())
                                .into(vpProfilePic);
                    }
                }
            }
        });
        if (post.getPostImage() != null) {
            Glide.with(ViewPost.this)
                    .load(post.getPostImage())
                    .into(vpImage);
        }
    }
}