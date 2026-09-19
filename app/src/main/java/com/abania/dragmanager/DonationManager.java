package com.abania.dragmanager;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

import java.util.List;

public class DonationManager {

    private static DatabaseHelper dbHelper;

    /**
     * ارسال درخواست اهدا
     */
    public static void sendDonationRequest(Context context, Medicine medicine, int quantity, String donorUserId) {
        dbHelper = new DatabaseHelper(context);

        // 1. دریافت اطلاعات کاربر از SharedPreferences
        SharedPreferences prefs = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        String userPhone = prefs.getString("user_phone", "نامشخص");
        String userName = prefs.getString("user_name", "کاربر ناشناس");

        // 2. ایجاد درخواست اهدا
        DonationRequest request = new DonationRequest();
        request.setMedicineName(medicine.getName());
        request.setMedicineId(medicine.getId());
        request.setQuantity(quantity);
        request.setDonorName(userName);
        request.setDonorPhone(userPhone);
        request.setStatus("PENDING");
        request.setTimestamp(System.currentTimeMillis());
        request.setMessage("درخواست اهدا از طرف " + userName + " (شماره: " + userPhone + ")");

        // 3. ذخیره در دیتابیس
        boolean saved = dbHelper.saveDonationRequest(request);

        if (saved) {
            Toast.makeText(context,
                    "✅ درخواست اهدا برای " + medicine.getName() +
                            " به تعداد " + quantity + " عدد ارسال شد",
                    Toast.LENGTH_LONG).show();

            // 4. ارسال نوتیفیکیشن
            sendNotificationToDonor(context, request);
        } else {
            Toast.makeText(context, "❌ خطا در ثبت درخواست اهدا", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * دریافت تمام درخواست‌های اهدا
     */
    public static List<DonationRequest> getDonationRequests(Context context) {
        dbHelper = new DatabaseHelper(context);
        return dbHelper.getDonationRequests();
    }

    /**
     * دریافت درخواست‌های اهدا بر اساس وضعیت
     */
    public static List<DonationRequest> getDonationRequestsByStatus(Context context, String status) {
        dbHelper = new DatabaseHelper(context);
        return dbHelper.getDonationRequestsByStatus(status);
    }

    /**
     * به‌روزرسانی وضعیت درخواست اهدا
     */
    public static boolean updateDonationRequestStatus(Context context, int requestId, String newStatus) {
        dbHelper = new DatabaseHelper(context);
        return dbHelper.updateDonationRequestStatus(requestId, newStatus);
    }

    /**
     * حذف درخواست اهدا
     */
    public static boolean deleteDonationRequest(Context context, int requestId) {
        dbHelper = new DatabaseHelper(context);
        return dbHelper.deleteDonationRequest(requestId);
    }

    /**
     * ارسال نوتیفیکیشن به اهداکننده
     */
    private static void sendNotificationToDonor(Context context, DonationRequest request) {
        String message = "درخواست جدید برای داروی " + request.getMedicineName() +
                " به تعداد " + request.getQuantity() + " عدد";

        // استفاده از NotificationHelper برای ارسال نوتیفیکیشن
        NotificationHelper.showDonationNotification(
                context,
                "📦 درخواست اهدا",
                message,
                (int) System.currentTimeMillis()
        );
    }
}