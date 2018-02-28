package com.app.siy.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.annotation.IdRes;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import com.app.siy.R;
import com.app.siy.activity.explorer.ExplorerHomeActivity;
import com.app.siy.activity.recorder.DataLibraryRecorderActivity;
import com.app.siy.activity.recorder.RecorderHomeActivity;
import com.app.siy.firebase.adapter.MessageAdapter;
import com.app.siy.firebase.model.Message;
import com.app.siy.rest.ApiClient;
import com.app.siy.rest.ApiInterface;
import com.app.siy.rest.ServerResponse;
import com.app.siy.rest.ServerResponseEndChatting;
import com.app.siy.rest.User;
import com.app.siy.sharedpreferences.MyPreferences;
import com.app.siy.utils.AppUtils;
import com.app.siy.utils.Constants;
import com.app.siy.utils.Progress;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatRoomActivity extends AppCompatActivity implements View.OnClickListener {


    private static final String TAG = "CHATROOM";
    private static final int CAMERA_PERMISSION_CONSTANT = 27;
    private static final int REQUEST_PERMISSION_SETTING = 29;
    private static final int EXTERNAL_STORAGE_PERMISSION_CONSTANT = 32;
    private static final int GALLERY_INTENT_CONSTANT = 35;
    private static final int CHAT_ROOM_REQUEST_CODE = 36;
    private Toolbar toolbar;
    private ImageView backImage;
    //private FcmUser fcmUser;
    private Button endChatButton;
    private AlertDialog alertDialog;

    private RecyclerView recyclerView;
    private MessageAdapter messageAdapter;
    private List<Message> chatMessageList = new ArrayList<>();

    private EditText etMessage;
    private ImageView ivBtnSend;
    private ImageView ivAttachFile;
    //private ImageView ivCapture;

    private FirebaseAuth mAuth;
    private String senderId;
    private String receiverId;
    private DatabaseReference rootReference;
    private User user;
    private MyPreferences myPreferences;
    private AlertDialog alertDialogHireNew;
    private AlertDialog alertDialogRating;
    private SharedPreferences permissionStatus;

    // used for Rating
    private String messageReceiverUserId;
    private String messageReceiverName;
    private String messageReceiverProfileImage;
    private String chattingStatus;

    private int dataLibraryId = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);

        // Run time permissions
        permissionStatus = getSharedPreferences("permissionStatus", MODE_PRIVATE);

        myPreferences = new MyPreferences(this);
        String userModel = myPreferences.getString(MyPreferences.USER_MODEL);
        Gson gson = new Gson();
        user = gson.fromJson(userModel, User.class);

        //Add messages for Chat
        //addChatMessage();


        if (myPreferences.getString(MyPreferences.ACCEPT_JOB_STATUS).equals("true")) {

        }

        rootReference = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();
        senderId = mAuth.getCurrentUser().getUid();
        toolbar = (Toolbar) findViewById(R.id.chat_room_include_toolbar_recorder);
        TextView toolbarText = toolbar.findViewById(R.id.chat_room_tool_bar_text);


        /*// Disable Image on Toolbar if user is Recorder
        CircleImageView profileImage = toolbar.findViewById(R.id.chat_room_user_image);
        if (user.getUserType().equals(Constants.USER_TYPE_RECORDER)) {
            profileImage.setVisibility(View.GONE);
        } else if (user.getUserType().equals(Constants.USER_TYPE_EXPLORER)) {
            profileImage.setVisibility(View.VISIBLE);
        }*/


        //if (getIntent() == null) {
        //fcmUser = (FcmUser) getIntent().getExtras().getSerializable("CHAT_DETAILS_RECORDER");
        //}

        receiverId = getIntent().getStringExtra("MESSAGE_RECEIVER_FIREBASE_ID");
        messageReceiverName = getIntent().getStringExtra("MESSAGE_RECEIVER_NAME");
        messageReceiverProfileImage = getIntent().getStringExtra("MESSAGE_RECEIVER_PROFILE_IMAGE");
        messageReceiverUserId = getIntent().getStringExtra("MESSAGE_RECEIVER_USER_ID");

        AppUtils.log(TAG, "Message Receiver Name on Chat Room " + messageReceiverName);
        AppUtils.log(TAG, "Message Receiver Full Name : " + messageReceiverName);

        toolbarText.setText(messageReceiverName);


        CircleImageView circleImageView = toolbar.findViewById(R.id.chat_room_user_image);
        // If user type is Explorer then set Profile Picture of Recorder.
        // If user type is Recorder then don't set profile picture of Explorer.

        if (user.getUserType().equals(Constants.USER_TYPE_EXPLORER)) {
            // Show profile Image of Recorders
            Glide.with(this).load(ApiClient.BASE_URL_UPLOADED_IMAGE + messageReceiverProfileImage).into(circleImageView);
        } else {
            circleImageView.setVisibility(View.GONE);
            // Don't show profile Image of Explorers
        }

        backImage = (ImageView) toolbar.findViewById(R.id.chat_room_back_image);
        backImage.setOnClickListener(this);

        endChatButton = toolbar.findViewById(R.id.chat_room_end_chat_button);
        endChatButton.setOnClickListener(this);

        etMessage = (EditText) findViewById(R.id.chat_room_et_chat_message);
        ivBtnSend = (ImageView) findViewById(R.id.chat_room_btn_send);
        ivAttachFile = (ImageView) findViewById(R.id.chat_room_attach_file);
        //ivCapture = (ImageView) findViewById(R.id.chat_room_capture);


        recyclerView = (RecyclerView) findViewById(R.id.chat_room_recycler_view_recorder);
        //Log.d("CHAT", "Message" + chatMessageList.get(0).getMessage());

        messageAdapter = new MessageAdapter(this, chatMessageList);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        recyclerView.setAdapter(messageAdapter);

        ivBtnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendTextMessage();
            }
        });


        // If user is Explorer then disable
        // then hide the upload file button
        if (user.getUserType().equals(Constants.USER_TYPE_EXPLORER)) {
            ivAttachFile.setVisibility(View.GONE);

            /*RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) ivAttachFile.getLayoutParams();

            params.addRule(RelativeLayout.ALIGN_RIGHT);
            //params.addRule(RelativeLayout.LEFT_OF, R.id.id_to_be_left_of);
            ivAttachFile.setLayoutParams(params); //causes layout update*/

        } else if (user.getUserType().equals(Constants.USER_TYPE_RECORDER)) {
            ivAttachFile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent dataLibraryIntent = new Intent(ChatRoomActivity.this, DataLibraryRecorderActivity.class);
                    dataLibraryIntent.putExtra("RECEIVER_ID", receiverId);
                    startActivityForResult(dataLibraryIntent, CHAT_ROOM_REQUEST_CODE);
                    //checkForStoragePermission();
                    AppUtils.log("Attach File");
                }
            });
        }

        /*ivCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppUtils.log("Upload Image");
            }
        });*/

        //getDataLibraryFromServer();
        fetchMessageFromFirebase();
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, GALLERY_INTENT_CONSTANT);
    }


    private void sendTextMessage() {
        AppUtils.log("Send Message - Recorder");
        AppUtils.log("CHAT", "Sender ID : " + senderId);
        AppUtils.log("CHAT", "Receiver ID : " + receiverId);
        String message = etMessage.getText().toString();
        if (!TextUtils.isEmpty(message)) {
            if (receiverId != null) {
                etMessage.setText("");
                String messageSenderReference = "Messages" + "/" + senderId + "/" + receiverId;
                String messageReceiverReference = "Messages" + "/" + receiverId + "/" + senderId;

                DatabaseReference messageReference = rootReference
                        .child("Users")
                        .child(senderId)
                        .child(receiverId)
                        .push();

                String messageKey = messageReference.getKey();

                Message messageModel = new Message(message, false, "text", String.valueOf(System.currentTimeMillis()), senderId, receiverId);
                //Message messageModel = new Message(message, false, "text", AppUtils.getCurrentTime(), senderId);

                Map messageBody = new HashMap<>();
                messageBody.put(messageSenderReference + "/" + messageKey, messageModel);
                messageBody.put(messageReceiverReference + "/" + messageKey, messageModel);

                rootReference.updateChildren(messageBody, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                        if (databaseError != null) {
                            AppUtils.log("Error " + databaseError.getMessage());
                        }
                    }
                });
            } else {
                AppUtils.log(TAG, "Receiver Id is null");
            }
        } else {
            AppUtils.showToast(this, "Please Enter Your Message");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_INTENT_CONSTANT) {
            if (resultCode == RESULT_OK) {
                Uri selectedImageUri = data.getData();
                String lastPathSegment = selectedImageUri.getLastPathSegment();
                AppUtils.log(TAG, "Last Path Segment1 : " + lastPathSegment);
                // Add Image to SIY Server
                // If Image is added to SIY server then put image link to Firebase Database also inside Message
                if (selectedImageUri != null) {
                    //addImageToServer(selectedImageUri);
                } else {
                    AppUtils.showToast(this, "Invalid File");
                }
            } else if (requestCode == CHAT_ROOM_REQUEST_CODE) {
                recyclerView.setAdapter(null);
                fetchMessageFromFirebase();
            }
        }

    }


    private void sendImageMessageToFirebase(String completeImagePath) {
        AppUtils.log(TAG, "Saving image to firebase message ... : " + completeImagePath);
        if (completeImagePath != null) {
            etMessage.setText("");
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
                    }
                }
            });
        } else {
            AppUtils.showToast(this, "Invalid Image File");
        }
    }

    private void fetchMessageFromFirebase() {
        //final Progress progress = new Progress(this, R.style.CustomProgressDialogTheme);
        //progress.show();
        AppUtils.log("CHAT", "Sender ID fetch messages : " + senderId);
        AppUtils.log("CHAT", "Receiver ID fetch messages : " + receiverId);
        if (receiverId != null) {
            rootReference.child("Messages")
                    .child(senderId)
                    .child(receiverId)
                    .addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                            AppUtils.log(TAG, "onChildAdded");
                            Message message = dataSnapshot.getValue(Message.class);
                            chatMessageList.add(message);
                            messageAdapter.notifyDataSetChanged();
                        }

                        @Override
                        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                            AppUtils.log(TAG, "onChildChanged");
                        }

                        @Override
                        public void onChildRemoved(DataSnapshot dataSnapshot) {
                            AppUtils.log(TAG, "onChildRemoved");
                        }

                        @Override
                        public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                            AppUtils.log(TAG, "onChildMoved");
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            AppUtils.log(TAG, "onCancelled");
                        }
                    });
        } else {
            AppUtils.log(TAG, "Receiver Id is null");
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.chat_room_back_image:
                onBackPressed();
                break;

            case R.id.chat_room_end_chat_button:
                //AppUtils.showToast(this, "End chat");

                if (user.getUserType().equals(Constants.USER_TYPE_EXPLORER)) {
                    displayEndChatDialogExplorer();
                } else {
                    displayEndChatDialogRecorder();
                }
                break;
        }
    }

    private void displayEndChatDialogExplorer() {
        AppUtils.log(TAG, "Displaying End Chat dialog ...");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.explorer_layout_end_chat, null);
        builder.setView(view);      //Working
        TextView btnCancel = view.findViewById(R.id.chat_room_tv_cancel);
        TextView btnOk = view.findViewById(R.id.chat_room_tv_ok);

        final RadioGroup radioGroup = view.findViewById(R.id.radio_group_end_chat);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {

            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (radioGroup.getCheckedRadioButtonId()) {
                    /*case R.id.chat_room_rb_hire_new:
                        hireNewAlertDialog();
                        break;*/

                    case R.id.chat_room_rb_re_hire:
                        Intent homeIntent = new Intent(ChatRoomActivity.this, ExplorerHomeActivity.class);
                        homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        homeIntent.putExtra(Constants.INTENT_KEY_IS_FROM_RE_HIRED, true);
                        startActivity(homeIntent);
                        break;

                    case R.id.chat_room_rb_complete_job:
                        chattingStatus = Constants.CHATTING_STATUS_COMPLETE_JOB;
                        endChatting();
                        break;
                }
            }
        });
        alertDialog = builder.create();
        alertDialog.show();
    }


    private void endChatting() {
        AppUtils.log(TAG, "Ending chat ... ");
        String currentUserType = user.getUserType();
        String requiredUser = "0";
        // Here we need receiver user type.
        if (currentUserType.equals(Constants.USER_TYPE_EXPLORER))
            requiredUser = Constants.USER_TYPE_RECORDER;
        else if (currentUserType.equals(Constants.USER_TYPE_RECORDER))
            requiredUser = Constants.USER_TYPE_EXPLORER;

        if (AppUtils.isNetworkAvailable(this)) {
            final Progress progress = new Progress(this, R.style.CustomProgressDialogTheme);
            progress.show();

            ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
            Call<ServerResponseEndChatting> serverResponseEndChattingCall =
                    apiInterface.endChatting(user.getAccessToken(), receiverId, requiredUser, chattingStatus);
            serverResponseEndChattingCall.enqueue(new Callback<ServerResponseEndChatting>() {
                @Override
                public void onResponse(Call<ServerResponseEndChatting> call, Response<ServerResponseEndChatting> response) {
                    progress.dismiss();
                    if (response.isSuccessful()) {
                        AppUtils.log(TAG, "End Chatting Success ");
                        if (user.getUserType().equals(Constants.USER_TYPE_EXPLORER)) {
                            // Go to Rating Page.
                            ratingDialogAndPaymentScreen();
                        } else if (user.getUserType().equals(Constants.USER_TYPE_RECORDER)) {
                            // Go To Recorder Home Page.
                            Intent homeIntent = new Intent(ChatRoomActivity.this, RecorderHomeActivity.class);
                            homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(homeIntent);
                        }

                    } else {
                        AppUtils.showToast(ChatRoomActivity.this, "Some Error Occurs, Please Try again ");
                        try {
                            AppUtils.log(TAG, "End Chatting Fails ... " + response.errorBody().string());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onFailure(Call<ServerResponseEndChatting> call, Throwable t) {
                    AppUtils.log(TAG, "End chatting onFailure ...");
                }
            });
        } else {
            AppUtils.log(TAG, Constants.NO_INTERNET_CONNECTION);
        }
    }

    private void submitRatingToServer(float rating) {
        if (AppUtils.isNetworkAvailable(this)) {
            final Progress progress = new Progress(this, R.style.CustomProgressDialogTheme);
            progress.setCancelable(false);
            progress.show();
            String accessToken = user.getAccessToken();
            ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
            Call<ServerResponse> call = apiInterface.giveRatingToRecorder(accessToken, messageReceiverUserId, rating);
            call.enqueue(new Callback<ServerResponse>() {
                @Override
                public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {
                    progress.dismiss();
                    if (response.isSuccessful()) {
                        AppUtils.log("Rating is Submitted");

                        Intent homeIntent2 = new Intent(ChatRoomActivity.this, ExplorerHomeActivity.class);
                        homeIntent2.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(homeIntent2);
                    } else {
                        AppUtils.log("Rating is not Submitted.");
                    }
                }

                @Override
                public void onFailure(Call<ServerResponse> call, Throwable t) {
                    progress.dismiss();
                    AppUtils.log("Give Rating - Failure: ");
                }
            });
        } else {
            AppUtils.showToast(this, Constants.NO_INTERNET_CONNECTION);
        }
    }


    //Creating Rating Bar Dialog
    private void ratingDialogAndPaymentScreen() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.explorer_layout_rating_page, null);
        builder.setView(view);      //Working

        EditText etPayedAmount = view.findViewById(R.id.et_amount_paid);
        EditText etOptionalTip = view.findViewById(R.id.et_amount_optional_tip);


        TextView tvRatingBarUserName = view.findViewById(R.id.rating_bar_user_name);
        tvRatingBarUserName.setText(messageReceiverName);
        String userImage = messageReceiverProfileImage;
        CircleImageView recorderImage = view.findViewById(R.id.profile_image_recorder);
        if (userImage == null) {
            Glide.with(this).load(R.drawable.default_profile).into(recorderImage);
        } else {
            Glide.with(this).load(ApiClient.BASE_URL_UPLOADED_IMAGE + userImage).into(recorderImage);
        }
        final RatingBar ratingBar = view.findViewById(R.id.rating_bar);
        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {

            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating,
                                        boolean fromUser) {
                if (rating < 1.0f)
                    ratingBar.setRating(1.0f);
            }
        });


        TextView btnDismiss = view.findViewById(R.id.rating_page_tv_dismiss);
        TextView btnSubmit = view.findViewById(R.id.rating_page_tv_submit);

        btnDismiss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialogRating.dismiss();
            }
        });
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialogRating.dismiss();
                alertDialog.dismiss();

                submitRatingToServer(ratingBar.getRating());

            }
        });
        alertDialogRating = builder.create();
        alertDialogRating.show();
    }

    private void hireNewAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.explorer_layout_hire_new, null);
        builder.setView(view);      //Working

        TextView btnCancel = view.findViewById(R.id.tv_cancel_hire_new);
        TextView btnYes = view.findViewById(R.id.tv_yes_hire_new);

        btnCancel.setOnClickListener(this);
        btnYes.setOnClickListener(this);

        alertDialogHireNew = builder.create();
        alertDialogHireNew.show();

    }

    private void displayEndChatDialogRecorder() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.recorder_layout_end_chat, null);
        builder.setView(view);

        final AlertDialog alertDialog = builder.create();
        alertDialog.show();


        TextView btnCancel = (TextView) view.findViewById(R.id.tv_cancel_end_chat_recorder);
        TextView btnYes = (TextView) view.findViewById(R.id.tv_yes_end_chat_recorder);

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });

        btnYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
                chattingStatus = Constants.CHATTING_STATUS_COMPLETE_JOB;
            }
        });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case CAMERA_PERMISSION_CONSTANT:

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //The External Storage Write Permission is granted to you... Continue your left job...
                    openGallery();
                } else {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                        //Show Information about why you need the permission
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setTitle("Need Camera Permission");
                        builder.setMessage("This app needs camera permission");
                        builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                                ActivityCompat.requestPermissions(ChatRoomActivity.this,
                                        new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CONSTANT);
                            }
                        });
                        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                        builder.show();
                    } else {
                        AppUtils.showToast(this, "Unable to get Permission");
                        //Toast.makeText(getBaseContext(), "Unable to get Permission", Toast.LENGTH_LONG).show();
                    }
                }
                break;
        }
    }//onRequestPermissionsResult
}