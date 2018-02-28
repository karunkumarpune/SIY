package com.app.siy.rest;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Manish-Pc on 07/02/2018.
 */

public class ParentModel {

    @SerializedName("user_id")
    private int userId;

    @SerializedName("first_name")
    private String firstName;

    @SerializedName("latitude")
    private String latitude;

    @SerializedName("longitude")
    private String longitude;

    @SerializedName("device_token")
    private String deviceToken;

    @SerializedName("device_type")
    private int deviceType;

    @SerializedName("user_type")
    private int userType;

    @SerializedName("last_name")
    private String lastName;

    @SerializedName("is_request_student")
    private int isRequestStudent;

    @SerializedName("term_condition")
    private int termsAndConditions;

    @SerializedName("desired_commute")
    private String desiredCommute;

    @SerializedName("firebase_id")
    private String firebaseId;

    @SerializedName("gender")
    private String gender;

    @SerializedName("language")
    private String language;

    @SerializedName("image")
    private String image;

    @SerializedName("professional")
    private String professional;


    @SerializedName("is_notify")
    private int isNotify;

    @SerializedName("is_bussy")
    private int isBusy;


    public ParentModel() {
    }

    public ParentModel(int userId, String firstName, String latitude, String longitude,
                       String deviceToken, int deviceType, int userType, String lastName,
                       int isRequestStudent, int termsAndConditions, String desiredCommute,
                       String firebaseId, String gender, String language, String image,
                       String professional, int isNotify, int isBusy) {
        this.userId = userId;
        this.firstName = firstName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.deviceToken = deviceToken;
        this.deviceType = deviceType;
        this.userType = userType;
        this.lastName = lastName;
        this.isRequestStudent = isRequestStudent;
        this.termsAndConditions = termsAndConditions;
        this.desiredCommute = desiredCommute;
        this.firebaseId = firebaseId;
        this.gender = gender;
        this.language = language;
        this.image = image;
        this.professional = professional;
        this.isNotify = isNotify;
        this.isBusy = isBusy;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getDeviceToken() {
        return deviceToken;
    }

    public void setDeviceToken(String deviceToken) {
        this.deviceToken = deviceToken;
    }

    public int getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(int deviceType) {
        this.deviceType = deviceType;
    }

    public int getUserType() {
        return userType;
    }

    public void setUserType(int userType) {
        this.userType = userType;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public int getIsRequestStudent() {
        return isRequestStudent;
    }

    public void setIsRequestStudent(int isRequestStudent) {
        this.isRequestStudent = isRequestStudent;
    }

    public int getTermsAndConditions() {
        return termsAndConditions;
    }

    public void setTermsAndConditions(int termsAndConditions) {
        this.termsAndConditions = termsAndConditions;
    }

    public String getDesiredCommute() {
        return desiredCommute;
    }

    public void setDesiredCommute(String desiredCommute) {
        this.desiredCommute = desiredCommute;
    }

    public String getFirebaseId() {
        return firebaseId;
    }

    public void setFirebaseId(String firebaseId) {
        this.firebaseId = firebaseId;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getProfessional() {
        return professional;
    }

    public void setProfessional(String professional) {
        this.professional = professional;
    }

    public int getIsNotify() {
        return isNotify;
    }

    public void setIsNotify(int isNotify) {
        this.isNotify = isNotify;
    }

    public int getIsBusy() {
        return isBusy;
    }

    public void setIsBusy(int isBusy) {
        this.isBusy = isBusy;
    }
}
