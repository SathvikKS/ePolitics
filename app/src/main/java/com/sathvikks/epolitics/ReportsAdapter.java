package com.sathvikks.epolitics;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.gson.Gson;

import java.net.URL;
import java.util.ArrayList;

public class ReportsAdapter extends RecyclerView.Adapter<PostAdapter.MyViewHolder> {
    FirebaseUser myUser;
    Context context;
    ArrayList<Post> reports;
    DatabaseReference dbRef;
    Intent editReportConent;
    private final PostView pvi;
    public ReportsAdapter(Context context, ArrayList<Post> reports, PostView pvi) {
        this.context = context;
        this.reports = reports;
        dbRef = Configs.getDbRef();
        this.pvi = pvi;
        myUser = Configs.getUser();
        editReportConent = new Intent(context, EditReport.class);
    }

    @NonNull
    @Override
    public PostAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.post_layout, parent, false);
        return new PostAdapter.MyViewHolder(view, pvi, reports);
    }

    @Override
    public void onBindViewHolder(@NonNull PostAdapter.MyViewHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.postUserName.setText(reports.get(position).getPostUserName());
        holder.postTime.setText(reports.get(position).getPostDateTime().substring(0, 16));
        if (reports.get(position).getPostDescription().length() > 120)
            holder.postDescription.setText(reports.get(position).getPostDescription().substring(0, 100)+"\n... Read more");
        else
            holder.postDescription.setText(reports.get(position).getPostDescription());
        if (reports.get(position).getEdited()) {
            holder.postLayoutEdited.setVisibility(View.VISIBLE);
        }
        if (reports.get(position).getPostUserEmail().equals(myUser.getEmail())) {
            holder.postOptions.setVisibility(View.VISIBLE);
            holder.postOptions.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PopupMenu popup = new PopupMenu(context, holder.postOptions);
                    popup.inflate(R.menu.post_menu_author);
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @Override
                        public boolean onMenuItemClick(MenuItem item) {
                            switch (item.getItemId()) {
                                case R.id.postOptionsEdit:
                                    Gson gson = new Gson();
                                    editReportConent.putExtra("report", gson.toJson(reports.get(position)));
                                    context.startActivity(editReportConent);
                                    break;
                                case R.id.postOptionsDelete:
                                    new AlertDialog.Builder(context)
                                            .setTitle("Remove issue report")
                                            .setMessage("Are you sure you want to delete this issue report?")
                                            .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int which) {
                                                    dialog.dismiss();
                                                }
                                            })
                                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialogInterface, int i) {
                                                    dbRef.child("reports").child((String)Configs.userObj.get("region")).child(reports.get(position).postId).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if (task.isSuccessful()) {
                                                                Toast.makeText(context, "The issue report has been removed", Toast.LENGTH_SHORT).show();
                                                            } else {
                                                                Toast.makeText(context, "Unable to delete the report at the moment", Toast.LENGTH_SHORT).show();
                                                                Log.i("sksLog", "unable to remove the post: "+task.getException().toString());
                                                            }
                                                        }
                                                    });
                                                }
                                            })
                                            .setIcon(android.R.drawable.ic_dialog_alert)
                                            .show();
                                    break;
                            }
                            return false;
                        }
                    });
                    popup.show();
                }
            });
        } else {
            holder.postOptions.setVisibility(View.GONE);
        }
        dbRef.child("users").child(Configs.generateEmail(reports.get(position).postUserEmail)).child("profilePicUrl").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
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
            new URL(reports.get(position).getPostImage()).toURI();
            holder.postImage.setVisibility(View.VISIBLE);
            Glide.with(context)
                    .load(reports.get(position).getPostImage())
                    .into(holder.postImage);
        } catch (Exception e) {
            holder.postImage.setVisibility(View.GONE);
        }

    }

    @Override
    public int getItemCount() {
        return reports.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        TextView postUserName, postTime, postDescription, postLayoutEdited, postOptions;
        ImageView postUserPic, postImage;
        public MyViewHolder(@NonNull View itemView, PostView pvi, ArrayList<Post> reports) {
            super(itemView);
            postLayoutEdited = itemView.findViewById(R.id.postLayoutEdited);
            postUserName = itemView.findViewById(R.id.postUserName);
            postTime = itemView.findViewById(R.id.postTime);
            postDescription = itemView.findViewById(R.id.postDescription);
            postUserPic = itemView.findViewById(R.id.postUserPic);
            postImage = itemView.findViewById(R.id.postImage);
            postOptions = itemView.findViewById(R.id.postOptions);
            postOptions.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
        }
    }
}
