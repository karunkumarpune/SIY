package com.app.siy.firebase.services;

import android.util.Log;

import com.app.siy.sharedpreferences.MyPreferences;
import com.app.siy.utils.AppUtils;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import static android.os.Build.VERSION_CODES.M;
import static com.bumptech.glide.gifdecoder.GifHeaderParser.TAG;

/**
 * Created by Manish-Pc on 08/01/2018.
 */

public class MyFirebaseInstanceIdService extends FirebaseInstanceIdService {
    MyPreferences myPreferences;

    @Override
    public void onTokenRefresh() {
        super.onTokenRefresh();

        myPreferences = new MyPreferences(this);

        String deviceToken = FirebaseInstanceId.getInstance().getToken();
        AppUtils.log("Refreshed Token : " + deviceToken);
        myPreferences.saveString(MyPreferences.DEVICE_TOKEN, deviceToken);

        //Log.d(TAG, "Refreshed token: " + refreshedToken);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.

        //sendRegistrationToServer(refreshedToken);
    }
}
