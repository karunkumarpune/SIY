package com.app.siy.activity.explorer;

import android.content.Intent;
import android.provider.CallLog;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.app.siy.R;
import com.app.siy.activity.ChatRoomActivity;
import com.app.siy.firebase.services.MyFirebaseMessagingService;
import com.app.siy.rest.ApiClient;
import com.app.siy.rest.ApiInterface;
import com.app.siy.rest.RequestAcceptedStatusModel;
import com.app.siy.rest.ServerResponse;
import com.app.siy.rest.ServerResponseRequestAcceptedStatus;
import com.app.siy.rest.User;
import com.app.siy.sharedpreferences.MyPreferences;
import com.app.siy.utils.AppUtils;
import com.app.siy.utils.Constants;
import com.app.siy.utils.Progress;
import com.bumptech.glide.Glide;
import com.google.gson.Gson;

import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;
import io.reactivex.internal.operators.parallel.ParallelRunOn;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.app.siy.rest.ApiClient.BASE_URL_UPLOADED_IMAGE;

public class BookingConfirmation extends AppCompatActivity implements View.OnClickListener {

    private AlertDialog alertDialogCancelBooking;
    private User user;
    private String TAG = "REST";
    private ImageView backGroundImageLayout;
    private TextView tvRecorderName;
    private TextView tvRecorderName2;

    private TextView tvCongrates;
    private CircleImageView circleImageView;
    private RatingBar ratingBarRecorder;
    private String notifyStatus = "0";
    private String recorderId;
    private String recorderFirebaseId;
    private User userRecorder;
    private MyPreferences myPreferences;
    private Button btnConfirmBooking;
    private Button btnCancelBooking;
    private String profileImageUrl;
    private String recorderRequirdName;

