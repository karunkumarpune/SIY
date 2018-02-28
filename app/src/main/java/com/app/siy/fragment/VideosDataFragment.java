package com.app.siy.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.app.siy.R;
import com.app.siy.adapter.GalleryAdapter;
import com.app.siy.model.DataLibraryModel;
import com.app.siy.utils.AppUtils;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class VideosDataFragment extends Fragment {


    private static final String TAG = "GALLERY";
    private ArrayList<DataLibraryModel> allVideoList = new ArrayList<>();
    private RecyclerView recyclerViewVideos;
    private GalleryAdapter galleryAdapterVideos;

    public VideosDataFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Bundle bundleVideos = getArguments();
        if (bundleVideos != null) {
            allVideoList = bundleVideos.getParcelableArrayList("VIDEO_DATA");
        } else {
            AppUtils.log(TAG, "Bundle is null.");
        }
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.recorder_fragment_videos_data, container, false);
        recyclerViewVideos = view.findViewById(R.id.rv_data_library_videos);
        recyclerViewVideos.setLayoutManager(new GridLayoutManager(getContext(), 2));
        recyclerViewVideos.setHasFixedSize(true);

        if (allVideoList != null) {
            galleryAdapterVideos = new GalleryAdapter(getContext(), allVideoList);
        }
        recyclerViewVideos.setAdapter(galleryAdapterVideos);
        return view;
    }

}
