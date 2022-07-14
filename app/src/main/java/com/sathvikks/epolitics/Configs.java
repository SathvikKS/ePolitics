package com.sathvikks.epolitics;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.util.Base64;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Objects;


@SuppressWarnings("rawtypes")
public class Configs {
    public static HashMap<String, Object> userObj = new HashMap<>();
    public static SharedPreferences sp;
    static ProgressDialog dialog;

    /**
     * returns database reference object
     * @return DatabaseReference
     */
    public static DatabaseReference getDbRef() {
        return FirebaseDatabase.getInstance().getReference();
    }

    /**
     * returns storage reference object
     * @return StorageReference - storageRef
     */
    public static StorageReference getStorageRef() {
        return FirebaseStorage.getInstance().getReference();
    }

    /**
     * return storage reference from url
     * @param url url
     * @return StorageReference - storageRef
     */
    public static StorageReference getStorageRef(String url){
        return FirebaseStorage.getInstance().getReferenceFromUrl(url);
    }

    /**
     * returns the firebase authentication object
     * @return FirebaseAuth
     */
    public static FirebaseAuth getmAuth() {
        return FirebaseAuth.getInstance();
    }

    /**
     * returns the firebase user object
     * @return FirebaseUser
     */
    public static FirebaseUser getUser() {
        return FirebaseAuth.getInstance().getCurrentUser();
    }

    /**
     * replace '.' with '', from the email
     * @param mail email to replace dot with comma
     * @return email with comma instead of dot
     */
    public static String generateEmail(String mail) {
        return mail.replace('.', ',');
    }

