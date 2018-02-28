package com.app.siy.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import com.app.siy.R;
import com.app.siy.activity.explorer.ExplorerHomeActivity;
import com.app.siy.activity.recorder.RecorderHomeActivity;
import com.app.siy.sharedpreferences.MyPreferences;
import com.app.siy.utils.AppUtils;
import com.app.siy.utils.Constants;
import com.google.firebase.iid.FirebaseInstanceId;

public class SplashScreen extends Activity {

    // Shared Preferences Object. Use to check whether to User have already accessed Home Page or not.
    // If User has already accessed Home page then do not show Login/Sign up Screen.
    // Also check whether user is Explorer or Recorder.
    private MyPreferences myPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);


        myPreferences = new MyPreferences(this);

        // Getting the Device Token
        String deviceToken = FirebaseInstanceId.getInstance().getToken();
        AppUtils.log("Device Token on Splash : " + deviceToken);

        //Save Device Token to SharedPreferences.
        boolean b = myPreferences.saveString(MyPreferences.DEVICE_TOKEN, deviceToken);
        if (b) AppUtils.log("Device Token is Stored to SharedPreferences");
        else AppUtils.log("Unable is Store Device Token to SharedPreferences");

        AppUtils.log("Device Token From SharedPreferences : " + myPreferences.getString(MyPreferences.DEVICE_TOKEN));
        new SplashAsyncTask().execute();
    }


    private class SplashAsyncTask extends AsyncTask<Integer, Integer, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(Integer... params) {
            try {
                Thread.sleep(Constants.SPLASH_DURATION_TIME);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return "";
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(String aVoid) {
            super.onPostExecute(aVoid);
            boolean profileAccessStatus = myPreferences.getBoolean(MyPreferences.PROFILE_ACCESS_STATUS);
            AppUtils.log("Profile Access " + profileAccessStatus);
            if (profileAccessStatus) {
                //User already accessed the Home Page.
                //Now check the user type -
                if (myPreferences.getString(MyPreferences.USER_TYPE).equals(Constants.USER_TYPE_EXPLORER)) {
                    //Go to Explorer Home Page
                    Intent explorerHomeIntent = new Intent(SplashScreen.this, ExplorerHomeActivity.class);
                    startActivity(explorerHomeIntent);
                    finishAffinity();
                } else if (myPreferences.getString(MyPreferences.USER_TYPE).equals(Constants.USER_TYPE_RECORDER)) {
                    //Go to Recorder Home Page
                    Intent recorderHomeIntent = new Intent(SplashScreen.this, RecorderHomeActivity.class);
                    startActivity(recorderHomeIntent);
                    finishAffinity();
                }
            } else {
                //User will Access the Home Page for the first time
                // Hence Go to MainActivity.java
                Intent recorderHomeIntent = new Intent(SplashScreen.this, MainActivity.class);
                startActivity(recorderHomeIntent);
                finishAffinity();
            }
        }
    }

    // Disable Back Button.
    @Override
    public void onBackPressed() {
    }
}