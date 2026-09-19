package com.abania.dragmanager;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;

public class AlarmReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "medicine_reminder_channel";

    @Override
    public void onReceive(Context context, Intent intent) {
        String medicineName = intent.getStringExtra("medicine_name");
        String dosage = intent.getStringExtra("dosage");
        int scheduleId = intent.getIntExtra("schedule_id", -1);

        if (medicineName == null || dosage == null || scheduleId == -1) {
            return;
        }

        NotificationManager manager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // اطمینان از وجود کانال (برای اندروید 8+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            android.app.NotificationChannel channel = new android.app.NotificationChannel(
                    CHANNEL_ID,
                    "یادآوری مصرف دارو",
                    NotificationManager.IMPORTANCE_HIGH
            );
            manager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("⏰ زمان مصرف دارو")
                .setContentText("داروی " + medicineName + " (" + dosage + ") را مصرف کنید.")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(false)
                .addAction(new NotificationCompat.Action(
                        android.R.drawable.ic_menu_edit,
                        "✅ مصرف کردم",
                        NotificationHelper.getConfirmIntent(context, scheduleId)
                ))
                .addAction(new NotificationCompat.Action(
                        android.R.drawable.ic_menu_close_clear_cancel,
                        "⏹️ پایان دوره",
                        NotificationHelper.getStopIntent(context, scheduleId)
                ));

        if (manager != null) {
            manager.notify(scheduleId, builder.build());
        }
    }
}