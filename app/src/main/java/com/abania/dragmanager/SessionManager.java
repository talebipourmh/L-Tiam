package com.abania.dragmanager;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "UserSession";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USER_PHONE = "userPhone";
    private static final String KEY_USER_NAME = "userName";
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private Context context;

    public SessionManager(Context context) {
        this.context = context;
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
    }

    // ذخیره اطلاعات کاربر پس از ورود موفق
    public void createLoginSession(String phone, String name) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_USER_PHONE, phone);
        editor.putString(KEY_USER_NAME, name);
        editor.apply();
    }

    // بررسی وضعیت ورود
    public boolean isLoggedIn() {
        return pref.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    // دریافت شماره موبایل کاربر
    public String getUserPhone() {
        return pref.getString(KEY_USER_PHONE, "");
    }

    // دریافت نام کاربر
    public String getUserName() {
        return pref.getString(KEY_USER_NAME, "کاربر مهمان");
    }

    // خروج از حساب و پاک کردن اطلاعات
    public void logout() {
        editor.clear();
        editor.apply();
    }
}