    private int handShake;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.explorer_activity_booking_confirmation);

        myPreferences = new MyPreferences(this);
        String jsonString = myPreferences.getString(MyPreferences.USER_MODEL);
        Gson gson = new Gson();
        user = gson.fromJson(jsonString, User.class);
        AppUtils.log(TAG, "User JSON on Booking Confirmation Page : " + jsonString);

        Toolbar toolbar = (Toolbar) findViewById(R.id.include_toolbar_booking_confirmed);
        TextView toolBarText = toolbar.findViewById(R.id.tool_bar_text);
        toolBarText.setText(Constants.TOOL_BAR_TEXT_BOOKING_CONFIRMATION);

        btnConfirmBooking = (Button) findViewById(R.id.btn_confirm_booking_explorer);
        btnConfirmBooking.setText("Approve Recorder");
        btnConfirmBooking.setOnClickListener(this);

        btnCancelBooking = (Button) findViewById(R.id.booking_btn_cancel_booking);
        btnCancelBooking.setOnClickListener(this);

        ImageView backImage = toolbar.findViewById(R.id.back_image_tool_bar);
        backImage.setOnClickListener(this);

        tvRecorderName = (TextView) findViewById(R.id.tv_booking_confirmation_explorer);
        tvRecorderName2 = (TextView) findViewById(R.id.booking_tv_name);
        tvCongrates = (TextView) findViewById(R.id.tv_congrates);
        circleImageView = (CircleImageView) findViewById(R.id.profile_image_recorder);
        ratingBarRecorder = (RatingBar) findViewById(R.id.rating_recorder);
        backGroundImageLayout = (ImageView) findViewById(R.id.background_image_booking_confirm);
        // getting user Id from Notification Intent.
        recorderId = getIntent().getStringExtra("USER_ID_RECORDER");

        // Cancel the Notification.
        int notificationId = getIntent().getIntExtra("NOTIFICATION_ID", 0);

        if (notificationId != 0) {
            AppUtils.cancelNotification(this, notificationId);
        }


        getRecorderDetailsFromAPI(recorderId);


        //AppUtils.cancelNotification(this, MyFirebaseMessagingService.NOTIFICATION_ID_TWO);
    }

    private void getRecorderDetailsFromAPI(final String userId) {
        AppUtils.log(TAG, "Getting Recorder details with user Id : " + userId);
        if (AppUtils.isNetworkAvailable(this)) {
            final Progress progress = new Progress(this, R.style.CustomProgressDialogTheme);
            progress.setCancelable(false);
            progress.show();
            ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
            Call<ServerResponse> serverResponseCall = apiInterface.getSingleUserDetails(user.getAccessToken(), Constants.USER_TYPE_RECORDER, userId);
            serverResponseCall.enqueue(new Callback<ServerResponse>() {
                @Override
                public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {
                    progress.dismiss();
                    if (response.isSuccessful()) {
                        AppUtils.log(TAG, "User Info with user id " + userId + " retrieved successfully !!");
                        if (response.body() != null) {
                            userRecorder = response.body().user;
                            recorderId = userRecorder.getUserId();
                            String recorderFirstName = userRecorder.getFirstName();
                            String recorderLastName = userRecorder.getLastName();
                            AppUtils.log(TAG, "Recorder First Name : " + recorderFirstName);
                            AppUtils.log(TAG, "Recorder Last Name : " + recorderLastName);
                            tvRecorderName.setText(" " + recorderFirstName + " " + recorderLastName.charAt(0));
                            recorderRequirdName = recorderFirstName + " " + recorderLastName.charAt(0);
                            tvRecorderName2.setText(recorderRequirdName);

                            String recorderRating = userRecorder.getRating();
                            if (recorderRating == null) {
                                ratingBarRecorder.setRating(0.0f);
                            } else {
                                ratingBarRecorder.setRating(Float.parseFloat(recorderRating));
                            }

                            String profileImageRecorderForBackground = userRecorder.getImage();
                            AppUtils.log(TAG, "Profile Image Of Recorder : " + profileImageRecorderForBackground);


                            // Set background Image
                            String fullpathBackgroundImage = ApiClient.BASE_URL_UPLOADED_IMAGE + profileImageRecorderForBackground;
                            Glide.with(BookingConfirmation.this).load(fullpathBackgroundImage).into(backGroundImageLayout);
                            recorderFirebaseId = user.getFirebaseId();


                            profileImageUrl = ApiClient.BASE_URL_UPLOADED_IMAGE + userRecorder.getImage();
                            AppUtils.log(TAG, "Profile Image full URL : " + profileImageUrl);
                            Glide.with(BookingConfirmation.this)
                                    .load(profileImageUrl)
                                    .into(circleImageView);
                            tvCongrates.setText(Constants.CONGRATS_YOUR_HAVE_JUST_HIRED);

                            String firebaseId = user.getFirebaseId();
                            AppUtils.log(TAG, "Firebase Id of Recorder : " + firebaseId);

                            getRequestAcceptStatus();
                        } else {
                            AppUtils.log(TAG, "User Recorder is Null !!");
                        }
                    } else {
                        AppUtils.log(TAG, "Error while getting user Info with user id " + userId);
                    }
                }

                @Override
                public void onFailure(Call<ServerResponse> call, Throwable t) {
                    progress.dismiss();
                }
            });
        } else {
            AppUtils.showToast(this, Constants.NO_INTERNET_CONNECTION);
        }
    }


    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        Intent backIntent = new Intent();
        backIntent.putExtra("STATUS_CODE", "2");
        setResult(RESULT_OK, backIntent);
        finish();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back_image_tool_bar:
                onBackPressed();
                break;

            case R.id.btn_confirm_booking_explorer:

                AppUtils.log(TAG, "Hand Shake Status : " + handShake);
                if (handShake == 0) {
                    AppUtils.log(TAG, "HandShake is 0");
                    AppUtils.log(TAG, "method call confirm booking with Recorder Id " + recorderId);
                    confirmationResponse(recorderId);
                } else if (handShake == 1) {
                    AppUtils.log(TAG, "HandShake is 1");
                    // Move the User to Chatting Page.
                    chatNow();
                }

                // Chat Now
                /*String approvalStatus = myPreferences.getString(MyPreferences.APPROVE_REQUEST_BY_EXPLORER);
                if (approvalStatus.equals("true")) {
                    // chat Now
                    chatNow();
                } else {
                    // Send Confirmation Request
                    //confirmationResponse(userRecorder.getFirebaseId());
                }*/

                break;

            case R.id.booking_btn_cancel_booking:
                //Chat Later
                cancelAlertDialog();
                break;
        }
    }

    private void getRequestAcceptStatus() {
        AppUtils.log(TAG, "Getting Request Accepted Status ... ");
        if (AppUtils.isNetworkAvailable(this)) {

            final Progress progress = new Progress(this, R.style.CustomProgressDialogTheme);
            progress.setCancelable(false);
            progress.show();

            AppUtils.log(TAG, "Access Token : Booking " + user.getAccessToken());
            AppUtils.log(TAG, "Explorer Id Booking: " + user.getUserId());
            AppUtils.log(TAG, "Recorder Id Booking : " + userRecorder.getUserId());

            String accessToken = user.getAccessToken();
            String explorerId = user.getUserId();
            String recorderId = userRecorder.getUserId();


            ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
            Call<ServerResponseRequestAcceptedStatus> requestAcceptedStatusCall
                    = apiInterface.getRequestAcceptStatus(accessToken, recorderId, explorerId);
            requestAcceptedStatusCall.enqueue(new Callback<ServerResponseRequestAcceptedStatus>() {
                @Override
                public void onResponse(Call<ServerResponseRequestAcceptedStatus> call, Response<ServerResponseRequestAcceptedStatus> response) {
                    progress.dismiss();
                    if (response.isSuccessful()) {
                        AppUtils.log(TAG, "Request Accepted Status Retrieved Successfully ...");
                        RequestAcceptedStatusModel requestAcceptedStatusModel = response.body().result;
                        int explorerNotifyStatus = requestAcceptedStatusModel.getExplorerNotifyStatus();
                        int recorderNotifyStatus = requestAcceptedStatusModel.getRecorderNotifyStatus();
                        handShake = requestAcceptedStatusModel.getHandShake();

                        AppUtils.log(TAG, "Explorer Notify Status : " + explorerNotifyStatus);
                        AppUtils.log(TAG, "Recorder Notify Status : " + recorderNotifyStatus);
                        AppUtils.log(TAG, "Hand Shake Status : " + handShake);

                        // Now Check HandShake Status -
                        if (handShake == 0) {
                            // Recorder Has Not approves Request
                            btnConfirmBooking.setText("Approve Booking");
                            btnCancelBooking.setText("Decline Job");
                        } else if (handShake == 1) {
                            // Recorder Has Accepted Request and Explorer has also Approves the Request.
                            // Change Button Text to Chat Now.
                            btnConfirmBooking.setText("Chat Now");
                            btnCancelBooking.setVisibility(View.INVISIBLE);
                        }
                    } else {
                        AppUtils.log(TAG, "Getting Request accepted status unsuccessful .. " + response.errorBody());
                    }
                }

                @Override
                public void onFailure(Call<ServerResponseRequestAcceptedStatus> call, Throwable t) {
                    progress.dismiss();
                    AppUtils.log(TAG, "Getting Request Accepted Status Failure . " + t.getMessage());
                }
            });
        } else {
            AppUtils.showToast(this, Constants.NO_INTERNET_CONNECTION);
        }
    }

    private void chatNow() {
        AppUtils.log(TAG, "Opening Chat Now in Explorer  Panel ... ");
        Intent chatRoomIntent = new Intent(this, ChatRoomActivity.class);
        if (userRecorder != null) {
            chatRoomIntent.putExtra("MESSAGE_RECEIVER_NAME", recorderRequirdName);
            chatRoomIntent.putExtra("MESSAGE_RECEIVER_PROFILE_IMAGE", userRecorder.getImage());
            chatRoomIntent.putExtra("MESSAGE_RECEIVER_USER_ID", userRecorder.getUserId());
            chatRoomIntent.putExtra("MESSAGE_RECEIVER_FIREBASE_ID", userRecorder.getFirebaseId());
            startActivity(chatRoomIntent);
        } else {
            AppUtils.log(TAG, "User Recorder is null");
        }
    }


    private void confirmationResponse(final String recorderId) {
        AppUtils.log(TAG, "Confirm booking with recorder Id : " + recorderId);
        notifyStatus = "1";
        String accessToken = user.getAccessToken();

        if (AppUtils.isNetworkAvailable(this)) {
            final Progress progress = new Progress(this, R.style.CustomProgressDialogTheme);
            progress.setCancelable(false);
            progress.show();

            ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
            Call<ServerResponse> serverResponseCall = apiInterface.acceptRequestByExplorer(
                    accessToken,
                    recorderId,
                    notifyStatus);
            serverResponseCall.enqueue(new Callback<ServerResponse>() {
                @Override
                public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {
                    progress.dismiss();
                    if (response.isSuccessful()) {
                        AppUtils.log(TAG, "Booking confirmation is successful with recorder id : " + recorderId);
                        // Explorer Approves the Request.
                        //myPreferences.saveString(MyPreferences.APPROVE_REQUEST_BY_EXPLORER, "true");
                        btnConfirmBooking.setText("Chat Now");
                    } else {
                        try {
                            AppUtils.log(TAG, "Error " + response.errorBody().string());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onFailure(Call<ServerResponse> call, Throwable t) {
                    progress.dismiss();
                    AppUtils.log(TAG, "FAILURE " + t.getMessage());
                }
            });
        } else {
            AppUtils.showToast(this, Constants.NO_INTERNET_CONNECTION);
        }

    }

    private void cancleBooking(String recorderId) {
        AppUtils.log(TAG, "Chat Later for Recorder Id : " + recorderId);
        notifyStatus = "2";
        if (AppUtils.isNetworkAvailable(this)) {
            final Progress progress = new Progress(this, R.style.CustomProgressDialogTheme);
            progress.setCancelable(false);
            progress.show();
            ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
            Call<ServerResponse> serverResponseCall = apiInterface
                    .acceptRequestByExplorer(user.getAccessToken(), recorderId, notifyStatus);
            serverResponseCall.enqueue(new Callback<ServerResponse>() {
                @Override
                public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {
                    progress.dismiss();
                    if (response.isSuccessful()) {
                        AppUtils.log(TAG, "Chat Later");
                        AppUtils.showToastBlack(BookingConfirmation.this, Constants.YOUR_BOOKING_IS_CANCEL);
                        Intent intent = new Intent(BookingConfirmation.this, ExplorerHomeActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                    } else {
                        AppUtils.log(TAG, "Error Chat Later");
                    }
                }

                @Override
                public void onFailure(Call<ServerResponse> call, Throwable t) {
                    progress.dismiss();
                    AppUtils.log(TAG, "onFailure Chat Later");
                }
            });
        } else {
            AppUtils.showToast(this, Constants.NO_INTERNET_CONNECTION);
        }
    }


    private void cancelAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.explorer_layout_cancel_blooking, null);
        builder.setView(view);
        TextView btnCancel = view.findViewById(R.id.booking_confirmed_tv_cancel);
        TextView btnYes = view.findViewById(R.id.booking_confirmed_tv_yes);
        alertDialogCancelBooking = builder.create();
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppUtils.log(TAG, "Chat Now");
                alertDialogCancelBooking.dismiss();
            }
        });
        btnYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialogCancelBooking.dismiss();
                AppUtils.log(TAG, "Chat Later");
                //  User presses Chat Later Button
                cancleBooking(userRecorder.getFirebaseId());
            }
        });
        alertDialogCancelBooking.show();
    }
}
