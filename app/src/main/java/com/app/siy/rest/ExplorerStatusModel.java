package com.app.siy.rest;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Manish-Pc on 05/02/2018.
 */

public class ExplorerStatusModel {

    @SerializedName("id")
    public int id;

    @SerializedName("explorer_id")
    public int explorerId;

    @SerializedName("recorder_id")
    public int recorderId;

    @SerializedName("explorer_notify_status")
    public int explorerNotifyStatus;

    @SerializedName("recorder_notify_status")
    public int recorderNotifyStatus;

    @SerializedName("created_at")
    public String createdDate;

    public ExplorerStatusModel() {
    }

    public ExplorerStatusModel(int id, int explorerId, int recorderId, int explorerNotifyStatus,
                               int recorderNotifyStatus, String createdDate) {
        this.id = id;
        this.explorerId = explorerId;
        this.recorderId = recorderId;
        this.explorerNotifyStatus = explorerNotifyStatus;
        this.recorderNotifyStatus = recorderNotifyStatus;
        this.createdDate = createdDate;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getExplorerId() {
        return explorerId;
    }

    public void setExplorerId(int explorerId) {
        this.explorerId = explorerId;
    }

    public int getRecorderId() {
        return recorderId;
    }

    public void setRecorderId(int recorderId) {
        this.recorderId = recorderId;
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

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }
}
