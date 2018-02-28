package com.app.siy.rest;

import com.app.siy.model.DataLibraryModel;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

/**
 * Created by Manish-Pc on 18/01/2018.
 */

public class ServerResponseDataLibrary {

    @SerializedName("message")
    public String message;

    @SerializedName("result")
    public ArrayList<DataLibraryModel> dataLibraryModel;

    public String getMessage() {
        return message;
    }
}
