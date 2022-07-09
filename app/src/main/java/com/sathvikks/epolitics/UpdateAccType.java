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

    public void fetchAccType(String accType) {
        listener.onAccTypeUpdate(accType);
    }
}
