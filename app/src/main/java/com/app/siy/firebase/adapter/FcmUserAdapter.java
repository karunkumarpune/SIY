package com.app.siy.firebase.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.app.siy.R;
import com.app.siy.firebase.model.FcmUser;
import com.app.siy.firebase.model.Message;
import com.app.siy.firebase.utils.FirebaseUtils;
import com.app.siy.rest.ApiClient;
import com.app.siy.rest.User;
import com.app.siy.sharedpreferences.MyPreferences;
import com.app.siy.utils.AppUtils;
import com.bumptech.glide.Glide;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.R.id.message;

/**
 * Created by Manish-Pc on 10/12/2017.
 */

public class FcmUserAdapter extends RecyclerView.Adapter<FcmUserAdapter.MyViewHolder> {
    private String TAG = "CHAT";
    private List<FcmUser> fcmUserList;
    private Context context;
    private User user;
    private MyPreferences myPreferences;
    private FirebaseUtils firebaseUtils;

    //private ArrayList<String> arrayListOfImages;
    private List<Message> messageList;


    public FcmUserAdapter(Context context, List<FcmUser> fcmUserList, List<Message> messageList) {
        this.context = context;
        this.fcmUserList = fcmUserList;
        this.messageList = messageList;

        myPreferences = new MyPreferences(context);
        Gson gson = new Gson();
        String userModel = myPreferences.getString(MyPreferences.USER_MODEL);
        user = gson.fromJson(userModel, User.class);
        firebaseUtils = new FirebaseUtils(context);
        //arrayListOfImages = firebaseUtils.getAllProfileImages();
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.explorer_layout_chat_list_row_layout, parent, false);
        MyViewHolder myViewHolder = new MyViewHolder(view);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        FcmUser user = fcmUserList.get(position);
        AppUtils.log("CHAT", "Profile Image : " + user.getProfile_image());
        Glide.with(context).load(ApiClient.BASE_URL_UPLOADED_IMAGE + user.getProfile_image())
                .into(holder.profileImagePath);
        holder.chatUserName.setText(user.getFirst_name() + " " + user.getLast_name().charAt(0));

        // Set this From Firebase.
        String noOfNewMessages = user.getNumber_of_new_messages();
        if (noOfNewMessages == null) noOfNewMessages = "0";
        if (!noOfNewMessages.equals("0"))
            holder.numberOfNewMessages.setText("" + noOfNewMessages);
        else holder.numberOfNewMessages.setVisibility(View.GONE);
        if (messageList != null && messageList.size() > 0) {
            Message message = messageList.get(position);

            // Set this Last Seen from Firebase.
            holder.messageTime.setText(message.getTime_of_message());
            holder.lastMessage.setText("bye");

            // Set this Value from Firebase.
            boolean isMessageSeen = message.is_seen();
            if (isMessageSeen)
                holder.deliveredIcon.setImageResource(R.drawable.chat_read_icon);
            else holder.deliveredIcon.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return fcmUserList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public CircleImageView profileImagePath;
        public ImageView deliveredIcon;
        public TextView chatUserName, lastMessage, messageTime, numberOfNewMessages;
        public RelativeLayout viewBackground, viewForeground;

        public MyViewHolder(View view) {
            super(view);
            profileImagePath = (CircleImageView) view.findViewById(R.id.chat_iv_user_image);
            numberOfNewMessages = (TextView) view.findViewById(R.id.chat_number_of_new_messages);
            deliveredIcon = (ImageView) view.findViewById(R.id.chat_delivered_icon);
            chatUserName = (TextView) view.findViewById(R.id.chat_user_name);
            lastMessage = (TextView) view.findViewById(R.id.chat_message);
            messageTime = (TextView) view.findViewById(R.id.chat_message_time);
            viewBackground = view.findViewById(R.id.rl_view_background);
            viewForeground = view.findViewById(R.id.rl_view_foreground);
        }
    }


    //Create this method to implement Swipe and Delete
    public void removeItem(int position) {
        fcmUserList.remove(position);
        notifyItemRemoved(position);
    }

    //Create this method to implement Swipe and Delete.
    public void restoreItem(FcmUser userModel, int position) {
        fcmUserList.add(position, userModel);
        notifyItemInserted(position);
    }
}