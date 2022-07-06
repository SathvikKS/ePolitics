package com.sathvikks.epolitics;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

public class Configs {
    public static HashMap userObj = new HashMap();
    public static SharedPreferences sp;
    static ProgressDialog dialog;

    /**
     *
     * @return DatabaseReference
     */
    public static DatabaseReference getDbRef() {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
        return dbRef;
    }

    /**
     *
     * @return FirebaseAuth
     */
    public static FirebaseAuth getmAuth() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        return mAuth;
    }

    /**
     *
     * @return FirebaseUser
     */
    public static FirebaseUser getUser() {
        FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
        return fUser;
    }

    /**
     *
     * @param mail email to replace dot with comma
     * @return email with comma instead of dot
     */
    public static String generateEmail(String mail) {
        return mail.replace('.', ',');
    }

    public void appendLog(String text) {
        File logFile = new File("sdcard/ePolitics.txt");
        if (!logFile.exists())
        {
            try
            {
                logFile.createNewFile();
            }
            catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        try
        {
            //BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            buf.append(text);
            buf.newLine();
            buf.close();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static HashMap fetchUserInfo(Context context, Boolean force) {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
        dialog = Configs.showProcessDialogue(context, "Fetching your information");
        if (userObj.isEmpty() || force) {
            dialog.show();
            if (!force)
                Log.i("sksLog", "Hashmap Empty");
            else
                Log.i("sksLog", "Hashmap forced");
            dbRef.child("users").child(Configs.generateEmail(Configs.getUser().getEmail())).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    if (!task.isSuccessful()) {
                        Log.i("sksLog", "unable to fetch user info\n" + task.getException().toString());
                    } else {
                        userObj = (HashMap) task.getResult().getValue();
                        Log.i("sksLog", "fetched info:\n");
                    }
                    dialog.dismiss();
                }
            });
        } else
            Log.i("sksLog", "Hashmap not empty");
        return userObj;
    }

    /**
     *
     * @param context context
     * @param message message to be displayed
     * @return returns the dialog object
     */
    public static ProgressDialog showProcessDialogue(Context context, String message) {
        ProgressDialog dialog = new ProgressDialog(context);
        dialog.setMessage(message);
        dialog.setCancelable(false);
        dialog.setInverseBackgroundForced(false);
        return dialog;
    }

    /**
     *
     * @param bitmap bitmap to be converted to string
     * @return the string format of the bitmap
     */
    public static String BitMapToString(Bitmap bitmap) {
        ByteArrayOutputStream baos=new  ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,100, baos);
        byte [] b=baos.toByteArray();
        return Base64.encodeToString(b, Base64.DEFAULT);
    }

    /**
     * @param encodedString string to be converted to bitmap
     * @return bitmap (from given string)
     */
    public static Bitmap StringToBitMap(String encodedString) {
        try {
            byte [] encodeByte=Base64.decode(encodedString,Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
        } catch(Exception e) {
            e.getMessage();
            return null;
        }
    }

    /**
     *
     * @param context this
     * @return accType
     */
    public static String getAccountType(Context context){
        sp = context.getSharedPreferences("com.sathvikks.epolitics", Context.MODE_PRIVATE);
        return sp.getString("accType", null);
    }

    /**
     *
     * @param context this
     * @param accType Account Type
     */
    public static void setAccountType(Context context, String accType) {
        sp = context.getSharedPreferences("com.sathvikks.epolitics", Context.MODE_PRIVATE);
        sp.edit().putString("accType", accType).apply();
    }

    /**
     *
     * @param context this
     */
    public static void delAccountType(Context context) {
        sp = context.getSharedPreferences("com.sathvikks.epolitics", Context.MODE_PRIVATE);
        sp.edit().remove("accType").apply();
    }
}
