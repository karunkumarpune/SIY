package com.app.siy.activity.recorder;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.app.siy.R;
import com.app.siy.activity.ChatListActivity;
import com.app.siy.activity.SettingsActivity;
import com.app.siy.firebase.model.FcmUser;
import com.app.siy.firebase.utils.FirebaseUtils;
import com.app.siy.rest.ApiClient;
import com.app.siy.rest.ApiInterface;
import com.app.siy.rest.ServerResponse;
import com.app.siy.rest.User;
import com.app.siy.sharedpreferences.MyPreferences;
import com.app.siy.utils.AppUtils;
import com.app.siy.utils.Constants;
import com.app.siy.utils.FileUtils;
import com.app.siy.utils.Progress;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RecorderHomeActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int REQUEST_PERMISSION_SETTING = 101;
    private static final int CAMERA_PERMISSION_CONSTANT = 100;
    private static final int REQUEST_VIDEO_CAPTURE = 102;
    private static final int EXTERNAL_STORAGE_PERMISSION_CONSTANT = 110;
    private static final String TAG_CHAT = "CHAT";
    private static final String TAG = "GALLERY";
    private static final int PERMISSION_CALLBACK_CONSTANT = 211;
    private boolean sentToSettings = false;
    private SharedPreferences permissionStatus;
    User user;
    Uri fileUri;

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private ScrollView scrollView;
    private RelativeLayout recordCapture;
    private RelativeLayout dataLibrary;
    private RelativeLayout taskList;
    private RelativeLayout chatList;
    private RelativeLayout paymentHistory;
    private RelativeLayout settings;
    private RelativeLayout help;
    private ImageView btnEditProfile;
    private ImageView btnNotification;

    private MyPreferences myPreferences;

    private RelativeLayout layoutHeaderExplorer;

    private FirebaseUtils firebaseUtils;
    private FirebaseAuth mAuth;
    private DatabaseReference userReference;
    private FirebaseAuth.AuthStateListener authStateListener;

    private ImageView backgroundImage;
    private Uri thumbImageUri;
    private String originalVideoPath;
    final String[] permissionsRequired = new String[]{Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recorder_activity_home);

        //Instantiate MyPreferences
        myPreferences = new MyPreferences(this);
        myPreferences.saveBoolean(MyPreferences.PROFILE_ACCESS_STATUS, true);
        myPreferences.saveString(MyPreferences.USER_TYPE, Constants.USER_TYPE_RECORDER);
        //Get data from SharedPreferences
        String jString = myPreferences.getString(MyPreferences.USER_MODEL);
        AppUtils.log("REST", "Json String : " + jString);
        user = new Gson().fromJson(jString, User.class);

        firebaseStatusAndNewEntry();

        //Getting data from SharedPreferences and place to the
        permissionStatus = getSharedPreferences("permissionStatus", MODE_PRIVATE);


        updateHeaderContent();

        //Check for Internet connection.
        if (!AppUtils.isNetworkAvailable(this)) {
            AppUtils.snackbar(this, findViewById(R.id.rl_recorder_home), "No internet connection");
        }


        scrollView = (ScrollView) findViewById(R.id.layout_home_recorder);
        dataLibrary = (RelativeLayout) findViewById(R.id.recorder_data_library);
        taskList = (RelativeLayout) findViewById(R.id.recorder_task_list);
        recordCapture = (RelativeLayout) findViewById(R.id.recorder_record_capture);
        chatList = (RelativeLayout) findViewById(R.id.recorder_chat_list);
        paymentHistory = (RelativeLayout) findViewById(R.id.recorder_payment_history);
        settings = (RelativeLayout) findViewById(R.id.recorder_setting);
        help = (RelativeLayout) findViewById(R.id.recorder_help);

        btnEditProfile = (ImageView) findViewById(R.id.edit_profile_recorder);
        btnNotification = (ImageView) findViewById(R.id.iv_notification_recorder);

        dataLibrary.setOnClickListener(this);
        taskList.setOnClickListener(this);
        recordCapture.setOnClickListener(this);
        chatList.setOnClickListener(this);
        paymentHistory.setOnClickListener(this);
        settings.setOnClickListener(this);
        help.setOnClickListener(this);
        btnEditProfile.setOnClickListener(this);
        btnNotification.setOnClickListener(this);
    }

    private void firebaseStatusAndNewEntry() {
        final String email = user.getEmail();
        final String passwordFromSharedPreferences = myPreferences.getString(MyPreferences.PASSWORD);

        firebaseUtils = new FirebaseUtils(this);
        mAuth = FirebaseAuth.getInstance();
        userReference = FirebaseDatabase.getInstance().getReference().child("Users");


        // Sign In User to Firebase.
        if (mAuth.getCurrentUser() == null) {
            // User is not logged In
            AppUtils.log(TAG_CHAT, "User with email : " + email + " is not logged in to Firebase");
            firebaseUtils.singInFirebase(email, passwordFromSharedPreferences);

            AppUtils.log(TAG_CHAT, "Checking " + email + " is present in the firebase database or Not ?");
            userReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String firebaseId = user.getFirebaseId();

                    if (firebaseId != null) {
                        if (dataSnapshot.hasChild(firebaseId)) {
                            // User is already present in the Firebase Database.
                            AppUtils.log(TAG_CHAT, email + " is present in the Firebase database with user id : " + firebaseId);
                        } else {
                            // User is not Present in the Firebase. User may be on the FirebaseAuth.
                            AppUtils.log(TAG_CHAT, email + " is not Present in the Firebase Database. Adding New user ... ");
                            FcmUser fcmUser = new FcmUser(
                                    user.getDeviceToken(),
                                    user.getFirstName(),
                                    user.getLastName(),
                                    user.getImage(),
                                    user.getUserId(),
                                    user.getUserType(),
                                    false,
                                    String.valueOf(System.currentTimeMillis()),
                                    "2"
                            );
                            firebaseUtils.addNewUserToFirebaseAuth(user.getEmail(), passwordFromSharedPreferences, fcmUser);
                        }
                    } else {
                        AppUtils.log(TAG_CHAT, "Firebase Id is Null for email : " + email);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });


        } else {
            AppUtils.log(TAG_CHAT, email + " is already logged in to Fireabase");
        }
    }

    private void updateHeaderContent() {
        layoutHeaderExplorer = (RelativeLayout) findViewById(R.id.layout_header_explorer);
        //layoutContentExplorer = (RelativeLayout) findViewById(R.id.layout_recorder_header);

        ImageView profileImage = layoutHeaderExplorer.findViewById(R.id.image_view_recorder_header);
        TextView recorderName = layoutHeaderExplorer.findViewById(R.id.recorder_name);
        backgroundImage = (ImageView) findViewById(R.id.background_image_recorder_home);
        recorderName.setText(user.getFirstName() + " " + user.getLastName());

        String profileUrl = user.getImage();
        AppUtils.log(TAG, "Profile Image Uri : " + profileUrl);
        Glide.with(this).load(ApiClient.BASE_URL_UPLOADED_IMAGE + profileUrl).into(profileImage);


        Glide.with(this).load(ApiClient.BASE_URL_UPLOADED_IMAGE + profileUrl).centerCrop().crossFade().into(backgroundImage);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.iv_notification_recorder:
                Intent notificationIntent = new Intent(this, NotificationsHistoryActivity.class);
                startActivity(notificationIntent);
                overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
                break;

            case R.id.edit_profile_recorder:
                startActivity(new Intent(this, EditProfileRecorder.class));
                break;

            case R.id.recorder_record_capture:
                //checkForStoragePermission();
                //checkForCameraPermission();
                checkForCameraAndStoragePermission();
                break;

            case R.id.recorder_task_list:
                startActivity(new Intent(getApplicationContext(), RecorderTaskListActivity.class));
                break;

            case R.id.recorder_chat_list:
                startActivity(new Intent(this, ChatListActivity.class));
                break;

            case R.id.recorder_data_library:
                openDataLibrary();
                //checkForStoragePermission();
                break;

            case R.id.recorder_payment_history:
                Intent paymentHistoryIntent = new Intent(this, PaymentHistoryRecorder.class);
                startActivity(paymentHistoryIntent);
                break;

            case R.id.recorder_setting:
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingsIntent);
                //overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
                //AppUtils.showToast(this, "Settings");
                break;

            case R.id.recorder_help:
                Intent helpIntent = new Intent(this, HelpActivityRecorder.class);
                startActivity(helpIntent);
                //overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
                //AppUtils.showToast(this, "Help");
                break;
        }
    }

    //Code for press back button once again to exit.
    boolean doubleBackToExitPressedOnce = false;

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        AppUtils.snackbar(this, findViewById(R.id.rl_recorder_home), "Press once again to Exit");
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 1000);
    }


    private void checkForCameraAndStoragePermission() {
        if (ActivityCompat.checkSelfPermission(
                RecorderHomeActivity.this, permissionsRequired[0]) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(RecorderHomeActivity.this, permissionsRequired[1]) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(RecorderHomeActivity.this, permissionsRequired[0])
                    || ActivityCompat.shouldShowRequestPermissionRationale(RecorderHomeActivity.this, permissionsRequired[1])) {
                //Show Information about why you need the permission
                AlertDialog.Builder builder = new AlertDialog.Builder(RecorderHomeActivity.this);
                builder.setTitle("Need Multiple Permissions");
                builder.setMessage("This app needs Camera and Storage permissions.");
                builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        ActivityCompat.requestPermissions(RecorderHomeActivity.this, permissionsRequired, PERMISSION_CALLBACK_CONSTANT);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            } else if (permissionStatus.getBoolean(permissionsRequired[0], false)) {
                //Previously Permission Request was cancelled with 'Dont Ask Again',
                // Redirect to Settings after showing Information about why you need the permission
                AlertDialog.Builder builder = new AlertDialog.Builder(RecorderHomeActivity.this);
                builder.setTitle("Need Multiple Permissions");
                builder.setMessage("This app needs Camera and Storage permissions.");
                builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        sentToSettings = true;
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivityForResult(intent, REQUEST_PERMISSION_SETTING);
                        Toast.makeText(getBaseContext(), "Go to Permissions to Grant  Camera and Location", Toast.LENGTH_LONG).show();
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
                //just request the permission
                ActivityCompat.requestPermissions(RecorderHomeActivity.this, permissionsRequired, PERMISSION_CALLBACK_CONSTANT);
            }


            SharedPreferences.Editor editor = permissionStatus.edit();
            editor.putBoolean(permissionsRequired[0], true);
            editor.commit();
        } else {
            openCameraForImageOrVideo();
        }
    }

    //Permissions
    private void checkForCameraPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                //Show Information about why you need the permission
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Need Camera Permission");
                builder.setMessage("This app needs camera permission.");
                builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        ActivityCompat.requestPermissions(RecorderHomeActivity.this,
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
            } else if (permissionStatus.getBoolean(Manifest.permission.CAMERA, false)) {
                //Previously Permission Request was cancelled with 'Dont Ask Again',
                // Redirect to Settings after showing Information about why you need the permission
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Need camera Permission");
                builder.setMessage("This app needs camera permission.");
                builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        sentToSettings = true;
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivityForResult(intent, REQUEST_PERMISSION_SETTING);

                        AppUtils.showToast(RecorderHomeActivity.this, "Go to Permissions to Grant Camera");
                        //Toast.makeText(RecorderHomeActivity.this, "Go to Permissions to Grant Camera", Toast.LENGTH_LONG).show();
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
                //just request the permission
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CONSTANT);
            }


            SharedPreferences.Editor editor = permissionStatus.edit();
            editor.putBoolean(Manifest.permission.CAMERA, true);
            editor.commit();


        } else {
            //You already have the permission, just go ahead.
            openCameraForImageOrVideo();
        }
    }//end of check for Storage permission.


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable("FILE_URI", fileUri);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        fileUri = savedInstanceState.getParcelable("FILE_URI");
    }

    private void openCameraForImageOrVideo() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select");
        builder.setItems(new String[]{"Record", "Capture"}, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    AppUtils.log(TAG, "Capturing image ... ");
                    // Record Video -> Open Image Capture Intent
                    Intent videoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                    /*fileUri = FileHelper.saveVideoToSpecifiedFolder(RecorderHomeActivity.this);
                    videoIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
                    videoIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    videoIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);*/
                    startActivityForResult(videoIntent, REQUEST_VIDEO_CAPTURE);
                } else if (which == 1) {
                    // Capture Image -> Open Video Capture Intent.
                    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
//-------------------------------------------------

            case PERMISSION_CALLBACK_CONSTANT:
                //if (requestCode == ) {
                //check if all permissions are granted
                boolean allgranted = false;
                for (int i = 0; i < grantResults.length; i++) {
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        allgranted = true;
                    } else {
                        allgranted = false;
                        break;
                    }
                }

                if (allgranted) {
                    openCameraForImageOrVideo();
                } else if (ActivityCompat.shouldShowRequestPermissionRationale(RecorderHomeActivity.this, permissionsRequired[0])
                        || ActivityCompat.shouldShowRequestPermissionRationale(RecorderHomeActivity.this, permissionsRequired[1])) {

                    //txtPermissions.setText("Permissions Required");

                    AlertDialog.Builder builder = new AlertDialog.Builder(RecorderHomeActivity.this);
                    builder.setTitle("Need Multiple Permissions");
                    builder.setMessage("This app needs Camera and Storage permissions.");
                    builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            ActivityCompat.requestPermissions(RecorderHomeActivity.this, permissionsRequired, PERMISSION_CALLBACK_CONSTANT);
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
                break;
//-----------------------------------------------
            /*case CAMERA_PERMISSION_CONSTANT:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //The External Storage Write Permission is granted to you... Continue your left job...
                    openCameraForImageOrVideo();
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
                                ActivityCompat.requestPermissions(RecorderHomeActivity.this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CONSTANT);
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

            case EXTERNAL_STORAGE_PERMISSION_CONSTANT:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //The External Storage Write Permission is granted to you... Continue your left job...
                    //openDataLibrary();
                    openCameraForImageOrVideo();
                } else {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        //Show Information about why you need the permission
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setTitle("Need Storage Permission");
                        builder.setMessage("This app needs storage permission");
                        builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                                ActivityCompat.requestPermissions(RecorderHomeActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, EXTERNAL_STORAGE_PERMISSION_CONSTANT);
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
                        AppUtils.showToast(RecorderHomeActivity.this, "Unable to get Permission");
                    }
                }
                break;*/

        }
    }//onRequestPermissionsResult


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_IMAGE_CAPTURE:
                if (resultCode == RESULT_OK) {
                    //String type = data.getType();
                    //AppUtils.log("Type : " + type);
                    //Uri uri = data.getData(); // Returns null in case of Camera
                    Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                    Uri imageTempUri = getImageUri(this, bitmap);
                    AppUtils.log(TAG, "Image Uri : " + imageTempUri);
                    addImageToServer(imageTempUri);

                    /*Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                    FileHelper.saveImageToSpecifiedFolder(bitmap);
                    AppUtils.showToast(this, "Saved");
                    AppUtils.log("Image Saved to the SIY/IMAGE folder");*/
                }
                break;

            case REQUEST_VIDEO_CAPTURE:
                if (resultCode == RESULT_OK) {
                    AppUtils.log("onActivityResult - Video");
                    Uri videoUri = data.getData();
                    AppUtils.log(TAG, "Video Uri ." + videoUri);
                    originalVideoPath = getRealPathFromURI(videoUri);
                    AppUtils.log(TAG, "Video Path : " + originalVideoPath);

                    Bitmap thumb = ThumbnailUtils.createVideoThumbnail(originalVideoPath,
                            MediaStore.Video.Thumbnails.MINI_KIND);

                    if (thumb != null) {
                        AppUtils.log(TAG, "Thumb image is not null");
                        int byteCount = thumb.getByteCount();
                        AppUtils.log(TAG, "Number of Bytes : " + byteCount);
                        thumbImageUri = getImageUri(this, thumb);
                        AppUtils.log(TAG, "Thumb Image Uri : " + thumbImageUri);
                    } else {
                        AppUtils.log(TAG, "Thumb image is null");
                    }
                    new VideoCompressorTask().execute(originalVideoPath);
                }
                break;
        }
    }


    class VideoCompressorTask extends AsyncTask<String, String, String> {
        Progress progress;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progress = new Progress(RecorderHomeActivity.this, R.style.CustomProgressDialogTheme);
            progress.show();
        }


        @Override
        protected String doInBackground(String... params) {
            AppUtils.log(TAG, "Processing in background " + params[0]);
            String originalPath = params[0];

            File dcimDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
            File compressedVideoFile = new File(dcimDirectory, "compressed.mp4");
            String compressedVideoFilePath = compressedVideoFile.getAbsolutePath();
            AppUtils.log(TAG, "Compressed Video path: " + compressedVideoFilePath);
            boolean isCompressed = com.yovenny.videocompress.MediaController.getInstance()
                    .convertVideo(originalPath, compressedVideoFilePath);
            if (isCompressed) {
                return compressedVideoFilePath;
            } else return originalPath;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            progress.dismiss();
            AppUtils.log(TAG, "Compressed Video path from onPostExecute : " + s);
            addVideoToServer(s);
        }
    }


    private void addVideoToServer(final String compressedVideoPath) {

        AppUtils.log(TAG, "Uploading Video to SIY server ... ");
        AppUtils.log(TAG, "Selected Video Path : " + compressedVideoPath);
        //AppUtils.log(TAG, "Last Path Segment 2: " + selectedImageUri.getLastPathSegment());
        if (AppUtils.isNetworkAvailable(this)) {
            final Progress progress = new Progress(this, R.style.CustomProgressDialogTheme);
            progress.setMessage("Uploading Video ... ");
            progress.setCancelable(false);
            progress.show();

            // Create Video Part.
            final File fileCompressed = new File(compressedVideoPath);
            //MultipartBody.Part imagePart = prepareFilePart("data", selectedImageUri);
            RequestBody requestBody = RequestBody.create(
                    MediaType.parse("video/*"),
                    fileCompressed
            );

            MultipartBody.Part videoPart = MultipartBody.Part.createFormData("data",
                    fileCompressed.getName(),
                    requestBody);
            // Create Thumbnail Part.

            MultipartBody.Part thumbnailPart = prepareFilePart("thumbnail", thumbImageUri);

            // Data type is 1 for Audio and Data type is 2 for Video.
            RequestBody dataTypeBody = createPartFromString("2");

            ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
            Call<ServerResponse> serverResponseCall = apiInterface
                    .addImageOrVideoToServer(user.getAccessToken(), videoPart, dataTypeBody, thumbnailPart);
            serverResponseCall.enqueue(new Callback<ServerResponse>() {
                @Override
                public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {
                    progress.dismiss();
                    if (response.isSuccessful()) {
                        AppUtils.showToast(RecorderHomeActivity.this, "Video Uploaded");
                        AppUtils.log(TAG, "Video Uploaded to SIY Server");


                        // Delete Compressed Video File
                        if (fileCompressed.delete()) AppUtils.log(TAG, "Compressed File Deleted.");
                        else AppUtils.log(TAG, "Unable to delete Compressed File.");

                        // Now Delete original Video File
                        File originalVideoFile = new File(originalVideoPath);
                        if (originalVideoFile.delete()) AppUtils.log(TAG, "Original File Deleted.");
                        else AppUtils.log(TAG, "Unable to Delete Original File.");

                    } else {
                        try {
                            AppUtils.log(TAG, "Error while uploading video to SIY server 1" + response.errorBody().string());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onFailure(Call<ServerResponse> call, Throwable t) {
                    progress.dismiss();
                    /*if (file.delete()) AppUtils.log(TAG, "File Deleted2");
                    else AppUtils.log(TAG, "File Not Deleted2");*/
                    AppUtils.log(TAG, "Video uploading fail 2: " + t.getMessage());
                }
            });
        } else {
            AppUtils.showToast(this, Constants.NO_INTERNET_CONNECTION);
        }
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    //Helper method. - Retrofit
    @NonNull
    private MultipartBody.Part prepareFilePart(String partName, Uri fileUri) {
        // https://github.com/iPaulPro/aFileChooser/blob/master/aFileChooser/src/com/ipaulpro/afilechooser/utils/FileUtils.java
        // use the FileUtils to get the actual file by uri
        //FileUtils class is present inside com.app.siy.utils.FileUtils

        MultipartBody.Part part = null;
        File file = FileUtils.getFile(this, fileUri);       //File file = new File(fileUri.getPath());  // will not work.
        RequestBody requestFile = null;

        try {
            if (file != null) {
                // create RequestBody instance from file
                requestFile =
                        RequestBody.create(
                                MediaType.parse("image/*"),
                                file
                        );

                part = MultipartBody.Part.createFormData(partName, file.getName(), requestFile);
            } else {
                //AppUtils.log("File is null");
            }
        } catch (Exception e) {
            //AppUtils.log("Exception while creating Part " + e.getMessage());
        }
        // MultipartBody.Part is used to send also the actual file name
        return part;
    }


    //Helper method. - Retrofit
    @NonNull
    private MultipartBody.Part prepareFilePartVideo(String partName, Uri fileUri) {
        // https://github.com/iPaulPro/aFileChooser/blob/master/aFileChooser/src/com/ipaulpro/afilechooser/utils/FileUtils.java
        // use the FileUtils to get the actual file by uri
        //FileUtils class is present inside com.app.siy.utils.FileUtils

        MultipartBody.Part part = null;
        File file = FileUtils.getFile(this, fileUri);       //File file = new File(fileUri.getPath());  // will not work.
        RequestBody requestFile = null;

        try {
            if (file != null) {
                // create RequestBody instance from file
                requestFile =
                        RequestBody.create(
                                MediaType.parse("video/*"),
                                file
                        );

                part = MultipartBody.Part.createFormData(partName, file.getName(), requestFile);
            } else {
                //AppUtils.log("File is null");
            }
        } catch (Exception e) {
            //AppUtils.log("Exception while creating Part " + e.getMessage());
        }
        // MultipartBody.Part is used to send also the actual file name
        return part;
    }


    //Helper method. - Retrofit
    @NonNull
    private RequestBody createPartFromString(String descriptionString) {
        return RequestBody.create(
                okhttp3.MultipartBody.FORM, descriptionString);
    }


    private void addImageToServer(Uri selectedImageUri) {
        AppUtils.log(TAG, "Uploading Image to SIY server ... ");
        AppUtils.log(TAG, "Selected Image Uri : " + selectedImageUri);
        //AppUtils.log(TAG, "Last Path Segment 2: " + selectedImageUri.getLastPathSegment());
        if (AppUtils.isNetworkAvailable(this)) {
            final Progress progress = new Progress(this, R.style.CustomProgressDialogTheme);
            progress.setCancelable(false);
            progress.show();
            MultipartBody.Part imagePart = prepareFilePart("data", selectedImageUri);
            MultipartBody.Part thumbnailPart = prepareFilePart("thumbnail", null);

            // Data type is 1 for Audio and Data type is 2 for Video.
            RequestBody dataTypeBody = createPartFromString("1");


            ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
            Call<ServerResponse> serverResponseCall = apiInterface
                    .addImageOrVideoToServer(user.getAccessToken(), imagePart, dataTypeBody, thumbnailPart);
            serverResponseCall.enqueue(new Callback<ServerResponse>() {
                @Override
                public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {
                    progress.dismiss();
                    if (response.isSuccessful()) {
                        AppUtils.showToast(RecorderHomeActivity.this, "Image Uploaded");
                        AppUtils.log(TAG, "Image Uploaded to SIY Server");

                    } else {
                        try {
                            AppUtils.log(TAG, "Error while uploading image to SIY server " + response.errorBody().string());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onFailure(Call<ServerResponse> call, Throwable t) {
                    progress.dismiss();
                    AppUtils.log(TAG, "Image uploading fail " + t.getMessage());
                }
            });
        } else {
            AppUtils.showToast(this, Constants.NO_INTERNET_CONNECTION);
        }
    }


    private void checkForStoragePermission() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                //Show Information about why you need the permission
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Need Storage Permission");
                builder.setMessage("This app needs storage permission.");
                builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        ActivityCompat.requestPermissions(RecorderHomeActivity.this,
                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, EXTERNAL_STORAGE_PERMISSION_CONSTANT);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            } else if (permissionStatus.getBoolean(Manifest.permission.WRITE_EXTERNAL_STORAGE, false)) {
                //Previously Permission Request was cancelled with Don't Ask Again',
                // Redirect to Settings after showing Information about why you need the permission
                AlertDialog.Builder builder = new AlertDialog.Builder(RecorderHomeActivity.this);
                builder.setTitle("Need Storage Permission");
                builder.setMessage("This app needs storage permission.");
                builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        sentToSettings = true;
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivityForResult(intent, REQUEST_PERMISSION_SETTING);
                        AppUtils.showToast(RecorderHomeActivity.this, "Go to Permissions to Grant Storage");
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
                //just request the permission
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        EXTERNAL_STORAGE_PERMISSION_CONSTANT);
            }


            SharedPreferences.Editor editor = permissionStatus.edit();
            editor.putBoolean(Manifest.permission.WRITE_EXTERNAL_STORAGE, true);
            editor.commit();


        } else {
            //You already have the permission, just go ahead.
            openCameraForImageOrVideo();
            //openDataLibrary();
        }
    }//end of check for Storage permission.


    private void openDataLibrary() {
        Intent dataLibraryIntent = new Intent(this, DataLibraryRecorderActivity.class);
        startActivity(dataLibraryIntent);
    }


    private String getRealPathFromURI(Uri contentUri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = managedQuery(contentUri, proj, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }


}