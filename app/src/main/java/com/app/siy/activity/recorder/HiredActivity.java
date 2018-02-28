package com.app.siy.activity.recorder;

import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.siy.R;
import com.app.siy.activity.ChatListActivity;
import com.app.siy.activity.ChatRoomActivity;
import com.app.siy.firebase.services.MyFirebaseMessagingService;
import com.app.siy.rest.ApiClient;
import com.app.siy.rest.ApiInterface;
import com.app.siy.rest.ServerResponse;
import com.app.siy.rest.User;
import com.app.siy.sharedpreferences.MyPreferences;
import com.app.siy.utils.AppUtils;
import com.app.siy.utils.Constants;
import com.app.siy.utils.Progress;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HiredActivity extends AppCompatActivity implements View.OnClickListener {
    private final String TAG = "HIRED";
    private Toolbar toolbar;
    private ImageView backImage;
    private TextView tvHiredBy;
    private TextView tvExplorerAddress;
    private Button btnAcceptJob;
    private Button btnDeclineJob;
    private User user;
    private String notificationNotifyStatus = "0";
    private MyPreferences myPreferences;
    private User userExplorer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recorder_activity_hired);

        myPreferences = new MyPreferences(this);
        String userModel = myPreferences.getString(MyPreferences.USER_MODEL);
        Gson gson = new Gson();
        user = gson.fromJson(userModel, User.class);

        toolbar = (Toolbar) findViewById(R.id.include_toolbar_hired);
        TextView toolbarText = toolbar.findViewById(R.id.tool_bar_text);
        toolbarText.setText("Hired");

        tvHiredBy = (TextView) findViewById(R.id.hired_tv_hired_name);
        btnDeclineJob = (Button) findViewById(R.id.hired_button_decline_job);

        final String userIdExplorer = getIntent().getStringExtra("USER_ID_EXPLORER");
        AppUtils.log(TAG, "User Id Explorer : " + userIdExplorer);

        // Cancel the Notification.
        int notificationId = getIntent().getIntExtra("NOTIFICATION_ID", 0);

        if (notificationId != 0) {
            AppUtils.cancelNotification(this, notificationId);
        }


        backImage = toolbar.findViewById(R.id.back_image_tool_bar);
        backImage.setOnClickListener(this);
        btnAcceptJob = (Button) findViewById(R.id.button_accept_job_by_recorder);
        tvExplorerAddress = (TextView) findViewById(R.id.tv_explorer_address_hired_activity);

        final String acceptJobStatus = myPreferences.getString(MyPreferences.ACCEPT_JOB_STATUS);
        if (acceptJobStatus.equals("true")) {
            // Job is Accepted -> Chat Now
            btnAcceptJob.setText("Chat Now");
            btnDeclineJob.setVisibility(View.INVISIBLE);
        } else {
            btnAcceptJob.setText("Accept Job");
        }

        btnAcceptJob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Accept Job
                if (acceptJobStatus.equals("true")) {
                    AppUtils.log(TAG, "Btn Chat Now");
                    // Move to Chat Now
                    chatNow();
                } else {
                    AppUtils.log(TAG, "Btn Accept Job");
                    acceptJobByRecorder(userIdExplorer);
                }
            }
        });

        btnDeclineJob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Decline Job
                showConfirmationDialog();
            }
        });

        getExplorerDetailsFromApi(userIdExplorer);
    }

    private void chatNow() {
        AppUtils.log(TAG, "Opening Chat Now ... ");
        String fullName = userExplorer.getFirstName() + " " + userExplorer.getLastName();
        AppUtils.log(TAG, "Message Receiver Full Name on Hired Page : " + fullName);
        Intent chatRoomIntent = new Intent(this, ChatRoomActivity.class);
        chatRoomIntent.putExtra("MESSAGE_RECEIVER_NAME", fullName);
        chatRoomIntent.putExtra("MESSAGE_RECEIVER_PROFILE_IMAGE", userExplorer.getImage());
        chatRoomIntent.putExtra("MESSAGE_RECEIVER_USER_ID", userExplorer.getUserId());
        chatRoomIntent.putExtra("MESSAGE_RECEIVER_FIREBASE_ID", userExplorer.getFirebaseId());
        startActivity(chatRoomIntent);
    }


    private void getExplorerDetailsFromApi(final String userIdExplorer) {
        AppUtils.log(TAG, "Getting user details from Explorer with id : " + userIdExplorer);
        if (AppUtils.isNetworkAvailable(this)) {
            final Progress progress = new Progress(this, R.style.CustomProgressDialogTheme);
            progress.setCancelable(false);
            progress.show();

            ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
            Call<ServerResponse> serverResponseCall = apiInterface.getSingleUserDetails(user.getAccessToken(),
                    Constants.USER_TYPE_EXPLORER, userIdExplorer);
            serverResponseCall.enqueue(new Callback<ServerResponse>() {
                @Override
                public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {
                    progress.dismiss();
                    if (response.isSuccessful()) {
                        AppUtils.log(TAG, "User Details retrieved successfully.");
                        if (response.body() != null) {
                            userExplorer = response.body().user;
                            String explorerFirstName = userExplorer.getFirstName();
                            String explorerLastName = userExplorer.getLastName();
                            tvHiredBy.setText(explorerFirstName + " " + explorerLastName.charAt(0));

                            String latitude = userExplorer.getLatitude();
                            String longitude = userExplorer.getLongitude();
                            List<Address> addressList = AppUtils
                                    .latLngToAddress(HiredActivity.this, Double.parseDouble(latitude),
                                            Double.parseDouble(longitude));

                            tvExplorerAddress.setText(addressList.get(0).getAddressLine(0));

                        } else {
                            AppUtils.log(TAG, "Response is Null");
                        }
                    } else {
                        try {
                            AppUtils.log(TAG, "ERROR " + response.errorBody().string());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onFailure(Call<ServerResponse> call, Throwable t) {
                    progress.dismiss();
                    AppUtils.log(TAG, "Failure " + t.getMessage());
                }
            });
        } else {
            AppUtils.showToast(this, Constants.NO_INTERNET_CONNECTION);
        }
    }

    private void acceptJobByRecorder(String explorerId) {
        AppUtils.log(TAG, "Accepting Job by Recorder ... ");
        notificationNotifyStatus = Constants.RECORDER_NOTIFY_STATUS_ACCEPT_JOB;
        if (AppUtils.isNetworkAvailable(this)) {
            final Progress progress = new Progress(this, R.style.CustomProgressDialogTheme);
            progress.setCancelable(false);
            progress.show();
            ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
            Call<ServerResponse> serverResponseCall = apiInterface
                    .acceptRequestByRecorder(user.getAccessToken(),
                            explorerId,
                            notificationNotifyStatus
                    );
            serverResponseCall.enqueue(new Callback<ServerResponse>() {
                @Override
                public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {
                    progress.dismiss();
                    if (response.isSuccessful()) {
                        //AppUtils.showToast(HiredActivity.this, "Your Request has been sent to Explorer");
                        showRequestedDialog();
                        myPreferences.saveString(MyPreferences.ACCEPT_JOB_STATUS, "true");
                        AppUtils.log(TAG, "Request Accepted By Recorder successfully.");
                    } else {
                        try {
                            AppUtils.log(TAG, "Error : " + response.errorBody().string());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onFailure(Call<ServerResponse> call, Throwable t) {
                    progress.dismiss();
                    AppUtils.log(TAG, "onFailure while accepting Job by Recorder. " + t.getMessage());
                }
            });
        } else {
            AppUtils.showToast(this, Constants.NO_INTERNET_CONNECTION);
        }
    }

    private void declineJobByRecorder(String explorerId) {
        AppUtils.log(TAG, "Declining Job By Recorder ...");
        notificationNotifyStatus = Constants.RECORDER_NOTIFY_STATUS_DECLINE_JOB;
        if (AppUtils.isNetworkAvailable(this)) {
            final Progress progress = new Progress(this, R.style.CustomProgressDialogTheme);
            progress.setCancelable(false);
            progress.show();
            ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
            Call<ServerResponse> serverResponseCall = apiInterface
                    .acceptRequestByRecorder(user.getAccessToken(), explorerId, notificationNotifyStatus);
            serverResponseCall.enqueue(new Callback<ServerResponse>() {
                @Override
                public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {
                    AppUtils.log(TAG, "Job is declined successfully !!");
                    progress.dismiss();
                    if (response.isSuccessful()) {
                        AppUtils.showToast(HiredActivity.this, "Your Job is Cancelled");
                        AppUtils.log(TAG, "Job is Cancelled");
                        Intent intent = new Intent(HiredActivity.this, RecorderHomeActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    } else {
                        AppUtils.log(TAG, "Job is not Cancelled.");
                        try {
                            AppUtils.showToast(HiredActivity.this, "Some Error Occurs " + response.errorBody().string());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onFailure(Call<ServerResponse> call, Throwable t) {
                    AppUtils.log(TAG, "onFailure while cancelling Job " + t.getMessage());
                }
            });
        } else {
            AppUtils.showToast(this, Constants.NO_INTERNET_CONNECTION);
        }
    }

    private void showRequestedDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Message");
        builder.setMessage("Your request has been sent to Explorer. Your will be notified after Explorer confirmation.");
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                btnAcceptJob.setText("Chat Now");
                startActivity(new Intent(HiredActivity.this, RecorderHomeActivity.class));
                finish();
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                startActivity(new Intent(HiredActivity.this, RecorderHomeActivity.class));
                finish();
            }
        });

        builder.show();
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back_image_tool_bar:
                onBackPressed();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent recorderHomeIntent = new Intent(this, RecorderHomeActivity.class);
        recorderHomeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(recorderHomeIntent);
        finish();
    }

    private void showConfirmationDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.recorder_layout_decline_job_confirmation, null);
        builder.setView(view);
        final AlertDialog alertDialog = builder.create();

        TextView btnCancel = view.findViewById(R.id.tv_cancel_decline_job_dialog);
        TextView btnYes = view.findViewById(R.id.tv_yes_decline_job_dialog);
        btnCancel.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                alertDialog.cancel();
            }
        });

        btnYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                declineJobByRecorder(userExplorer.getUserId());
            }
        });
        alertDialog.show();
    }
}