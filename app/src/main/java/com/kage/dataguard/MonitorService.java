package com.kage.dataguard;

import android.app.*;
import android.content.Context;
import android.content.Intent;
import android.os.*;
import java.util.Calendar;

public class MonitorService extends Service {
    private static final String CHANNEL = "kage_guard_monitor";
    private static final int ID = 4101;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable check = new Runnable() {
        @Override public void run() {
            long bytes = MainActivity.readTodayBytes(MonitorService.this);
            long limit = getSharedPreferences("limits", MODE_PRIVATE).getLong("budget", 500) * 1048576L;
            if (bytes >= 0 && bytes >= limit) {
                Notification n = new Notification.Builder(MonitorService.this, CHANNEL)
                    .setSmallIcon(android.R.drawable.stat_notify_error)
                    .setContentTitle("KAGE DATA GUARD: daily budget reached")
                    .setContentText("Mobile data has reached your configured budget. Open system data settings.")
                    .setContentIntent(PendingIntent.getActivity(MonitorService.this, 1,
                        new Intent("android.settings.DATA_USAGE_SETTINGS"), PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE))
                    .setAutoCancel(true).build();
                ((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).notify(ID + 1, n);
            }
            handler.postDelayed(this, 15 * 60 * 1000L);
        }
    };
    @Override public void onCreate() {
        super.onCreate();
        NotificationManager nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        nm.createNotificationChannel(new NotificationChannel(CHANNEL, "KAGE monitoring", NotificationManager.IMPORTANCE_LOW));
        startForeground(ID, new Notification.Builder(this, CHANNEL).setSmallIcon(android.R.drawable.stat_notify_sync)
            .setContentTitle("KAGE DATA GUARD").setContentText("Mobile data monitoring is active").build());
        handler.post(check);
    }
    @Override public int onStartCommand(Intent i, int flags, int id) { return START_STICKY; }
    @Override public void onDestroy() { handler.removeCallbacks(check); super.onDestroy(); }
    @Override public IBinder onBind(Intent i) { return null; }
}
