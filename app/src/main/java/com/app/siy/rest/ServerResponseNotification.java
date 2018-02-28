package com.app.siy.rest;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Manish-Pc on 20/01/2018.
 */

public class ServerResponseNotification {
    @SerializedName("message")
    public String message;


    @SerializedName("result")
    public NotificationModel notificationModel;


    public String getMessage() {
        return message;
    }
}


