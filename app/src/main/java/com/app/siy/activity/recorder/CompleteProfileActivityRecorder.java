package com.app.siy.activity.recorder;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.icu.util.Calendar;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.app.siy.R;
import com.app.siy.activity.EditPaymentSettingActivity;
import com.app.siy.activity.explorer.CompleteProfileActivityExplorer;
import com.app.siy.helper.FileHelper;
import com.app.siy.rest.ApiClient;
import com.app.siy.rest.ApiInterface;
import com.app.siy.rest.ServerResponse;
import com.app.siy.rest.User;
import com.app.siy.sharedpreferences.MyPreferences;
import com.app.siy.utils.AppUtils;
import com.app.siy.utils.Constants;
import com.app.siy.utils.FileUtils;
import com.app.siy.utils.Progress;
import com.bumptech.glide.Glide;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class CompleteProfileActivityRecorder extends AppCompatActivity implements View.OnClickListener, AdapterView.OnItemSelectedListener {

    private static final int OPEN_CAMERA_CONSTANT = 50;
    private static final int CAMERA_PERMISSION_CONSTANT = 79;
    private static int IMAGE_CODE = 49;
    private static final int CHANGE_PROFILE_IMAGE_CONSTANT = 51;
    private static final int CHANGE_CERTIFICATE_CONSTANT = 52;
    private static final int OPEN_GALLERY_CONSTANT = 53;
    private int profileOrCertificateCode;
    private static final int EXTERNAL_STORAGE_PERMISSION_CONSTANT = 100;
    private static final int REQUEST_PERMISSION_SETTING = 101;
    private static final int LOCATION_PERMISSION_CONSTANT = 200;
    private boolean sentToSettings = false;
    private SharedPreferences permissionStatus;

    private static final int PLACE_PICKER_REQUEST = 800;
    private Toolbar toolbar;
    private ImageView backImage;
    private Button btnSubmit;
    private RelativeLayout editPaymentSetting;
    private TextView dobPicker;
    private EditText etFirstName;
    private EditText etLastName;
    private TextView tvCurrentAddress;
    private TextView tvDob;
    private Spinner spProfession, spDesiredCommute;
    private String selectedCommute;
    private String selectedProfession;
    private CheckBox cbIArgee;
    private RelativeLayout rlBackground;
    private TextView tvTermsAndConditions;

    private double selectedAddressLatitude;
    private double selectedAddressLongitude;

    private ImageView btnUploadeProfileImage;
    private ImageView btnUploadCertificate;
    private CircleImageView civProfileImage;
    private ImageView ivCertificate;

    private Uri profileUriForServer;
    private Uri certificateUriForServer;
    private String selectedAddress;

    private Spinner spPreferredLanguage;
    private String selectedLanguage;

    private MyPreferences myPreferences;
    private FileHelper fileHelper;
    private Progress progress;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recorder_activity_complete_profile);

        //Instantiate MyPreferences
        myPreferences = new MyPreferences(this);

        fileHelper = new FileHelper(this);

        btnSubmit = (Button) findViewById(R.id.btn_submit_complete_my_profile_recorder);
        editPaymentSetting = (RelativeLayout) findViewById(R.id.layout_edit_payment_setting_recorder);
        dobPicker = (TextView) findViewById(R.id.txt_date_of_birth_picker_recorder);
        toolbar = (Toolbar) findViewById(R.id.include_toolbar_recorder);
        TextView textView = toolbar.findViewById(R.id.tool_bar_text);
        textView.setText("My Profile");
        backImage = (ImageView) toolbar.findViewById(R.id.back_image_tool_bar);

        rlBackground = (RelativeLayout) findViewById(R.id.rl_complete_profile_recorder);
        backImage.setOnClickListener(this);
        btnSubmit.setOnClickListener(this);
        editPaymentSetting.setOnClickListener(this);
        dobPicker.setOnClickListener(this);

        etFirstName = (EditText) findViewById(R.id.et_first_name_recorder);
        etLastName = (EditText) findViewById(R.id.et_last_name_recorder);
        tvDob = (TextView) findViewById(R.id.txt_date_of_birth_picker_recorder);

        tvCurrentAddress = (TextView) findViewById(R.id.tv_current_address);
        tvCurrentAddress.setOnClickListener(this);

        spProfession = (Spinner) findViewById(R.id.spinner_i_am_a);
        spProfession.setOnItemSelectedListener(this);

        spDesiredCommute = (Spinner) findViewById(R.id.spinner_desired_commute);
        spDesiredCommute.setOnItemSelectedListener(this);

        spPreferredLanguage = (Spinner) findViewById(R.id.spinner_preferred_language_recorder);
        spPreferredLanguage.setOnItemSelectedListener(this);

        cbIArgee = (CheckBox) findViewById(R.id.checkbox_agree_terms_and_condition_recorder);

        tvTermsAndConditions = (TextView) findViewById(R.id.tv_terms_and_conditions);
        tvTermsAndConditions.setOnClickListener(this);

        btnUploadeProfileImage = (ImageView) findViewById(R.id.btn_upload_profile_image);
        btnUploadeProfileImage.setOnClickListener(this);

        btnUploadCertificate = (ImageView) findViewById(R.id.iv_choose_certificate);
        btnUploadCertificate.setOnClickListener(this);

        civProfileImage = (CircleImageView) findViewById(R.id.civ_profile_image_recorder);
        ivCertificate = (ImageView) findViewById(R.id.iv_certificate_recorder);

        //Getting Selected Item from Spinner - Profession
        professionType();
        //Getting Selected Item from Spinner - Desired Commute
        getDesiredCommuteFromSpinner();
        displayPreferredLanguageToSpinner();
        permissionStatus = getSharedPreferences("permissionStatus", MODE_PRIVATE);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        if (progress != null) {
            progress.dismiss();
        }
    }


    @Override
    protected void onStop() {
        super.onStop();
        if (progress != null) {
            progress.dismiss();
        }
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.iv_choose_certificate:
                checkForStoragePermission(CHANGE_CERTIFICATE_CONSTANT);
                break;

            case R.id.btn_upload_profile_image:
                //checkForStoragePermission(PICK_PROFILE_IMAGE);
                checkForStoragePermission(CHANGE_PROFILE_IMAGE_CONSTANT);
                break;

            case R.id.tv_terms_and_conditions:
                startActivity(new Intent(CompleteProfileActivityRecorder.this, TermsAndConditionsActivitiyRecorder.class));
                break;

            case R.id.tv_current_address:
                checkForLocationPermission();
                break;
            case R.id.back_image_tool_bar:
                onBackPressed();
                break;

            case R.id.txt_date_of_birth_picker_recorder:

                //Hide the Keyboard.
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(rlBackground.getWindowToken(), 0);
                selectDOB();
                break;

            case R.id.layout_edit_payment_setting_recorder:
                startActivity(new Intent(getApplicationContext(), EditPaymentSettingActivity.class));
                overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
                break;

            case R.id.btn_submit_complete_my_profile_recorder:
                validateInputFields();
                break;
        }
    }


    private void selectGalleryOrCamera(int imageConstant) {
        IMAGE_CODE = imageConstant;
        String choiceOptions[] = {"Gallery", "Camera"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Complete action using");
        builder.setItems(choiceOptions, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    // Open Gallery
                    Intent galleryIntentForChangeProfile = new Intent();
                    galleryIntentForChangeProfile.setAction(Intent.ACTION_GET_CONTENT);
                    galleryIntentForChangeProfile.setType("image/*");
                    startActivityForResult(galleryIntentForChangeProfile, OPEN_GALLERY_CONSTANT);
                } else if (which == 1) {
                    // Open Camera.
                    checkForCameraPermission();
                }
            }
        });
        builder.show();
    }

    private void openCamera() {
        Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(takePicture, OPEN_CAMERA_CONSTANT);
    }

    private void validateInputFields() {
        String accessToken = myPreferences.getString(MyPreferences.ACCESS_TOKEN);
        //String accessToken = MyPreferences.getAccessToken(this);
        AppUtils.log("REST", "Access Token on Recorder complete Profile " + accessToken);
        //String contentType = "application/x-www-form-urlencoded";
        //String contentType = "multipart/form-data";

        String userType = Constants.USER_TYPE_RECORDER;
        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String dob = tvDob.getText().toString().trim();
        String currentAddress = tvCurrentAddress.getText().toString().trim();
        /*double latitude = selectedAddressLatitude;
        double longitude = selectedAddressLongitude;*/
        String selectedAddress = this.selectedAddress;
        String isRequestStudentRecorder = Constants.STUDENT_RECORDER_NO;
        String termsAndConditions = "";
        if (cbIArgee.isChecked()) {
            termsAndConditions = Constants.TERMS_AND_CONDITIONS_CHECKED_YES;
        } else if (!cbIArgee.isChecked()) {
            termsAndConditions = Constants.TERMS_AND_CONDITIONS_CHECKED_NO;
        }
        String selectedCommute = this.selectedCommute;
        String selectedProfession = this.selectedProfession;


        String profileStatus = Constants.PROFILE_STATUS_COMPLETE_PROFILE;
        String language = this.selectedLanguage;


        if (TextUtils.isEmpty(firstName)) {
            AppUtils.showToast(this, "Please enter first name");
        } else if (TextUtils.isEmpty(lastName)) {
            AppUtils.showToast(this, "Please enter last name");
        } else if (TextUtils.isEmpty(dob)) {
            AppUtils.showToast(this, "Please select DOB");
        } else if (TextUtils.isEmpty(currentAddress)) {
            AppUtils.showToast(this, "Please enter current address");
        } else if (TextUtils.isEmpty(selectedProfession)) {
            AppUtils.showToast(this, "Please select your profession");
        } else if (TextUtils.isEmpty(selectedCommute)) {
            AppUtils.showToast(this, "Please select commute address");
        } else if (TextUtils.isEmpty(selectedLanguage)) {
            AppUtils.showToast(this, "Please select Preferred Language");
        } else if (!cbIArgee.isChecked()) {
            AppUtils.showToast(this, "Please select terms and conditions");
        } else if (profileUriForServer == null) {
            AppUtils.showToast(CompleteProfileActivityRecorder.this, "Please select profile image");
        } else if (certificateUriForServer == null) {
            AppUtils.showToast(CompleteProfileActivityRecorder.this, "Please select your certificate");
        }

        if (!TextUtils.isEmpty(firstName) && !TextUtils.isEmpty(lastName) && !TextUtils.isEmpty(dob)
                && !TextUtils.isEmpty(currentAddress) && !TextUtils.isEmpty(selectedProfession)
                && !TextUtils.isEmpty(selectedCommute)
                && !TextUtils.isEmpty(selectedLanguage)
                && cbIArgee.isChecked()
                && profileUriForServer != null
                && ivCertificate.getDrawable() != null
                ) {
            completeProfile(accessToken, userType, selectedAddress,
                    selectedProfession, firstName, lastName,
                    dob, isRequestStudentRecorder, termsAndConditions,
                    profileUriForServer, certificateUriForServer, selectedCommute, profileStatus, language);
        }
    }

    //Helper method.
    @NonNull
    private MultipartBody.Part prepareFilePart(String partName, Uri fileUri) {
        // https://github.com/iPaulPro/aFileChooser/blob/master/aFileChooser/src/com/ipaulpro/afilechooser/utils/FileUtils.java
        // use the FileUtils to get the actual file by uri
        //FileUtils class is present inside com.app.siy.utils.FileUtils

        MultipartBody.Part part = null;
        File file = FileUtils.getFile(this, fileUri);       //File file = new File(fileUri.getPath());  // will not work.
        RequestBody requestFile = null;

        try {
            if (file != null) {
                // create RequestBody instance from file
                requestFile =
                        RequestBody.create(
                                MediaType.parse("image/*"),
                                file
                        );

                part = MultipartBody.Part.createFormData(partName, file.getName(), requestFile);
            } else {
                //AppUtils.log("File is null");
            }
        } catch (Exception e) {
            //AppUtils.log("Exception while creating Part " + e.getMessage());
        }
        // MultipartBody.Part is used to send also the actual file name
        return part;
    }

    //Helper method.
    @NonNull
    private RequestBody createPartFromString(String descriptionString) {
        return RequestBody.create(
                okhttp3.MultipartBody.FORM, descriptionString);
    }

    private void completeProfile(final String accessToken, String userType, String address,
                                 String selectedProfession, String firstName, String lastName,
                                 String dob, String isRequestStudentRecorder, String termsAndConditions,
                                 Uri profileImageUri, Uri certificateUri, String selectedCommute,
                                 String profileStatus, String language) {

        AppUtils.log("Profile URI for Server : " + profileImageUri);
        //PartMap for Fields other tna Image and Certificate.
        HashMap<String, RequestBody> map = new HashMap<>();
        map.put("user_type", createPartFromString(userType));
        map.put("address", createPartFromString(address));
        map.put("professional", createPartFromString(selectedProfession));
        map.put("first_name", createPartFromString(firstName));
        map.put("last_name", createPartFromString(lastName));
        map.put("dob", createPartFromString(dob));
        map.put("is_request_student", createPartFromString(isRequestStudentRecorder));
        map.put("term_condition", createPartFromString(termsAndConditions));
        map.put("desired_commute", createPartFromString(selectedCommute));
        map.put("profile_status", createPartFromString(profileStatus));
        map.put("language", createPartFromString(language));

        //Part for Image anc certificate.
        //"image" and "certificate" is the json field name.
        MultipartBody.Part imageBody = prepareFilePart("image", profileImageUri);
        MultipartBody.Part certificateBody = prepareFilePart("certificate", certificateUri);

        Retrofit retrofit = ApiClient.getClient();
        ApiInterface apiInterface = retrofit.create(ApiInterface.class);

        // Check for Internet Connection.
        if (AppUtils.isNetworkAvailable(this)) {

            //Show Progress Dialog.
            final Progress progressDialog = new Progress(this, R.style.CustomProgressDialogTheme);
            progressDialog.setCancelable(false);
            progressDialog.show();

            progressDialog.setCancelable(false);


            Call<ServerResponse> call = apiInterface.completeProfileRecorder(accessToken, map, imageBody, certificateBody);
            call.enqueue(new Callback<ServerResponse>() {
                @Override
                public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {
                    if (response.isSuccessful()) {
                        progressDialog.dismiss();
                        try {
                            //------ Save User to SharedPreferences.
                            //Get user object -
                            User user = response.body().user;
                            //Get JSON String of User object from response object.
                            String userJson = new Gson().toJson(user);
                            AppUtils.log("REST", "JSON String - SignIn - " + userJson);
                            //Save user json to SharedPreferences
                            myPreferences.saveString(MyPreferences.USER_MODEL, userJson);
                            //-----  Save User End


                            AppUtils.log("REST", response.body().getMessage() + " with user ID : " + response.body().user.getUserId());
                            String otp = response.body().user.getOtp();
                            String email = response.body().user.getEmail();
                            String userId = response.body().user.getUserId();

                            Intent intent = new Intent(CompleteProfileActivityRecorder.this, VerifyEmailIDRecorder.class);
                            intent.putExtra("OTP", otp);
                            intent.putExtra("EMAIL", email);
                            intent.putExtra("USERID", userId);
                            startActivity(intent);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        try {
                            AppUtils.log("REST", "Un Success" + response.errorBody().string());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onFailure(Call<ServerResponse> call, Throwable t) {
                    progressDialog.dismiss();
                    AppUtils.snackbar(CompleteProfileActivityRecorder.this, "Time Out, Try again!");
                    AppUtils.log("REST", "Failure " + t.getMessage());
                }
            });

        } else {
            AppUtils.snackbar(this, findViewById(android.R.id.content), "No Internet Connection");
        }
    }


    private void professionType() {
        String genderFromResource[] = getResources().getStringArray(R.array.i_am_a);
        List<String> genderList = new ArrayList<>(Arrays.asList(genderFromResource));
        ArrayAdapter adapter = new ArrayAdapter(this, R.layout.layout_spinner_item, genderList) {
            @Override
            public boolean isEnabled(int position) {
                if (position == 0) return false;
                else return true;
            }

            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView textView = (TextView) view;
                //textView.setTextColor(getResources().getColor(R.color.colorText));
                textView.setPadding(80, 30, 40, 30);
                if (position == 0) textView.setTextColor(Color.GRAY);
                else {
                    textView.setTextColor(Color.BLACK);
                }
                return view;
            }
        };

        spProfession.setAdapter(adapter);
    }


    private void getDesiredCommuteFromSpinner() {
        //Getting string Desired commute from string.xml file
        String[] desiredCommuteData = getResources().getStringArray(R.array.desired_commute);
        List<String> desiredCommuteList = new ArrayList<>(Arrays.asList(desiredCommuteData));
        ArrayAdapter desiredCommuteAdapter = new ArrayAdapter(this, R.layout.layout_spinner_item, desiredCommuteList) {
            @Override
            public boolean isEnabled(int position) {
                if (position == 0) return false;
                else return true;
            }

            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView textView = (TextView) view;
                //textView.setTextColor(getResources().getColor(R.color.colorTextLight));
                textView.setPadding(80, 30, 40, 30);
                if (position == 0) textView.setTextColor(Color.GRAY);
                else {
                    textView.setTextColor(Color.BLACK);
                }
                return view;
            }
        };

        spDesiredCommute.setAdapter(desiredCommuteAdapter);
    }


    private void displayPreferredLanguageToSpinner() {
        final String languages[] = getResources().getStringArray(R.array.preferred_languages_explorer);
        final List<String> languageList = new ArrayList<>(Arrays.asList(languages));
        ArrayAdapter adapter = new ArrayAdapter(CompleteProfileActivityRecorder.this, R.layout.layout_spinner_item, languageList) {
            @Override
            public boolean isEnabled(int position) {
                if (position == 0) return false;
                else return true;
            }

            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView textView = (TextView) view;
                textView.setTextColor(Color.parseColor("#efefef"));
                textView.setPadding(80, 30, 40, 30);
                if (position == 0) textView.setTextColor(Color.GRAY);
                else {
                    textView.setTextColor(Color.BLACK);
                }
                return view;
            }
        };
        spPreferredLanguage.setAdapter(adapter);
    }


    private void displayPlacePicker() {
        progress = new Progress(this, R.style.CustomProgressDialogTheme);
        progress.show();
        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
        try {
            startActivityForResult(builder.build(this), PLACE_PICKER_REQUEST); // for activity
        } catch (GooglePlayServicesRepairableException e) {
            AppUtils.log("SIY", "GooglePlayServicesRepairableException : " + e.getMessage());
        } catch (GooglePlayServicesNotAvailableException e) {
            AppUtils.log("SIY", "GooglePlayServicesNotAvailableException : " + e.getMessage());
        }
    }

    private void selectDOB() {
        Calendar calendar = Calendar.getInstance();
        final int day = calendar.get(Calendar.DATE);
        int month = calendar.get(Calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);

        DatePickerDialog dobPickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                dobPicker.setText(year + "/" + (++month) + "/" + dayOfMonth);
            }
        }, year, month, day);
        dobPickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        dobPickerDialog.show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (position > 0) {
            String selectedItem = (String) parent.getItemAtPosition(position);
            if (parent.getId() == R.id.spinner_i_am_a) {
                this.selectedProfession = selectedItem;
            } else if (parent.getId() == R.id.spinner_desired_commute) {
                this.selectedCommute = selectedItem;
            } else if (parent.getId() == R.id.spinner_preferred_language_recorder) {
                this.selectedLanguage = selectedItem;
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }


    //New permissions

    private void checkForLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                //Show Information about why you need the permission
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Need location Permission");
                builder.setMessage("This app needs location permission.");
                builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        ActivityCompat.requestPermissions(CompleteProfileActivityRecorder.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERMISSION_CONSTANT);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            } else if (permissionStatus.getBoolean(Manifest.permission.ACCESS_COARSE_LOCATION, false)) {
                //Previously Permission Request was cancelled with 'Dont Ask Again',
                // Redirect to Settings after showing Information about why you need the permission
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Need location Permission");
                builder.setMessage("This app needs location permission.");
                builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        sentToSettings = true;
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivityForResult(intent, REQUEST_PERMISSION_SETTING);
                        AppUtils.showToast(CompleteProfileActivityRecorder.this, "Go to Permissions to Grant Storage");
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            } else {
                //just request the permission
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERMISSION_CONSTANT);
            }


            SharedPreferences.Editor editor = permissionStatus.edit();
            editor.putBoolean(Manifest.permission.ACCESS_COARSE_LOCATION, true);
            editor.commit();


        } else {
            //You already have the permission, just go ahead.
            displayPlacePicker();
        }
    }// End of check for Location Permission.


    private void checkForStoragePermission(int certificateOrProfileCode) {

        this.profileOrCertificateCode = certificateOrProfileCode;

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                //Show Information about why you need the permission
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Need Storage Permission");
                builder.setMessage("This app needs storage permission.");
                builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        ActivityCompat.requestPermissions(CompleteProfileActivityRecorder.this,
                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                EXTERNAL_STORAGE_PERMISSION_CONSTANT);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            } else if (permissionStatus.getBoolean(Manifest.permission.WRITE_EXTERNAL_STORAGE, false)) {
                //Previously Permission Request was cancelled with Don't Ask Again',
                // Redirect to Settings after showing Information about why you need the permission
                AlertDialog.Builder builder = new AlertDialog.Builder(CompleteProfileActivityRecorder.this);
                builder.setTitle("Need Storage Permission");
                builder.setMessage("This app needs storage permission.");
                builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        sentToSettings = true;
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivityForResult(intent, REQUEST_PERMISSION_SETTING);
                        AppUtils.showToast(CompleteProfileActivityRecorder.this, "Go to Permissions to Grant Storage");
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            } else {
                //just request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        EXTERNAL_STORAGE_PERMISSION_CONSTANT);
            }


            SharedPreferences.Editor editor = permissionStatus.edit();
            editor.putBoolean(Manifest.permission.WRITE_EXTERNAL_STORAGE, true);
            editor.commit();
        } else {
            //You already have the permission, just go ahead.
            selectGalleryOrCamera(certificateOrProfileCode);
        }
    }//end of check for Storage permission.


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {


            case LOCATION_PERMISSION_CONSTANT:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //The External Storage Write Permission is granted to you... Continue your left job...
                    displayPlacePicker();
                } else {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.ACCESS_COARSE_LOCATION)) {
                        //Show Information about why you need the permission
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setTitle("Need location Permission");
                        builder.setMessage("This app needs location permission");
                        builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                                ActivityCompat.requestPermissions(CompleteProfileActivityRecorder.this,
                                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                                        LOCATION_PERMISSION_CONSTANT);
                            }
                        });
                        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                        builder.show();
                    } else {
                        AppUtils.showToast(CompleteProfileActivityRecorder.this, "Unable to get Permission");
                    }
                }
                break;


            case CAMERA_PERMISSION_CONSTANT:

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //The External Storage Write Permission is granted to you... Continue your left job...
                    openCamera();
                } else {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                        //Show Information about why you need the permission
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setTitle("Need Camera Permission");
                        builder.setMessage("This app needs camera permission");
                        builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                                ActivityCompat.requestPermissions(CompleteProfileActivityRecorder.this,
                                        new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CONSTANT);
                            }
                        });
                        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                        builder.show();
                    } else {
                        AppUtils.showToast(this, "Unable to get Permission");
                        //Toast.makeText(getBaseContext(), "Unable to get Permission", Toast.LENGTH_LONG).show();
                    }
                }
                break;

            case EXTERNAL_STORAGE_PERMISSION_CONSTANT:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //The External Storage Write Permission is granted to you... Continue your left job...
                    //openCamera();

                } else {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        //Show Information about why you need the permission
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setTitle("Need Storage Permission");
                        builder.setMessage("This app needs Storage permission");
                        builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                                ActivityCompat.requestPermissions(CompleteProfileActivityRecorder.this,
                                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                        EXTERNAL_STORAGE_PERMISSION_CONSTANT);
                            }
                        });
                        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                        builder.show();
                    } else {
                        AppUtils.showToast(this, "Unable to get Permission");
                        //Toast.makeText(getBaseContext(), "Unable to get Permission", Toast.LENGTH_LONG).show();
                    }
                }
                break;
        }
    }//onRequestPermissionsResult


    //Permissions
    private void checkForCameraPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                //Show Information about why you need the permission
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Need Camera Permission");
                builder.setMessage("This app needs camera permission.");
                builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        ActivityCompat.requestPermissions(CompleteProfileActivityRecorder.this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CONSTANT);
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            } else if (permissionStatus.getBoolean(Manifest.permission.CAMERA, false)) {
                //Previously Permission Request was cancelled with 'Dont Ask Again',
                // Redirect to Settings after showing Information about why you need the permission
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Need camera Permission");
                builder.setMessage("This app needs camera permission.");
                builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        //sentToSettings = true;
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivityForResult(intent, REQUEST_PERMISSION_SETTING);

                        AppUtils.showToast(CompleteProfileActivityRecorder.this, "Go to Permissions to Grant Camera");
                        //Toast.makeText(RecorderHomeActivity.this, "Go to Permissions to Grant Camera", Toast.LENGTH_LONG).show();
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            } else {
                //just request the permission
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CONSTANT);
            }


            SharedPreferences.Editor editor = permissionStatus.edit();
            editor.putBoolean(Manifest.permission.CAMERA, true);
            editor.commit();
        } else {
            //You already have the permission, just go ahead.
            openCamera();
        }
    }//end of check for Camera permission.


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //--------------------
        switch (requestCode) {
            case OPEN_CAMERA_CONSTANT:
                if (IMAGE_CODE == CHANGE_PROFILE_IMAGE_CONSTANT) {
                    if (resultCode == RESULT_OK) {
                        //Uri imageUri = data.getData();      // In case of  Camera data.getData() is null.
                        Bundle bundle = data.getExtras();
                        Object imageObject = bundle.get("data");
                        Bitmap bmpImage = (Bitmap) imageObject;
                        this.profileUriForServer = fileHelper.getImageUri(this, bmpImage);
                        civProfileImage.setImageBitmap(bmpImage);
                    }


                } else if (IMAGE_CODE == CHANGE_CERTIFICATE_CONSTANT) {
                    if (resultCode == RESULT_OK) {

                        Bundle bundle = data.getExtras();
                        Bitmap certificateImage = (Bitmap) bundle.get("data");
                        ivCertificate.setImageBitmap(certificateImage);
                        this.certificateUriForServer = fileHelper.getImageUri(this, certificateImage);
                    }
                }
                break;

            case OPEN_GALLERY_CONSTANT:
                if (IMAGE_CODE == CHANGE_PROFILE_IMAGE_CONSTANT) {
                    if (resultCode == RESULT_OK) {
                        Uri imageUri = data.getData();
                        CropImage.activity(imageUri)
                                .setGuidelines(CropImageView.Guidelines.ON)
                                .setAspectRatio(1, 1)
                                .start(CompleteProfileActivityRecorder.this);
                    }

                } else if (IMAGE_CODE == CHANGE_CERTIFICATE_CONSTANT) {
                    if (resultCode == RESULT_OK) {
                        Uri imageUri = data.getData();
                        CropImage.activity(imageUri)
                                .setGuidelines(CropImageView.Guidelines.ON)
                                .setAspectRatio(1, 1)
                                .start(CompleteProfileActivityRecorder.this);
                    }
                }
                break;

            case CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE:
                if (IMAGE_CODE == CHANGE_PROFILE_IMAGE_CONSTANT) {
                    if (resultCode == RESULT_OK) {
                        CropImage.ActivityResult resultProfileImage = CropImage.getActivityResult(data);
                        Uri uriProfile = resultProfileImage.getUri();
                        this.profileUriForServer = uriProfile;
                        Glide.with(this).load(uriProfile).into(civProfileImage);
                    }

                } else if (IMAGE_CODE == CHANGE_CERTIFICATE_CONSTANT) {
                    if (resultCode == RESULT_OK) {
                        CropImage.ActivityResult resultCertificate = CropImage.getActivityResult(data);
                        Uri uriCertificate = resultCertificate.getUri();
                        this.certificateUriForServer = uriCertificate;
                        Glide.with(this).load(uriCertificate).into(ivCertificate);
                    }
                }
                break;

            case PLACE_PICKER_REQUEST:
                if (resultCode == RESULT_OK) {
                    Place place = PlacePicker.getPlace(this, data);
                    this.selectedAddress = place.getAddress().toString();
                    tvCurrentAddress.setText(place.getAddress());

                    LatLng selectedLagLng = place.getLatLng();
                    this.selectedAddressLatitude = selectedLagLng.latitude;
                    this.selectedAddressLongitude = selectedLagLng.longitude;
                    tvCurrentAddress.setText(place.getAddress());
                }
                break;

            case REQUEST_PERMISSION_SETTING:
                if (resultCode == RESULT_OK) {
                    if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        //Got Permission
                        selectGalleryOrCamera(requestCode);
                    }
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        //Got Permission
                        displayPlacePicker();
                    }
                }
                break;
        }
    }
}