package com.abania.dragmanager;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;
import java.util.List;

public class NotificationActionReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        int scheduleId = intent.getIntExtra("schedule_id", -1);

        if (scheduleId == -1) return;

        DatabaseHelper dbHelper = new DatabaseHelper(context);

        if ("CONFIRM_MEDICATION".equals(action)) {
            // کاربر مصرف را تأیید کرده است
            List<MedicationSchedule> schedules = dbHelper.getActiveSchedules();
            for (MedicationSchedule schedule : schedules) {
                if (schedule.getId() == scheduleId) {
                    int newRemaining = schedule.getRemainingQuantity() - 1;
                    if (newRemaining <= 0) {
                        // اگر دارو تمام شد، برنامه را غیرفعال کن
                        dbHelper.deactivateSchedule(scheduleId);
                        Toast.makeText(context, "✅ دوره مصرف دارو به پایان رسید", Toast.LENGTH_SHORT).show();
                    } else {
                        dbHelper.updateRemainingQuantity(scheduleId, newRemaining);
                        // کاهش موجودی داروخانه
                        updateMedicineStock(context, schedule.getMedicineName());
                        Toast.makeText(context, "✅ مصرف دارو ثبت شد. " + newRemaining + " عدد باقی مانده", Toast.LENGTH_SHORT).show();
                    }
                    break;
                }
            }

            // بستن نوتیفیکیشن
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager != null) {
                manager.cancel(scheduleId);
            }

        } else if ("STOP_MEDICATION".equals(action)) {
            // کاربر دوره را پایان داده است
            dbHelper.deactivateSchedule(scheduleId);
            Toast.makeText(context, "⏹️ دوره مصرف دارو پایان یافت", Toast.LENGTH_SHORT).show();

            // بستن نوتیفیکیشن
            NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager != null) {
                manager.cancel(scheduleId);
            }
        }
    }

    private void updateMedicineStock(Context context, String medicineName) {
        DatabaseHelper dbHelper = new DatabaseHelper(context);
        List<Medicine> medicines = dbHelper.getAllMedicines();
        boolean found = false;
        
        for (Medicine m : medicines) {
            if (m.getName().equals(medicineName)) {
                int newQuantity = m.getQuantity() - 1;
                if (newQuantity < 0) newQuantity = 0;
                m.setQuantity(newQuantity);
                dbHelper.updateMedicine(m);
                found = true;
                break;
            }
        }
        
        if (!found) {
            // اگر دارو در داروخانه نبود، پیام بده
            Toast.makeText(context, "⚠️ دارو در داروخانه یافت نشد!", Toast.LENGTH_SHORT).show();
        }
    }
}