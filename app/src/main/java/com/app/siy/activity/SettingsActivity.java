package com.app.siy.activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.app.siy.R;
import com.app.siy.rest.ApiClient;
import com.app.siy.rest.ApiInterface;
import com.app.siy.rest.ServerResponse;
import com.app.siy.sharedpreferences.MyPreferences;
import com.app.siy.utils.AppUtils;
import com.app.siy.utils.Progress;
import com.google.firebase.auth.FirebaseAuth;

import okhttp3.internal.http.RetryAndFollowUpInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener {

    private Toolbar toolbar;
    private ImageView backImage;
    private RelativeLayout layoutAboutUs;
    private RelativeLayout layoutPrivacyPolicy;
    private RelativeLayout layoutFAQ;
    private RelativeLayout layoutLogout;

    private MyPreferences myPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        //Instantiate MyPreferences
        myPreferences = new MyPreferences(this);

        toolbar = (Toolbar) findViewById(R.id.include_toolbar_settings);
        TextView textView = toolbar.findViewById(R.id.tool_bar_text);
        textView.setText("Settings");
        backImage = (ImageView) toolbar.findViewById(R.id.back_image_tool_bar);
        backImage.setOnClickListener(this);

        layoutAboutUs = (RelativeLayout) findViewById(R.id.layout_about_us);
        layoutPrivacyPolicy = (RelativeLayout) findViewById(R.id.layout_privacy_policy);
        layoutFAQ = (RelativeLayout) findViewById(R.id.layout_faq);
        layoutLogout = (RelativeLayout) findViewById(R.id.layout_log_out);

        layoutAboutUs.setOnClickListener(this);
        layoutPrivacyPolicy.setOnClickListener(this);
        layoutFAQ.setOnClickListener(this);
        layoutLogout.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back_image_tool_bar:
                onBackPressed();
                break;

            case R.id.layout_about_us:
                AppUtils.showToast(this, "About Us");
                break;

            case R.id.layout_privacy_policy:
                AppUtils.showToast(this, "Privacy Policy");
                break;

            case R.id.layout_faq:
                AppUtils.showToast(this, "FAQ");
                break;

            case R.id.layout_log_out:
                logOut();
                break;
        }
    }

    private void logOut() {
        final Progress progressDialog = new Progress(this, R.style.CustomProgressDialogTheme);
        progressDialog.show();
        progressDialog.setCancelable(false);
        Retrofit retrofit = ApiClient.getClient();
        ApiInterface apiInterface = retrofit.create(ApiInterface.class);

        Call<ServerResponse> serverResponseCall = apiInterface.logout(myPreferences.getString(MyPreferences.ACCESS_TOKEN));
        serverResponseCall.enqueue(new Callback<ServerResponse>() {
            @Override
            public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {
                progressDialog.dismiss();

                //Clear preferences file.
                myPreferences.clearPreferences();

                //MyPreferences.clearPreferences(SettingsActivity.this);
                if (response.isSuccessful()) {


                    // Logout From Firebase also.
                    String firebaseUserEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
                    FirebaseAuth.getInstance().signOut();
                    AppUtils.log("CHAT", firebaseUserEmail + " is logged Out from Firebase successfully.");

                    Intent mainActiviytIntent = new Intent(SettingsActivity.this, MainActivity.class);
                    mainActiviytIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(mainActiviytIntent);
                    AppUtils.log("REST", "" + response.body().getMessage());
                    AppUtils.showToast(SettingsActivity.this, "Logged out successfully");
                    finishAffinity();


                } else {
                    Intent mainActiviytIntent = new Intent(SettingsActivity.this, MainActivity.class);
                    mainActiviytIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(mainActiviytIntent);
                    AppUtils.showToast(SettingsActivity.this, "Your session has been expired.");
                    finishAffinity();

                }
            }

            @Override
            public void onFailure(Call<ServerResponse> call, Throwable t) {
                progressDialog.dismiss();
                AppUtils.log("REST", "onFailure");

                //Display Snack when user clicks on Log out button.
                //But there is no internet connection.
                //Check for Internet connection.
                if (!AppUtils.isNetworkAvailable(SettingsActivity.this)) {
                    AppUtils.snackbar(SettingsActivity.this, findViewById(R.id.rl_settings), "No internet connection");
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
    }
}
