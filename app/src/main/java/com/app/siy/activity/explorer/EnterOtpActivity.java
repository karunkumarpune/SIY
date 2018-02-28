package com.app.siy.activity.explorer;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.app.siy.R;
import com.app.siy.rest.ApiClient;
import com.app.siy.rest.ApiInterface;
import com.app.siy.rest.ServerResponse;
import com.app.siy.utils.AppUtils;
import com.app.siy.utils.Constants;
import com.app.siy.utils.Progress;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EnterOtpActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText etEnterOTP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.explorer_activity_enter_otp);
        Toolbar toolbar = (Toolbar) findViewById(R.id.tool_bar_include_otp_activity);
        setSupportActionBar(toolbar);

        TextView toolbarText = toolbar.findViewById(R.id.tool_bar_text);
        toolbarText.setText(Constants.ENTER_OTP);

        ImageView backButton = toolbar.findViewById(R.id.back_image_tool_bar);
        backButton.setOnClickListener(this);

        etEnterOTP = (EditText) findViewById(R.id.et_enter_otp);
        Button btnSubmitOTP = (Button) findViewById(R.id.btn_otp_submit);
        TextView tvResendVerificationCode = (TextView) findViewById(R.id.txt_resend_verification_code);

        btnSubmitOTP.setOnClickListener(this);
        tvResendVerificationCode.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        int id = v.getId();
        switch (id) {
            case R.id.back_image_tool_bar:
                onBackPressed();
                break;

            case R.id.btn_otp_submit:
                validateInputFields();
                break;
            case R.id.txt_resend_verification_code:
                resendVerificationCode();
                break;
        }
    }

    private void resendVerificationCode() {
        AppUtils.showToastBlack(this, Constants.VERIFICATION_CODE_IS_SENT);

    }

    private void validateInputFields() {
        String otpFromUser = etEnterOTP.getText().toString().trim();

        String userId = getIntent().getStringExtra(Constants.INTENT_KEY_USER_ID);
        String otpFromServer = getIntent().getStringExtra(Constants.INTENT_KEY_OTP);

        if (!TextUtils.isEmpty(otpFromUser)) {
            etEnterOTP.setText("");
            verifyOtp(userId, otpFromServer, otpFromUser);

        } else {
            AppUtils.showToastBlack(this, Constants.PLEASE_ENTER_OTP);
        }
    }

    private void verifyOtp(String userId, String otpFromServer, String otpFromUser) {
        if (AppUtils.isNetworkAvailable(this)) {
            final Progress progressDialog = new Progress(this, R.style.CustomProgressDialogTheme);
            progressDialog.setCancelable(false);
            progressDialog.show();

            ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
            Call<ServerResponse> call = apiInterface.verifyOtp(otpFromServer, userId);

            if (otpFromServer.equals(otpFromUser)) {
                call.enqueue(new Callback<ServerResponse>() {
                    @Override
                    public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {
                        progressDialog.dismiss();
                        if (response.isSuccessful()) {
                            AppUtils.log("Enter OTP Activity - OTP verified");
                            Intent intent = getIntent();
                            intent.setClass(EnterOtpActivity.this, ResetPasswordActivity.class);
                            startActivity(intent);
                        } else {
                            AppUtils.log("Enter OTP Activity - OTP not verified");
                        }
                    }

                    @Override
                    public void onFailure(Call<ServerResponse> call, Throwable t) {
                        progressDialog.dismiss();
                        AppUtils.log("Enter Otp Activity - onFailure");
                    }
                });
            } else {
                progressDialog.dismiss();
                AppUtils.log("Invalid OTP");
                AppUtils.showToast(this, Constants.INVALID_OTP);
            }
        } else {
            AppUtils.snackbar(this, Constants.NO_INTERNET_CONNECTION);
        }
    }

    public void onBackPressed() {
        super.onBackPressed();
    }
}