package com.codegama.todolistapplication.broadcastReceiver;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.legacy.content.WakefulBroadcastReceiver;

import com.codegama.todolistapplication.R;
import com.codegama.todolistapplication.WakeLocker;
import com.codegama.todolistapplication.activity.AlarmActivity;

import java.util.Objects;

public class AlarmBroadcastReceiver extends BroadcastReceiver {
    public static final String TAG_NOTIFICATION = "NOTIFICATION_MESSAGE";
    public static final String CHANNEL_ID = "channel_1111";
    private static final String TAG = "Receiver";

    String title, desc, date, time,period,importance;
    int req_id;
    @Override
    public void onReceive(Context context, Intent intent) {

        WakeLocker.acquire(context);

        req_id = intent.getIntExtra("ID",0);
        title = intent.getStringExtra("TITLE");
        desc = intent.getStringExtra("DESC");
        date = intent.getStringExtra("DATE");
        time = intent.getStringExtra("TIME");
        period = intent.getStringExtra("PER");
        importance = intent.getStringExtra("IMP");

        //((Activity) context). getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
//        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
//            // Set the alarm here.
//            Toast.makeText(context, "Alarm just rang...", Toast.LENGTH_SHORT).show();
//        }
/*
        NotificationCompat.Builder notification = new NotificationCompat.Builder(context, "123")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("Name")
                .setContentText("Name")
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
        notificationManagerCompat.notify(200, notification.build());
 */
//        Toast.makeText(context, "Broadcast receiver called", Toast.LENGTH_SHORT).show();

        // If android 10 or higher
        //if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {startActivityNotification(context,req_id,title,desc);}

        Intent i = new Intent(context, AlarmActivity.class);
        i.putExtra("ID", req_id);
        i.putExtra("TITLE", title);
        i.putExtra("DESC", desc);
        i.putExtra("DATE", date);
        i.putExtra("TIME", time);
        i.putExtra("PER", period);
        i.putExtra("IMP", importance);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
    }

    // notification method to support opening activities on Android 10
    public static void startActivityNotification (Context context, int notificationID,String title, String message) {
        //TODO: resource: https://stackoverflow.com/questions/57833208/cant-start-activity-from-broadcastreceiver-on-android-10

        NotificationManager mNotificationManager =
                (NotificationManager)
                        context.getSystemService(Context.NOTIFICATION_SERVICE);
        //Create GPSNotification builder
        NotificationCompat.Builder mBuilder;


        mBuilder = new NotificationCompat . Builder (context)
                .setSmallIcon(R.drawable.calendar)
                .setContentTitle(title)
                .setContentText(message)
                .setColor(context.getResources().getColor(R.color.colorPrimaryDark))
                .setAutoCancel(true)
                //.setContentIntent(ContentPendingIntent)
                .setDefaults(Notification.DEFAULT_LIGHTS | Notification . DEFAULT_VIBRATE)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Activity Opening Notification",
                    NotificationManager.IMPORTANCE_HIGH
            );
            mChannel.enableLights(true);
            mChannel.enableVibration(true);
            mChannel.setDescription("Activity opening notification");

            mBuilder.setChannelId(CHANNEL_ID);

            Objects.requireNonNull(mNotificationManager).createNotificationChannel(mChannel);
        }
        Objects.requireNonNull(mNotificationManager).notify(
                TAG_NOTIFICATION, notificationID,
                mBuilder.build()
        );
    }
}
