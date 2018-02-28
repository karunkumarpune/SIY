package com.app.siy.firebase.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.NotificationCompat;

import com.app.siy.R;
import com.app.siy.activity.explorer.BookingConfirmation;
import com.app.siy.activity.recorder.HiredActivity;
import com.app.siy.utils.AppUtils;
import com.app.siy.utils.Constants;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

import retrofit2.Retrofit;

import static android.R.id.message;
import static com.app.siy.R.drawable.notification;

/**
 * Created by Manish-Pc on 08/01/2018.
 */

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    public static final int NOTIFICATION_ID_HIRE_RECORDER = 10;
    public static final int NOTIFICATION_ID_ACCEPT_REQUEST_BY_RECORDER = 11;
    public static final int NOTIFICATION_ID_ACCEPT_REQUEST_BY_EXPLORER = 12;

    private final String TAG = getClass().getSimpleName();

    private static final int requestCode = 20;


    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        if (remoteMessage != null) {
            AppUtils.log(TAG, "Data : " + remoteMessage.getData());

            /*AppUtils.log("From : " + remoteMessage.getFrom());
            AppUtils.log("Notification Body : " + remoteMessage.getNotification());*/

            Map<String, String> notificationData = remoteMessage.getData();
            String notificationType = notificationData.get("notification_type");
            String userId = notificationData.get("user_id");
            String message = notificationData.get("message");

            AppUtils.log(TAG, "Notification Type : " + notificationType);
            AppUtils.log(TAG, "User Id : " + userId);
            AppUtils.log(TAG, "Message : " + message);

            if (notificationType.equals("HireRecorder")) {
                // Notification on Recorder Panel.
                // This Notification comes when user searches a recorder.
                // Nearby Recorder will get this Notification.

                NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
                builder.setContentTitle("SIY");
                builder.setContentText(message);
                builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
                builder.setSmallIcon(R.drawable.notification);
                builder.setAutoCancel(false);

                Intent resultIntent = new Intent(this, HiredActivity.class);
                resultIntent.putExtra("USER_ID_EXPLORER", userId);
                resultIntent.putExtra("NOTIFICATION_ID", NOTIFICATION_ID_HIRE_RECORDER);
                TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(this);
                taskStackBuilder.addParentStack(HiredActivity.class);
                taskStackBuilder.addNextIntent(resultIntent);

                //getPendingIntent(int requestCode, int flags)
                PendingIntent pendingIntent = taskStackBuilder.getPendingIntent(requestCode, PendingIntent.FLAG_UPDATE_CURRENT);
                builder.setContentIntent(pendingIntent);

                Notification notification = builder.build();
                NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                manager.notify(NOTIFICATION_ID_HIRE_RECORDER, notification);

            } else if (notificationType.equals("acceptRequestByrecorder")) {
                // Notification on Explorer Panel
                // In Explorer Panel. When Recorder Accepts the Job
                // then this Notification will be on Explorer Side.

                NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
                builder.setContentTitle("SIY");
                builder.setContentText(message);
                builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
                builder.setSmallIcon(R.drawable.notification);
                builder.setAutoCancel(false);

                Intent resultIntent = new Intent(this, BookingConfirmation.class);
                resultIntent.putExtra("USER_ID_RECORDER", userId);
                resultIntent.putExtra("NOTIFICATION_ID", NOTIFICATION_ID_ACCEPT_REQUEST_BY_RECORDER);
                TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(this);
                taskStackBuilder.addParentStack(BookingConfirmation.class);
                taskStackBuilder.addNextIntent(resultIntent);

                //getPendingIntent(int requestCode, int flags)
                PendingIntent pendingIntent = taskStackBuilder.getPendingIntent(requestCode,
                        PendingIntent.FLAG_UPDATE_CURRENT);
                builder.setContentIntent(pendingIntent);

                Notification notification = builder.build();
                NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                manager.notify(NOTIFICATION_ID_ACCEPT_REQUEST_BY_RECORDER, notification);
            } else if (notificationType.equals("acceptRequestByExplorer")) {
                // Notification on Recorder Panel.
                // When Recorder Accepts the Job then Recorder will get this Notification.

                NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
                builder.setContentTitle("SIY");
                builder.setContentText(message);
                builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
                builder.setSmallIcon(R.drawable.notification);
                builder.setAutoCancel(false);

                Intent resultIntent = new Intent(this, HiredActivity.class);
                resultIntent.putExtra("USER_ID_EXPLORER", userId);
                resultIntent.putExtra("NOTIFICATION_ID", NOTIFICATION_ID_ACCEPT_REQUEST_BY_EXPLORER);

                TaskStackBuilder taskStackBuilder = TaskStackBuilder.create(this);
                taskStackBuilder.addParentStack(HiredActivity.class);
                taskStackBuilder.addNextIntent(resultIntent);

                //getPendingIntent(int requestCode, int flags)
                PendingIntent pendingIntent = taskStackBuilder.getPendingIntent(requestCode,
                        PendingIntent.FLAG_UPDATE_CURRENT);
                builder.setContentIntent(pendingIntent);

                Notification notification = builder.build();
                NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                manager.notify(NOTIFICATION_ID_ACCEPT_REQUEST_BY_EXPLORER, notification);
            }

            AppUtils.log(TAG, "Notification Type : " + notificationType);
            AppUtils.log(TAG, "User Id : " + userId);
            AppUtils.log(TAG, "Message : " + message);
        }
    }

    @Override
    public void onMessageSent(String s) {
        super.onMessageSent(s);
    }

    @Override
    public void onDeletedMessages() {
        super.onDeletedMessages();
    }
}
