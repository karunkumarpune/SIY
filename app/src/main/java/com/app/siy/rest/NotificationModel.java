package com.app.siy.rest;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by Manish-Pc on 20/01/2018.
 */

public class NotificationModel implements Serializable {

    @SerializedName("id")
    private String id;

    @SerializedName("user_id")
    private String userId;

    @SerializedName("receiver_id")
    private String receiverId;

    @SerializedName("message")
    private String message;

    @SerializedName("notification_type")
    private String notificationType;

    @SerializedName("created_at")
    private String createdTime;

    @SerializedName("updated_at")
    private String updatedTime;


    public NotificationModel() {
    }

    public NotificationModel(String id, String userId, String receiverId, String message,
                             String notificationType, String createdTime, String updatedTime) {
        this.id = id;
        this.userId = userId;
        this.receiverId = receiverId;
        this.message = message;
        this.notificationType = notificationType;
        this.createdTime = createdTime;
        this.updatedTime = updatedTime;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getNotificationType() {
        return notificationType;
    }

    public void setNotificationType(String notificationType) {
        this.notificationType = notificationType;
    }

    public String getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(String createdTime) {
        this.createdTime = createdTime;
    }

    public String getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(String updatedTime) {
        this.updatedTime = updatedTime;
    }
}
