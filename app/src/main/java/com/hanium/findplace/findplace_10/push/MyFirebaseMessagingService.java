package com.hanium.findplace.findplace_10.push;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.hanium.findplace.findplace_10.MainActivity;
import com.hanium.findplace.findplace_10.R;

import java.util.Iterator;
import java.util.List;


public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        if (remoteMessage.getData().size() > 0) {
            String body = remoteMessage.getData().get("body").toString();
            String title = remoteMessage.getData().get("title").toString();
            sendNotification(title, body);
        }

    }

    private void sendNotification(String title, String body) {

        if(!checkOnTop(this)){
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                    PendingIntent.FLAG_ONE_SHOT);

            String channelId = getString(R.string.default_notification_channel_id);
            Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            NotificationCompat.Builder notificationBuilder =
                    new NotificationCompat.Builder(this, channelId)
                            .setSmallIcon(R.drawable.logo)
                            .setContentTitle(title)
                            .setContentText(body)
                            .setAutoCancel(true)
                            .setPriority(NotificationCompat.PRIORITY_MAX)
                            .setDefaults(Notification.DEFAULT_ALL)
                            .setContentIntent(pendingIntent);

            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            // Since android Oreo notification channel is needed.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(channelId,
                        "Channel human readable title",
                        NotificationManager.IMPORTANCE_DEFAULT);
                notificationManager.createNotificationChannel(channel);

                channel.enableLights(true);
                channel.setLightColor(Color.GREEN);
                channel.enableVibration(true);
                channel.setVibrationPattern(new long[]{100, 100, 100, 100});
                notificationManager.createNotificationChannel(channel);

            }
            notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
        }

    }

    //현재 최상위 액티비티 및 백그라운드 상태 확인.
    public boolean checkOnTop(Context context){

        boolean ret = false;

        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        List<ActivityManager.RunningTaskInfo> info = manager.getRunningTasks(1);

        ComponentName cn = info.get(0).topActivity;

        String packageName = cn.getPackageName();
        String activityName = cn.getShortClassName().substring(1);
        Log.d("MyLog----------", "현재 엑티비티 : "+activityName);
        if(activityName.equals("ChatActivity") && packageName.equals(context.getPackageName()) && pm.isScreenOn()){
            ret = true;
        }

        return ret;
    }

}