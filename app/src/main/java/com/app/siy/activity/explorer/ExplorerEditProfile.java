package com.app.siy.activity.explorer;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.app.siy.R;
import com.app.siy.activity.EditPaymentSettingActivity;
import com.app.siy.rest.ApiClient;
import com.app.siy.rest.ApiInterface;
import com.app.siy.rest.ServerResponse;
import com.app.siy.rest.User;
import com.app.siy.sharedpreferences.MyPreferences;
import com.app.siy.utils.AppUtils;
import com.app.siy.utils.Constants;
import com.app.siy.utils.Progress;
import com.google.gson.Gson;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ExplorerEditProfile extends AppCompatActivity implements View.OnClickListener,
        AdapterView.OnItemSelectedListener {

    private EditText etFirstName;
    private EditText etLastName;
    private MyPreferences myPreferences;
    private User user;
    private String selectedCommute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.explorer_activity_edit_profile);
        gettingIdOfInputFields();

        myPreferences = new MyPreferences(this);
        String userString = myPreferences.getString(MyPreferences.USER_MODEL);
        user = new Gson().fromJson(userString, User.class);


        displayExistingValuesFromStorage();
    }

    private void displayExistingValuesFromStorage() {
        etFirstName.setText(user.getFirstName());
        etLastName.setText(user.getLastName());
    }

    private void gettingIdOfInputFields() {
        RelativeLayout layoutEditPayment = (RelativeLayout) findViewById(R.id.layout_edit_payment_setting_edit_profile);
        Toolbar toolbar = (Toolbar) findViewById(R.id.include_toolbar_edit_profile);
        TextView toolbarText = toolbar.findViewById(R.id.tool_bar_text);
        toolbarText.setText(Constants.TOOL_BAR_TEXT_EDIT_PROFILE);

        ImageView backImage = (ImageView) findViewById(R.id.back_image_tool_bar);
        backImage.setOnClickListener(this);
        layoutEditPayment.setOnClickListener(this);

        etFirstName = (EditText) findViewById(R.id.et_first_name_explorer_edit_profile);
        etLastName = (EditText) findViewById(R.id.et_last_name_explorer_edit_profile);

        Button btnSave = (Button) findViewById(R.id.btn_save_edit_profile_explorer);
        btnSave.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back_image_tool_bar:
                onBackPressed();
                break;

            case R.id.layout_edit_payment_setting_edit_profile:
                startActivity(new Intent(this, EditPaymentSettingActivity.class));
                overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
                break;

            case R.id.btn_save_edit_profile_explorer:
                validateInputFields();
                break;
        }
    }

    private void validateInputFields() {
        AppUtils.hideKeyboard(this);

        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();


        //---
        String accessToken = user.getAccessToken();

        String userType = user.getUserType();

        String dob = user.getDob();

        String isRequestStudentRecorder = user.getIsRequestStudent();

        String termsAndConditions = user.getTermsAndConditions();

        String preferredLanguage = user.getLanguage();

        String profileStatus = Constants.PROFILE_STATUS_EDIT_PROFILE;
        //----

        if (TextUtils.isEmpty(firstName)) {
            AppUtils.showToast(this, Constants.PLEASE_ENTER_FIRST_NAME);
        } else if (TextUtils.isEmpty(lastName)) {
            AppUtils.showToast(this, Constants.PLEASE_ENTER_LAST_NAME);
        }

        if (!TextUtils.isEmpty(firstName) && !TextUtils.isEmpty(lastName)) {
            editProfileExplorer(accessToken, userType, firstName,
                    lastName, dob, isRequestStudentRecorder,
                    termsAndConditions, preferredLanguage, profileStatus);
        }
    }

    private void editProfileExplorer(String accessToken, String userType, String firstName,
                                     String lastName, String dob, String isRequestStudent,
                                     String termsAndConditions, String preferredLanguage,
                                     String profileStatus) {

        if (AppUtils.isNetworkAvailable(this)) {
            final Progress progressDialog = new Progress(this, R.style.CustomProgressDialogTheme);
            progressDialog.setCancelable(false);
            progressDialog.show();

            ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);

            Call<ServerResponse> serverResponseCall = apiInterface.completeProfile(
                    accessToken, userType, firstName, lastName,
                    dob, isRequestStudent, termsAndConditions,
                    preferredLanguage, profileStatus);
            serverResponseCall.enqueue(new Callback<ServerResponse>() {
                @Override
                public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {

                    AppUtils.log("onResponse");
                    if (response.isSuccessful()) {
                        //AppUtils.showToast(ExplorerEditProfile.this, "Profile Updated");
                        progressDialog.dismiss();
                        /*Intent intent = new Intent(ExplorerEditProfile.this, ExplorerHomeActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        */
                        AppUtils.showToast(ExplorerEditProfile.this, Constants.PROFILE_UPDATED);
                        //------ Save User to SharedPreferences.
                        //Get user object -
                        User user = response.body().user;

                        //Get JSON String of User object from response object.
                        String userJson = new Gson().toJson(user);
                        AppUtils.log("JSON String - SignIn - " + userJson);
                        //Save user json to SharedPreferences
                        myPreferences.saveString(MyPreferences.USER_MODEL, userJson);
                        //-----  Save User End
                    } else {
                        progressDialog.dismiss();
                        try {
                            AppUtils.log("Unsuccessful : " + response.errorBody().string());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onFailure(Call<ServerResponse> call, Throwable t) {
                    progressDialog.dismiss();
                    AppUtils.log("Failure - Edit Profile.");
                }
            });
        } else {
            AppUtils.snackbar(this, Constants.NO_INTERNET_CONNECTION);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        AppUtils.hideKeyboard(this);
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String selectedText = (String) parent.getItemAtPosition(position);
        if (position > 0) {
            this.selectedCommute = selectedText;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}