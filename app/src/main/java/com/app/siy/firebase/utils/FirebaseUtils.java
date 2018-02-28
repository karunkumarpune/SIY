package com.app.siy.firebase.utils;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;

import com.app.siy.R;
import com.app.siy.firebase.model.FcmUser;
import com.app.siy.rest.ApiClient;
import com.app.siy.rest.ApiInterface;
import com.app.siy.rest.ServerResponse;
import com.app.siy.rest.User;
import com.app.siy.sharedpreferences.MyPreferences;
import com.app.siy.utils.AppUtils;
import com.app.siy.utils.Progress;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by Manish-Pc on 10/01/2018.
 */

public class FirebaseUtils {

    private static final String TAG = "CHAT";

    private FirebaseAuth mAuth;
    private Context context;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference rootReference;
    private DatabaseReference userNodeReference;
    private MyPreferences myPreferences;
    private User user;

    public FirebaseUtils(Context context) {
        this.context = context;
        mAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        rootReference = firebaseDatabase.getReference();
        userNodeReference = rootReference.child("Users");
        myPreferences = new MyPreferences(context);

        String userString = myPreferences.getString(MyPreferences.USER_MODEL);
        Gson gson = new Gson();
        user = gson.fromJson(userString, User.class);
    }

    public void checkUserExistInDatabaseOrNot(final String firebaseUserId) {
        // This method is used when user is logged in Using Facebook, Google SignIn or other ways.
        // User's recored is present inside Firebase Auth but not in Firebase Database.
        // User is logged In (using FirebaseAuth) but no record is present inside FirebaseDatabase.
        //final String loggedInUser = mAuth.getCurrentUser().getUid();
        DatabaseReference userReference = FirebaseDatabase.getInstance().getReference().child("Users");
        userReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.hasChild(firebaseUserId)) {
                    // User data is present inside Firebase Database.
                    AppUtils.log("User's data is present inside Firebase Database");

                } else {
                    // User's data is not present inside Firebase Database.
                    AppUtils.log("User's data is Not present inside Firebase Database");
                    // Signup user with email and Password.
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    public void addNewUserToFirebaseAuth(final String email, String password, final FcmUser userModel) {
        AppUtils.log(TAG, "Adding new user to Firebase Auth ... ");
        final Progress progress = new Progress(context, R.style.CustomProgressDialogTheme);
        progress.setCancelable(false);

        if (!((Activity) context).isFinishing()) {
            //show dialog
            progress.show();
        }

        Task<AuthResult> result = mAuth.createUserWithEmailAndPassword(email, password);
        result.addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    progress.dismiss();
                    String firebaseUserId = mAuth.getCurrentUser().getUid();
                    AppUtils.log(TAG, "New User with email " + email + " is Added to Firebase Auth with user Id : " + firebaseUserId);

                    // Update Model with Firebase UserId
                    user.setFirebaseId(firebaseUserId);
                    // Save Firebase UId to Shared Preferences.
                    myPreferences.saveString(MyPreferences.FIREBASE_USER_ID, firebaseUserId);

                    // Now add Details to Firebase Database.
                    addUserInfoFirebaseDatabase(firebaseUserId, userModel);

                    // Save Firebase UId to server
                    saveFirebaseUserIdToServer(firebaseUserId);

                    // Getting Device Token
                    String deviceToken = FirebaseInstanceId.getInstance().getToken();
                    AppUtils.log(TAG, "Firebase Device Token with email " + email + " is : " + deviceToken);
                } else {
                    progress.dismiss();
                    AppUtils.log(TAG, "Error while adding new user : " + email + " to firebase auth : " + task.getException().getMessage());
                }
            }
        });

        result.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                AppUtils.log(TAG, "Exception while adding new user " + email + " to the Firebase : " + e.getMessage());
            }
        });
    }

    public void addUserInfoFirebaseDatabase(final String firebaseUserId, final FcmUser userModelFirebase) {
        AppUtils.log(TAG, "Adding " + userModelFirebase.getFirst_name() + " to Firebase Database ... ");
        final Progress progress = new Progress(context, R.style.CustomProgressDialogTheme);
        progress.show();
        progress.setCancelable(false);

        Task<Void> task = userNodeReference.child(firebaseUserId).setValue(userModelFirebase);
        task.addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    progress.dismiss();
                    AppUtils.log(TAG, userModelFirebase.getFirst_name() + " is added to the Firebase Database with Firebase id : " + firebaseUserId);
                } else {
                    progress.dismiss();
                    AppUtils.log(TAG, "Some Error while adding " + userModelFirebase.getFirst_name() + "to Firebase Database.");
                }
            }
        });
    }

    private void saveFirebaseUserIdToServer(final String firebaseUserId) {
        AppUtils.log(TAG, "Saving firebase user id to Server ... ");
        final Progress progress = new Progress(context, R.style.CustomProgressDialogTheme);
        progress.show();
        progress.setCancelable(false);

        ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
        Call<ServerResponse> serverResponseCall = apiInterface.saveFirebaseIdToServer(user.getAccessToken(), firebaseUserId);
        serverResponseCall.enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {
                if (response.isSuccessful()) {
                    progress.dismiss();
                    AppUtils.log(TAG, "Firebase UserId is saved to server with id : " + firebaseUserId);
                } else {
                    progress.dismiss();
                    try {
                        AppUtils.log(TAG, "Unable to save Firebase UserId " + firebaseUserId + ". Error : " + response.errorBody().string());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<ServerResponse> call, Throwable t) {
                progress.dismiss();
                AppUtils.log(TAG, "Exception While saving firebase user id to server : " + t.getMessage());
            }
        });
    }


    public ArrayList<String> getAllProfileImages() {
        AppUtils.log(TAG, "Getting all profile Images from Server ... ");
        final ArrayList<String> alOfProfileImages = new ArrayList();
        Query query = rootReference.child("Users").orderByChild("user_type").equalTo("2");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot ds : dataSnapshot.getChildren()) {
                        String profileImagePath = ds.child("profile_image").getValue().toString();
                        //String profileImagePath = dataSnapshot.child("profile_image").toString();
                        alOfProfileImages.add(profileImagePath);
                        AppUtils.log(TAG, "Profile Image Path : " + profileImagePath);
                        AppUtils.log(TAG, "Array List of Images : " + alOfProfileImages);
                    }
                } else {
                    AppUtils.log(TAG, "Some Error occurs while getting image from Firebase Database.");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                AppUtils.log(TAG, "Some Error While getting Image " + databaseError.getMessage());
            }
        });
        return alOfProfileImages;
    }

    public void singInFirebase(final String email, final String password) {
        AppUtils.log(TAG, "sign in user with email " + email + " and password " + password + " to firebase ...");
        Task<AuthResult> authResultTask = mAuth.signInWithEmailAndPassword(email, password);
        authResultTask.addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                // Getting current user Id. i.e. Online User Id
                if (task.isSuccessful()) {
                    //saveFirebaseUserIdToServer(firebaseUserId);
                    AppUtils.log(TAG, "Logged In SuccessFully to the firebase with email : " + email);


                } else {
                    AppUtils.log(TAG, "Unable to Logged Into Firebase. " + task.getException().getMessage());
                    AppUtils.log(TAG, "User is not present inside Firebase database.");

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

                    addNewUserToFirebaseAuth(email, password, fcmUser);

                }
            }
        });

        authResultTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                AppUtils.log(TAG, "Some Error while sign in to Firebase with email id " + email + " Error : " + e.getMessage());
            }
        });
    }
}