package com.app.siy.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.icu.text.DateFormat;
import android.net.Uri;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.app.siy.R;
import com.app.siy.activity.recorder.CompleteProfileActivityRecorder;
import com.app.siy.firebase.adapter.FcmUserAdapter;
import com.app.siy.firebase.model.FcmUser;
import com.app.siy.firebase.model.Message;
import com.app.siy.helper.SwipeToDeleteHelperRecorder;
import com.app.siy.rest.User;
import com.app.siy.sharedpreferences.MyPreferences;
import com.app.siy.utils.AppUtils;
import com.app.siy.utils.Constants;
import com.app.siy.utils.Progress;
import com.app.siy.utils.RecyclerItemClickListener;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.app.siy.utils.AppUtils.milliSecondToDateAndTime;
import static com.app.siy.utils.AppUtils.millisToTime;

public class ChatListActivity extends AppCompatActivity implements View.OnClickListener,
        SwipeToDeleteHelperRecorder.SwipeToDeleteHelperListenerRecorder {


    private static final String TAG = "CHAT";
    private static final int REQUEST_PERMISSION_SETTING = 28;
    private static final int EXTERNAL_STORAGE_PERMISSION_CONSTANT = 31;
    private static final int LOCATION_PERMISSION_CONSTANT = 32;
    private Toolbar toolbar;
    private ImageView backImage;

    private List<FcmUser> fcmUserList = new ArrayList<>();
    private List<Message> messageList;
    private RecyclerView recyclerView;
    private FcmUserAdapter fcmUserAdapter;
    private RelativeLayout backgroundLayout;
    ArrayList<String> messageReceiverList;
    private FirebaseAuth mAuth;
    private User user;
    private MyPreferences myPreferences;
    private SharedPreferences permissionStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_list);

        toolbar = (Toolbar) findViewById(R.id.include_toolbar_chat_list_recorder);
        TextView toolbarText = toolbar.findViewById(R.id.tool_bar_text);
        toolbarText.setText("Chat List");
        myPreferences = new MyPreferences(this);
        String userString = myPreferences.getString(MyPreferences.USER_MODEL);
        Gson gson = new Gson();
        user = gson.fromJson(userString, User.class);

        permissionStatus = getSharedPreferences("permissionStatus", MODE_PRIVATE);

        backgroundLayout = (RelativeLayout) findViewById(R.id.layout_chat_list_recorder);
        backImage = (ImageView) toolbar.findViewById(R.id.back_image_tool_bar);
        backImage.setOnClickListener(this);

        recyclerView = (RecyclerView) findViewById(R.id.chat_recycler_view_recorder);
        fcmUserAdapter = new FcmUserAdapter(this, fcmUserList, messageList);

        //Important to add LayoutManager
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        //recyclerView.setItemAnimator(new DefaultItemAnimator());

        recyclerView.setAdapter(fcmUserAdapter);
        getUsersFromFirebaseAndDisplayToRecyclerView();

        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new SwipeToDeleteHelperRecorder(0,
                ItemTouchHelper.LEFT, this);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView);

        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(this, recyclerView,
                new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        Intent intent = new Intent(ChatListActivity.this, ChatRoomActivity.class);
                        //intent.putExtra("CHAT_DETAILS_RECORDER", fcmUserList.get(position));
                        String userFullName = fcmUserList.get(position).getFirst_name() + " " + fcmUserList.get(position).getLast_name();
                        AppUtils.log(TAG, "User Full Name on Chat List : " + userFullName);
                        intent.putExtra("MESSAGE_RECEIVER_NAME", userFullName);
                        intent.putExtra("MESSAGE_RECEIVER_PROFILE_IMAGE", fcmUserList.get(position).getProfile_image());
                        intent.putExtra("MESSAGE_RECEIVER_USER_ID", fcmUserList.get(position).getUser_id());
                        intent.putExtra("MESSAGE_RECEIVER_FIREBASE_ID", messageReceiverList.get(position));
                        startActivity(intent);
                    }

                    @Override
                    public void onLongItemClick(View view, int position) {
                        // do whatever
                    }
                })
        );
        //checkForStoragePermission();
    }


    private void getUsersFromFirebaseAndDisplayToRecyclerView() {
        AppUtils.log(TAG, "Getting user details from firebase ...");
        final Progress progress = new Progress(this, R.style.CustomProgressDialogTheme);
        progress.show();

        messageReceiverList = new ArrayList<>();
        // If current user is Explorer then display only Recorders List.
        // If current user is Recorder then display only Explorers List.


        final DatabaseReference rootUserReference = FirebaseDatabase.getInstance().getReference();

        rootUserReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String userType = user.getUserType();
                String requiredUserType = "0";
                AppUtils.log(TAG, "User Type : " + userType);
                if (userType.equals(Constants.USER_TYPE_RECORDER))
                    requiredUserType = Constants.USER_TYPE_EXPLORER;
                if (userType.equals(Constants.USER_TYPE_EXPLORER))
                    requiredUserType = Constants.USER_TYPE_RECORDER;
                if (dataSnapshot.hasChild("Users")) {
                    AppUtils.log(TAG, "Users node is present");


                    // User present Now Filter the database.
                    // Filter the Database Users on the basic of User Type.
                    Query query = rootUserReference.child("Users").orderByChild("user_type").equalTo(requiredUserType);

                    query.addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                            progress.dismiss();
                            if (dataSnapshot.hasChildren()) {
                                AppUtils.log(TAG, "User details from Firebase is retrieved successfully ");
                                AppUtils.log(TAG, "onChild Added");
                                AppUtils.log(TAG, "Display all Recorders");
                                FcmUser fcmUser = dataSnapshot.getValue(FcmUser.class);
                                fcmUserList.add(fcmUser);
                                fcmUserAdapter.notifyDataSetChanged();
                                String messageReceiverId = dataSnapshot.getKey().toString();
                                AppUtils.log(TAG, "Message Receiver ID : " + messageReceiverId);
                                messageReceiverList.add(messageReceiverId);
                            } else {
                                AppUtils.showToast(ChatListActivity.this, "No User is present");
                            }
                        }

                        @Override
                        public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                            AppUtils.log(TAG, "onChildChanged");
                            progress.dismiss();
                        }

                        @Override
                        public void onChildRemoved(DataSnapshot dataSnapshot) {
                            AppUtils.log(TAG, "onChildRemoved");
                            progress.dismiss();
                        }

                        @Override
                        public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                            AppUtils.log(TAG, "onChildMoved");
                            progress.dismiss();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            AppUtils.log(TAG, "onCancelled");
                            progress.dismiss();
                        }
                    });

                } else {
                    // Not any User is present.
                    AppUtils.showToast(ChatListActivity.this, "No User");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                progress.dismiss();
                AppUtils.log(TAG, "onCancelled  " + databaseError.getMessage());
            }
        });
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
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
        if (viewHolder instanceof FcmUserAdapter.MyViewHolder) {
            //Get the removed item name to display it in snack bar.

            String name = fcmUserList.get(viewHolder.getAdapterPosition()).getFirst_name();

            //Back of removed item for undo purpose
            final FcmUser deletedItem = fcmUserList.get(viewHolder.getAdapterPosition());
            final int deletedIndex = viewHolder.getAdapterPosition();

            //Remove the item from RecyclerView
            fcmUserAdapter.removeItem(viewHolder.getAdapterPosition());

            Snackbar snackbar = Snackbar.make(backgroundLayout, name + " removed from list!", Snackbar.LENGTH_LONG);
            snackbar.setAction("UNDO ", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    fcmUserAdapter.restoreItem(deletedItem, deletedIndex);
                }
            });
            snackbar.show();
        }
    }


    private void checkForStoragePermission() {

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                //Show Information about why you need the permission
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Need Storage Permission");
                builder.setMessage("This app needs storage permission.");
                builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        ActivityCompat.requestPermissions(ChatListActivity.this,
                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                EXTERNAL_STORAGE_PERMISSION_CONSTANT);
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
                AlertDialog.Builder builder = new AlertDialog.Builder(ChatListActivity.this);
                builder.setTitle("Need Storage Permission");
                builder.setMessage("This app needs storage permission.");
                builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivityForResult(intent, REQUEST_PERMISSION_SETTING);
                        AppUtils.showToast(ChatListActivity.this, "Go to Permissions to Grant Storage");
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
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        EXTERNAL_STORAGE_PERMISSION_CONSTANT);
            }


            SharedPreferences.Editor editor = permissionStatus.edit();
            editor.putBoolean(Manifest.permission.WRITE_EXTERNAL_STORAGE, true);
            editor.commit();


        } else {
            //You already have the permission, just go ahead.
        }
    }//end of check for Storage permission.


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {


            case EXTERNAL_STORAGE_PERMISSION_CONSTANT:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //The External Storage Write Permission is granted to you... Continue your left job...
                    //openCamera();

                } else {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        //Show Information about why you need the permission
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setTitle("Need Storage Permission");
                        builder.setMessage("This app needs Storage permission");
                        builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                                ActivityCompat.requestPermissions(ChatListActivity.this,
                                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                        EXTERNAL_STORAGE_PERMISSION_CONSTANT);
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