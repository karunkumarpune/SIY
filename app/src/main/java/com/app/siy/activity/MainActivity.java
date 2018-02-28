package com.app.siy.activity;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.app.siy.R;
import com.app.siy.sharedpreferences.MyPreferences;
import com.app.siy.utils.AppUtils;
import com.app.siy.utils.Constants;
import com.google.firebase.iid.FirebaseInstanceId;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_PERMISSION_SETTING = Constants.PERMISSION_CONSTANT_SETTINGS;
    private static final int LOCATION_PERMISSION_CONSTANT = Constants.PERMISSION_CONSTANT_LOCATION;
    //private boolean sentToSettings = false;
    private SharedPreferences permissionStatus;

    private RelativeLayout relativeLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        permissionStatus = getSharedPreferences(Constants.PERMISSION_STATUS, MODE_PRIVATE);

        Button btnExplorer = (Button) findViewById(R.id.btn_explorer_main);
        Button btnRecorder = (Button) findViewById(R.id.btn_recorder_main);
        relativeLayout = (RelativeLayout) findViewById(R.id.relative_layout_main_activity);

        //check for location permission.
        checkForLocationPermission();

        btnExplorer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent explorerIntent = new Intent(MainActivity.this, SignupActivity.class);
                explorerIntent.putExtra(Constants.USER_TYPE, Constants.USER_TYPE_EXPLORER);
                startActivity(explorerIntent);
                overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
            }
        });

        btnRecorder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent recorderIntent = new Intent(MainActivity.this, SignupActivity.class);
                recorderIntent.putExtra(Constants.USER_TYPE, Constants.USER_TYPE_RECORDER);
                startActivity(recorderIntent);
                overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
            }
        });

        // Getting the Device Token
        String deviceToken = FirebaseInstanceId.getInstance().getToken();
        AppUtils.log("Device Token on MainActivity : " + deviceToken);
        // Add Device Token to Preferences.
        // This will be used when user clicks on Logout then control goes to MainActivity.
        // If user again login from here then we require Device Token.
        new MyPreferences(this).saveString(MyPreferences.DEVICE_TOKEN, deviceToken);
    }


    private void checkForLocationPermission() {
        if (ActivityCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.ACCESS_COARSE_LOCATION)) {
                //Show Information about why you need the permission
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle(Constants.PERMISSION_LOCATION_TITLE);
                builder.setMessage(Constants.PERMISSION_LOCATION_DESCRIPTION);
                builder.setPositiveButton(Constants.PERMISSION_GRANT, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                                LOCATION_PERMISSION_CONSTANT);
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
                //Previously Permission Request was cancelled with 'Don't Ask Again',
                // Redirect to Settings after showing Information about why you need the permission
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
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
                        //AppUtils.showToast(MainActivity.this, "Go to Permissions to Grant Storage");
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
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        LOCATION_PERMISSION_CONSTANT);
            }

            SharedPreferences.Editor editor = permissionStatus.edit();
            editor.putBoolean(Manifest.permission.ACCESS_COARSE_LOCATION, true);
            //editor.commit();
            editor.apply();

        } else {
            //You already have the permission, just go ahead.
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case LOCATION_PERMISSION_CONSTANT:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //The External Storage Write Permission is granted to you
                    // Continue your left job
                } else {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                            Manifest.permission.ACCESS_COARSE_LOCATION)) {
                        //Show Information about why you need the permission
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setTitle(Constants.PERMISSION_LOCATION_TITLE);
                        builder.setMessage(Constants.PERMISSION_LOCATION_DESCRIPTION);
                        builder.setPositiveButton(Constants.PERMISSION_GRANT, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                                ActivityCompat.requestPermissions(MainActivity.this,
                                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                                        LOCATION_PERMISSION_CONSTANT);
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
                        AppUtils.showToast(this, Constants.PERMISSION_UNABLE_TO_GET_PERMISSION);
                    }
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_PERMISSION_SETTING:
                if (ActivityCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    //Got Permission
                    //
                }
                break;
        }
    }

    //Press Once Again to Exit.
    boolean doubleBackToExitPressedOnce = false;

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        AppUtils.snackbar(this, relativeLayout, Constants.PRESS_ONCE_AGAIN_TO_EXIT);
        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 1000);
    }


}