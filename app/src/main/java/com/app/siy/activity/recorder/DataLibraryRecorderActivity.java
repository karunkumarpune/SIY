package com.app.siy.activity.recorder;

import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.app.siy.R;
import com.app.siy.adapter.DataLibraryAdapter;
import com.app.siy.fragment.AllDataFragment;
import com.app.siy.fragment.ImageDataFragment;
import com.app.siy.fragment.VideosDataFragment;
import com.app.siy.model.DataLibraryModel;
import com.app.siy.rest.ApiClient;
import com.app.siy.rest.ApiInterface;
import com.app.siy.rest.ServerResponseDataLibrary;
import com.app.siy.rest.User;
import com.app.siy.sharedpreferences.MyPreferences;
import com.app.siy.utils.AppUtils;
import com.app.siy.utils.Constants;
import com.app.siy.utils.Progress;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.media.CamcorderProfile.get;

public class DataLibraryRecorderActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "GALLERY";
    private RelativeLayout rootLayout;
    private Toolbar toolbar;
    private ImageView backImage;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private DataLibraryAdapter dataLibraryAdapter;
    //ArrayList<Bitmap> imageAndVideoArrayList;

    //Add tabs
    AllDataFragment allDataFragment = new AllDataFragment();
    ImageDataFragment imageDataFragment = new ImageDataFragment();
    VideosDataFragment videosDataFragment = new VideosDataFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_library);

        AppUtils.log("onCreate - Data Library");



        loadAllDataFromServer();
        //getAllImagesFromGivenFolder();

        toolbar = (Toolbar) findViewById(R.id.include_toolbar_data_library);
        TextView toolbarText = toolbar.findViewById(R.id.tool_bar_text);
        toolbarText.setText("Data Library");

        backImage = (ImageView) toolbar.findViewById(R.id.back_image_tool_bar);
        backImage.setOnClickListener(this);

        rootLayout = (RelativeLayout) findViewById(R.id.root_layout_data_library);
        tabLayout = (TabLayout) findViewById(R.id.tab_layout_data_library);

        viewPager = (ViewPager) findViewById(R.id.view_pager_data_library);
        dataLibraryAdapter = new DataLibraryAdapter(getSupportFragmentManager());


        dataLibraryAdapter.addTab(allDataFragment, "All");
        dataLibraryAdapter.addTab(imageDataFragment, "Images");
        dataLibraryAdapter.addTab(videosDataFragment, "Videos");
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 89) {
            if (resultCode == RESULT_OK) {
                setResult(RESULT_OK);
                finish();
            }
        }
    }

    private void loadAllDataFromServer() {
        AppUtils.log(TAG, "Getting all images from Server ... ");
        if (AppUtils.isNetworkAvailable(this)) {
            final Progress progress = new Progress(this, R.style.CustomProgressDialogTheme);
            progress.setCancelable(false);
            progress.show();

            MyPreferences myPreferences = new MyPreferences(this);
            String userString = myPreferences.getString(MyPreferences.USER_MODEL);
            Gson gson = new Gson();
            User user = gson.fromJson(userString, User.class);
            String accessToken = user.getAccessToken();
            AppUtils.log("Access Token : Data Library : " + accessToken);
            ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
            Call<ServerResponseDataLibrary> call = apiInterface.getAllDataLibraryFromServer(accessToken);
            call.enqueue(new Callback<ServerResponseDataLibrary>() {
                @Override
                public void onResponse(Call<ServerResponseDataLibrary> call,
                                       Response<ServerResponseDataLibrary> response) {
                    progress.dismiss();
                    if (response.isSuccessful()) {
                        AppUtils.log(TAG, "All Data retrieved successfully !!");
                        if (response.body() != null) {
                            ArrayList<DataLibraryModel> dataLibraryModelArrayList = response.body().dataLibraryModel;

                            Bundle bundle = new Bundle();
                            // Add All Images/Videos to the AllDataFragment.
                            bundle.putParcelableArrayList("ALL_DATA", dataLibraryModelArrayList);
                            AppUtils.log(TAG, "Data Library Model Array List : " + dataLibraryModelArrayList);
                            allDataFragment.setArguments(bundle);


                            // Create Data Library Model List for all Images.
                            ArrayList<DataLibraryModel> dataLibraryModelImage = new ArrayList<DataLibraryModel>();
                            for (DataLibraryModel dm : dataLibraryModelArrayList) {
                                if (dm.getDataType() == 1) {
                                    dataLibraryModelImage.add(dm);
                                }
                            }
                            Bundle bundleImage = new Bundle();
                            bundleImage.putParcelableArrayList("IMAGE_DATA", dataLibraryModelImage);
                            imageDataFragment.setArguments(bundleImage);

                            // Create Data Library Model List for all Videos.
                            ArrayList<DataLibraryModel> dataLibraryModelVideos = new ArrayList<DataLibraryModel>();
                            for (DataLibraryModel dm : dataLibraryModelArrayList) {
                                if (dm.getDataType() == 2) {
                                    dataLibraryModelVideos.add(dm);
                                }
                            }
                            Bundle bundleVideo = new Bundle();
                            bundleVideo.putParcelableArrayList("VIDEO_DATA", dataLibraryModelVideos);
                            videosDataFragment.setArguments(bundleVideo);


                            viewPager.setAdapter(dataLibraryAdapter);
                            tabLayout.setupWithViewPager(viewPager);
                            for (int i = 0; i < dataLibraryModelArrayList.size(); i++) {
                                DataLibraryModel dataLibraryModel = dataLibraryModelArrayList.get(i);
                                String data = dataLibraryModel.getData();
                                int dataLibraryId = dataLibraryModel.getDataLibraryId();
                                int dataType = dataLibraryModel.getDataType();
                                int userId = dataLibraryModel.getUserId();
                                AppUtils.log(TAG, "Data : " + data);
                                AppUtils.log(TAG, "Data Library Id : " + dataLibraryId);
                                AppUtils.log(TAG, "Data Tpye : " + dataType);
                                AppUtils.log(TAG, "User Id : " + userId);
                            }
                        } else {
                            AppUtils.showToast(DataLibraryRecorderActivity.this, "No Data");
                        }
                    } else {
                        try {
                            AppUtils.log(TAG, "Some Error Occurs !" + response.errorBody().string());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onFailure(Call<ServerResponseDataLibrary> call, Throwable t) {
                    progress.dismiss();
                    AppUtils.log(TAG, "onFailure : " + t.getMessage());
                }
            });
        } else {
            AppUtils.log(Constants.NO_INTERNET_CONNECTION);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back_image_tool_bar:
                onBackPressed();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
    }
}