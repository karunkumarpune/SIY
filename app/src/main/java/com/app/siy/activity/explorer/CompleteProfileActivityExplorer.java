package com.app.siy.activity.explorer;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.icu.util.Calendar;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.app.siy.R;
import com.app.siy.activity.EditPaymentSettingActivity;
import com.app.siy.activity.MainActivity;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class CompleteProfileActivityExplorer extends AppCompatActivity implements
        View.OnClickListener, AdapterView.OnItemSelectedListener {

    private Dialog progress;
    private TextView textViewDOBPicker;
    private Spinner preferredLanguageExplorer;
    private String selectedLanguage;
    private EditText etFirstName;
    private EditText etLastName;
    private CheckBox checkBoxAgreeTermsAndConditions;
    private Switch swStudentRecorder;
    private TextView tvDobHeader;
    private TextView tvPreferredLanguageHeader;
    private MyPreferences myPreferences;
    private RelativeLayout rootLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.explorer_activity_complete_profile);

        //Instantiate MyPreferences
        myPreferences = new MyPreferences(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.include_toolbar_in_complete_profile_explorer);
        setSupportActionBar(toolbar);
        textViewDOBPicker = (TextView) findViewById(R.id.txt_date_of_birth_picker);
        preferredLanguageExplorer = (Spinner) findViewById(R.id.spinner_preferred_language);
        swStudentRecorder = (Switch) findViewById(R.id.sw_student_recorder);
        RelativeLayout editPaymentSetting = (RelativeLayout) findViewById(R.id.layout_edit_payment_setting);
        Button btnSubmit = (Button) findViewById(R.id.btn_submit_my_profile_explorer);
        //RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.relative_layout_submit_profile);
        etFirstName = (EditText) findViewById(R.id.et_first_name_explorer);
        etLastName = (EditText) findViewById(R.id.et_last_name_explorer);
        checkBoxAgreeTermsAndConditions = (CheckBox) findViewById(R.id.checkbox_agree_terms_and_condition);
        tvDobHeader = (TextView) findViewById(R.id.dob_header);
        TextView textView = toolbar.findViewById(R.id.tool_bar_text);
        textView.setText(Constants.TOOL_BAR_TEXT_MY_PROFILE);

        rootLayout = (RelativeLayout) findViewById(R.id.relative_layout_submit_profile);

        ImageView backButton = toolbar.findViewById(R.id.back_image_tool_bar);
        backButton.setOnClickListener(this);
        textViewDOBPicker.setOnClickListener(this);

        preferredLanguageExplorer.setOnItemSelectedListener(this);
        tvPreferredLanguageHeader = (TextView) findViewById(R.id.tv_preferred_language_header);

        TextView tvTermsAndConditions = (TextView) findViewById(R.id.tv_terms_and_conditions);
        tvTermsAndConditions.setOnClickListener(this);

        editPaymentSetting.setOnClickListener(this);
        btnSubmit.setOnClickListener(this);
        checkBoxAgreeTermsAndConditions.setOnClickListener(this);

        displayDataToSpinner();

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            progress = new Dialog(this, R.style.CustomDialogTheme);
        } else {
            progress = new Dialog(this);
        }

        try {
            LayoutInflater layoutInflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
            View progressView = layoutInflater.inflate(R.layout.congratulations_dialog_layout_explorer,
                    (ViewGroup) findViewById(R.id.congratulation_layout), false);
            progress.setContentView(progressView);
            progress.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            progress.setCancelable(false);
            progress.setCanceledOnTouchOutside(false);
        } catch (Exception e) {
            AppUtils.log("Exception : " + e);
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.tv_terms_and_conditions:
                startActivity(new Intent(this, TermsAndConditions.class));
                break;

            case R.id.back_image_tool_bar:
                onBackPressed();
                break;


            case R.id.txt_date_of_birth_picker:
                AppUtils.hideKeyboard(this);
                //Display Date Picker Dialog
                selectDateOfBirth();
                break;

            case R.id.layout_edit_payment_setting:
                startActivity(new Intent(this, EditPaymentSettingActivity.class));
                overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
                break;

            case R.id.btn_submit_my_profile_explorer:
                validateInputFields();
                break;
        }
    }

    private void validateInputFields() {


        String accessToken = myPreferences.getString(MyPreferences.ACCESS_TOKEN);

        String userType = Constants.USER_TYPE_EXPLORER;

        String firstName = etFirstName.getText().toString().trim();

        String lastName = etLastName.getText().toString().trim();

        String dob = textViewDOBPicker.getText().toString().trim();

        String isRequestStudentRecorder;
        boolean isStudentRecorder = swStudentRecorder.isChecked();
        if (!isStudentRecorder) {
            isRequestStudentRecorder = Constants.STUDENT_RECORDER_YES;
        } else {
            isRequestStudentRecorder = Constants.STUDENT_RECORDER_NO;
        }

        String termsAndConditions;
        if (checkBoxAgreeTermsAndConditions.isChecked()) {
            termsAndConditions = Constants.TERMS_AND_CONDITIONS_CHECKED_YES;
        } else {
            termsAndConditions = Constants.TERMS_AND_CONDITIONS_CHECKED_NO;
        }
        String profileStatus = Constants.PROFILE_STATUS_COMPLETE_PROFILE;

        if (TextUtils.isEmpty(firstName)) {
            AppUtils.showToast(this, Constants.PLEASE_ENTER_FIRST_NAME);
        } else if (TextUtils.isEmpty(lastName)) {
            AppUtils.showToast(this, Constants.PLEASE_ENTER_LAST_NAME);
        } else if (TextUtils.isEmpty(dob)) {
            AppUtils.showToast(this, Constants.PLEASE_SELECT_DOB);
        } else if (TextUtils.isEmpty(selectedLanguage)) {
            AppUtils.showToast(this, Constants.PLEASE_SELECT_A_PREFERRED_LANGUAGE);
        } else if (!checkBoxAgreeTermsAndConditions.isChecked()) {
            AppUtils.showToast(this, Constants.PLEASE_CHECK_TERMS_AND_CONDITIONS);
        }


        if (!TextUtils.isEmpty(firstName) && !TextUtils.isEmpty(lastName)
                && !TextUtils.isEmpty(dob) && !TextUtils.isEmpty(selectedLanguage)
                && checkBoxAgreeTermsAndConditions.isChecked()) {
            AppUtils.log("Is Student Recorder : " + isRequestStudentRecorder);
            AppUtils.log("Is terms and conditions checked : " + termsAndConditions);
            completeProfile(accessToken, userType, firstName, lastName, dob,
                    isRequestStudentRecorder, termsAndConditions,
                    selectedLanguage, profileStatus);
        }
    }

    private void completeProfile(String accessToken, String userType, String firstName,
                                 String lastName, String dob, String isRequestStudent,
                                 String termsAndConditions, String preferredLanguage,
                                 String profileStatus) {
        AppUtils.log("Access Token : " + accessToken);
        AppUtils.log("User Type : " + userType);
        AppUtils.log("First Name : " + firstName);
        AppUtils.log("Last Name : " + lastName);
        AppUtils.log("DOB : " + dob);
        AppUtils.log("Is Student Recorder " + isRequestStudent);
        AppUtils.log("Terms and conditions : " + termsAndConditions);
        AppUtils.log("Preferred Language : " + preferredLanguage);
        AppUtils.log("Profile Status : " + profileStatus);

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
                        //------ Save User to SharedPreferences.
                        //Get user object -
                        User user = response.body().user;

                        //Get JSON String of User object from response object.
                        String userJson = new Gson().toJson(user);
                        AppUtils.log("JSON String - Complete Profile Explorer : - " + userJson);
                        //Save user json to SharedPreferences
                        myPreferences.saveString(MyPreferences.USER_MODEL, userJson);

                        //-----  Save User End

                        rootLayout.setVisibility(View.INVISIBLE);
                        progressDialog.dismiss();

                        Intent verifyEmailExplorerIntent = new Intent(CompleteProfileActivityExplorer.this,
                                VerifyEmailIdExplorer.class);
                        startActivity(verifyEmailExplorerIntent);
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
                    AppUtils.log("Failure - complete profile");
                }
            });
        } else {
            AppUtils.snackbar(this, Constants.NO_INTERNET_CONNECTION);
        }
    }

    private void displayDataToSpinner() {
        final String languages[] = getResources().getStringArray(R.array.preferred_languages_explorer);
        final List<String> languageList = new ArrayList<>(Arrays.asList(languages));
        ArrayAdapter adapter = new ArrayAdapter(CompleteProfileActivityExplorer.this,
                R.layout.layout_spinner_item, languageList) {
            @Override
            public boolean isEnabled(int position) {
                if (position == 0)
                    return false;
                else
                    return true;
            }

            @Override
            public View getDropDownView(int position, @Nullable View convertView,
                                        @NonNull ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView textView = (TextView) view;
                textView.setTextColor(ContextCompat.getColor(CompleteProfileActivityExplorer.this,
                        R.color.colorLayoutBackground));
                textView.setPadding(80, 30, 40, 30);
                if (position == 0) textView.setTextColor(Color.GRAY);
                else {
                    textView.setTextColor(Color.BLACK);
                }
                return view;
            }
        };
        adapter.setDropDownViewResource(R.layout.layout_spinner_item);
        preferredLanguageExplorer.setAdapter(adapter);
    }

    private void selectDateOfBirth() {

        Calendar calendar = Calendar.getInstance();
        final int day = calendar.get(Calendar.DATE);
        int month = calendar.get(Calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);


        DatePickerDialog dobPickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        textViewDOBPicker.setText(year + "/" + (++month) + "/" + day);
                        tvDobHeader.setText(Constants.DATE_OF_BIRTH);
                    }
                }, year, month, day);

        dobPickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        dobPickerDialog.show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(CompleteProfileActivityExplorer.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (position > 0) {
            String selectedItem = (String) parent.getItemAtPosition(position);
            if (parent.getId() == R.id.spinner_preferred_language) {
                selectedLanguage = selectedItem;
                tvPreferredLanguageHeader.setText(Constants.PREFERRED_LANGUAGE);
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
    }
}