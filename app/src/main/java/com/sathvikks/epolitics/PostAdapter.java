package com.sathvikks.epolitics;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.MyViewHolder> {
    Context context;
    ArrayList<Post> posts;
    DatabaseReference dbRef;
    private final PostView pvi;
    public PostAdapter(Context context, ArrayList<Post> posts, PostView pvi) {
        this.context = context;
        this.posts = posts;
        dbRef = Configs.getDbRef();
        this.pvi = pvi;
    }

    @NonNull
    @Override
    public PostAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.post_layout, parent, false);
        return new PostAdapter.MyViewHolder(view, pvi, posts);
    }

    @Override
    public void onBindViewHolder(@NonNull PostAdapter.MyViewHolder holder, int position) {
        holder.postUserName.setText(posts.get(position).getPostUserName());
        holder.postTime.setText(posts.get(position).getPostDateTime().substring(0, 16));
        if (posts.get(position).getPostDescription().length() > 120)
            holder.postDescription.setText(posts.get(position).getPostDescription().substring(0, 100)+"\n... Read more");
        else
            holder.postDescription.setText(posts.get(position).getPostDescription());
        if (posts.get(position).getEdited()) {
            holder.postLayoutEdited.setVisibility(View.VISIBLE);
        }
        dbRef.child("users").child(Configs.generateEmail(posts.get(position).postUserEmail)).child("profilePicUrl").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.isSuccessful()) {
                    try {
                        new URL((String) task.getResult().getValue()).toURI();
                        Glide.with(context)
                                .load(task.getResult().getValue().toString())
                                .into(holder.postUserPic);
                    } catch (Exception ignored) {

                    }
                }
            }
        });
        try {
            new URL(posts.get(position).getPostImage()).toURI();
            holder.postImage.setVisibility(View.VISIBLE);
            Glide.with(context)
                    .load(posts.get(position).getPostImage())
                    .into(holder.postImage);
        } catch (Exception e) {
            holder.postImage.setVisibility(View.GONE);
        }

    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView postUserName, postTime, postDescription, postLayoutEdited;
        ImageView postUserPic, postImage;
        public MyViewHolder(@NonNull View itemView, PostView pvi, ArrayList<Post> posts) {
            super(itemView);
            postLayoutEdited = itemView.findViewById(R.id.postLayoutEdited);
            postUserName = itemView.findViewById(R.id.postUserName);
            postTime = itemView.findViewById(R.id.postTime);
            postDescription = itemView.findViewById(R.id.postDescription);
            postUserPic = itemView.findViewById(R.id.postUserPic);
            postImage = itemView.findViewById(R.id.postImage);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (pvi != null) {
                        int pos = getAdapterPosition();
                        if (pos != RecyclerView.NO_POSITION) {
                            pvi.onPostClick(posts.get(pos));
                        }
                    }
                }
            });
        }
    }
}
