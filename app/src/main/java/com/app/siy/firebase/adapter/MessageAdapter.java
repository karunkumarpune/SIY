package com.app.siy.firebase.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import com.app.siy.R;
import com.app.siy.firebase.model.Message;
import com.app.siy.utils.AppUtils;
import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

import static com.app.siy.utils.AppUtils.milliSecondToDateAndTime;

/**
 * Created by Manish-Pc on 14/12/2017.
 */

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    //private int currentLoggedInUserId = 89;

    // private int LEFT_MESSAGE = 10;
    // private int RIGHT_MESSAGE = 11;

    private String TAG = "ADAPTER";
    private List<Message> chatMessageList;
    private FirebaseAuth mAuth;
    String senderId;
    private Context context;
    //private MyPreferences myPreferences;

    public MessageAdapter(Context context, List<Message> chatMessageList) {
        this.context = context;
        //myPreferences = new MyPreferences(context);
        this.chatMessageList = chatMessageList;
        mAuth = FirebaseAuth.getInstance();
        senderId = mAuth.getCurrentUser().getUid();
        //Log.d("CHAT", "on Adapter " + chatMessageList.get(0).getMessage());

        AppUtils.log(TAG, "Sender Id : " + senderId);
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder " + viewType);
        // viewType = 2130968612
        // R.layout.chat_room_row_layout_right = 2130968612
        // R.layout.chat_room_row_layout_left = 2130968611
        Log.d(TAG, "R.layout.chat_room_row_layout_right " + R.layout.chat_room_row_layout_right);
        Log.d(TAG, "R.layout.chat_room_row_layout_left " + R.layout.chat_room_row_layout_left);
        if (viewType == R.layout.chat_room_row_layout_right) {
            View rightView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.chat_room_row_layout_right, parent, false);
            return new RightChatViewHolder(rightView);
        } else {
            View leftView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.chat_room_row_layout_left, parent, false);
            return new LeftChatViewHolder(leftView);
        }
        //    return null;
    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder");
        Message messageModel = chatMessageList.get(position);
        if (messageModel.getType().equals("text")) {
            if (getItemViewType(position) == R.layout.chat_room_row_layout_right) {
                RightChatViewHolder rightChatViewHolder = (RightChatViewHolder) holder;
                // Hide the Image View and VideoView
                rightChatViewHolder.ivRight.setVisibility(View.GONE);
                rightChatViewHolder.vvRight.setVisibility(View.GONE);

                // Display Message Text and Message Time.
                rightChatViewHolder.chatMessageRight.setVisibility(View.VISIBLE);
                rightChatViewHolder.chatMessageTimeRight.setVisibility(View.VISIBLE);


                rightChatViewHolder.chatMessageRight.setText(messageModel.getMessage());
                String dateAndTime = AppUtils.milliSecondToDateAndTime(Long.parseLong(messageModel.getTime_of_message()));
                AppUtils.log(TAG, dateAndTime);//Jan 26,2018 17:06
                String time = dateAndTime.substring(12);    // 17:06
                rightChatViewHolder.chatMessageTimeRight.setText(time);
            } else {//if (getItemViewType(position) == LEFT_MESSAGE) {
                LeftChatViewHolder leftChatViewHolder = (LeftChatViewHolder) holder;

                // Hide the ImageView and VideoView
                leftChatViewHolder.ivLeft.setVisibility(View.GONE);
                leftChatViewHolder.vvLeft.setVisibility(View.GONE);

                // Display the Text And Message Time.
                leftChatViewHolder.chatMessageLeft.setVisibility(View.VISIBLE);
                leftChatViewHolder.chatMessageTimeLeft.setVisibility(View.VISIBLE);

                leftChatViewHolder.chatMessageLeft.setText(messageModel.getMessage());
                String dateAndTime = AppUtils.milliSecondToDateAndTime(Long.parseLong(messageModel.getTime_of_message()));
                AppUtils.log(TAG, dateAndTime);//Jan 26,2018 17:06
                String time = dateAndTime.substring(12);    // 17:06
                leftChatViewHolder.chatMessageTimeLeft.setText(time);
            }
        } else if (messageModel.getType().equals("image")) {
            if (getItemViewType(position) == R.layout.chat_room_row_layout_right) {
                RightChatViewHolder rightChatViewHolder = (RightChatViewHolder) holder;

                // Hide the TextView and VideoView
                rightChatViewHolder.chatMessageRight.setVisibility(View.GONE);
                rightChatViewHolder.vvRight.setVisibility(View.GONE);

                // Show the ImageView and Time (TextView)
                rightChatViewHolder.ivRight.setVisibility(View.VISIBLE);
                rightChatViewHolder.chatMessageTimeRight.setVisibility(View.VISIBLE);

                String dateAndTime = AppUtils.milliSecondToDateAndTime(Long.parseLong(messageModel.getTime_of_message()));
                AppUtils.log(TAG, dateAndTime);//Jan 26,2018 17:06
                String time = dateAndTime.substring(12);    // 17:06
                rightChatViewHolder.chatMessageTimeRight.setText(time);

                Glide.with(context).load(messageModel.getMessage()).into(rightChatViewHolder.ivRight);

            } else {//if (getItemViewType(position) == LEFT_MESSAGE) {
                LeftChatViewHolder leftChatViewHolder = (LeftChatViewHolder) holder;

                // Hide the TextView and VideoView
                leftChatViewHolder.chatMessageLeft.setVisibility(View.GONE);
                leftChatViewHolder.vvLeft.setVisibility(View.GONE);

                // Show the ImageView and Time (TextView)
                leftChatViewHolder.ivLeft.setVisibility(View.VISIBLE);
                leftChatViewHolder.chatMessageTimeLeft.setVisibility(View.VISIBLE);

                String dateAndTime = AppUtils.milliSecondToDateAndTime(Long.parseLong(messageModel.getTime_of_message()));
                AppUtils.log(TAG, dateAndTime);//Jan 26,2018 17:06
                String time = dateAndTime.substring(12);    // 17:06
                leftChatViewHolder.chatMessageTimeLeft.setText(time);
                Glide.with(context).load(messageModel.getMessage()).into(leftChatViewHolder.ivLeft);
            }
        } else if (messageModel.getType().equals("video")) {
            if (getItemViewType(position) == R.layout.chat_room_row_layout_right) {
                RightChatViewHolder rightChatViewHolder = (RightChatViewHolder) holder;

                // Hide the TextView(Text Message) and ImageView
                rightChatViewHolder.chatMessageRight.setVisibility(View.GONE);
                rightChatViewHolder.ivRight.setVisibility(View.GONE);

                // Show the ImageView and Time (TextView)
                rightChatViewHolder.vvRight.setVisibility(View.VISIBLE);
                rightChatViewHolder.chatMessageTimeRight.setVisibility(View.VISIBLE);

                String dateAndTime = AppUtils.milliSecondToDateAndTime(Long.parseLong(messageModel.getTime_of_message()));
                AppUtils.log(TAG, dateAndTime);//Jan 26,2018 17:06
                String time = dateAndTime.substring(12);    // 17:06
                rightChatViewHolder.chatMessageTimeRight.setText(time);

                // Display Video To VideoView.
                rightChatViewHolder.vvRight.setVideoPath(messageModel.getMessage());

            } else {//if (getItemViewType(position) == LEFT_MESSAGE) {
                LeftChatViewHolder leftChatViewHolder = (LeftChatViewHolder) holder;

                // Hide the TextView(Text Message) and ImageView
                leftChatViewHolder.chatMessageLeft.setVisibility(View.GONE);
                leftChatViewHolder.ivLeft.setVisibility(View.GONE);

                // Show the ImageView and Time (TextView)
                leftChatViewHolder.vvLeft.setVisibility(View.VISIBLE);
                leftChatViewHolder.chatMessageTimeLeft.setVisibility(View.VISIBLE);

                String dateAndTime = AppUtils.milliSecondToDateAndTime(Long.parseLong(messageModel.getTime_of_message()));
                AppUtils.log(TAG, dateAndTime);//Jan 26,2018 17:06
                String time = dateAndTime.substring(12);    // 17:06
                leftChatViewHolder.chatMessageTimeLeft.setText(time);
                leftChatViewHolder.vvLeft.setVideoPath(messageModel.getMessage());
                //Glide.with(context).load(messageModel.getMessage()).into(leftChatViewHolder.ivLeft);
            }
        }
    }

    @Override
    public int getItemCount() {
        return chatMessageList.size();
    }


    //Returns the type of View - .i.e Left or Right.
    @Override
    public int getItemViewType(int position) {
        Message message = chatMessageList.get(position);
        //Log.d("CHAT", "Position : " + position);
        AppUtils.log(TAG, "Current User from Firebase : " + senderId);
        AppUtils.log(TAG, "Sender Id from Model : " + message.getSender_id());
        if (message.getSender_id().equals(senderId)) {
            return R.layout.chat_room_row_layout_right;
        } else {
            return R.layout.chat_room_row_layout_left;
        }
    }

    public class LeftChatViewHolder extends RecyclerView.ViewHolder {
        public TextView chatMessageLeft;
        public TextView chatMessageTimeLeft;
        public ImageView ivLeft;
        public VideoView vvLeft;

        public LeftChatViewHolder(View itemView) {
            super(itemView);
            AppUtils.log(TAG, "Left Chat");
            chatMessageLeft = itemView.findViewById(R.id.chat_room_tv_message_left);
            chatMessageTimeLeft = itemView.findViewById(R.id.chat_room_tv_message_time_left);
            ivLeft = itemView.findViewById(R.id.chat_room_image_left);
            vvLeft = itemView.findViewById(R.id.chat_room_video_left);
        }
    }

    public class RightChatViewHolder extends RecyclerView.ViewHolder {
        public TextView chatMessageRight;
        public TextView chatMessageTimeRight;
        public ImageView ivRight;
        public VideoView vvRight;

        public RightChatViewHolder(View itemView) {
            super(itemView);
            Log.d(TAG, "Right Chat");
            chatMessageRight = itemView.findViewById(R.id.chat_room_tv_message_right);
            chatMessageTimeRight = itemView.findViewById(R.id.chat_room_message_time_right);
            ivRight = itemView.findViewById(R.id.chat_room_image_right);
            vvRight = itemView.findViewById(R.id.chat_room_video_right);
        }
    }
}