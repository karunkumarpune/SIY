package com.app.siy.activity.recorder;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.app.siy.R;
import com.app.siy.activity.EditPaymentSettingActivity;
import com.app.siy.activity.explorer.ExplorerHomeActivity;
import com.app.siy.helper.FileHelper;
import com.app.siy.rest.ApiClient;
import com.app.siy.rest.ApiInterface;
import com.app.siy.rest.ServerResponse;
import com.app.siy.rest.User;
import com.app.siy.sharedpreferences.MyPreferences;
import com.app.siy.utils.AppUtils;
import com.app.siy.utils.Constants;
import com.app.siy.utils.DownloadImageCallback;
import com.app.siy.utils.DownloadImageTask;
import com.app.siy.utils.FileUtils;
import com.app.siy.utils.Progress;
import com.bumptech.glide.Glide;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
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


public class EditProfileRecorder extends AppCompatActivity implements View.OnClickListener, DownloadImageCallback {

    private static final String FINE_LOCATION = "100";
    private static final String COARSE_LOCATION = "101";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 102;
    private static final int PLACE_PICKER_REQUEST = 800;
    private static final int OPEN_CAMERA_CONSTANT = 30;
    private static final int OPEN_GALLERY_CONSTANT = 32;
    private static final int EXTERNAL_STORAGE_PERMISSION_CONSTANT = 51;
    private static final int REQUEST_PERMISSION_SETTING = 59;
    private static final int LOCATION_PERMISSION_CONSTANT = 58;
    private static final int PROFILE_IMAGE_CONSTANT = 29;
    private static final int CERTIFICATE_IMAGE_CONSTANT = 28;
    private static final int GET_PROFILE_IMAGE_REQUEST_CODE_FROM_DOWNLOAD_IMAGE_TASK = 56;
    private static final int GET_CERTIFICATE_IMAGE_REQUEST_CODE_FROM_DOWNLOAD_IMAGE_TASK = 57;
    private static final int CAMERA_PERMISSION_CONSTANT = 78;
    private int IMAGE_CODE = 0;
    private Toolbar toolbar;
    private ImageView backImage;
    private EditText etFirstName;
    private EditText etLastName;
    private TextView tvCurrentAddress;
    private Spinner spDesiredCommute;
    private RelativeLayout rlEditPaymentSetting;
    private Button btnSave;
    private String selectedDesiredCommute;
    private List<String> selectRangeList;
    private int selectedPosition;
    private CircleImageView profileImageCiv;
    private ImageView ivCertificateImage;
    private MyPreferences myPreferences;
    private User user;
    private String selectedAddress;
    private ImageView ivBtnUploadProfileImage;
    private ImageView ivBtnUploadCertificateImage;
    private FileHelper fileHelper;
    private Uri profileUriFromUserSelection;
    private Uri certificateUriFromSelection;
    private Spinner spPreferredLanguage;
    private String selectedLanguage;

