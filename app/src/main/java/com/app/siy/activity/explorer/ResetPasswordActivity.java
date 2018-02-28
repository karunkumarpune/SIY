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
import com.app.siy.activity.SignInActivity;
import com.app.siy.rest.ApiClient;
import com.app.siy.rest.ApiInterface;
import com.app.siy.rest.ServerResponse;
import com.app.siy.utils.AppUtils;
import com.app.siy.utils.Progress;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ResetPasswordActivity extends AppCompatActivity implements View.OnClickListener {

    private EditText etNewPassword;
    private EditText etConfirmNewPassword;
    private Button btnSubmitResetPassword;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.explorer_activity_reset_password);
        toolbar = (Toolbar) findViewById(R.id.tool_bar_include_reset_password_activity);
        TextView toolbarText = (TextView) toolbar.findViewById(R.id.tool_bar_text);
        toolbarText.setText(getResources().getString(R.string.txt_reset_password));

        ImageView backImage = toolbar.findViewById(R.id.back_image_tool_bar);
        backImage.setOnClickListener(this);

        etNewPassword = (EditText) findViewById(R.id.et_new_password);
        etConfirmNewPassword = (EditText) findViewById(R.id.et_new_confirm_password);
        btnSubmitResetPassword = (Button) findViewById(R.id.btn_submit_reset_password);

        btnSubmitResetPassword.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back_image_tool_bar:
                onBackPressed();
                break;

            case R.id.btn_submit_reset_password:
                validateInputFields();
                break;
        }
    }

    private void validateInputFields() {
        String newPassword = etNewPassword.getText().toString().trim();
        String newConfirmPassword = etConfirmNewPassword.getText().toString().trim();

        if (TextUtils.isEmpty(newPassword)) {
            AppUtils.showToastBlack(this, "Password can't be left empty");
        } else if (TextUtils.isEmpty(newConfirmPassword)) {
            AppUtils.showToastBlack(this, "Confirm password can't be left empty");
        } else if (!newPassword.equals(newConfirmPassword)) {
            AppUtils.showToastBlack(this, "Password don't matched");
        }

        if (!TextUtils.isEmpty(newPassword) && !TextUtils.isEmpty(newConfirmPassword) && newPassword.equals(newConfirmPassword)) {
            /// /Fields are non-empty and both password are matched

            String userId = getIntent().getStringExtra("USER_ID");
            resetPassword(userId, newPassword);
        }
    }

    private void resetPassword(String userId, String newPassword) {
        if (AppUtils.isNetworkAvailable(this)) {
            final Progress progressDialog = new Progress(this, R.style.CustomProgressDialogTheme);
            progressDialog.setCancelable(false);
            progressDialog.show();

            ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
            Call<ServerResponse> call = apiInterface.resetPassword(userId, newPassword);
            call.enqueue(new Callback<ServerResponse>() {
                @Override
                public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {
                    progressDialog.dismiss();
                    if (response.isSuccessful()) {
                        AppUtils.log("REST", "Success-ResetPassword");
                        AppUtils.showToast(ResetPasswordActivity.this, "Password changed successfully.");
                        //Response is successful,  -> Move to SignInActivity.
                        Intent signInIntent = new Intent(ResetPasswordActivity.this, SignInActivity.class);
                        signInIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(signInIntent);
                        finish();

                    } else {
                        AppUtils.log("REST", "UnSuccess-ResetPassword");
                    }
                }

                @Override
                public void onFailure(Call<ServerResponse> call, Throwable t) {
                    progressDialog.dismiss();
                    AppUtils.log("REST", "onFailure-ResetPassword");
                }
            });
        } else {
            AppUtils.snackbar(this, "No Internet Connection");
        }
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}