package com.app.siy.firebase.model;

import java.io.Serializable;

/**
 * Created by Manish-Pc on 14/12/2017.
 */

// Display Data from Firebase Database to RecyclerView.


public class FcmUser implements Serializable {

    private String device_token;
    private String first_name;
    private String last_name;
    private String profile_image;
    private String user_id;
    private String user_type;
    private boolean online_status;
    private String registration_date_time;
    private String number_of_new_messages;


    public FcmUser() {
    }

    public FcmUser(String device_token, String first_name, String last_name, String profile_image,
                   String user_id, String user_type, boolean online_status,
                   String registration_date_time, String number_of_new_messages) {
        this.device_token = device_token;
        this.first_name = first_name;
        this.last_name = last_name;
        this.profile_image = profile_image;
        this.user_id = user_id;
        this.user_type = user_type;
        this.online_status = online_status;
        this.registration_date_time = registration_date_time;
        this.number_of_new_messages = number_of_new_messages;
    }

    public String getDevice_token() {
        return device_token;
    }

    public void setDevice_token(String device_token) {
        this.device_token = device_token;
    }

    public String getFirst_name() {
        return first_name;
    }

    public void setFirst_name(String first_name) {
        this.first_name = first_name;
    }

    public String getLast_name() {
        return last_name;
    }

    public void setLast_name(String last_name) {
        this.last_name = last_name;
    }

    public String getProfile_image() {
        return profile_image;
    }

    public void setProfile_image(String profile_image) {
        this.profile_image = profile_image;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getUser_type() {
        return user_type;
    }

    public void setUser_type(String user_type) {
        this.user_type = user_type;
    }

    public boolean isOnline_status() {
        return online_status;
    }

    public void setOnline_status(boolean online_status) {
        this.online_status = online_status;
    }

    public String getRegistration_date_time() {
        return registration_date_time;
    }

    public void setRegistration_date_time(String registration_date_time) {
        this.registration_date_time = registration_date_time;
    }

    public String getNumber_of_new_messages() {
        return number_of_new_messages;
    }

    public void setNumber_of_new_messages(String number_of_new_messages) {
        this.number_of_new_messages = number_of_new_messages;
    }
}