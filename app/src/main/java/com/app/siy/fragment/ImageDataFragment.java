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
public class ImageDataFragment extends Fragment {


    private String TAG = "GALLERY";
    private ArrayList<DataLibraryModel> allImagesList = new ArrayList<>();
    private RecyclerView recyclerView;
    private GalleryAdapter galleryAdapter;

    public ImageDataFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Bundle bundle = getArguments();
        if (bundle != null) {
            allImagesList = bundle.getParcelableArrayList("IMAGE_DATA");
            AppUtils.log(TAG, "All Data List on All Data Fragment : " + allImagesList);
        } else {
            AppUtils.log(TAG, "Bundle is empty at Image Data Fragment");
        }
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.recorder_fragment_image_data, container, false);
        recyclerView = view.findViewById(R.id.rv_data_library_images);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        recyclerView.setHasFixedSize(true);
        if (allImagesList != null) {
            galleryAdapter = new GalleryAdapter(getContext(), allImagesList);
        } else {
            AppUtils.log(TAG, "Image Data is Null");
        }
        recyclerView.setAdapter(galleryAdapter);
        return view;
    }
}
