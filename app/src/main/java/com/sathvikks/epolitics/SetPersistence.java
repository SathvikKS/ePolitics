package com.sathvikks.epolitics;

import com.google.firebase.database.FirebaseDatabase;

public class SetPersistence extends android.app.Application {
    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}
