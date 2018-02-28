package com.app.siy.firebase.model;

/**
 * Created by Manish-Pc on 15/01/2018.
 */

// Model to Create chat message in Firebase.
public class Message {
    private String message;
    private boolean is_seen;
    private String type;
    private String date_time_of_message;
    private String sender_id;
    private String receiver_id;

    public Message() {
    }

    public Message(String message, boolean is_seen, String type, String date_time_of_message,
                   String sender_id, String receiver_id) {
        this.message = message;
        this.is_seen = is_seen;
        this.type = type;
        this.date_time_of_message = date_time_of_message;
        this.sender_id = sender_id;
        this.receiver_id = receiver_id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean is_seen() {
        return is_seen;
    }

    public void setIs_seen(boolean is_seen) {
        this.is_seen = is_seen;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTime_of_message() {
        return date_time_of_message;
    }

    public void setTime_of_message(String time_of_message) {
        this.date_time_of_message = time_of_message;
    }

    public String getSender_id() {
        return sender_id;
    }

    public void setSender_id(String sender_id) {
        this.sender_id = sender_id;
    }

    public String getReceiver_id() {
        return receiver_id;
    }

    public void setReceiver_id(String receiver_id) {
        this.receiver_id = receiver_id;
    }
}