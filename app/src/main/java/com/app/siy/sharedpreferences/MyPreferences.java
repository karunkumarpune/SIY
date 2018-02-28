package com.app.siy.sharedpreferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

/**
 * Created by Manish-Pc on 05/12/2017.
 */

public class MyPreferences {

    //-------------------------------------------------------------

    private static final String PREF_FILE_NAME = "siy_pref_file";

    public static final String USER_TYPE = "user_type";
    public static final String DEVICE_TOKEN = "device_token";
    public static final String ACCESS_TOKEN = "access_token";
    public static final String USER_ID = "user_id";
    public static final String USER_MODEL = "user_model";
    public static final String FIREBASE_USER_ID = "firebase_user_id";
    public static final String MESSAGE_SENDER_ID = "message_sender_id";
    public static final String MESSAGE_RECEIVER_ID = "message_receiver_id";
    public static final String ACCEPT_JOB_STATUS = "accept_job_status";
    public static final String APPROVE_REQUEST_BY_EXPLORER = "approve_request";


    //Check, if the user is already login then directly move to Home page
    // instead of Login and Sign up page.
    public static final String PROFILE_ACCESS_STATUS = "profile_access_status";

    public static final String PASSWORD = "password";


    static SharedPreferences sharedPreferences;
    static SharedPreferences.Editor editor;

    public MyPreferences(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public boolean saveString(String key, String value) {
        return editor.putString(key, value).commit();
    }

    public String getString(String key) {
        return sharedPreferences.getString(key, "null");
    }

    public boolean clearPreferences() {
        return editor.clear().commit();
    }

    public boolean saveBoolean(String key, boolean value) {
        return editor.putBoolean(key, value).commit();
    }

    public boolean getBoolean(String key) {
        return sharedPreferences.getBoolean(key, false);
    }
}