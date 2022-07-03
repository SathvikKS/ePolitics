package com.sathvikks.epolitics;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

public class Configs extends MainActivity {
    public static HashMap userObj = new HashMap();
    private static Boolean success = false;
    private static FirebaseAuth mAuth;
    public static DatabaseReference getDbRef() {
        DatabaseReference dbRef = FirebaseDatabase.getInstance("https://epolitics-ecdb4-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference();
        return dbRef;
    }

    public static FirebaseAuth getmAuth() {
        mAuth = FirebaseAuth.getInstance();
        return mAuth;
    }

    public static FirebaseUser getUser() {
        FirebaseUser fUser = FirebaseAuth.getInstance().getCurrentUser();
        return fUser;
    }

    public static String generateEmail(String mail) {
        return mail.replace('.', ',');
    }

    public void appendLog(String text)
    {
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
        ProgressDialog dialog = Configs.showProcessDialogue(context, "Fetching your information");
        dialog.show();
        if (userObj.isEmpty() || force == true) {
            Log.i("sksLog", "Hashmap Empty");
            dbRef.child("users").child(Configs.generateEmail(Configs.getUser().getEmail())).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    if (!task.isSuccessful()) {
                        Log.i("sksLog", "unable to fetch user info\n" + task.getException().toString());
                    } else {
                        userObj = (HashMap) task.getResult().getValue();
                    }
                }
            });
        } else
            Log.i("sksLog", "Hashmap not empty");
        dialog.hide();
        return userObj;
    }

    public static ProgressDialog showProcessDialogue(Context context, String message) {
        ProgressDialog dialog = new ProgressDialog(context);
        dialog.setMessage(message);
        dialog.setCancelable(false);
        dialog.setInverseBackgroundForced(false);
        return dialog;
    }
    public static String BitMapToString(Bitmap bitmap){
        ByteArrayOutputStream baos=new  ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,100, baos);
        byte [] b=baos.toByteArray();
        String temp= Base64.encodeToString(b, Base64.DEFAULT);
        return temp;
    }
    /**
     * @param encodedString
     * @return bitmap (from given string)
     */
    public static Bitmap StringToBitMap(String encodedString){
        try {
            byte [] encodeByte=Base64.decode(encodedString,Base64.DEFAULT);
            Bitmap bitmap= BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            return bitmap;
        } catch(Exception e) {
            e.getMessage();
            return null;
        }
    }
}
