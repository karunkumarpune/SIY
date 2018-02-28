package com.app.siy.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.app.siy.R;
import com.app.siy.activity.ImageCroppedActivity;
import com.app.siy.model.DataLibraryModel;
import com.app.siy.rest.ApiClient;
import com.app.siy.utils.AppUtils;
import com.bumptech.glide.Glide;

import java.util.ArrayList;

/**
 * Created by Manish-Pc on 18/01/2018.
 */

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.MyViewHolder> {


    private static final String TAG = "GALLERY";
    private static final int IMAGE_CROPPED_ACTIVITY = 89;
    ArrayList<DataLibraryModel> allImagesList;
    Context context;


    public GalleryAdapter(Context context, ArrayList<DataLibraryModel> allImagesList) {
        this.allImagesList = allImagesList;
        this.context = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_gallery_single_row, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        DataLibraryModel dataLibraryModel = allImagesList.get(position);
        AppUtils.log(TAG, "Single Image file : " + dataLibraryModel);
        int dataLibraryId = dataLibraryModel.getDataLibraryId();
        AppUtils.log(TAG, "Data Library Id : " + dataLibraryId);
        String completePathImage = ApiClient.BASE_URL_UPLOADED_IMAGE + dataLibraryModel.getData();
        int dataType = dataLibraryModel.getDataType();
        if (dataType == 1) {
            // Image Type.
            // Load Images from dataLibrary/image folder.
            holder.imageViewPlay.setVisibility(View.GONE);
            AppUtils.log(TAG, "Loading images from : " + completePathImage);
            Glide.with(context).load(completePathImage).centerCrop().into(holder.imageView);
        } else if (dataType == 2) {
            // Video Type
            // Load Video Thumbnail.
            String thumbnailPath = ApiClient.BASE_URL_UPLOADED_IMAGE + dataLibraryModel.getThumbnail();
            AppUtils.log(TAG, "Loading Thumbnail from : " + thumbnailPath);
            holder.imageViewPlay.setVisibility(View.VISIBLE);
            Glide.with(context).load(thumbnailPath).centerCrop().into(holder.imageView);
        }
        //holder.imageView.setImageBitmap(singleImageFile);
        //DataLibraryModel dataLibraryModel = allDataList.get(position);
        //String imageLocation = dataLibraryModel.getData();
        /*String img = ApiClient.BASE_URL_UPLOADED_IMAGE + imageLocation;
        AppUtils.log("COMPLETE IMAGE LOCATION : " + img);
        Glide.with(context).load(img).diskCacheStrategy(DiskCacheStrategy.ALL).centerCrop().crossFade().into(holder.imageView);*/
    }

    @Override
    public int getItemCount() {
        return allImagesList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageView;
        public ImageView imageViewPlay;

        public MyViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.iv_image);
            imageViewPlay = itemView.findViewById(R.id.iv_image_play_icon);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();
                    DataLibraryModel dataLibraryModel = allImagesList.get(position);
                    AppUtils.log(TAG, "Selected Image full screen : " + dataLibraryModel.getData());
                    int dataLibraryId = dataLibraryModel.getDataLibraryId();
                    AppUtils.log(TAG, "Selected Image DataLibrary Id : " + dataLibraryId);
                    Intent imageCroppedIntent = ((Activity) context).getIntent();
                    //Intent imageCroppedIntent = new Intent(context, ImageCroppedActivity.class);
                    imageCroppedIntent.setClass(context, ImageCroppedActivity.class);
                    //Intent imageCroppedActivity = new Intent(context, ImageCroppedActivity.class);
                    imageCroppedIntent.putExtra("DATA_LIBRARY_MODEL", dataLibraryModel);
                    ((Activity) context).startActivityForResult(imageCroppedIntent, IMAGE_CROPPED_ACTIVITY);
                    //imageCroppedActivity.putExtra("RECEIVER_ID", )
                    //context.startActivity(imageCroppedActivity);
                }
            });
        }
    }
}
