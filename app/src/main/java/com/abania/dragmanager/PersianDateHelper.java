package com.abania.dragmanager;

import java.util.Calendar;
import java.util.TimeZone;

public class PersianDateHelper {

    // تبدیل تاریخ میلادی به شمسی (دستی)
    public static String getCurrentDate() {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Tehran"));
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // تبدیل سال میلادی به شمسی (تقریبی)
        int persianYear = year - 621;
        // تنظیم ماه‌های شمسی (تقریبی)
        int persianMonth = month;
        int persianDay = day;

        // اصلاح برای شروع سال شمسی (فروردین ≈ 21 مارس)
        if (month < 3) {
            persianYear -= 1;
            persianMonth = month + 9;
        } else {
            persianMonth = month - 3;
        }

        return String.format("%04d/%02d/%02d", persianYear, persianMonth, persianDay);
    }

    // ✅ متد کمکی مشترک: تفاضل روز بین یک تاریخ شمسی و امروز (همان فرمول ساده‌شده‌ی قبلی)
    public static long getDaysDifference(String persianDate) {
        try {
            String[] parts = persianDate.split("/");
            if (parts.length != 3) return Long.MAX_VALUE;
            int year = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]);
            int day = Integer.parseInt(parts[2]);

            // تاریخ فعلی شمسی
            String currentDate = getCurrentDate();
            String[] currentParts = currentDate.split("/");
            int currentYear = Integer.parseInt(currentParts[0]);
            int currentMonth = Integer.parseInt(currentParts[1]);
            int currentDay = Integer.parseInt(currentParts[2]);

            // محاسبه تفاوت به روز (ساده شده)
            return (currentYear - year) * 365L +
                    (currentMonth - month) * 30L +
                    (currentDay - day);
        } catch (Exception e) {
            return Long.MAX_VALUE;
        }
    }

    // بررسی اینکه آیا تاریخ بیش از یک هفته گذشته است
    public static boolean isOlderThanWeek(String persianDate) {
        return getDaysDifference(persianDate) > 7;
    }

    // ✅ بررسی اینکه آیا تاریخ در بازه‌ی N روز اخیر است (برای هشدار درخواست‌های نزدیک)
    public static boolean isWithinLastDays(String persianDate, int days) {
        long diff = getDaysDifference(persianDate);
        return diff >= 0 && diff <= days;
    }
}