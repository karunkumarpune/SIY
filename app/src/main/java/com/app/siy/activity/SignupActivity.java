package com.app.siy.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.app.siy.R;
import com.app.siy.activity.explorer.TutorialActivityExplorer;
import com.app.siy.activity.recorder.TutorialActivityRecorder;
import com.app.siy.rest.ApiClient;
import com.app.siy.rest.ApiInterface;
import com.app.siy.rest.ServerResponse;
import com.app.siy.rest.User;
import com.app.siy.sharedpreferences.MyPreferences;
import com.app.siy.utils.AppUtils;
import com.app.siy.utils.Constants;
import com.app.siy.utils.GPSTracker;
import com.app.siy.utils.Progress;
import com.google.gson.Gson;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignupActivity extends Activity implements View.OnClickListener {

    private EditText etEmailExplorer;
    private EditText etPassword;
    private EditText etConfirmPassword;
    //private Button btnSignUp;
    //private TextView tvSignIn;
    private RelativeLayout relativeLayout;
    //private ImageView backButton;
    private double latitude;
    private double longitude;
    private MyPreferences myPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        //Instantiate MyPreferences
        myPreferences = new MyPreferences(this);

        AppUtils.log("REST", "onCreate-SignUp");
        relativeLayout = findViewById(R.id.relative_layout_sign_up);

        ImageView backButton = findViewById(R.id.iv_back_btn_sign_up);
        backButton.setOnClickListener(this);

        etEmailExplorer = findViewById(R.id.et_email_explorer);
        etEmailExplorer.setTypeface(Typeface.DEFAULT);

        etPassword = findViewById(R.id.et_password_explorer);
        etPassword.setTypeface(Typeface.DEFAULT);
        etPassword.setTransformationMethod(new PasswordTransformationMethod());

        etConfirmPassword = findViewById(R.id.et_confirm_password_explorer);
        etConfirmPassword.setTypeface(Typeface.DEFAULT);
        etConfirmPassword.setTransformationMethod(new PasswordTransformationMethod());

        Button btnSignUp = findViewById(R.id.btn_sign_up);
        btnSignUp.setOnClickListener(this);

        TextView tvSignIn = findViewById(R.id.tv_sign_in);
        tvSignIn.setOnClickListener(this);


        GPSTracker gps = new GPSTracker(this);

        // check if GPS enabled
        if (gps.canGetLocation()) {

            this.latitude = gps.getLatitude();
            this.longitude = gps.getLongitude();

        } else {
            // can't get location
            // GPS or Network is not enabled
            // Ask user to enable GPS/network in settings
            gps.showSettingsAlert();
        }

        //Check for Internet connection.
        if (!AppUtils.isNetworkAvailable(this)) {
            AppUtils.snackbar(this, relativeLayout, Constants.NO_INTERNET_CONNECTION);
        }
    }//onCreate

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.btn_sign_up:

                // Hide the Keyboard.
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(relativeLayout.getWindowToken(), 0);

                validateInputFields();
                break;

            case R.id.tv_sign_in:
                //Start SignIn Activity with previous Intent.
                Intent signInIntent = getIntent();
                signInIntent.setClass(this, SignInActivity.class);
                startActivity(signInIntent);
                //Slide from Right to Left
                overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);

                finish();
                break;

            case R.id.iv_back_btn_sign_up:
                onBackPressed();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //Slide from Left to Right
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
    }

    private void validateInputFields() {
        String email = etEmailExplorer.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString();
        AppUtils.log("Latitude : " + latitude + " Longitude : " + longitude);

        if (TextUtils.isEmpty(email)) {
            AppUtils.showToast(this, Constants.EMAIL_CAN_NOT_BE_EMPTY);
        } else if (TextUtils.isEmpty(password)) {
            AppUtils.showToast(this, Constants.PASSWORD_CAN_NOT_BE_EMPTY);
        } else if (password.length() < 8 || password.length() >= 15) {
            AppUtils.showToast(this, Constants.PASSWORD_MUST_BE_CHARACTERS_LONG);
        } else if (TextUtils.isEmpty(confirmPassword)) {
            AppUtils.showToast(this, Constants.CONFIRM_PASSWORD_CAN_NOT_BE_EMPTY);
        } else if (confirmPassword.length() < 8 || confirmPassword.length() >= 15) {
            AppUtils.showToast(this, Constants.CONFIRM_PASSWORD_MUST_BE_CHARACTERS_LONG);
        } else if (!AppUtils.isValidEmail(email)) {
            AppUtils.showToast(this, Constants.NOT_A_VALID_EMAIL);
        }

        if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)
                && !TextUtils.isEmpty(confirmPassword) && AppUtils.isValidEmail(email)
                && !(password.length() < 8 || password.length() >= 15)
                && !(confirmPassword.length() < 8 || confirmPassword.length() >= 15)) {
            //Fields are non empty -> proceed
            if (password.equals(confirmPassword)) {
                String deviceToken = myPreferences.getString(MyPreferences.DEVICE_TOKEN);
                String userType = getIntent().getStringExtra(Constants.USER_TYPE);

                AppUtils.log("Latitude " + latitude + "Longitude " + longitude);
                signup(email, password, String.valueOf(latitude), String.valueOf(longitude),
                        userType, Constants.DEVICE_TYPE_ANDROID, deviceToken);
            } else {
                AppUtils.showToast(this, Constants.PASSWORD_DO_NOT_MATCHES);
            }
        }
    }

    private void signup(final String email, final String password, String latitude, String longitude,
                        final String userType, String deviceType, String deviceToken) {

        AppUtils.log("Device Token Signup : " + deviceToken);
        if (AppUtils.isNetworkAvailable(this)) {
            final Progress progressDialog = new Progress(this, R.style.CustomProgressDialogTheme);
            progressDialog.setCancelable(false);
            progressDialog.show();

            ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
            Call<ServerResponse> call = apiInterface.signup(email, password, latitude, longitude,
                    userType, deviceType, deviceToken);
            call.enqueue(new Callback<ServerResponse>() {
                @Override
                public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {
                    if (response.isSuccessful()) {
                        AppUtils.log("REST", "Response is successful - Signup");
                        progressDialog.dismiss();
                        ServerResponse serverResponse = response.body();

                        String accessToken = serverResponse.user.getAccessToken();
                        String deviceToken = serverResponse.user.getDeviceToken();

                        AppUtils.log("REST", "Access Token Signup " + accessToken);
                        AppUtils.log("REST", "Device Token Signup " + deviceToken);


                        //Get user object -
                        User user = response.body().user;

                        //------ Save User to SharedPreferences.
                        //Get JSON String of User object from response object.
                        String userJson = new Gson().toJson(user);
                        AppUtils.log("REST", "JSON String - SignIn - " + userJson);
                        //Save user json to SharedPreferences
                        myPreferences.saveString(MyPreferences.USER_MODEL, userJson);

                        //-----  Save User End

                        myPreferences.saveString(MyPreferences.ACCESS_TOKEN, accessToken);
                        //myPreferences.saveString(MyPreferences.DEVICE_TOKEN, deviceToken);
                        myPreferences.saveString(MyPreferences.USER_ID, response.body().user.getUserId());

                        // Save Password to SharedPreferences.
                        // This will used to register user in Firebase at HomePage.
                        myPreferences.saveString(MyPreferences.PASSWORD, password);


                        if (userType.equals(Constants.USER_TYPE_EXPLORER)) {
                            AppUtils.log("REST", "Explorer Intro Slider");
                            Intent explorerIntroSlider = new Intent(SignupActivity.this,
                                    TutorialActivityExplorer.class);
                            startActivity(explorerIntroSlider);

                        } else if (userType.equals(Constants.USER_TYPE_RECORDER)) {
                            AppUtils.log("REST", "Recorder Intro Slider");
                            Intent recorderIntroSlider = new Intent(SignupActivity.this,
                                    TutorialActivityRecorder.class);
                            startActivity(recorderIntroSlider);
                        }
                    } else {
                        progressDialog.dismiss();
                        if ((response.code() == 400)) {
                            AppUtils.log("Email is already registered : " + response.code());
                            AppUtils.showToast(SignupActivity.this, "" + response.message());
                        }
                        try {
                            AppUtils.log("REST", "Error : " + response.errorBody().string());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onFailure(Call<ServerResponse> call, Throwable t) {
                    progressDialog.dismiss();
                    AppUtils.log("REST", "Failure");
                }
            });
        } else {
            AppUtils.snackbar(this, "No Internet Connection");
        }
    }
}