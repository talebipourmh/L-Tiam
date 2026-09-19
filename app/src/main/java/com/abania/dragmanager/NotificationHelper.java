package com.abania.dragmanager;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import androidx.core.app.NotificationCompat;

public class NotificationHelper {

    // کانال‌های مختلف برای انواع نوتیفیکیشن
    private static final String CHANNEL_ID_REMINDER = "medicine_reminder_channel";
    private static final String CHANNEL_ID_DONATION = "donation_channel";
    private static final String CHANNEL_NAME_REMINDER = "یادآوری دارو";
    private static final String CHANNEL_NAME_DONATION = "درخواست‌های اهدا";
    private static final String CHANNEL_DESC_REMINDER = "نوتیفیکیشن‌های مربوط به یادآوری مصرف دارو";
    private static final String CHANNEL_DESC_DONATION = "نوتیفیکیشن‌های مربوط به درخواست‌های اهدا";

    // =============== متدهای مربوط به یادآوری دارو ===============

    // ایجاد Intent برای دکمه "مصرف کردم"
    public static PendingIntent getConfirmIntent(Context context, int scheduleId) {
        Intent intent = new Intent(context, NotificationActionReceiver.class);
        intent.setAction("CONFIRM_MEDICATION");
        intent.putExtra("schedule_id", scheduleId);
        intent.putExtra("action_type", "confirm");
        return PendingIntent.getBroadcast(context, scheduleId, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    // ایجاد Intent برای دکمه "پایان دوره"
    public static PendingIntent getStopIntent(Context context, int scheduleId) {
        Intent intent = new Intent(context, NotificationActionReceiver.class);
        intent.setAction("STOP_MEDICATION");
        intent.putExtra("schedule_id", scheduleId);
        intent.putExtra("action_type", "stop");
        return PendingIntent.getBroadcast(context, scheduleId + 1000, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    // نمایش نوتیفیکیشن یادآوری دارو (با دکمه‌های اقدام)
    public static void showReminderNotification(Context context, String title, String message, int scheduleId) {
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // ایجاد کانال برای اندروید 8+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID_REMINDER,
                    CHANNEL_NAME_REMINDER,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription(CHANNEL_DESC_REMINDER);
            channel.enableVibration(true);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID_REMINDER)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(false)
                .addAction(new NotificationCompat.Action(
                        android.R.drawable.ic_menu_edit,
                        "✅ مصرف کردم",
                        getConfirmIntent(context, scheduleId)
                ))
                .addAction(new NotificationCompat.Action(
                        android.R.drawable.ic_menu_close_clear_cancel,
                        "⏹️ پایان دوره",
                        getStopIntent(context, scheduleId)
                ));

        if (notificationManager != null) {
            notificationManager.notify(scheduleId, builder.build());
        }
    }

    // =============== متدهای مربوط به درخواست اهدا ===============

    // نمایش نوتیفیکیشن درخواست اهدا (با Intent به MessagesActivity)
    public static void showDonationNotification(Context context, String title, String message, int notificationId) {
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // ایجاد کانال برای اندروید 8+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID_DONATION,
                    CHANNEL_NAME_DONATION,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription(CHANNEL_DESC_DONATION);
            channel.enableVibration(true);
            notificationManager.createNotificationChannel(channel);
        }

        // Intent برای باز کردن صفحه پیام‌ها
        Intent intent = new Intent(context, MessagesActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // ساخت نوتیفیکیشن
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID_DONATION)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        notificationManager.notify(notificationId, builder.build());
    }

    // نمایش نوتیفیکیشن درخواست اهدا با آیکون سفارشی
    public static void showDonationNotificationWithIcon(Context context, String title, String message,
                                                        int iconResId, int notificationId) {
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID_DONATION,
                    CHANNEL_NAME_DONATION,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription(CHANNEL_DESC_DONATION);
            channel.enableVibration(true);
            notificationManager.createNotificationChannel(channel);
        }

        Intent intent = new Intent(context, MessagesActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID_DONATION)
                .setSmallIcon(iconResId)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        notificationManager.notify(notificationId, builder.build());
    }

    // =============== متد عمومی ===============

    // متد برای حذف تمام نوتیفیکیشن‌ها
    public static void clearAllNotifications(Context context) {
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.cancelAll();
        }
    }

    // حذف نوتیفیکیشن خاص
    public static void clearNotification(Context context, int notificationId) {
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.cancel(notificationId);
        }
    }
}