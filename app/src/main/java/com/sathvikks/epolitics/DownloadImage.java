package com.sathvikks.epolitics;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadImage extends AsyncTask<String, Integer, Void> {
    private Context context;
    Drawable d;
    private ProgressDialog dialog;
    Bitmap bitmap;

    public DownloadImage(Context context, ProgressDialog dialog) {
        this.context = context;
        this.dialog = dialog;
    }

    @Override
    protected Void doInBackground(String... strings) {
        int count;
        try {
            URL url = new URL(strings[0]);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.connect();
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                Log.i( "sksLog", "Server returned HTTP " + connection.getResponseCode()
                        + " " + connection.getResponseMessage());
                return null;
            }
            InputStream is = connection.getInputStream();
            bitmap = BitmapFactory.decodeStream(is);

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void unused) {
        super.onPostExecute(unused);
        d = new BitmapDrawable(context.getResources(), bitmap);
        Configs.userObj.put("profilePic", d);
        Configs.userObj.put("profilePicB", bitmap);
        Log.i("sksLog", "bitmap drawable" +d.toString());
        dialog.dismiss();
    }
}
