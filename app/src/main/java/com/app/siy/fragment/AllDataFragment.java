package com.app.siy.fragment;


import android.os.Bundle;
import android.support.annotation.Nullable;
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
public class AllDataFragment extends Fragment {

    private String TAG = "GALLERY";
    private ArrayList<DataLibraryModel> allDataList = new ArrayList<>();
    private RecyclerView recyclerView;
    private GalleryAdapter adapter;

    public AllDataFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Bundle bundle = getArguments();

        if (bundle != null) {
            allDataList = bundle.getParcelableArrayList("ALL_DATA");
            AppUtils.log(TAG, "All Data List on All Data Fragment : " + allDataList);
        } else {
            AppUtils.log(TAG, "Bundle is null");
        }
        AppUtils.log("GALLERY", "ALL DATA FRAGMENT " + allDataList);
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.recorder_fragment_all_data, container, false);
        recyclerView = view.findViewById(R.id.rv_data_library_all);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        recyclerView.setHasFixedSize(true);
        if (allDataList != null) {
            adapter = new GalleryAdapter(getContext(), allDataList);
        }
        recyclerView.setAdapter(adapter);
        return view;
    }
}
