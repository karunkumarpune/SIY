package com.app.siy.rest;

import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.PartMap;

/**
 * Created by Manish-Pc on 19/12/2017.
 */

public interface ApiInterface {

    @FormUrlEncoded
    @POST("signup")
    Call<ServerResponse> signup(
            @Field("email") String email,
            @Field("password") String password,
            @Field("latitude") String latitude,
            @Field("longitude") String longitude,
            @Field("user_type") String userType,
            @Field("device_type") String deviceType,
            @Field("device_token") String deviceToken
    );


    @FormUrlEncoded
    @POST("login")
    Call<ServerResponse> signIn(
            @Field("email") String email,
            @Field("password") String password,
            @Field("latitude") String latitude,
            @Field("longitude") String longitude,
            @Field("user_type") String userType,
            @Field("device_type") String deviceType,
            @Field("device_token") String deviceToken);


    //Complete profile for Explorer
    @FormUrlEncoded
    @POST("createProfile")
    Call<ServerResponse> completeProfile(
            @Header("accessToken") String accessToken,
            @Field("user_type") String userType,
            @Field("first_name") String firstName,
            @Field("last_name") String lastName,
            @Field("dob") String dob,
            @Field("is_request_student") String isRequestStudent,
            @Field("term_condition") String termsAndCondition,
            @Field("language") String language,
            @Field("profile_status") String profileStatus
    );


    //Complete Profile for Recorder
    // This api will also be used for EditProfile page also.
    @Multipart
    @POST("createProfile")
    Call<ServerResponse> completeProfileRecorder(
            @Header("accessToken") String accessToken,
            @PartMap Map<String, RequestBody> partMap,
            @Part MultipartBody.Part image,
            @Part MultipartBody.Part certificate
    );


    @FormUrlEncoded
    @POST("verifyOtp")
    Call<ServerResponse> verifyOtp(
            @Field("otp") String otp,
            @Field("user_id") String userId
    );

    @FormUrlEncoded
    @POST("changeEmail")
    Call<ServerResponse> changeEmailId(
            @Field("email") String email,
            @Field("user_id") String userId
    );


    //Forgot Password.
    @FormUrlEncoded
    @POST("forgetPassword")
    Call<ServerResponse> forgetPassword(
            @Field("user_type") String userType,
            @Field("email") String email
    );

    //Reset Password
    @FormUrlEncoded
    @POST("resetPassword")
    Call<ServerResponse> resetPassword(
            @Field("user_id") String userId,
            @Field("password") String password
    );

    //Logout
    @POST("logout")
    Call<ServerResponse> logout(
            @Header("accessToken") String accessToken
    );

    @FormUrlEncoded
    @POST("searchRecorder")
    Call<ServerResponse> searchRecorder(
            @Header("accessToken") String accessToken,
            @Field("latitude") String latidude,
            @Field("longitude") String longitude
    );

    @FormUrlEncoded
    @POST("getUserDetail")
    Call<ServerResponse> getSingleUserDetails(
            @Header("accessToken") String accessToken,
            @Field("user_type") String userType,
            @Field("user_id") String userId
    );

    @FormUrlEncoded
    @POST("saveFirebaseId")
    Call<ServerResponse> saveFirebaseIdToServer(
            @Header("accessToken") String accessToken,
            @Field("firebase_id") String firebaseId
    );

    @FormUrlEncoded
    @POST("giveRating")
    Call<ServerResponse> giveRatingToRecorder(
            @Header("accessToken") String accessToken,
            @Field("recorder_id") String recorderId,
            @Field("rating") float rating
    );

    @GET("getDatalibrary")
    Call<ServerResponseDataLibrary> getAllDataLibraryFromServer(
            @Header("accessToken") String accessToken
    );


    @GET("notificationHistory")
    Call<ServerResponseNotification> getNotificationHistory(
            @Header("accessToken") String accessToken
    );


    @FormUrlEncoded
    @PUT("acceptRequest")
    Call<ServerResponse> acceptRequestByRecorder(
            @Header("accessToken") String accessToken,
            @Field("explorer_id") String explorerId,
            @Field("recorder_notify_status") String recorderNotifyStatus
    );

    @FormUrlEncoded
    @PUT("acceptRequestByExplorer")
    Call<ServerResponse> acceptRequestByExplorer(
            @Header("accessToken") String accessToken,
            @Field("recorder_id") String recorderId,
            @Field("explorer_notify_status") String notificationStatus
    );

    @Multipart
    @POST("addDatalibrary")
    Call<ServerResponse> addImageOrVideoToServer(
            @Header("accessToken") String accessToken,
            @Part MultipartBody.Part image,
            @Part("data_type") RequestBody dataType,
            @Part MultipartBody.Part certificate
    );

    @FormUrlEncoded
    @POST("shareData")
    Call<ServerResponse> shareData(
            @Header("acessToken") String accessToken,
            @Field("datalibary_id") String dataLibraryId
    );

    @GET("getExplorerRequestStatus")
    Call<ServerResponseExplorerRequestStatus> getExplorerRequestStatus(
            @Header("accessToken") String accessToken
    );


    @FormUrlEncoded
    @POST("getRequestAcceptStatus")
    Call<ServerResponseRequestAcceptedStatus> getRequestAcceptStatus(
            @Header("accessToken") String accessToken,
            @Field("recorder_id") String recorderId,
            @Field("explorer_id") String explorerId
    );

    @FormUrlEncoded
    @PUT("endChating")
    Call<ServerResponseEndChatting> endChatting(
            @Header("accessToken") String accessToken,  // Access Token of Current User.
            @Field("firebase_id") String firebaseId,       // firebase_id of other user.
            @Field("user_type") String userType,            // user_type of other user.
            @Field("chating_status") String chattingStatus  // 1    = End Chatting.
    );

}