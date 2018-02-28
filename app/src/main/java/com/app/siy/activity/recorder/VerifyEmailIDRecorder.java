package com.app.siy.activity.recorder;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.app.siy.R;
import com.app.siy.rest.ApiClient;
import com.app.siy.rest.ApiInterface;
import com.app.siy.rest.ServerResponse;
import com.app.siy.rest.User;
import com.app.siy.sharedpreferences.MyPreferences;
import com.app.siy.utils.AppUtils;
import com.app.siy.utils.Constants;
import com.app.siy.utils.Progress;
import com.google.gson.Gson;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;


public class VerifyEmailIDRecorder extends AppCompatActivity implements View.OnClickListener {
    private Toolbar toolbar;
    private ImageView backBtnImage;
    private TextView tvChangeEmailId;
    private Button btnSendEmail;
    private RelativeLayout relativeLayout;
    private TextView tvVerifyEmaildId;
    private EditText etVerificationCode;

    private MyPreferences myPreferences;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recorder_activity_verify_email_id);

        myPreferences = new MyPreferences(this);

        toolbar = (Toolbar) findViewById(R.id.include_toolbar_verify_email_id_recorder);
        tvChangeEmailId = (TextView) findViewById(R.id.tv_change_email_id);
        btnSendEmail = (Button) findViewById(R.id.btn_send_email);
        relativeLayout = (RelativeLayout) findViewById(R.id.layout_verify_email_recorder);

        TextView toolbarText = toolbar.findViewById(R.id.tool_bar_text);
        toolbarText.setText("Verify Email ID");
        backBtnImage = (ImageView) findViewById(R.id.back_image_tool_bar);

        etVerificationCode = (EditText) findViewById(R.id.et_verification_code);

        tvVerifyEmaildId = (TextView) findViewById(R.id.tv_email_id);
        //Get Email from Intent.
        String email = getIntent().getStringExtra("EMAIL");
        tvVerifyEmaildId.setText(email);


        backBtnImage.setOnClickListener(this);
        tvChangeEmailId.setOnClickListener(this);
        btnSendEmail.setOnClickListener(this);

        //Getting user id from SharedPreferences.
        String jsonStringFromPref = myPreferences.getString(MyPreferences.USER_MODEL);
        User user = new Gson().fromJson(jsonStringFromPref, User.class);
        this.userId = user.getUserId();


    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back_image_tool_bar:
                onBackPressed();
                break;

            case R.id.tv_change_email_id:
                //AppUtils.showToastBlack(this, "Change EmailID");
                changeEmailIdDialog();
                break;

            case R.id.btn_send_email:
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(relativeLayout.getWindowToken(), 0);

                validateInputFields();
                break;
        }
    }

    private void validateInputFields() {
        String otpFromUser = etVerificationCode.getText().toString().trim();
        //String otpFromServer = this.otpFromServer;

        //AppUtils.log("REST", "OTP " + otpFromServer);
        //AppUtils.log("REST", "User Id  --" + getIntent().getStringExtra("USERID"));


        if (TextUtils.isEmpty(otpFromUser)) {
            AppUtils.showToast(this, "Please enter OTP");
        }

        if (!TextUtils.isEmpty(otpFromUser)) {
            validateOtp(otpFromUser, userId);
        }
    }

    private void validateOtp(String otpFromUser, String userId) {
        if (AppUtils.isNetworkAvailable(this)) {
            final Progress progressDialog = new Progress(this, R.style.CustomProgressDialogTheme);
            progressDialog.setCancelable(false);
            progressDialog.show();


            Retrofit retrofit = ApiClient.getClient();
            ApiInterface apiInterface = retrofit.create(ApiInterface.class);
            Call<ServerResponse> call = apiInterface.verifyOtp(otpFromUser, userId);

            call.enqueue(new Callback<ServerResponse>() {
                @Override
                public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {

                    //Hide the Soft keyboard.
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(relativeLayout.getWindowToken(), 0);


                    if (response.isSuccessful()) {
                        progressDialog.dismiss();
                        AppUtils.showToast(VerifyEmailIDRecorder.this, "Email ID is verified.");
                        AppUtils.log("REST", "Email is verified");
                        relativeLayout.setVisibility(View.GONE);

                        showCongratulationsScreenAndMoveToRecorderHome();

                    } else {
                        progressDialog.dismiss();
                        AppUtils.showToast(VerifyEmailIDRecorder.this, "OTP is not valid.");
                        AppUtils.log("REST", "Response is unsuccessful");
                    }
                }

                @Override
                public void onFailure(Call<ServerResponse> call, Throwable t) {
                    progressDialog.dismiss();
                    AppUtils.log("REST", "onFailure");
                }
            });
        } else {
            AppUtils.snackbar(this, Constants.NO_INTERNET_CONNECTION);
        }
    }

    private void changeEmailIdDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.recorder_layout_enter_new_emailid, null);
        builder.setView(view);

        final AlertDialog alertDialogChangeEmail = builder.create();
        alertDialogChangeEmail.show();


        TextView btnCancel = (TextView) view.findViewById(R.id.new_emaiid_tv_cancel);
        TextView btnYes = (TextView) view.findViewById(R.id.new_email_tv_yes);
        final EditText etNewEmailId = (EditText) view.findViewById(R.id.et_change_email_id);


        btnYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialogChangeEmail.dismiss();

                String newEmailId = etNewEmailId.getText().toString();
                if (AppUtils.isValidEmail(newEmailId)) {

                    tvVerifyEmaildId.setText(newEmailId);
                    changeEmailId(newEmailId, VerifyEmailIDRecorder.this.userId);
                } else {
                    AppUtils.showToast(VerifyEmailIDRecorder.this, "Not a valid email Id");
                }
            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialogChangeEmail.dismiss();
            }
        });
    }

    private void changeEmailId(final String newEmail, final String userId) {

        AppUtils.log("REST", "New Email : " + newEmail);
        AppUtils.log("REST", "UserId :  : " + userId);

        final Progress progressDialog = new Progress(this, R.style.CustomProgressDialogTheme);
        progressDialog.setCancelable(false);
        progressDialog.show();

        Retrofit retrofit = ApiClient.getClient();
        ApiInterface apiInterface = retrofit.create(ApiInterface.class);
        Call<ServerResponse> call = apiInterface.changeEmailId(newEmail, userId);
        call.enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {
                progressDialog.dismiss();
                if (response.isSuccessful()) {
                    AppUtils.log("REST", "Success " + response.body().getMessage());
                    AppUtils.showToast(VerifyEmailIDRecorder.this, "Email Id is changed.");
                } else {
                    AppUtils.log("REST", "Un Success " + response.body().getMessage());
                    AppUtils.showToast(VerifyEmailIDRecorder.this, "Some Error Occurs");
                }
            }

            @Override
            public void onFailure(Call<ServerResponse> call, Throwable t) {
                progressDialog.dismiss();
                AppUtils.log("REST", "onFailure");
            }
        });
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private void showCongratulationsScreenAndMoveToRecorderHome() {
        final Dialog congratulationsDialog;
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View progressView = layoutInflater.inflate(R.layout.congratulations_dialog_layout_recorder,
                (ViewGroup) findViewById(R.id.congratulation_layout), false);

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            congratulationsDialog = new Dialog(this, R.style.CustomDialogTheme);
        } else {
            congratulationsDialog = new Dialog(this);
        }
        congratulationsDialog.setContentView(progressView);
        //progress = new Progress(this, R.style.CustomDialogTheme);
        congratulationsDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        congratulationsDialog.setCancelable(false);
        congratulationsDialog.setCanceledOnTouchOutside(false);


        congratulationsDialog.show();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(2000);
                    startActivity(new Intent(VerifyEmailIDRecorder.this, RecorderHomeActivity.class));
                    finishAffinity();
                    congratulationsDialog.dismiss();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}