package com.app.siy.adapter;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import com.app.siy.R;
import com.app.siy.rest.NotificationModel;
import com.app.siy.rest.User;

import org.w3c.dom.Text;

/**
 * Created by Manish-Pc on 13/12/2017.
 */

public class NotificationAdapter extends ArrayAdapter {

    List<NotificationModel> notificationModelList;
    User userExplorer;
    Context context;
    int resource;


    public NotificationAdapter(@NonNull Context context, @LayoutRes int resource,
                               List<NotificationModel> notificationList, User userExplorer) {
        super(context, resource, notificationList);
        this.context = context;
        this.resource = resource;
        this.notificationModelList = notificationList;
        this.userExplorer = userExplorer;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        NotificationModel notificationData = notificationModelList.get(position);

        View view = LayoutInflater.from(context).inflate(resource, parent, false);
        TextView tvExplorerName = view.findViewById(R.id.notification_header);
        TextView content = view.findViewById(R.id.notification_content);
        TextView time = view.findViewById(R.id.notification_time);
        tvExplorerName.setText(userExplorer.getFirstName());
        content.setText(notificationData.getMessage());
        time.setText(notificationData.getUpdatedTime().substring(10, 16));
        return view;
    }

    @Nullable
    @Override
    public Object getItem(int position) {
        return notificationModelList.size();
    }
}
