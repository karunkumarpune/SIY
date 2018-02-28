package com.app.siy.activity.explorer;

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

public class DataLibraryExplorerActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "GALLERY";
    RelativeLayout rootLayout;
    private Toolbar toolbar;
    private ImageView backImage;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private DataLibraryAdapter dataLibraryAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_library);

        AppUtils.log("onCreate - Data Library");

        toolbar = (Toolbar) findViewById(R.id.include_toolbar_data_library);
        TextView toolbarText = toolbar.findViewById(R.id.tool_bar_text);
        toolbarText.setText("Data Library");

        backImage = (ImageView) toolbar.findViewById(R.id.back_image_tool_bar);
        backImage.setOnClickListener(this);

        rootLayout = (RelativeLayout) findViewById(R.id.root_layout_data_library);
        tabLayout = (TabLayout) findViewById(R.id.tab_layout_data_library);

        viewPager = (ViewPager) findViewById(R.id.view_pager_data_library);
        dataLibraryAdapter = new DataLibraryAdapter(getSupportFragmentManager());

        /*//Add tabs
        AllDataFragment allFragment = new AllDataFragment();
        dataLibraryAdapter.addTab(allFragment, "All");
        dataLibraryAdapter.addTab(new ImageDataFragment(), "Images");
        dataLibraryAdapter.addTab(new VideosDataFragment(), "Videos");

        viewPager.setAdapter(dataLibraryAdapter);
        tabLayout.setupWithViewPager(viewPager);*/

        loadAllDataFromServer();

    }


    private void loadAllDataFromServer() {
        if (AppUtils.isNetworkAvailable(this)) {
            final Progress progress = new Progress(this, R.style.CustomProgressDialogTheme);
            //progress.setCancelable(false);
            progress.show();

            MyPreferences myPreferences = new MyPreferences(this);
            String userString = myPreferences.getString(MyPreferences.USER_MODEL);
            Gson gson = new Gson();
            User user = gson.fromJson(userString, User.class);
            String accessToken = user.getAccessToken();
            AppUtils.log(TAG, "Access Token : Data Library : " + accessToken);
            ApiInterface apiInterface = ApiClient.getClient().create(ApiInterface.class);
            Call<ServerResponseDataLibrary> call = apiInterface.getAllDataLibraryFromServer(accessToken);
            call.enqueue(new Callback<ServerResponseDataLibrary>() {
                @Override
                public void onResponse(Call<ServerResponseDataLibrary> call,
                                       Response<ServerResponseDataLibrary> response) {
                    progress.dismiss();
                    if (response.isSuccessful()) {
                        ServerResponseDataLibrary serverResponseDataLibrary = response.body();
                        if (serverResponseDataLibrary != null) {
                            ArrayList<DataLibraryModel> dataLibraryList = response.body().dataLibraryModel;
                            AppUtils.log(TAG, "Data Library List " + dataLibraryList);

                            Bundle bundle = new Bundle();
                            bundle.putParcelableArrayList("ALL_USERS", dataLibraryList);
                            //Add tabs
                            AllDataFragment allDataFragment = new AllDataFragment();
                            allDataFragment.setArguments(bundle);
                            dataLibraryAdapter.addTab(allDataFragment, "All");
                            dataLibraryAdapter.addTab(new ImageDataFragment(), "Images");
                            dataLibraryAdapter.addTab(new VideosDataFragment(), "Videos");

                            viewPager.setAdapter(dataLibraryAdapter);
                            tabLayout.setupWithViewPager(viewPager);

                            String jsonString = new Gson().toJson(dataLibraryList);
                            AppUtils.log(TAG, "Json String : " + jsonString);
                        } else {
                            AppUtils.showToast(DataLibraryExplorerActivity.this, "No Data");
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