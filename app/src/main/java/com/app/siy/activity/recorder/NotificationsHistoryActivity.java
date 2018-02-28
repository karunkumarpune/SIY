package com.app.siy.activity.recorder;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.app.siy.R;
import com.app.siy.adapter.NotificationAdapter;
import com.app.siy.rest.ApiClient;
import com.app.siy.rest.ApiInterface;
import com.app.siy.rest.NotificationModel;
import com.app.siy.rest.ServerResponse;
import com.app.siy.rest.ServerResponseNotification;
import com.app.siy.rest.User;
import com.app.siy.sharedpreferences.MyPreferences;
import com.app.siy.utils.AppUtils;
import com.app.siy.utils.Constants;
import com.app.siy.utils.Progress;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationsHistoryActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "NOTIFICATION";
    private Toolbar toolbar;
    private ImageView backImage;
    private ListView listView;
    private NotificationAdapter adapter;
    private List<NotificationModel> notificationModelArrayList = new ArrayList<>();
    private User user;
    private User userExplorer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recorder_activity_notifications);

        MyPreferences myPreferences = new MyPreferences(this);
        String userJson = myPreferences.getString(MyPreferences.USER_MODEL);
        Gson gson = new Gson();
        user = gson.fromJson(userJson, User.class);

        toolbar = (Toolbar) findViewById(R.id.include_toolbar_notifications_recorder);
        TextView textView = (TextView) toolbar.findViewById(R.id.tool_bar_text);
        textView.setText("Notifications");

        backImage = toolbar.findViewById(R.id.back_image_tool_bar);
        backImage.setOnClickListener(this);

        listView = (ListView) findViewById(R.id.notification_list_view);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(NotificationsHistoryActivity.this, HiredActivity.class);
                intent.putExtra("USER_ID_EXPLORER", userExplorer.getUserId());
                startActivity(intent);
            }
        });


        getNotificationHistoryFromAPI(user.getAccessToken());
    }

    private void getNotificationHistoryFromAPI(final String accessToken) {
        AppUtils.log(TAG, "Getting Notification from server ... ");
        if (AppUtils.isNetworkAvailable(this)) {
            final Progress progress = new Progress(this, R.style.CustomProgressDialogTheme);
            progress.setCancelable(false);
            progress.show();

            ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
            Call<ServerResponseNotification> responseNotificationCall = apiInterface.getNotificationHistory(accessToken);
            responseNotificationCall.enqueue(new Callback<ServerResponseNotification>() {
                @Override
                public void onResponse(Call<ServerResponseNotification> call,
                                       Response<ServerResponseNotification> response) {
                    progress.dismiss();
                    if (response.isSuccessful()) {
                        AppUtils.log(TAG, "Notification Retrieved Successfully!");
                        ServerResponseNotification serverResponseNotification = response.body();
                        if (serverResponseNotification != null) {
                            NotificationModel notificationModel = serverResponseNotification.notificationModel;
                            notificationModelArrayList.add(notificationModel);

                            // Now Get Explorer Id From NotificationModel
                            String explorerId = notificationModel.getUserId();

                            getExplorerDetailsFromAPI(accessToken, explorerId);

                        } else {
                            AppUtils.showToast(NotificationsHistoryActivity.this, "No Notifications");
                        }

                        // Now Hit the API - getUserDetails using this explorerId
                        // Request for getUserDetails -
                        // user_type is 1.
                        // user_id is explorerId
                        // accessToken is current Recorder Access Token.

                        // We require at NotificationActivity page.
                        // explorer Name - From getUserDetails Api.
                        // Explorer Message - From NotificationModel.
                        // Hiring Time - from NotificationModel.

                        // Required info at Hired Page , when clicked on Notification History
                        // Latitude and Longitude - get from getUserDetails API.
                        // Explorer Name - Get from getUserDetails API.
                        // Explorer Id === user_id from NotificationModel.
                    } else {
                        try {
                            AppUtils.log(TAG, "Unable to Get Notification History : " + response.errorBody().string());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onFailure(Call<ServerResponseNotification> call, Throwable t) {
                    progress.dismiss();
                    AppUtils.log(TAG, "onFailure NotificationActivityRecorder : " + t.getMessage());
                }
            });
        } else {
            AppUtils.showToast(this, Constants.NO_INTERNET_CONNECTION);
        }
    }

    private void getExplorerDetailsFromAPI(String accessToken, final String explorerId) {
        AppUtils.log(TAG, "Getting explorer with id : " + explorerId + " details from server ...");
        AppUtils.log(TAG, "AccessToken " + accessToken);
        AppUtils.log(TAG, "Explorer id : " + explorerId);
        String userType = Constants.USER_TYPE_EXPLORER;

        if (AppUtils.isNetworkAvailable(this)) {
            final Progress progress = new Progress(this, R.style.CustomProgressDialogTheme);
            progress.setCancelable(false);
            progress.show();

            ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
            Call<ServerResponse> serverResponseCall = apiInterface
                    .getSingleUserDetails(accessToken, userType, explorerId);
            serverResponseCall.enqueue(new Callback<ServerResponse>() {
                @Override
                public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {
                    progress.dismiss();
                    if (response.isSuccessful()) {
                        AppUtils.log(TAG, "Explorer Information Retrieved successfully.");
                        userExplorer = response.body().user;

                        adapter = new NotificationAdapter(NotificationsHistoryActivity.this,
                                R.layout.recorder_notification_row_layout, notificationModelArrayList, userExplorer);
                        listView.setAdapter(adapter);
                        adapter.notifyDataSetChanged();
                    } else {
                        try {
                            AppUtils.log(TAG, "Unable to get Explorer Details " + response.errorBody().string());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onFailure(Call<ServerResponse> call, Throwable t) {
                    progress.dismiss();
                    AppUtils.log(TAG, "onFailure NotificationActivityRecorder: " + t.getMessage());
                }
            });
        } else {
            AppUtils.showToast(this, Constants.NO_INTERNET_CONNECTION);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back_image_tool_bar:
                onBackPressed();
                break;
        }
    }
}
