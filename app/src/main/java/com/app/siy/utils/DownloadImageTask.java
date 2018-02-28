package com.app.siy.utils;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

import com.app.siy.R;

import java.io.InputStream;

/**
 * Created by Manish-Pc on 03/01/2018.
 */

public class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {

    Context context;
    int requestCode;
    DownloadImageCallback imageCallback;
    Progress progressDialog;

    public DownloadImageTask() {
    }

    public DownloadImageTask(Context context, DownloadImageCallback imageCallback, int requestCode) {
        this.context = context;
        this.imageCallback = imageCallback;
        this.requestCode = requestCode;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        progressDialog = new Progress(context, R.style.CustomProgressDialogTheme);
        progressDialog.setCancelable(false);
        progressDialog.show();
    }


    @Override
    protected Bitmap doInBackground(String... URL) {

        String imageURL = URL[0];

        Bitmap bitmap = null;
        try {
            // Download Image from URL
            InputStream input = new java.net.URL(imageURL).openStream();
            // Decode Bitmap
            bitmap = BitmapFactory.decodeStream(input);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    @Override
    protected void onPostExecute(Bitmap result) {
        progressDialog.dismiss();
        // Set the bitmap into ImageView
        imageCallback.getImage(result, requestCode);
    }
}