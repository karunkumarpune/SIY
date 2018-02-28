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
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.app.siy.R;
import com.app.siy.activity.explorer.CompleteProfileActivityExplorer;
import com.app.siy.activity.explorer.ExplorerHomeActivity;
import com.app.siy.activity.explorer.ForgotPasswordActivity;
import com.app.siy.activity.explorer.VerifyEmailIdExplorer;
import com.app.siy.activity.recorder.CompleteProfileActivityRecorder;
import com.app.siy.activity.recorder.RecorderHomeActivity;
import com.app.siy.activity.recorder.VerifyEmailIDRecorder;
import com.app.siy.rest.ApiClient;
import com.app.siy.rest.ApiInterface;
import com.app.siy.rest.ServerResponse;
import com.app.siy.rest.User;
import com.app.siy.utils.AppUtils;
import com.app.siy.sharedpreferences.MyPreferences;
import com.app.siy.utils.Constants;
import com.app.siy.utils.Progress;
import com.google.gson.Gson;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignInActivity extends Activity implements View.OnClickListener {
    private Button btnSignUp;
    private Button btnSignIn;
    private EditText etEmailLogin;
    private EditText etPasswordLogin;
    private TextView tvForgotPassword;
    private RelativeLayout rootLayout;
    private MyPreferences myPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        //Instantiate MyPreference
        myPreferences = new MyPreferences(this);
        rootLayout = findViewById(R.id.rl_sign_in);

        //Getting ID of View Components
        btnSignIn = findViewById(R.id.btn_sign_in_signin_activity);
        btnSignUp = findViewById(R.id.btn_sign_up_signin_activity);
        tvForgotPassword = findViewById(R.id.text_view_forgot_password);
        etEmailLogin = findViewById(R.id.et_email_login);
        etEmailLogin.setTypeface(Typeface.DEFAULT);

        etPasswordLogin = findViewById(R.id.et_password_login);
        etPasswordLogin.setTypeface(Typeface.DEFAULT);
        etPasswordLogin.setTransformationMethod(new PasswordTransformationMethod());


        btnSignUp.setOnClickListener(this);
        btnSignIn.setOnClickListener(this);
        tvForgotPassword.setOnClickListener(this);

        if (!AppUtils.isNetworkAvailable(this)) {
            AppUtils.snackbar(this, findViewById(R.id.rl_sign_in), Constants.NO_INTERNET_CONNECTION);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_sign_in_signin_activity:
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(rootLayout.getWindowToken(), 0);

                validateInputFields();
                break;
            case R.id.btn_sign_up_signin_activity:
                Intent signUpIntent = new Intent(SignInActivity.this, SignupActivity.class);
                signUpIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(signUpIntent);
                //Slide from Left to Right
                overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
                finish();
                break;

            case R.id.text_view_forgot_password:
                startActivity(new Intent(SignInActivity.this, ForgotPasswordActivity.class));
                break;
        }
    }

    private void validateInputFields() {
        String email = etEmailLogin.getText().toString().trim();
        String password = etPasswordLogin.getText().toString().trim();
        if (TextUtils.isEmpty(email)) {
            AppUtils.showToast(this, Constants.EMAIL_CAN_NOT_BE_EMPTY);
        } else if (TextUtils.isEmpty(password)) {
            AppUtils.showToast(this, Constants.PASSWORD_CAN_NOT_BE_EMPTY);
        } else if (password.length() < 8 || password.length() >= 15) {
            AppUtils.showToast(this, Constants.PASSWORD_MUST_BE_CHARACTERS_LONG);
        } else if (!AppUtils.isValidEmail(email)) {
            AppUtils.showToast(this, Constants.NOT_A_VALID_EMAIL);
        }

        String userType = getIntent().getStringExtra(Constants.USER_TYPE);
        double latitude = 21.83;
        double longitude = 45.90;


        String deviceToken = myPreferences.getString(MyPreferences.DEVICE_TOKEN);

        if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)
                && AppUtils.isValidEmail(email)
                && !(password.length() < 8 || password.length() >= 15)) {

            signIn(email, password, String.valueOf(latitude), String.valueOf(longitude),
                    userType, Constants.DEVICE_TYPE_ANDROID, deviceToken);
        }
    }

    private void signIn(String email, final String password, String latitude, String longitude,
                        String userType, String deviceType, String deviceToken) {
        AppUtils.log("User Type -- " + userType);
        if (AppUtils.isNetworkAvailable(this)) {
            final Progress progress = new Progress(this, R.style.CustomProgressDialogTheme);
            progress.setCancelable(false);
            progress.show();

            ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);

            Call<ServerResponse> call = apiInterface.signIn(email, password, latitude, longitude,
                    userType, deviceType, deviceToken);
            call.enqueue(new Callback<ServerResponse>() {
                @Override
                public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {
                    progress.dismiss();
                    AppUtils.log("Response Code Sign In " + response.code());
                    if (response.isSuccessful()) {
                        etEmailLogin.setText("");
                        etPasswordLogin.setText("");

                        //Get user object -
                        User user = response.body().user;

                        //Get JSON String of User object from response object.
                        String userJson = new Gson().toJson(user);
                        AppUtils.log("JSON String - SignIn - " + userJson);
                        //Save user json to SharedPreferences
                        myPreferences.saveString(MyPreferences.USER_MODEL, userJson);


                        String userType = response.body().user.getUserType();
                        AppUtils.log("User type sign in " + userType);

                        String accessToken = response.body().user.getAccessToken();
                        String deviceToken = response.body().user.getDeviceToken();

                        AppUtils.log("Access Token Sign in " + accessToken);
                        AppUtils.log("Device Token Sign in " + deviceToken);

                        myPreferences.saveString(MyPreferences.ACCESS_TOKEN, accessToken);
                        myPreferences.saveString(MyPreferences.USER_ID, response.body().user.getUserId());

                        // Save Password to SharedPreferences.
                        // This will used to register user in Firebase at HomePage.
                        myPreferences.saveString(MyPreferences.PASSWORD, password);

                        //Check if the profile is completed or not ?
                        String profile = response.body().user.getProfile();
                        AppUtils.log("Profile completed status : " + profile);
                        if (profile.equals(Constants.PROFILE_COMPLETED)) {
                            //Profile is Completed. -> Check the user type.
                            //If user type is Explorer then show Home Page.
                            //If user type is Recorder then move to OTP verification Page.

                            //Check for User type.

                            if (userType.equals(Constants.USER_TYPE_EXPLORER)) {
                                //User type explorer -> Now check for Email Verification.
                                //
                                String emailVerificationStatus = response.body().user.getIsVerify();
                                if (emailVerificationStatus.equals(Constants.EMAIL_VERIFIED)) {
                                    // Email Is Already Verified -> Move the user to Explorer Home Page.
                                    startActivity(new Intent(SignInActivity.this, ExplorerHomeActivity.class));
                                    finishAffinity();
                                } else {
                                    // Email is not verified. -> Move to the Email Verification Page.
                                    startActivity(new Intent(SignInActivity.this, VerifyEmailIdExplorer.class));
                                }


                            } else if (userType.equals(Constants.USER_TYPE_RECORDER)) {
                                //User type is recorder -> Now check for Email Verification.

                                String emailVerifyStatus = response.body().user.getIsVerify();
                                AppUtils.log("Email Verification Status : " + emailVerifyStatus);
                                if (emailVerifyStatus.equals(Constants.EMAIL_VERIFIED)) {
                                    //Email is verified -> Show Home Page. of Recorder.
                                    startActivity(new Intent(SignInActivity.this, RecorderHomeActivity.class));
                                    finishAffinity();
                                } else {
                                    //Email is not Verified. -> Show OTP Verification Page.
                                    startActivity(new Intent(SignInActivity.this, VerifyEmailIDRecorder.class));
                                }
                            }

                        } else {
                            //Profile is not completed.
                            // Now check the user type.
                            // If user type is Explorer then go to CompleteProfileExplorer
                            // If user type is Recorder then go to CompleteProfileRecorder.
                            if (userType.equals(Constants.USER_TYPE_EXPLORER)) {
                                Intent intent = new Intent(SignInActivity.this, CompleteProfileActivityExplorer.class);
                                startActivity(intent);
                            } else if (userType.equals(Constants.USER_TYPE_RECORDER)) {
                                Intent recorderCompleteProfileIntent = new Intent(SignInActivity.this, CompleteProfileActivityRecorder.class);
                                startActivity(recorderCompleteProfileIntent);
                            }
                        }
                    } else {
                        progress.dismiss();
                        try {
                            String errorStringJson = response.errorBody().string();
                            AppUtils.log("Login Fails - SignInActivity" + errorStringJson);
                            // Parse this Json

                            JSONObject jsonObject = new JSONObject(errorStringJson);
                            String errorMessage = jsonObject.getString("message");
                            AppUtils.log("REST", "Error String : " + errorMessage);
                            AppUtils.showToast(SignInActivity.this, "" + errorMessage);
                        } catch (Exception e) {
                            AppUtils.showToast(SignInActivity.this, "Unknown Error. Try later.");
                            AppUtils.log("REST", "Exception -- : " + e.getMessage());
                        }
                    }
                }

                @Override
                public void onFailure(Call<ServerResponse> call, Throwable t) {
                    progress.dismiss();
                    AppUtils.log("onFailure - SignInActivity : " + t.getMessage());
                }
            });
        } else {
            AppUtils.snackbar(this, Constants.NO_INTERNET_CONNECTION);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //Slide from Left to Right
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
    }
}