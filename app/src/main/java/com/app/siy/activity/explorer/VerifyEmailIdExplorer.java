package com.app.siy.activity.explorer;

import android.app.Dialog;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.app.siy.R;
import com.app.siy.activity.recorder.VerifyEmailIDRecorder;
import com.app.siy.rest.ApiClient;
import com.app.siy.rest.ApiInterface;
import com.app.siy.rest.ServerResponse;
import com.app.siy.rest.User;
import com.app.siy.sharedpreferences.MyPreferences;
import com.app.siy.utils.AppUtils;
import com.app.siy.utils.Progress;
import com.google.gson.Gson;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VerifyEmailIdExplorer extends AppCompatActivity implements View.OnClickListener {


    private TextView tvEmailId;
    private EditText etVerificationCode;
    private Button btnSubmitExplorer;
    private TextView tvChangeEmailId;
    private MyPreferences myPreferences;
    private RelativeLayout rootLayout;
    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.explorer_activity_verify_email_id);


        myPreferences = new MyPreferences(this);

        //Getting user id from SharedPreferences.
        String jsonStringFromPref = myPreferences.getString(MyPreferences.USER_MODEL);
        user = new Gson().fromJson(jsonStringFromPref, User.class);

        tvEmailId = (TextView) findViewById(R.id.tv_email_id_explorer);
        tvEmailId.setText(user.getEmail());

        etVerificationCode = (EditText) findViewById(R.id.et_verification_code_explorer);
        btnSubmitExplorer = (Button) findViewById(R.id.btn_submit_explorer);
        tvChangeEmailId = (TextView) findViewById(R.id.tv_change_email_id_explorer);

        rootLayout = (RelativeLayout) findViewById(R.id.layout_verify_email_explorer);

        Toolbar toolbar = (Toolbar) findViewById(R.id.include_toolbar_verify_email_id_explorer);
        setSupportActionBar(toolbar);

        TextView toolbarText = toolbar.findViewById(R.id.tool_bar_text);
        toolbarText.setText("Verify Email ID");

        ImageView backImage = toolbar.findViewById(R.id.back_image_tool_bar);
        backImage.setOnClickListener(this);


        btnSubmitExplorer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validateInputFields();
            }
        });


        tvChangeEmailId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeEmailIdDialog();
            }
        });
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    private void validateInputFields() {
        String verificationCode = etVerificationCode.getText().toString().trim();
        if (!TextUtils.isEmpty(verificationCode)) {
            String userId = user.getUserId();
            verifyOtp(verificationCode, userId);
        } else {
            AppUtils.snackbar(this, "Please Enter OTP");
        }
    }

    private void verifyOtp(String otp, String userId) {
        AppUtils.hideKeyboard(this);

        final Progress progress = new Progress(this, R.style.CustomProgressDialogTheme);
        progress.setCancelable(false);
        progress.show();

        ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
        Call<ServerResponse> responseCall = apiInterface.verifyOtp(otp, userId);
        responseCall.enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {
                progress.dismiss();
                if (response.isSuccessful()) {
                    AppUtils.snackbar(VerifyEmailIdExplorer.this, "Email Is Verified");
                    progress.dismiss();
                    AppUtils.log("Email ID is verified");

                    // Hide the background
                    rootLayout.setVisibility(View.GONE);

                    showCongratulationsDialogAndMoveToExplorerHome();

                } else {
                    AppUtils.log("Not a valid OTP");
                    AppUtils.snackbar(VerifyEmailIdExplorer.this, "Not a valid OTP");
                }
            }

            @Override
            public void onFailure(Call<ServerResponse> call, Throwable t) {
                progress.dismiss();
                AppUtils.log("onFailure : VerifyEmailExplorer : " + t.getMessage());
            }
        });
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

                String newEmailId = etNewEmailId.getText().toString().trim();
                if (AppUtils.isValidEmail(newEmailId)) {

                    //
                    tvEmailId.setText(newEmailId);
                    String userId = user.getUserId();

                    // Hit API
                    changeEmailIdToAPI(newEmailId, userId);
                } else {
                    AppUtils.showToast(VerifyEmailIdExplorer.this, "Not a valid email Id");
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

    private void changeEmailIdToAPI(String newEmailId, String userId) {
        final Progress progress = new Progress(this, R.style.CustomProgressDialogTheme);
        progress.setCancelable(false);
        progress.show();

        ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
        Call<ServerResponse> serverResponseCall = apiInterface.changeEmailId(newEmailId, userId);
        serverResponseCall.enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {
                progress.dismiss();
                if (response.isSuccessful()) {
                    AppUtils.snackbar(VerifyEmailIdExplorer.this, "Your Email ID is changed. Please Verify It.");
                    AppUtils.log("Email ID is Changed - Explorer");
                } else {
                    AppUtils.log("Email ID is not Changed - Explorer");
                }
            }

            @Override
            public void onFailure(Call<ServerResponse> call, Throwable t) {
                progress.dismiss();
                AppUtils.log("onFailure : VerifyEmailIDExplorer");
            }
        });
    }


    private void showCongratulationsDialogAndMoveToExplorerHome() {
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View progressView = layoutInflater.inflate(R.layout.congratulations_dialog_layout_explorer,
                (ViewGroup) findViewById(R.id.congratulation_layout),
                false);

        final Dialog congratulationsDialog;
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            congratulationsDialog = new Dialog(VerifyEmailIdExplorer.this, R.style.CustomDialogTheme);
        } else {
            congratulationsDialog = new Dialog(VerifyEmailIdExplorer.this);
        }
        congratulationsDialog.setContentView(progressView);
        congratulationsDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        congratulationsDialog.setCancelable(false);
        congratulationsDialog.setCanceledOnTouchOutside(false);

        congratulationsDialog.show();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(2000);
                    startActivity(new Intent(VerifyEmailIdExplorer.this,
                            ExplorerHomeActivity.class));
                    finishAffinity();
                    congratulationsDialog.dismiss();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back_image_tool_bar:
                onBackPressed();
                break;
        }
    }
}