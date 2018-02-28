package com.app.siy.activity.explorer;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.app.siy.R;
import com.app.siy.activity.ChatListActivity;
import com.app.siy.activity.SettingsActivity;
import com.app.siy.firebase.model.FcmUser;
import com.app.siy.firebase.utils.FirebaseUtils;
import com.app.siy.rest.ApiClient;
import com.app.siy.rest.ApiInterface;
import com.app.siy.rest.ExplorerStatusModel;
import com.app.siy.rest.ParentModel;
import com.app.siy.rest.ServerResponse;
import com.app.siy.rest.ServerResponseExplorerRequestStatus;
import com.app.siy.rest.User;
import com.app.siy.sharedpreferences.MyPreferences;
import com.app.siy.utils.AppUtils;
import com.app.siy.utils.Constants;
import com.app.siy.utils.Progress;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import static com.google.android.gms.maps.CameraUpdateFactory.newLatLng;

public class ExplorerHomeActivity extends FragmentActivity implements OnMapReadyCallback,
        View.OnClickListener {


    private String TAG_CHAT = "CHAT";
    private String TAG_REST = "REST";
    private static final int PLACE_PICKER_REQUEST = 800;
    private static final int REQUEST_PERMISSION_SETTING = 101;
    private SharedPreferences permissionStatus;

    private FrameLayout frameLayout;
    private GoogleMap mMap;
    private LocationManager locationManager;
    private Toolbar toolbar;
    private Button btnHireRecorder;
    private TextView tvSearch;
    private boolean isFromReHired = false;
    private User userOb;
    private MyPreferences myPreferences;
    private double selectedLatitude;
    private double selectedLongitude;
    private FirebaseUtils firebaseUtils;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private DatabaseReference userReference;
    private Progress progress;
    private RelativeLayout rootLayout;
    private DrawerLayout drawerLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.explorer_activity_home);
        gettingIdOfInputFields();


        //  -- Get User data from SharedPreferences.
        myPreferences = new MyPreferences(this);
        myPreferences.saveBoolean(MyPreferences.PROFILE_ACCESS_STATUS, true);
        myPreferences.saveString(MyPreferences.USER_TYPE, Constants.USER_TYPE_EXPLORER);

        //Getting User Object From Shared Preferences
        String jsonString = myPreferences.getString(MyPreferences.USER_MODEL);
        AppUtils.log("JSON", "JSON String : " + jsonString);
        userOb = new Gson().fromJson(jsonString, User.class);


        firebaseStatusAndNewEntry();

        permissionStatus = getSharedPreferences(Constants.PERMISSION_STATUS, MODE_PRIVATE);

        setContentToSidebar();

        //Check for Internet connection.
        if (!AppUtils.isNetworkAvailable(this)) {
            AppUtils.snackbar(this, findViewById(R.id.rl_content_layout_home_recorder),
                    Constants.NO_INTERNET_CONNECTION);

        }

        //Change the Button label when explorer comes from selection of Re-Hired option.
        isFromReHired = getIntent().getBooleanExtra(Constants.INTENT_KEY_IS_FROM_RE_HIRED, false);
        if (isFromReHired) {
            btnHireRecorder.setText(Constants.RE_HIRED);
        } else {
            btnHireRecorder.setText(Constants.HIRE_A_RECORDER);
        }

        String accessToken = userOb.getAccessToken();
        AppUtils.log("Access Token : " + accessToken);

        displayMapAtPresentLocation();

        // Getting Explorer Request Status
        getExplorerRequestStatus();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        //getExplorerRequestStatus();
    }

    private void getExplorerRequestStatus() {
        AppUtils.log(TAG_REST, "Getting Explorer Request Status ...");
        final Progress progress = new Progress(this, R.style.CustomProgressDialogTheme);
        progress.show();
        progress.setCancelable(false);

        if (AppUtils.isNetworkAvailable(this)) {
            ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
            Call<ServerResponseExplorerRequestStatus> serverResponseCall =
                    apiInterface.getExplorerRequestStatus(userOb.getAccessToken());

            serverResponseCall.enqueue(new Callback<ServerResponseExplorerRequestStatus>() {
                @Override
                public void onResponse(Call<ServerResponseExplorerRequestStatus> call,
                                       Response<ServerResponseExplorerRequestStatus> response) {
                    progress.dismiss();
                    if (response.isSuccessful()) {
                        AppUtils.log(TAG_REST, "Get explorer request status response is successful");
                        String message = response.body().message;
                        AppUtils.log(TAG_REST, "Message : " + message);
                        int status = response.body().status;
                        AppUtils.log(TAG_REST, "Status : " + status);

                        if (status == ApiClient.EXPLORER_REQUEST_STATUS_ACCEPTED) { // STATUS = 1
                            ParentModel parentModel = response.body().result;
                            Gson gson = new Gson();
                            String parentJson = gson.toJson(parentModel);
                            AppUtils.log(TAG_REST, "JSON " + parentJson);
                            AppUtils.log(TAG_REST, "Explorer Request Accepted");
                            //User userRecorder = response.body().user;
                            int recorderId = parentModel.getUserId();
                            // If status is 1 then Recorder id is present inside user_id;
                            AppUtils.log(TAG_REST, "Explorer Home - Recorder User Id :-- " + recorderId);
                            Intent bookingConfirmationIntent = new Intent(ExplorerHomeActivity.this,
                                    BookingConfirmation.class);
                            bookingConfirmationIntent.putExtra("USER_ID_RECORDER", String.valueOf(recorderId));
                            startActivityForResult(bookingConfirmationIntent, 34);
                            //finish();
                        } else if (status == ApiClient.EXPLORER_REQUEST_STATUS_PENDING) {   // PENDING = 2
                            if (response.body() != null) {
                                AppUtils.log(TAG_REST, "Explorer Request is Pending");
                                ParentModel parentModel = response.body().result;
                                Gson gson = new Gson();
                                String parentJson = gson.toJson(parentModel);
                                AppUtils.log(TAG_REST, "JSON " + parentJson);


                                /*//int id = parentModel.getId();
                                int explorerId = parentModel.getExplorerId();
                                int recorderId = parentModel.getRecorderId();
                                int explorerNotifyStatus = parentModel.getExplorerNotifyStatus();
                                int recorderNotifyStatus = parentModel.getRecorderNotifyStatus();
                                String createdDate = parentModel.getCreatedDate();

                                //AppUtils.log(TAG_REST, "Id : " + id);
                                AppUtils.log(TAG_REST, "Explorer Id : " + explorerId);
                                AppUtils.log(TAG_REST, "Recorder Id : " + recorderId);
                                AppUtils.log(TAG_REST, "Explorer Notify Status : " + explorerNotifyStatus);
                                AppUtils.log(TAG_REST, "Recorder Notify Status : " + recorderNotifyStatus);
                                AppUtils.log(TAG_REST, "Created Date : " + createdDate);*/
                                displayLoaderScreen();
                            } else {
                                AppUtils.log(TAG_REST, "Explorer Model is null");
                            }

                        } else if (status == ApiClient.EXPLORER_REQUEST_STATUS_NEW_USER) {  // NEW USER = 3
                            // New User -> Show this Activity
                            AppUtils.log(TAG_REST, "Explorer Request is new Request");
                        }
                    } else {
                        try {
                            AppUtils.log(TAG_REST, "Get explorer request status response is Fail " + response.errorBody().string());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onFailure(Call<ServerResponseExplorerRequestStatus> call, Throwable t) {
                    progress.dismiss();
                    AppUtils.log(TAG_REST, "Get explorer request status onFailure " + t.getMessage());
                }
            });
        } else {
            AppUtils.showToast(this, Constants.NO_INTERNET_CONNECTION);
        }

    }

    private void displayLoaderScreen() {
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.explorer_home_loader_layout, null);
        rootLayout.addView(view);
        frameLayout.setEnabled(false);
        tvSearch.setEnabled(false);
        btnHireRecorder.setEnabled(false);
    }

    private void firebaseStatusAndNewEntry() {
        final String email = userOb.getEmail();
        final String passwordFromSharedPreferences = myPreferences.getString(MyPreferences.PASSWORD);

        firebaseUtils = new FirebaseUtils(this);
        mAuth = FirebaseAuth.getInstance();
        userReference = FirebaseDatabase.getInstance().getReference().child("Users");


        // Sign In User to Firebase.
        if (mAuth.getCurrentUser() == null) {
            // User is not logged In
            //
            AppUtils.log(TAG_CHAT, "User with email : " + email + " is not logged in to Firebase");
            firebaseUtils.singInFirebase(email, passwordFromSharedPreferences);

            AppUtils.log(TAG_CHAT, "Checking " + email + " is present in the firebase database or Not ?");
            userReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String firebaseId = userOb.getFirebaseId();
                    AppUtils.log(TAG_CHAT, "Firebase Id : " + firebaseId);
                    if (firebaseId != null) {
                        if (dataSnapshot.hasChild(firebaseId)) {
                            // User is already present in the Firebase Database.
                            AppUtils.log(TAG_CHAT, email + " is present in the Fireabase database with user id : " + firebaseId);
                        } else {
                            // User is not Present in the Firebase. User may be on the FirebaseAuth.
                            AppUtils.log(TAG_CHAT, email + " is not Present in the Firebase Database. Adding New user ... ");
                            FcmUser fcmUser = new FcmUser(
                                    userOb.getDeviceToken(),
                                    userOb.getFirstName(),
                                    userOb.getLastName(),
                                    userOb.getImage(),
                                    userOb.getUserId(),
                                    userOb.getUserType(),
                                    false,
                                    String.valueOf(System.currentTimeMillis()),
                                    "2"
                            );
                            firebaseUtils.addNewUserToFirebaseAuth(userOb.getEmail(), passwordFromSharedPreferences, fcmUser);
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

    private void gettingIdOfInputFields() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        btnHireRecorder = findViewById(R.id.btn_hire_a_recorder);
        btnHireRecorder.setOnClickListener(this);


        RelativeLayout chatListLayout = findViewById(R.id.side_bar_chat_list_layout);
        RelativeLayout dataLibraryLayout = findViewById(R.id.side_bar_data_library_layout);
        RelativeLayout paymentLayout = findViewById(R.id.side_bar_payment_layout);
        RelativeLayout settingsLayout = findViewById(R.id.side_bar_settings_layout);
        RelativeLayout helpLayout = findViewById(R.id.side_bar_help_layout);

        ImageView editProfile = findViewById(R.id.side_bar_edit_profile);

        tvSearch = findViewById(R.id.tv_search_address);
        tvSearch.setOnClickListener(this);

        chatListLayout.setOnClickListener(this);
        dataLibraryLayout.setOnClickListener(this);
        paymentLayout.setOnClickListener(this);
        settingsLayout.setOnClickListener(this);
        helpLayout.setOnClickListener(this);

        editProfile.setOnClickListener(this);

        drawerLayout = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this,
                drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close);

        drawerLayout.addDrawerListener(actionBarDrawerToggle);

        toolbar = findViewById(R.id.tool_bar_include_explorer_home);
        TextView textView = toolbar.findViewById(R.id.txt_toolbar_explorer);
        textView.setText(Constants.FIND_YOUR_RECORDER);
        ImageView hamburger = toolbar.findViewById(R.id.iv_hamburger);
        hamburger.setOnClickListener(this);

        //Instantiate location Manger.
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        rootLayout = findViewById(R.id.rl_content_layout_home_recorder);

        //drawerLayout = findViewById(R.id.drawer_layout);
        frameLayout = findViewById(R.id.main_container_explorer_home);
    }

    private void displayMapAtPresentLocation() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, new LocationListener() {

                @Override
                public void onLocationChanged(Location location) {
                    mMap.clear();
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    LatLng latLng = new LatLng(latitude, longitude);
                    Geocoder geocoder = new Geocoder(getApplicationContext());
                    try {
                        List<Address> addressList = geocoder.getFromLocation(latitude, longitude, 1);
                        String locality = addressList.get(0).getLocality();
                        String countryName = addressList.get(0).getCountryName();
                        String address = locality + " , " + countryName;
                        mMap.addMarker(new MarkerOptions().position(latLng).title(address));
                        mMap.moveCamera(newLatLng(latLng));
                        //mMap.moveCamera(CameraUpdateFactory.zoomTo(5));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                @Override
                public void onProviderEnabled(String provider) {

                }

                @Override
                public void onProviderDisabled(String provider) {

                }
            });
        } else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    LatLng latLng = new LatLng(latitude, longitude);
                    //GeoCoder class will convert latitude and longitude into meaningful Address.
                    Geocoder geocoder = new Geocoder(getApplicationContext());
                    try {
                        List<Address> addressList = geocoder.getFromLocation(latitude, longitude, 1);
                        String locality = addressList.get(0).getLocality();
                        String countryName = addressList.get(0).getCountryName();
                        String address = locality + " , " + countryName;
                        mMap.addMarker(new MarkerOptions().position(latLng).title(address));
                        mMap.moveCamera(newLatLng(latLng));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                @Override
                public void onProviderEnabled(String provider) {

                }

                @Override
                public void onProviderDisabled(String provider) {

                }
            });
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        AppUtils.log("ACTIVITY", "onResume");
        boolean isLocationEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!isLocationEnabled) {
            final Snackbar snackbar = Snackbar.make(findViewById(R.id.rl_content_layout_home_recorder),
                    Constants.LOCATION_IS_NOT_ENABLED, Snackbar.LENGTH_INDEFINITE);
            snackbar.getView().setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary));
            snackbar.setActionTextColor(Color.BLACK);
            snackbar.show();
            snackbar.setAction(Constants.ENABLE, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    snackbar.dismiss();
                    Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(myIntent);
                }
            });

        }
    }


    /*@Override
    protected void onStart() {
        super.onStart();
        AppUtils.log("CHAT", "onStart");
        mAuth.addAuthStateListener(authStateListener);
    }*/

    private void setContentToSidebar() {
        //Getting Id of Header Layout - Explorer Home Page.
        RelativeLayout headerLayout = findViewById(R.id.include_header_layout_explorer_home_page);
        //Getting ID of Explorer Name -
        TextView explorerName = headerLayout.findViewById(R.id.explorer_name_side_bar);

        //Setting Name to Explorer Side bar.
        explorerName.setText("" + userOb.getFirstName() + " " + userOb.getLastName());

        //Getting Id of Footer Layout  - Explore Home Page.
        //footerLayout = findViewById(R.id.include_footer_layout_explorer_home_page);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        //Enable my Location Button
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        //mMap.setMyLocationEnabled(true);

        //Getting current location and zoom the Map accordingly.
        String latitude = userOb.getLatitude();
        String longitude = userOb.getLongitude();


        LatLng latLng = new LatLng(Double.valueOf(latitude), Double.valueOf(longitude));
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(latLng).title(""));

        //Add circle to Google Map.
        CircleOptions circleOptions = new CircleOptions()
                .center(latLng)
                //.radius(Double.parseDouble(userOb.getDesiredCommute()))
                .radius(Constants.CIRCLE_RADIUS)
                // Fill color of the circle
                // 0x represents, this is an hexadecimal code
                // 22 represents percentage of transparency. For 100% transparency, specify 00.
                // For 0% transparency ( ie, opaque ) , specify ff
                // The remaining 6 characters(37aea1) specify the fill color
                .fillColor(0x2237aea1)
                .strokeColor(Color.BLACK)
                .strokeWidth(Constants.STROKE_WIDTH);

        mMap.addCircle(circleOptions);
        //Zoom the camera to the Selected Location.
        CameraUpdate location = CameraUpdateFactory.newLatLngZoom(latLng, 15);
        mMap.animateCamera(location);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.tv_search_address:
                checkForLocationPermission();
                break;

            case R.id.side_bar_chat_list_layout:
                drawerLayout.closeDrawers();
                Intent chatListIntent = new Intent(this, ChatListActivity.class);
                startActivity(chatListIntent);
                overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
                break;

            case R.id.side_bar_data_library_layout:
                drawerLayout.closeDrawers();
                Intent dataLibraryIntent = new Intent(this, DataLibraryExplorerActivity.class);
                startActivity(dataLibraryIntent);
                overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
                break;


            case R.id.side_bar_payment_layout:
                drawerLayout.closeDrawers();
                startActivity(new Intent(this, PaymentActivityExplorer.class));
                overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
                break;

            case R.id.side_bar_settings_layout:
                drawerLayout.closeDrawers();
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingsIntent);
                overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
                break;

            case R.id.side_bar_help_layout:
                drawerLayout.closeDrawers();
                startActivity(new Intent(this, HelpActivity.class));
                overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
                break;

            case R.id.side_bar_edit_profile:
                drawerLayout.closeDrawers();
                Intent editProfileIntent = new Intent(this, ExplorerEditProfile.class);
                startActivity(editProfileIntent);
                overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
                break;

            case R.id.iv_hamburger:
                drawerLayout.openDrawer(GravityCompat.START);
                break;


            case R.id.btn_hire_a_recorder:
                if (!TextUtils.isEmpty(tvSearch.getText().toString())) {
                    searchRecorderApi(selectedLatitude, selectedLongitude);
                } else {
                    AppUtils.snackbar(this, "Please enter a valid address to send your recorder");
                }
                break;
        }
    }

    //Code for press back button once again to exit.
    boolean doubleBackToExitPressedOnce = false;

    @Override
    public void onBackPressed() {

        if (drawerLayout.isDrawerOpen(Gravity.START)) {
            drawerLayout.closeDrawer(Gravity.START);
        }
        if (!drawerLayout.isDrawerOpen(Gravity.START)) {
            if (doubleBackToExitPressedOnce) {
                super.onBackPressed();
                return;
            }

            this.doubleBackToExitPressedOnce = true;

            AppUtils.snackbar(this, findViewById(R.id.rl_content_layout_home_recorder),
                    Constants.PRESS_ONCE_AGAIN_TO_EXIT);
            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    doubleBackToExitPressedOnce = false;
                }
            }, 1000);
        }
    }

    private void displayPlacePicker() {
        progress = new Progress(this, R.style.CustomProgressDialogTheme);
        progress.show();
        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
        try {
            startActivityForResult(builder.build(this), PLACE_PICKER_REQUEST); // for activity
        } catch (GooglePlayServicesRepairableException e) {
            AppUtils.log("GooglePlayServicesRepairableException : " + e.getMessage());
        } catch (GooglePlayServicesNotAvailableException e) {
            AppUtils.log("GooglePlayServicesNotAvailableException : " + e.getMessage());
        }
    }

    @Override
    protected void onPostResume() {
        AppUtils.log(TAG_REST, "onPostResume");
        super.onPostResume();
        if (progress != null) {
            progress.dismiss();
        }
    }

    @Override
    protected void onStop() {
        AppUtils.log(TAG_REST, "onStop");
        super.onStop();
        if (progress != null) {
            progress.dismiss();
        }
    }

    //Search Recorder
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(this, data);
                String address = place.getAddress().toString();
                tvSearch.setText(address);
                LatLng searchedLatLng = place.getLatLng();

                this.selectedLatitude = searchedLatLng.latitude;
                this.selectedLongitude = searchedLatLng.longitude;

                //Add marker to the Selected Location.
                mMap.clear();
                mMap.addMarker(new MarkerOptions().position(searchedLatLng).title(address));

                //Add circle to Google Map.
                CircleOptions circleOptions = new CircleOptions()
                        .center(searchedLatLng)
                        .radius(Constants.CIRCLE_RADIUS)

                        // Fill color of the circle
                        // 0x represents, this is an hexadecimal code
                        // 55 represents percentage of transparency. For 100% transparency, specify 00.
                        // For 0% transparency ( ie, opaque ) , specify ff
                        // The remaining 6 characters(37aea1) specify the fill color

                        .fillColor(0x2237aea1)
                        .strokeColor(Color.BLACK)
                        .strokeWidth(Constants.STROKE_WIDTH);

                mMap.addCircle(circleOptions);

                //Zoom the camera to the Selected Location.
                CameraUpdate location = CameraUpdateFactory.newLatLngZoom(searchedLatLng, 15);
                mMap.animateCamera(location);
            }
        } else if (requestCode == 34) {
            if (resultCode == RESULT_OK) {
                String statusCode = data.getStringExtra("STATUS_CODE");
                Log.d(TAG_REST, "Status Code : " + statusCode);
                if (statusCode.equals("2")) {
                    displayLoaderScreen();
                }
            }
        }
    }

    private void searchRecorderApi(double latitude, double longitude) {
        AppUtils.log(TAG_REST, "Searching new Recorder ... ");
        AppUtils.log(TAG_REST, "Latitude searchRecorderApi : " + latitude);
        AppUtils.log(TAG_REST, "Longitude searchRecorderApi : " + longitude);
        AppUtils.log(TAG_REST, "Search Recorder");
        if (AppUtils.isNetworkAvailable(this)) {
            final Progress progressDialog = new Progress(this, R.style.CustomProgressDialogTheme);
            progressDialog.setCancelable(false);
            progressDialog.show();

            String accessToken = userOb.getAccessToken();
            Retrofit retrofit = ApiClient.getClient();
            ApiInterface apiInterface = retrofit.create(ApiInterface.class);
            Call<ServerResponse> call = apiInterface.searchRecorder(accessToken,
                    String.valueOf(latitude), String.valueOf(longitude));
            call.enqueue(new Callback<ServerResponse>() {
                @Override
                public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {
                    progressDialog.dismiss();
                    if (response.isSuccessful()) {

                        int responseCode = response.code();
                        if (responseCode == 204) {
                            AppUtils.showToast(ExplorerHomeActivity.this, "No Recorder is currently available.Please try again.");
                        } else if (responseCode == 200) {
                            AppUtils.log(TAG_REST, "Your Request Has been sent");
                            showRequestSendDialog(ExplorerHomeActivity.this, "Message", "Your request has been sent");
                        } else {
                            AppUtils.showToast(ExplorerHomeActivity.this, "Unknown Error");
                        }
                    } else {
                        try {
                            AppUtils.log("Explorer Home : Some Error Occurs" + response.errorBody().string());
                        } catch (IOException e) {
                        }
                    }
                }

                @Override
                public void onFailure(Call<ServerResponse> call, Throwable t) {
                    progressDialog.dismiss();
                    AppUtils.log("Explorer Home : " + t.getMessage());
                }
            });
        } else {
            AppUtils.showToast(this, Constants.NO_INTERNET_CONNECTION);
        }
    }

    //Run time permissions -
    private void checkForLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)) {
                //Show Information about why you need the permission
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(Constants.PERMISSION_LOCATION_TITLE);
                builder.setMessage(Constants.PERMISSION_LOCATION_DESCRIPTION);
                builder.setPositiveButton(Constants.PERMISSION_GRANT, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        ActivityCompat.requestPermissions(ExplorerHomeActivity.this,
                                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                                Constants.PERMISSION_CONSTANT_LOCATION);
                    }
                });
                builder.setNegativeButton(Constants.PERMISSION_CANCEL, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            } else if (permissionStatus.getBoolean(Manifest.permission.ACCESS_COARSE_LOCATION, false)) {
                //Previously Permission Request was cancelled with 'Dont Ask Again',
                // Redirect to Settings after showing Information about why you need the permission
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(Constants.PERMISSION_LOCATION_TITLE);
                builder.setMessage(Constants.PERMISSION_LOCATION_DESCRIPTION);
                builder.setPositiveButton(Constants.PERMISSION_GRANT, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        //sentToSettings = true;
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivityForResult(intent, REQUEST_PERMISSION_SETTING);
                        //AppUtils.showToast(ExplorerHomeActivity.this, "Go to Permissions to Grant Storage", Toast.LENGTH_LONG).show();
                    }
                });
                builder.setNegativeButton(Constants.PERMISSION_CANCEL, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            } else {
                //just request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        Constants.PERMISSION_CONSTANT_LOCATION);
            }
            SharedPreferences.Editor editor = permissionStatus.edit();
            editor.putBoolean(Manifest.permission.ACCESS_COARSE_LOCATION, true);
            //editor.commit();
            editor.apply();
        } else {
            //You already have the permission, just go ahead.
            displayPlacePicker();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // location permission
        if (requestCode == Constants.PERMISSION_CONSTANT_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //The External Storage Write Permission is granted to you... Continue your left job...
                displayPlacePicker();
            } else {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    //Show Information about why you need the permission
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle(Constants.PERMISSION_LOCATION_TITLE);
                    builder.setMessage(Constants.PERMISSION_LOCATION_DESCRIPTION);
                    builder.setPositiveButton(Constants.PERMISSION_GRANT, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                            ActivityCompat.requestPermissions(ExplorerHomeActivity.this,
                                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                                    Constants.PERMISSION_CONSTANT_LOCATION);
                        }
                    });
                    builder.setNegativeButton(Constants.PERMISSION_CANCEL, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    builder.show();
                } else {
                    AppUtils.showToast(this, Constants.PERMISSION_UNABLE_TO_GET_PERMISSION, Toast.LENGTH_LONG).show();
                }
            }
        }
    }


    private void showRequestSendDialog(Context context, String title, String message) {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(title);
            builder.setMessage(message);

            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    displayLoaderScreen();
                }
            });
            builder.show();
        } catch (Exception e) {
            AppUtils.log("Exception " + e.getMessage());
        }
    }
}