package com.app.siy.rest;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by Manish-Pc on 05/02/2018.
 */

public class ServerResponseExplorerRequestStatus {

    @SerializedName("message")
    public String message;

    @SerializedName("status")
    public int status;

    @SerializedName("result")
    public ParentModel result;
}
