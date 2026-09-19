package com.abania.dragmanager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.TextView;

public class LoginActivity extends AppCompatActivity {

    private EditText etPhone, etCode;
    private com.google.android.material.textfield.TextInputLayout tilCode;
    private Button btnSendCode, btnVerify;
    private SharedPreferences prefs;
	private TextView tvResendCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // بررسی لاگین قبلی
        prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        boolean isLoggedIn = prefs.getBoolean("is_logged_in", false);
        String savedPhone = prefs.getString("user_phone", "");

        // اگر قبلاً لاگین کرده بود، مستقیماً به صفحه اصلی برو
        if (isLoggedIn && !savedPhone.isEmpty()) {
            goToMainActivity();
            return;
        }

        etPhone = findViewById(R.id.etPhone);
        etCode = findViewById(R.id.etCode);
		tilCode = findViewById(R.id.tilCode);
        btnSendCode = findViewById(R.id.btnSendCode);
        btnVerify = findViewById(R.id.btnVerify);
		tvResendCode = findViewById(R.id.tvResendCode);

        // اگر شماره قبلاً ذخیره شده، نمایش بده
        if (!savedPhone.isEmpty()) {
            etPhone.setText(savedPhone);
        }

        btnSendCode.setOnClickListener(v -> {
            String phone = etPhone.getText().toString().trim();
            if (TextUtils.isEmpty(phone)) {
                Toast.makeText(this, "❌ شماره تلفن را وارد کنید", Toast.LENGTH_SHORT).show();
                return;
            }
            tilCode.setVisibility(View.VISIBLE);
            btnVerify.setVisibility(View.VISIBLE);
            btnSendCode.setVisibility(View.GONE);
            tvResendCode.setVisibility(View.VISIBLE);
            Toast.makeText(this, "📤 کد فعالسازی ارسال شد (برای تست: 1234)", Toast.LENGTH_SHORT).show();
        });
		
		tvResendCode.setOnClickListener(v -> {
            String phone = etPhone.getText().toString().trim();
            if (TextUtils.isEmpty(phone)) {
                Toast.makeText(this, "❌ شماره تلفن را وارد کنید", Toast.LENGTH_SHORT).show();
                return;
                }
                Toast.makeText(this, "📤 کد فعالسازی مجدداً ارسال شد", Toast.LENGTH_SHORT).show();
            });

        btnVerify.setOnClickListener(v -> {
            String code = etCode.getText().toString().trim();
            if (TextUtils.isEmpty(code)) {
                Toast.makeText(this, "❌ کد را وارد کنید", Toast.LENGTH_SHORT).show();
                return;
            }

            // برای تست: هر کد ۴ رقمی قبول می‌شود
            if (code.length() >= 4) {
                String phone = etPhone.getText().toString().trim();
                saveUserInfo(phone);
                goToMainActivity();
            } else {
                Toast.makeText(this, "❌ کد نامعتبر است", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveUserInfo(String phoneNumber) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("user_phone", phoneNumber);
        editor.putString("user_name", "کاربر " + phoneNumber);
        editor.putBoolean("is_logged_in", true);
        editor.apply();
        
        Toast.makeText(this, "✅ خوش آمدید!", Toast.LENGTH_SHORT).show();
    }

    private void goToMainActivity() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}