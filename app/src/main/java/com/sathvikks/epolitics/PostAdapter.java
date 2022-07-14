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

import java.util.ArrayList;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.MyViewHolder> {
    Context context;
    ArrayList<Post> posts;

    public PostAdapter(Context context, ArrayList<Post> posts) {
        this.context = context;
        this.posts = posts;
    }

    @NonNull
    @Override
    public PostAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.post_layout, parent, false);
        return new PostAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostAdapter.MyViewHolder holder, int position) {
        holder.postUserName.setText(posts.get(position).getPostUserName());
        holder.postTime.setText(posts.get(position).getPostDateTime().substring(0, 16));
        if (posts.get(position).getPostDescription().length() > 120)
            holder.postDescription.setText(posts.get(position).getPostDescription().substring(0, 100)+"\n... Read more");
        else
            holder.postDescription.setText(posts.get(position).getPostDescription());
        if (posts.get(position).getPostUserPic() != null) {
            Glide.with(context)
                    .load(posts.get(position).getPostUserPic())
                    .into(holder.postUserPic);
        }
        if (posts.get(position).getPostImage() != null) {
            Glide.with(context)
                    .load(posts.get(position).getPostImage())
                    .into(holder.postImage);
        } else {
            holder.postImage.setVisibility(View.GONE);
        }

    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView postUserName, postTime, postDescription;
        ImageView postUserPic, postImage;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            postUserName = itemView.findViewById(R.id.postUserName);
            postTime = itemView.findViewById(R.id.postTime);
            postDescription = itemView.findViewById(R.id.postDescription);
            postUserPic = itemView.findViewById(R.id.postUserPic);
            postImage = itemView.findViewById(R.id.postImage);
        }
    }
}
