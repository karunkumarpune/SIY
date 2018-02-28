package com.app.siy.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by Manish-Pc on 18/01/2018.
 */

public class DataLibraryModel implements Parcelable {
    @SerializedName("datalibary_id")
    private int dataLibraryId;

    @SerializedName("user_id")
    private int userId;
    @SerializedName("data_type")
    private int dataType;

    @SerializedName("data")
    private String data;

    @SerializedName("is_shared")
    private int isShared;

    @SerializedName("created_at")
    private String createdDate;

    @SerializedName("updated_at")
    private String updatedDate;

    @SerializedName("thumbnail")
    private String thumbnail;

    public DataLibraryModel() {
    }

    public DataLibraryModel(int dataLibraryId, int userId, int dataType, String data, int isShared,
                            String createdDate, String updatedDate, String thumbnail) {
        this.dataLibraryId = dataLibraryId;
        this.userId = userId;
        this.dataType = dataType;
        this.data = data;
        this.isShared = isShared;
        this.createdDate = createdDate;
        this.updatedDate = updatedDate;
        this.thumbnail = thumbnail;
    }


    protected DataLibraryModel(Parcel in) {
        dataLibraryId = in.readInt();
        userId = in.readInt();
        dataType = in.readInt();
        data = in.readString();
        isShared = in.readInt();
        createdDate = in.readString();
        updatedDate = in.readString();
    }


    public static final Creator<DataLibraryModel> CREATOR = new Creator<DataLibraryModel>() {
        @Override
        public DataLibraryModel createFromParcel(Parcel in) {
            return new DataLibraryModel(in);
        }

        @Override
        public DataLibraryModel[] newArray(int size) {
            return new DataLibraryModel[size];
        }
    };

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public int getDataLibraryId() {
        return dataLibraryId;
    }

    public void setDataLibraryId(int dataLibraryId) {
        this.dataLibraryId = dataLibraryId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getDataType() {
        return dataType;
    }

    public void setDataType(int dataType) {
        this.dataType = dataType;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public int getIsShared() {
        return isShared;
    }

    public void setIsShared(int isShared) {
        this.isShared = isShared;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public String getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(String updatedDate) {
        this.updatedDate = updatedDate;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(dataLibraryId);
        dest.writeInt(userId);
        dest.writeInt(dataType);
        dest.writeString(data);
        dest.writeInt(isShared);
        dest.writeString(createdDate);
        dest.writeString(updatedDate);
    }
}
