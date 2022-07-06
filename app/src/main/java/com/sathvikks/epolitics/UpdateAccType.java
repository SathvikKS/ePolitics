package com.sathvikks.epolitics;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;

import java.util.HashMap;

public class UpdateAccType {
    private myListener listener;

    public void setListener(myListener listener) {
        this.listener = listener;
    }

    public void fetchAccType(Context context) {
        DatabaseReference dbRef = Configs.getDbRef();
        dbRef.child("users").child(Configs.generateEmail(Configs.getUser().getEmail())).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                HashMap userObj;
                if (!task.isSuccessful()) {
                    Log.i("sksLog", "unable to fetch user info\n" + String.valueOf(task.getException()));
                } else {
                    userObj = (HashMap) task.getResult().getValue();
                    listener.onAccTypeUpdate((String) userObj.get("accType"));
                    Configs.setAccountType(context, (String) userObj.get("accType"));
                }
            }
        });
    }
}
