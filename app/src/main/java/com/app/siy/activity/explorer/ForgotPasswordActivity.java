package com.app.siy.activity.explorer;

import android.app.ProgressDialog;
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
import retrofit2.Retrofit;

public class ForgotPasswordActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText etEmailId;
    private Button btnSendEmail;
    private Toolbar toolbar;
    private ImageView backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.explorer_activity_forgot_password);

        toolbar = (Toolbar) findViewById(R.id.include_toolbar);
        setSupportActionBar(toolbar);
        TextView toolbarText = toolbar.findViewById(R.id.tool_bar_text);
        toolbarText.setText(getResources().getString(R.string.txt_forgot_password));

        backButton = toolbar.findViewById(R.id.back_image_tool_bar);
        backButton.setOnClickListener(this);

        etEmailId = (EditText) findViewById(R.id.et_email_id_forgot_password);
        btnSendEmail = (Button) findViewById(R.id.btn_send_email);
        btnSendEmail.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back_image_tool_bar:
                onBackPressed();
                break;

            case R.id.btn_send_email:
                this.validateInputFields();
                break;
        }
    }


    private void validateInputFields() {
        String email = etEmailId.getText().toString().trim();
        if (TextUtils.isEmpty(email)) {
            AppUtils.showToastBlack(this, Constants.EMAIL_ID_IS_MANDATORY);
        } else if (!AppUtils.isValidEmail(email)) {
            AppUtils.showToast(this, Constants.NOT_A_VALID_EMAIL);
        }

        if (!TextUtils.isEmpty(email) && AppUtils.isValidEmail(email)) {
            forgetPassword(email);
        }
    }

    private void forgetPassword(final String email) {
        if (AppUtils.isNetworkAvailable(this)) {
            final Progress progressDialog = new Progress(this, R.style.CustomProgressDialogTheme);
            progressDialog.setCancelable(false);
            progressDialog.show();

            Retrofit retrofit = ApiClient.getClient();
            ApiInterface apiInterface = retrofit.create(ApiInterface.class);
            Call<ServerResponse> call = apiInterface.forgetPassword(Constants.USER_TYPE_EXPLORER, email);
            call.enqueue(new Callback<ServerResponse>() {
                @Override
                public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {
                    progressDialog.dismiss();
                    if (response.isSuccessful()) {
                        etEmailId.setText("");
                        AppUtils.showToast(ForgotPasswordActivity.this, "OTP is sent to " + email);
                        AppUtils.log("REST", "Forgot Password : enter email ID : " + response.body().user.getEmail());
                        String userId = response.body().user.getUserId();
                        String otp = response.body().user.getOtp();
                        Intent intent = new Intent(ForgotPasswordActivity.this, EnterOtpActivity.class);
                        intent.putExtra("USER_ID", userId);
                        intent.putExtra("OTP", otp);
                        startActivity(intent);
                    } else {
                        AppUtils.showToast(ForgotPasswordActivity.this, "Email ID is not registered.");
                        AppUtils.log("REST", "Forgot Password - Email id is not registered");
                    }
                }

                @Override
                public void onFailure(Call<ServerResponse> call, Throwable t) {
                    progressDialog.dismiss();
                    AppUtils.log("REST", "Un successful - Forgot Password ");
                }
            });
        } else {
            AppUtils.snackbar(this, "No Internet Connection");
        }
    }
}