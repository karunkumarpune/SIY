package com.app.siy.rest;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Manish-Pc on 08/02/2018.
 */

public class ServerResponseEndChatting {

    @SerializedName("firebase_id")
    public String firebaseId;


    @SerializedName("user_type")
    public String userType;

    @SerializedName("chating_status")
    public String chattingStatus;
}