    private SharedPreferences permissionStatus;
    private int profileOrCertificateCode;
    private Progress progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recorder_activity_edit_profile);

        fileHelper = new FileHelper(this);

        toolbar = (Toolbar) findViewById(R.id.include_toolbar_edit_profile_recorder);
        TextView toolbarText = toolbar.findViewById(R.id.tool_bar_text);
        toolbarText.setText("Edit Profile");

        permissionStatus = getSharedPreferences("permissionStatus", MODE_PRIVATE);

        //Instantiate MyPreferences.
        myPreferences = new MyPreferences(this);
        String jsonStringModel = myPreferences.getString(MyPreferences.USER_MODEL);
        //Convert to User Object
        user = new Gson().fromJson(jsonStringModel, User.class);


        backImage = toolbar.findViewById(R.id.back_image_tool_bar);
        backImage.setOnClickListener(this);

        etFirstName = (EditText) findViewById(R.id.et_first_name_recorder);
        etLastName = (EditText) findViewById(R.id.et_last_name_recorder);

        spDesiredCommute = (Spinner) findViewById(R.id.spinner_desired_commute);
        rlEditPaymentSetting = (RelativeLayout) findViewById(R.id.layout_edit_payment_setting_recorder);
        btnSave = (Button) findViewById(R.id.btn_save_edit_profile_recorder);
        tvCurrentAddress = (TextView) findViewById(R.id.tv_current_address);


        ivBtnUploadProfileImage = (ImageView) findViewById(R.id.iv_change_image_profile);
        ivBtnUploadCertificateImage = (ImageView) findViewById(R.id.iv_choose_certificate);
        ivBtnUploadProfileImage.setOnClickListener(this);
        ivBtnUploadCertificateImage.setOnClickListener(this);

        tvCurrentAddress.setOnClickListener(this);
        rlEditPaymentSetting.setOnClickListener(this);
        btnSave.setOnClickListener(this);

        spPreferredLanguage = (Spinner) findViewById(R.id.spinner_preferred_language_recorder_recorder_edit_profile);

        selectDesiredCommute();

        displayPreferredLanguageToSpinner();


        displayExistingDataToEditProfilePage();

        saveProfileImageToStorage();
        saveCertificateImageToStorage();
    }


    private Uri getImageUriFromStorage(String location) {
        Uri imageUri = null;
        try {
            File rootLocation = Environment.getExternalStorageDirectory();
            File completePath = new File(rootLocation + "/" + location);
            imageUri = Uri.fromFile(completePath);
            return imageUri;
        } catch (Exception e) {
            AppUtils.log("Some Error ");
        }
        return imageUri;
    }

    private void saveCertificateImageToStorage() {
        try {
            DownloadImageTask downloadImageTask = new DownloadImageTask(this, this,
                    GET_CERTIFICATE_IMAGE_REQUEST_CODE_FROM_DOWNLOAD_IMAGE_TASK);
            downloadImageTask.execute(ApiClient.BASE_URL_UPLOADED_IMAGE + user.getCertificate());
        } catch (Exception e) {
            AppUtils.log("Exception while saving certificate : " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void saveProfileImageToStorage() {
        try {
            DownloadImageTask downloadImageTask = new DownloadImageTask(this, this,
                    GET_PROFILE_IMAGE_REQUEST_CODE_FROM_DOWNLOAD_IMAGE_TASK);
            downloadImageTask.execute(ApiClient.BASE_URL_UPLOADED_IMAGE + user.getImage());
        } catch (Exception e) {
            AppUtils.log("Exception while saving Profile Image. : " + e.getMessage());
            e.printStackTrace();
        }
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
            case R.id.iv_change_image_profile:
                checkForStoragePermission(PROFILE_IMAGE_CONSTANT);
                //select GalleryOrCamera(CHANGE_PROFILE_IMAGE_CONSTANT);
                break;

            case R.id.iv_choose_certificate:
                checkForStoragePermission(CERTIFICATE_IMAGE_CONSTANT);
                //select GalleryOrCamera(CHANGE_CERTIFICATE_CONSTANT);
                break;

            case R.id.back_image_tool_bar:
                onBackPressed();
                break;

            case R.id.layout_edit_payment_setting_recorder:
                startActivity(new Intent(this, EditPaymentSettingActivity.class));
                break;

            case R.id.btn_save_edit_profile_recorder:
                validateInputFields();
                break;

            case R.id.tv_current_address:
                checkForLocationPermission();
                //AppUtils.showToastBlack(this, "Current Address");
                break;
        }
    }


    private void selecteGalleryOrCamera(int imageConstant) {
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

    private void displayExistingDataToEditProfilePage() {
        profileImageCiv = (CircleImageView) findViewById(R.id.profile_image_recorder_edit_profile);
        ivCertificateImage = (ImageView) findViewById(R.id.iv_certificate_recorder_edit_profile);

        String localImagePath = user.getImage();
        String localCertificatePath = user.getCertificate();

        //String imageFullPath = ApiClient.BASE_URL_UPLOADED_IMAGE + localImagePath;
        //AppUtils.log("Image Path 11 : " + imageFullPath);


        String currentFirstName = user.getFirstName();
        String currentLastName = user.getLastName();
        String currentLatitude = user.getLatitude();
        String currentLongitude = user.getLongitude();

        tvCurrentAddress.setText("" + user.getAddress());

        etFirstName.setText(currentFirstName);
        etLastName.setText(currentLastName);


        // -- Profile
        String profileStringFromStorage = ApiClient.BASE_URL_UPLOADED_IMAGE + user.getImage();
        AppUtils.log("Profile Image Uri from Storage : " + profileStringFromStorage);
        Glide.with(this).load(profileStringFromStorage).into(profileImageCiv);

        // -- Certificate
        String certificateStringFromStorage = ApiClient.BASE_URL_UPLOADED_IMAGE + user.getCertificate();
        AppUtils.log("Certificate Url from Storage :  " + certificateStringFromStorage);

        Glide.with(this).load(certificateStringFromStorage).into(ivCertificateImage);
    }

    private void validateInputFields() {

        // Then Get Image from Storage
        Uri imageUriFromStorage1 = getImageUriFromStorage("profile_image_" + user.getUserId() + ".jpg");
        Uri certificateUriFromStorage1 = getImageUriFromStorage("certificate_image_" + user.getUserId() + ".jpg");

        AppUtils.log("Image Uri from Storage : " + imageUriFromStorage1);
        AppUtils.log("Certificate Uri from Storage : " + certificateUriFromStorage1);


        // -- Updated Fields -
        Uri profileImageUriForServer = this.profileUriFromUserSelection;

        if (profileImageUriForServer == null) {
            profileImageUriForServer = imageUriFromStorage1;
        }

        Uri certificateImageUriForServer = this.certificateUriFromSelection;
        if (certificateImageUriForServer == null) {
            certificateImageUriForServer = certificateUriFromStorage1;
        }

        /*AppUtils.log("Profile Image from Storage : " + profileUriFromStorage);
        AppUtils.log("Profile Image from User Selection : " + profileUriFromUserSelection);
        AppUtils.log("Profile Image To Upload : " + profileImageUriForServer);*/


        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();

        String selectedCommute = this.selectedDesiredCommute;
        if (selectedCommute == null) {
            selectedCommute = user.getDesiredCommute();
        }

        String address = this.selectedAddress;
        if (address == null) {
            address = user.getAddress();
        }

        String language = this.selectedLanguage;

        String userType = user.getUserType();
        String accessToken = myPreferences.getString(MyPreferences.ACCESS_TOKEN);
        String isRequestStudentRecorder = user.getIsRequestStudent();
        String termsAndConditions = user.getTermsAndConditions();
        String selectedProfession = user.getProfessional();
        String dob = user.getDob();

        // -- Not updated fields

        // New Filed - profile_status.
        String profileStatus = Constants.PROFILE_STATUS_EDIT_PROFILE;

        if (TextUtils.isEmpty(firstName)) {
            AppUtils.showToast(this, "Enter First Name");
        } else if (TextUtils.isEmpty(lastName)) {
            AppUtils.showToast(this, "Enter Last Name");
        } else if (TextUtils.isEmpty(selectedLanguage)) {
            AppUtils.showToast(this, "Please select preferred Language");
        }

        if (!TextUtils.isEmpty(firstName) && !TextUtils.isEmpty(lastName)) {
            editProfileRecorder(accessToken, userType, address,
                    selectedProfession, firstName, lastName,
                    dob, isRequestStudentRecorder, termsAndConditions,
                    profileImageUriForServer, certificateImageUriForServer, selectedCommute,
                    profileStatus, language);
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

    //-------------------------- END OF HELPER METHODS --------------

    private void editProfileRecorder(String accessToken, String userType, String selectedAddress,
                                     String selectedProfession, String firstName,
                                     String lastName, String dob, String isRequestStudentRecorder,
                                     String termsAndConditions, Uri profileImageUri,
                                     Uri certificateImageUri, String selectedCommute,
                                     String profileStatus, String language) {

        MultipartBody.Part profileImageBody = prepareFilePart("image", profileImageUri);
        MultipartBody.Part certificateImageBody = prepareFilePart("certificate", certificateImageUri);

        //PartMap for Fields other tna Image and Certificate.
        HashMap<String, RequestBody> map = new HashMap<>();
        map.put("user_type", createPartFromString(userType));
        map.put("address", createPartFromString(selectedAddress));
        map.put("professional", createPartFromString(selectedProfession));
        map.put("first_name", createPartFromString(firstName));
        map.put("last_name", createPartFromString(lastName));
        map.put("dob", createPartFromString(dob));
        map.put("is_request_student", createPartFromString(isRequestStudentRecorder));
        map.put("term_condition", createPartFromString(termsAndConditions));
        map.put("desired_commute", createPartFromString(selectedCommute));
        map.put("profile_status", createPartFromString(profileStatus));
        map.put("language", createPartFromString(language));

        if (AppUtils.isNetworkAvailable(this)) {

            final Progress progressDialog = new Progress(this, R.style.CustomProgressDialogTheme);
            progressDialog.setCancelable(false);
            progressDialog.show();

            Retrofit retrofit = ApiClient.getClient();
            ApiInterface apiInterface = retrofit.create(ApiInterface.class);
            Call<ServerResponse> serverResponseCall = apiInterface
                    .completeProfileRecorder(accessToken, map, profileImageBody, certificateImageBody);
            serverResponseCall.enqueue(new Callback<ServerResponse>() {
                @Override
                public void onResponse(Call<ServerResponse> call, Response<ServerResponse> response) {
                    progressDialog.dismiss();

                    if (response.isSuccessful()) {

                        /*// Delete Certificate Image.
                        fileHelper.deleteImageFromStorage("certificate_image_" + user.getUserId() + ".jpg");

                        // Delete Profile Image when user complete editing.
                        boolean deleteStatus = fileHelper.deleteImageFromStorage("profile_image_" + user.getUserId() + ".jpg");
                        if (deleteStatus) {
                            AppUtils.log("File Deleted");
                        } else {
                            AppUtils.log("File Not Deleted");
                        }*/

                        //------ Save User to SharedPreferences.
                        //Get user object -
                        User user = response.body().user;
                        //Get JSON String of User object from response object.
                        String userJson = new Gson().toJson(user);
                        AppUtils.log("REST", "JSON String - SignIn - " + userJson);
                        //Save user json to SharedPreferences
                        myPreferences.saveString(MyPreferences.USER_MODEL, userJson);
                        //-----  Save User End
                        Intent intent = new Intent(EditProfileRecorder.this, RecorderHomeActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        EditProfileRecorder.this.finish();

                        AppUtils.showToast(EditProfileRecorder.this, "Profile Updated");
                    } else {
                        //Some Error Occurs
                        try {
                            AppUtils.log("REST", "Some Error Occurs " + response.errorBody().string());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onFailure(Call<ServerResponse> call, Throwable t) {
                    progressDialog.dismiss();
                    AppUtils.showToast(EditProfileRecorder.this, "Some Error, try again.");
                    AppUtils.log("REST", "onFailure - Exception " + t.getMessage());
                }
            });

            // Internet is not Available.
        } else {
            AppUtils.snackbar(this, findViewById(android.R.id.content), "No Internet Connection.");
        }

    }


    // Delete Saved Image file from Storage when user press back button.
    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Delete the Image temporary stored.
        // Delete Profile Image.
        boolean deleteStatus = fileHelper.deleteImageFromStorage("profile_image_" + user.getUserId() + ".jpg");
        if (deleteStatus) {
            AppUtils.log("File Deleted");
        } else {
            AppUtils.log("File Not Deleted");
        }
        // Delete Certificate Image.
        fileHelper.deleteImageFromStorage("certificate_image_" + user.getUserId() + ".jpg");

    }

    @Override
    protected void onResume() {
        super.onResume();

        // Display current desired commute.
        String selectedDesiredCommuteFromDB = user.getDesiredCommute();
        String[] selectRange = getResources().getStringArray(R.array.desired_commute);
        ArrayList selectRangeList = new ArrayList<>(Arrays.asList(selectRange));
        int position = selectRangeList.indexOf(selectedDesiredCommuteFromDB);

        //Set selected Commute to the Spinner.
        //Make sure to do this code in onResume().
        spDesiredCommute.setSelection(position);

        // Display current language
        String selectedLanguageFromDb = user.getLanguage();
        String[] selectLanguage = getResources().getStringArray(R.array.preferred_languages_explorer);
        ArrayList selectLanguageList = new ArrayList(Arrays.asList(selectLanguage));
        int languagePosition = selectLanguageList.indexOf(selectedLanguageFromDb);
        spPreferredLanguage.setSelection(languagePosition);
    }

    //Select Range
    private void selectDesiredCommute() {
        //Getting string Desired commute from string.xml file
        String[] selectRange = getResources().getStringArray(R.array.desired_commute);
        selectRangeList = new ArrayList<>(Arrays.asList(selectRange));

        ArrayAdapter desiredCommuteAdapter = new ArrayAdapter(this, R.layout.layout_spinner_item, selectRangeList) {
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

        spDesiredCommute.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    EditProfileRecorder.this.selectedDesiredCommute = (String) parent.getItemAtPosition(position);
                    EditProfileRecorder.this.selectedPosition = position;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void displayPreferredLanguageToSpinner() {
        final String languages[] = getResources().getStringArray(R.array.preferred_languages_explorer);
        final List<String> languageList = new ArrayList<>(Arrays.asList(languages));
        ArrayAdapter adapter = new ArrayAdapter(EditProfileRecorder.this, R.layout.layout_spinner_item, languageList) {
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
        adapter.setDropDownViewResource(R.layout.layout_spinner_item);
        spPreferredLanguage.setAdapter(adapter);

        spPreferredLanguage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                if (position > 0) {
                    String selectedItem = (String) parent.getSelectedItem();
                    EditProfileRecorder.this.selectedLanguage = selectedItem;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }


    /*private boolean checkForLocationPermission() {
        String permissions[] = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
        if (ContextCompat.checkSelfPermission(this, FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this, COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                //Open Place Picker
                displayPlacePicker();
                return true;
            } else {
                ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
                return false;
            }
        } else {
            ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST_CODE);
            return false;
        }
    }*/

    private void checkForLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)) {
                //Show Information about why you need the permission
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(Constants.PERMISSION_LOCATION_TITLE);
                builder.setMessage(Constants.PERMISSION_LOCATION_DESCRIPTION);
                builder.setPositiveButton(Constants.PERMISSION_GRANT, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        ActivityCompat.requestPermissions(EditProfileRecorder.this,
                                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                                Constants.PERMISSION_CONSTANT_LOCATION);
                    }
                });
                builder.setNegativeButton(Constants.PERMISSION_CANCEL, new DialogInterface.OnClickListener() {
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
                builder.setTitle(Constants.PERMISSION_LOCATION_TITLE);
                builder.setMessage(Constants.PERMISSION_LOCATION_DESCRIPTION);
                builder.setPositiveButton(Constants.PERMISSION_GRANT, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        //sentToSettings = true;
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivityForResult(intent, REQUEST_PERMISSION_SETTING);
                        //AppUtils.showToast(ExplorerHomeActivity.this, "Go to Permissions to Grant Storage", Toast.LENGTH_LONG).show();
                    }
                });
                builder.setNegativeButton(Constants.PERMISSION_CANCEL, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                builder.show();
            } else {
                //just request the permission
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        Constants.PERMISSION_CONSTANT_LOCATION);
            }
            SharedPreferences.Editor editor = permissionStatus.edit();
            editor.putBoolean(Manifest.permission.ACCESS_COARSE_LOCATION, true);
            //editor.commit();
            editor.apply();
        } else {
            //You already have the permission, just go ahead.
            displayPlacePicker();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case OPEN_CAMERA_CONSTANT:
                if (IMAGE_CODE == PROFILE_IMAGE_CONSTANT) {
                    if (resultCode == RESULT_OK) {
                        //Uri imageUri = data.getData();      // In case of  Camera data.getData() is null.
                        Bundle bundle = data.getExtras();
                        Object imageObject = bundle.get("data");
                        Bitmap bmpImage = (Bitmap) imageObject;
                        this.profileUriFromUserSelection = fileHelper.getImageUri(this, bmpImage);
                        profileImageCiv.setImageBitmap(bmpImage);
                    }


                } else if (IMAGE_CODE == CERTIFICATE_IMAGE_CONSTANT) {
                    if (resultCode == RESULT_OK) {

                        Bundle bundle = data.getExtras();
                        Bitmap certificateImage = (Bitmap) bundle.get("data");
                        ivCertificateImage.setImageBitmap(certificateImage);
                        this.certificateUriFromSelection = fileHelper.getImageUri(this, certificateImage);
                    }
                }
                break;

            case OPEN_GALLERY_CONSTANT:
                if (IMAGE_CODE == PROFILE_IMAGE_CONSTANT) {
                    if (resultCode == RESULT_OK) {
                        Uri imageUri = data.getData();
                        CropImage.activity(imageUri)
                                .setGuidelines(CropImageView.Guidelines.ON)
                                .setAspectRatio(1, 1)
                                .start(EditProfileRecorder.this);
                    }

                } else if (IMAGE_CODE == CERTIFICATE_IMAGE_CONSTANT) {
                    if (resultCode == RESULT_OK) {
                        Uri imageUri = data.getData();
                        CropImage.activity(imageUri)
                                .setGuidelines(CropImageView.Guidelines.ON)
                                .setAspectRatio(1, 1)
                                .start(EditProfileRecorder.this);
                    }
                }
                break;

            case CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE:
                if (IMAGE_CODE == PROFILE_IMAGE_CONSTANT) {
                    if (resultCode == RESULT_OK) {
                        CropImage.ActivityResult resultProfileImage = CropImage.getActivityResult(data);
                        Uri uriProfile = resultProfileImage.getUri();
                        this.profileUriFromUserSelection = uriProfile;
                        Glide.with(this).load(uriProfile).into(profileImageCiv);
                    }

                } else if (IMAGE_CODE == CERTIFICATE_IMAGE_CONSTANT) {
                    if (resultCode == RESULT_OK) {
                        CropImage.ActivityResult resultCertificate = CropImage.getActivityResult(data);
                        Uri uriCertificate = resultCertificate.getUri();
                        this.certificateUriFromSelection = uriCertificate;
                        Glide.with(this).load(uriCertificate).into(ivCertificateImage);
                    }
                }
                break;

            case PLACE_PICKER_REQUEST:
                if (resultCode == RESULT_OK) {
                    Place place = PlacePicker.getPlace(this, data);
                    String str = place.getAddress().toString();
                    this.selectedAddress = str;
                    tvCurrentAddress.setText(str);
                    AppUtils.log("Selected Address : " + str);
                }
                break;
        }
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


    // Getting Downloaded Bitmap Image from DownloadImageTask.
    @Override
    public void getImage(Bitmap bitmap, int requestCode) {
        if (requestCode == GET_PROFILE_IMAGE_REQUEST_CODE_FROM_DOWNLOAD_IMAGE_TASK) {
            // Get the Bitmap Image.
            if (bitmap != null) {
                AppUtils.log("Image is not null.");
                // Save to Storage.
                String certificateLocation = fileHelper.saveBitmapImageToStorage(bitmap, "profile_image_" + user.getUserId() + ".jpg");
                AppUtils.log("Certificate saved at location " + certificateLocation);
            } else {
                AppUtils.log("Image is null.");
            }
        }
        if (requestCode == GET_CERTIFICATE_IMAGE_REQUEST_CODE_FROM_DOWNLOAD_IMAGE_TASK) {
            // Get the Certificate.
            if (bitmap != null) {
                AppUtils.log("Certificate not is null");
                String location = fileHelper.saveBitmapImageToStorage(bitmap,
                        "certificate_image_" + user.getUserId() + ".jpg");
                AppUtils.log("Image Saved to External Storage at location : " + location);
            } else {
                AppUtils.log("Certificate is Null");
            }
        }
    }


    //// ----

    private void checkForStoragePermission(int certificateOrProfileCode) {

        this.profileOrCertificateCode = certificateOrProfileCode;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                //Show Information about why you need the permission
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Need Storage Permission");
                builder.setMessage("This app needs storage permission.");
                builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        ActivityCompat.requestPermissions(EditProfileRecorder.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, EXTERNAL_STORAGE_PERMISSION_CONSTANT);
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
                //Previously Permission Request was cancelled with 'Dont Ask Again',
                // Redirect to Settings after showing Information about why you need the permission
                AlertDialog.Builder builder = new AlertDialog.Builder(EditProfileRecorder.this);
                builder.setTitle("Need Storage Permission");
                builder.setMessage("This app needs storage permission.");
                builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        //sentToSettings = true;
                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", getPackageName(), null);
                        intent.setData(uri);
                        startActivityForResult(intent, REQUEST_PERMISSION_SETTING);
                        AppUtils.showToast(EditProfileRecorder.this, "Go to Permissions to Grant Storage");
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
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, EXTERNAL_STORAGE_PERMISSION_CONSTANT);
            }


            SharedPreferences.Editor editor = permissionStatus.edit();
            editor.putBoolean(Manifest.permission.WRITE_EXTERNAL_STORAGE, true);
            editor.commit();


        } else {
            //You already have the permission, just go ahead.
            selecteGalleryOrCamera(certificateOrProfileCode);
        }
    }//end of check for Storage permission.


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case EXTERNAL_STORAGE_PERMISSION_CONSTANT:

                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //The External Storage Write Permission is granted to you... Continue your left job...
                    selecteGalleryOrCamera(profileOrCertificateCode);
                } else {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        //Show Information about why you need the permission
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setTitle("Need Storage Permission");
                        builder.setMessage("This app needs storage permission");
                        builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                                ActivityCompat.requestPermissions(EditProfileRecorder.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, EXTERNAL_STORAGE_PERMISSION_CONSTANT);
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
                        AppUtils.showToast(EditProfileRecorder.this, "Unable to get Permission");
                    }
                }
                break;

            case LOCATION_PERMISSION_CONSTANT:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //The External Storage Write Permission is granted to you... Continue your left job...
                    displayPlacePicker();
                } else {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                        //Show Information about why you need the permission
                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
                        builder.setTitle("Need location Permission");
                        builder.setMessage("This app needs location permission");
                        builder.setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                                ActivityCompat.requestPermissions(EditProfileRecorder.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERMISSION_CONSTANT);
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
                        AppUtils.showToast(EditProfileRecorder.this, "Unable to get Permission");
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
                                ActivityCompat.requestPermissions(EditProfileRecorder.this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CONSTANT);
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
    }


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
                        ActivityCompat.requestPermissions(EditProfileRecorder.this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CONSTANT);
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

                        AppUtils.showToast(EditProfileRecorder.this, "Go to Permissions to Grant Camera");
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

}