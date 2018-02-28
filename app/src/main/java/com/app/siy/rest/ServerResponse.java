package com.app.siy.rest;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Manish-Pc on 19/12/2017.
 */

public class ServerResponse {

    @SerializedName("message")
    public String message;

    @SerializedName("result")
    public User user;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