    /**
     * fetch the information about the user logged in into a hashmap
     * @param context this
     * @param force force fetch user info
     * @return userObj hashmap
     */
    public static HashMap fetchUserInfo(Context context, Boolean force) {
        sp = context.getSharedPreferences("com.sathvikks.epolitics", Context.MODE_PRIVATE);
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
        dialog = Configs.showProcessDialogue(context, "Fetching your information");
        dialog.setProgress(0);
        if (userObj.isEmpty() || force) {
            dialog.show();
            if (!force)
                Log.i("sksLog", "Hashmap Empty");
            else
                Log.i("sksLog", "Hashmap forced");
            dbRef.child("users").child(Configs.generateEmail(Objects.requireNonNull(Configs.getUser().getEmail()))).get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DataSnapshot> task) {
                    if (!task.isSuccessful()) {
                        Log.i("sksLog", "unable to fetch user info\n" + task.getException());
                    } else {
                        userObj = (HashMap) task.getResult().getValue();
                        Log.i("sksLog", "fetched info:\n"+task.getResult().getValue());
                        if (getAccountType(context) == null) {
                            Log.i("sksLog", "acctype was null, set to: "+userObj.get("accType"));
                            MainActivity.uat.fetchAccType((String) userObj.get("accType"));
                            Configs.setAccountType(context, (String) userObj.get("accType"));
                        }
                        if (getAccRegion(context) == null || Objects.equals(getAccRegion(context), "null")) {
                            Log.i("sksLog", "region was null, set to: "+userObj.get("region"));
                            MainActivity.uar.fetchRegion((String) userObj.get("region"));
                            Configs.setAccRegion(context, (String) userObj.get("region"));
                        }
                        dialog.dismiss();
                        if (sp.getString("profilePicture", null) == null) {
                            if (userObj.get("profilePicUrl") != null) {
                                dialog.dismiss();
                                dialog = Configs.showProcessDialogue(context, "Downloading profile picture");
                                dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                                dialog.setIndeterminate(false);
                                dialog.setProgress(0);
                                dialog.show();
                                Log.i("sksLog", "profile picture url is: "+userObj.get("profilePicUrl"));
                                StorageReference dpRef = Configs.getStorageRef((String) userObj.get("profilePicUrl"));
                                //StorageReference dpRef = FirebaseStorage.getInstance().getReferenceFromUrl((String) Objects.requireNonNull(userObj.get("profilePicUrl")));
                                try {
                                    File localFile = File.createTempFile("images", "jpg");
                                    dpRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                        @Override
                                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                            Bitmap bitmap = BitmapFactory.decodeFile(localFile.getAbsolutePath());
                                            Configs.userObj.put("profilePic", new BitmapDrawable(context.getResources(), bitmap));
                                            Configs.storeProfilePic(bitmap, context);
                                            dialog.dismiss();
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception exception) {
                                            dialog.dismiss();
                                            Log.i("sksLog", "unable to download picture: "+ exception);
                                        }
                                    })
                                            .addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
                                                @Override
                                                public void onProgress(@NonNull FileDownloadTask.TaskSnapshot snapshot) {
                                                    dialog.setProgress((int) snapshot.getBytesTransferred()/1024);
                                                    if ((int) snapshot.getTotalByteCount() > 0)
                                                        dialog.setMax((int) snapshot.getTotalByteCount()/1024);
                                                    double progress = (100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount();
                                                    Log.d("sksLog", "Download is " + progress + "% done");
                                                }
                                            });
                                } catch (IOException e) {
                                    dialog.dismiss();
                                    e.printStackTrace();
                                }
                            } else {
                                dialog.dismiss();
                            }
                        } else {
                            Log.i("sksLog", "shared pref not null "+sp.getString("profilePicture", null).substring(0, 4));
                            Configs.userObj.put("profilePic", new BitmapDrawable(context.getResources(), Configs.StringToBitMap(sp.getString("profilePicture", null))));
                        }
                    }

                }
            });
        } else
            Log.i("sksLog", "Hashmap not empty");
        return userObj;
    }

    /**
     * Stores profile picture in the stored preferences
     * @param bitmap  picture to store
     * @param context  this
     */
    public static void storeProfilePic(Bitmap bitmap, Context context) {
        sp = context.getSharedPreferences("com.sathvikks.epolitics", Context.MODE_PRIVATE);
        sp.edit().putString("profilePicture", Configs.BitMapToString(bitmap)).apply();
    }

    /**
     * Removes profile picture from shared preferences
     * @param context this
     */
    public static void removeProfilePic(Context context) {
        sp = context.getSharedPreferences("com.sathvikks.epolitics", Context.MODE_PRIVATE);
        sp.edit().remove("profilePicture").apply();
    }

    /**
     * show a progress dialogue
     * @param context this
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
     * covert bitmap to base64 string
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
     * extract bitmap from base64
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
     * get the account type of the user logged in
     * @param context this
     * @return accType
     */
    public static String getAccountType(Context context){
        sp = context.getSharedPreferences("com.sathvikks.epolitics", Context.MODE_PRIVATE);
        return sp.getString("accType", null);
    }

    /**
     * store the account type of the user in shared preferences
     * @param context this
     * @param accType Account Type
     */
    public static void setAccountType(Context context, String accType) {
        sp = context.getSharedPreferences("com.sathvikks.epolitics", Context.MODE_PRIVATE);
        sp.edit().putString("accType", accType).apply();
    }

    /**
     * delete the account type stored in shared preferences
     * @param context this
     */
    public static void delAccountType(Context context) {
        sp = context.getSharedPreferences("com.sathvikks.epolitics", Context.MODE_PRIVATE);
        sp.edit().remove("accType").apply();
    }

    /**
     * reduces the size of the image
     * @param bitmap  bitmap to resize
     * @param percent  resize percent
     * @return resized bitmap
     */
    public static Bitmap getResizedBitmap(Bitmap bitmap, int percent) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        float bitmapRatio = (float)width / (float) height;
        if (bitmapRatio > 1) {
            width = percent*width/100;
            height = (int) (width / bitmapRatio);
        } else {
            height = percent*height/100;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(bitmap, width, height, true);
    }

    public static String getAccRegion(Context context) {
        sp = context.getSharedPreferences("com.sathvikks.epolitics", Context.MODE_PRIVATE);
        return sp.getString("region", null);
    }

    public static void setAccRegion(Context context, String region) {
        sp = context.getSharedPreferences("com.sathvikks.epolitics", Context.MODE_PRIVATE);
        sp.edit().putString("region", region).apply();
    }

    public static void delAccRegion(Context context) {
        sp = context.getSharedPreferences("com.sathvikks.epolitics", Context.MODE_PRIVATE);
        sp.edit().remove("region").apply();
    }
}
