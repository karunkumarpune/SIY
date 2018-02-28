package com.app.siy.activity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.nfc.Tag;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.VideoView;

import com.app.siy.R;
import com.app.siy.firebase.model.Message;
import com.app.siy.model.DataLibraryModel;
import com.app.siy.rest.ApiClient;
import com.app.siy.rest.ApiInterface;
import com.app.siy.rest.ServerResponse;
import com.app.siy.rest.User;
import com.app.siy.sharedpreferences.MyPreferences;
import com.app.siy.utils.AppUtils;
import com.app.siy.utils.Constants;
import com.app.siy.utils.Progress;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.common.api.Api;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.app.siy.rest.ApiClient.BASE_URL_UPLOADED_IMAGE;

public class ImageCroppedActivity extends AppCompatActivity {


    Toolbar toolbar;
    private static final String TAG = "GALLERY";
    private MyPreferences myPreferences;
    private User user;
    private DataLibraryModel dataLibraryModel;
    private DatabaseReference rootReference;
    private FirebaseAuth mAuth;
    private String senderId;
    private String receiverId;
    private ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_cropped);

        VideoView videoView = (VideoView) findViewById(R.id.video_view);
        ImageView imageView = (ImageView) findViewById(R.id.image_cropped);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar_video);
        dataLibraryModel = getIntent().getParcelableExtra("DATA_LIBRARY_MODEL");
        receiverId = getIntent().getStringExtra("RECEIVER_ID");
        AppUtils.log(TAG, "Receiver ID on ImageCroppedActivity : " + receiverId);
        rootReference = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        senderId = mAuth.getCurrentUser().getUid();

        myPreferences = new MyPreferences(this);
        String userModel = myPreferences.getString(MyPreferences.USER_MODEL);
        Gson gson = new Gson();
        user = gson.fromJson(userModel, User.class);

        toolbar = (Toolbar) findViewById(R.id.include_toolbar_cropped_image);
        ImageView backImage = toolbar.findViewById(R.id.iv_back_gallery);
        backImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        Button btnShareImage = toolbar.findViewById(R.id.btn_share_image);
        if (receiverId == null) {
            btnShareImage.setVisibility(View.INVISIBLE);
        } else {
            btnShareImage.setVisibility(View.VISIBLE);
        }
        btnShareImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppUtils.log(TAG, "Share Image Button");
                shareData();
            }
        });


        if (dataLibraryModel.getDataType() == 1) {
            // Image Type -> Hide VideoView
            imageView.setVisibility(View.VISIBLE);
            videoView.setVisibility(View.GONE);
            progressBar.setVisibility(View.GONE);
            String imagePath = BASE_URL_UPLOADED_IMAGE + dataLibraryModel.getData();
            AppUtils.log(TAG, "Image Path : " + imagePath);
            Glide.with(this).load(imagePath)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .crossFade()
                    .into(imageView);

        } else if (dataLibraryModel.getDataType() == 2) {
            // Video Type -> Hide ImageView and Display VideoView.
            /*final Progress progress = new Progress(this, R.style.CustomProgressDialogTheme);
            progress.show();*/
            videoView.setVisibility(View.VISIBLE);
            imageView.setVisibility(View.GONE);
            progressBar.setVisibility(View.VISIBLE);
            String videoPath = ApiClient.BASE_URL_UPLOADED_IMAGE + dataLibraryModel.getData();
            AppUtils.log(TAG, "Video Path : " + videoPath);
            videoView.setVideoPath(videoPath);
            videoView.setMediaController(new MediaController(this));
            videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mp.start();

                    mp.setOnVideoSizeChangedListener(new MediaPlayer.OnVideoSizeChangedListener() {
                        @Override
                        public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
                            mp.start();
                            progressBar.setVisibility(View.GONE);
                            //progress.dismiss();
                        }
                    });
                }
            });
        }

    }

    private void shareData() {
        AppUtils.log(TAG, "Sharing data ...");
        if (AppUtils.isNetworkAvailable(this)) {
            final Progress progress = new Progress(this, R.style.CustomProgressDialogTheme);
            progress.setCancelable(false);
            progress.show();
            final String dataLibraryId = String.valueOf(dataLibraryModel.getDataLibraryId());
            ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
            Call<ServerResponse> serverResponseCall = apiInterface.shareData(user.getAccessToken(), dataLibraryId);
            serverResponseCall.enqueue(new Callback<ServerResponse>() {
                @Override
                public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {
                    progress.dismiss();
                    if (response.isSuccessful()) {
                        AppUtils.log(TAG, "File shared successfully.");
                        AppUtils.showToast(ImageCroppedActivity.this, "File Shared");

                        String sharedDataPath = dataLibraryModel.getData();
                        // Now Save this Image to Firebase.

                        String sharedDataCompletePath = BASE_URL_UPLOADED_IMAGE + sharedDataPath;
                        AppUtils.log(TAG, "Shared Data Complete Path 22: " + sharedDataCompletePath);
                        int dataType = dataLibraryModel.getDataType();
                        if (dataType == 1) {
                            // Send Image Message
                            sendImageMessageToFirebase(sharedDataCompletePath);
                        } else if (dataType == 2) {
                            // Send Video Message
                            sendVideoMessageToFirebase(sharedDataCompletePath);
                        }

                    } else {
                        try {
                            AppUtils.log(TAG, "Some Error occurs " + response.errorBody().string());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onFailure(Call<ServerResponse> call, Throwable t) {
                    progress.dismiss();
                    AppUtils.log(TAG, "File Sharing Failed. " + t.getMessage());
                }
            });
        } else {
            AppUtils.showToast(this, Constants.NO_INTERNET_CONNECTION);
        }
    }

    private void sendImageMessageToFirebase(String completeImagePath) {
        AppUtils.log(TAG, "Saving image to firebase message ... : " + completeImagePath);
        if (completeImagePath != null) {
            //etMessage.setText("");
            String messageSenderReference = "Messages" + "/" + senderId + "/" + receiverId;
            String messageReceiverReference = "Messages" + "/" + receiverId + "/" + senderId;

            DatabaseReference messageReference = rootReference
                    .child("Users")
                    .child(senderId)
                    .child(receiverId)
                    .push();

            String messageKey = messageReference.getKey();

            Message messageModel = new Message(completeImagePath,
                    false,
                    "image",
                    String.valueOf(System.currentTimeMillis()),
                    senderId,
                    receiverId);

            Map messageBody = new HashMap<>();
            messageBody.put(messageSenderReference + "/" + messageKey, messageModel);
            messageBody.put(messageReceiverReference + "/" + messageKey, messageModel);

            rootReference.updateChildren(messageBody, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if (databaseError != null) {
                        AppUtils.log(TAG, "Error " + databaseError.getMessage());
                    } else {
                        AppUtils.log(TAG, "Image message saved to Firebase database");
                        setResult(RESULT_OK);
                        finish();
                        /*Intent chatRoomIntent = new Intent(ImageCroppedActivity.this, ChatRoomActivity.class);
                        chatRoomIntent.putExtra("MESSAGE_RECEIVER_ID", receiverId);
                        startActivityForResult(chatRoomIntent);
                        finish();*/
                    }
                }
            });
        } else {
            AppUtils.showToast(this, "Invalid Image File");
        }
    }


    private void sendVideoMessageToFirebase(String completeImagePath) {
        AppUtils.log(TAG, "Saving video to firebase message ... : " + completeImagePath);
        if (completeImagePath != null) {
            //etMessage.setText("");
            String messageSenderReference = "Messages" + "/" + senderId + "/" + receiverId;
            String messageReceiverReference = "Messages" + "/" + receiverId + "/" + senderId;

            DatabaseReference messageReference = rootReference
                    .child("Users")
                    .child(senderId)
                    .child(receiverId)
                    .push();

            String messageKey = messageReference.getKey();

            Message messageModel = new Message(completeImagePath,
                    false,
                    "video",
                    String.valueOf(System.currentTimeMillis()),
                    senderId,
                    receiverId);

            Map messageBody = new HashMap<>();
            messageBody.put(messageSenderReference + "/" + messageKey, messageModel);
            messageBody.put(messageReceiverReference + "/" + messageKey, messageModel);

            rootReference.updateChildren(messageBody, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if (databaseError != null) {
                        AppUtils.log(TAG, "Error " + databaseError.getMessage());
                    } else {
                        AppUtils.log(TAG, "Video Link is saved to Firebase database");
                        setResult(RESULT_OK);
                        finish();
                        /*Intent chatRoomIntent = new Intent(ImageCroppedActivity.this, ChatRoomActivity.class);
                        chatRoomIntent.putExtra("MESSAGE_RECEIVER_ID", receiverId);
                        startActivityForResult(chatRoomIntent);
                        finish();*/
                    }
                }
            });
        } else {
            AppUtils.showToast(this, "Invalid Video File");
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


    }
}
