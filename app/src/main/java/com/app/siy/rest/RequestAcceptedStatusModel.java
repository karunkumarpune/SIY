package com.app.siy.rest;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Manish-Pc on 08/02/2018.
 */

public class RequestAcceptedStatusModel {


    @SerializedName("explorer_notify_status")
    private int explorerNotifyStatus;

    @SerializedName("recorder_notify_status")
    private int recorderNotifyStatus;

    @SerializedName("handshake")
    private int handShake;


    public RequestAcceptedStatusModel(int explorerNotifyStatus, int recorderNotifyStatus, int handShake) {
        this.explorerNotifyStatus = explorerNotifyStatus;
        this.recorderNotifyStatus = recorderNotifyStatus;
        this.handShake = handShake;
    }

    public int getExplorerNotifyStatus() {
        return explorerNotifyStatus;
    }

    public void setExplorerNotifyStatus(int explorerNotifyStatus) {
        this.explorerNotifyStatus = explorerNotifyStatus;
    }

    public int getRecorderNotifyStatus() {
        return recorderNotifyStatus;
    }

    public void setRecorderNotifyStatus(int recorderNotifyStatus) {
        this.recorderNotifyStatus = recorderNotifyStatus;
    }

    public int getHandShake() {
        return handShake;
    }

    public void setHandShake(int handShake) {
        this.handShake = handShake;
    }
}
