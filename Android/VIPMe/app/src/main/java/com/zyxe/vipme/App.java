package com.zyxe.vipme;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;

public class App extends Application {
    public static final String CHANNEL_ID = "Sentinel";
    public static final String CHANNEL_ID_1 = "channel1";
    public static final String CHANNEL_ID_2 = "channel2";
    public static final String CHANNEL_ID_4 = "channel4";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannels();
    }

    private void createNotificationChannels() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Sentinel Security",
                    NotificationManager.IMPORTANCE_HIGH
            );

            NotificationChannel channel1 = new NotificationChannel(
                    CHANNEL_ID_1,
                    "Channel 1",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel1.setDescription("This is channel 1 - Alarm");

            NotificationChannel channel2 = new NotificationChannel(
                    CHANNEL_ID_2,
                    "Channel 2",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel2.setDescription("This is channel 2 - GPS");

            NotificationChannel channel4 = new NotificationChannel(
                    CHANNEL_ID_4,
                    "Channel 4",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel4.setDescription("This is channel 4 - Error");


            NotificationManager manager = getSystemService(NotificationManager.class);

            manager.createNotificationChannel(channel1);
            manager.createNotificationChannel(channel2);
            manager.createNotificationChannel(channel4);
            manager.createNotificationChannel(serviceChannel);
        }
    }
}