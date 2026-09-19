package com.abania.dragmanager;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.core.view.GravityCompat;
import com.google.android.material.navigation.NavigationView;
import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AlphaAnimation;
import android.widget.LinearLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.cardview.widget.CardView;
import org.json.JSONArray;
import org.json.JSONObject;
import com.google.android.material.floatingactionbutton.FloatingActionButton;


public class MainActivity extends AppCompatActivity {

    // ✅ ۴ باکس نواری جدید داشبورد
    private TextView tvBox1Count, tvBox1Total; // سالم (سبز)
    private TextView tvBox2Count, tvBox2Total; // منقضی (قرمز)
    private TextView tvBox3Count, tvBox3Total; // اهدای تحویل‌شده (آبی)
    private TextView tvBox4Count, tvBox4Total; // درخواست دریافت‌شده (بنفش)
    private View barBox1Fill, barBox1Spacer;
    private View barBox2Fill, barBox2Spacer;
    private View barBox3Fill, barBox3Spacer;
    private View barBox4Fill, barBox4Spacer;

    private Button btnAddMedicine, btnSchedule, btnDonation, btnRequestMedicine;
    private DatabaseHelper dbHelper;
    private static final String CHANNEL_ID = "medicine_alerts";
    private static final String REMINDER_CHANNEL_ID = "medicine_reminder_channel";
    private static final int NOTIFICATION_PERMISSION_CODE = 100;
    private static final String ALERT_PREF = "AlertPrefs";
    private static final String LAST_ALERT_DATE = "last_alert_date";
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private TextView tvUserName;
    private ImageView ivHeaderAvatar;
    private SessionManager sessionManager;
    private SharedPreferences avatarPref;
    private ImageView ivFooterBanner;
    private androidx.viewpager2.widget.ViewPager2 bannerViewPager;
    private LinearLayout bannerDotsLayout;
    private ImageView[] dots;
    private int[] bannerImages = {
            R.drawable.footer_banner1,
            R.drawable.footer_banner2,
            R.drawable.footer_banner3,
            R.drawable.footer_banner4
    };
    private Handler bannerHandler = new Handler();
    private Runnable bannerRunnable;
    private TextView tvMedicineTicker;
    private TextView tvNextReminderText; // ✅ باکس یادآوری داروی بعدی
    private CardView cardNearbyRequests; // ✅ هشدار درخواست‌های جدید نزدیک
    private TextView tvNearbyRequestsText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // پیدا کردن ویجت‌ها - ✅ ۴ باکس نواری جدید
        tvBox1Count = findViewById(R.id.tvBox1Count);
        tvBox1Total = findViewById(R.id.tvBox1Total);
        barBox1Fill = findViewById(R.id.barBox1Fill);
        barBox1Spacer = findViewById(R.id.barBox1Spacer);

        tvBox2Count = findViewById(R.id.tvBox2Count);
        tvBox2Total = findViewById(R.id.tvBox2Total);
        barBox2Fill = findViewById(R.id.barBox2Fill);
        barBox2Spacer = findViewById(R.id.barBox2Spacer);

        tvBox3Count = findViewById(R.id.tvBox3Count);
        tvBox3Total = findViewById(R.id.tvBox3Total);
        barBox3Fill = findViewById(R.id.barBox3Fill);
        barBox3Spacer = findViewById(R.id.barBox3Spacer);

        tvBox4Count = findViewById(R.id.tvBox4Count);
        tvBox4Total = findViewById(R.id.tvBox4Total);
        barBox4Fill = findViewById(R.id.barBox4Fill);
        barBox4Spacer = findViewById(R.id.barBox4Spacer);

        bannerViewPager = findViewById(R.id.bannerViewPager);
        bannerDotsLayout = findViewById(R.id.bannerDotsLayout);
        setupBanner();
        tvMedicineTicker = findViewById(R.id.tvMedicineTicker);
        tvMedicineTicker.setSelected(true);
        tvNextReminderText = findViewById(R.id.tvNextReminderText);
        cardNearbyRequests = findViewById(R.id.cardNearbyRequests);
        tvNearbyRequestsText = findViewById(R.id.tvNearbyRequestsText);

        dbHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);
        avatarPref = getSharedPreferences("AvatarPref", MODE_PRIVATE);

        // تنظیم DrawerLayout
        drawerLayout = findViewById(R.id.drawerLayout);
        ImageView ivMenuToggle = findViewById(R.id.ivMenuToggle);
        ivMenuToggle.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        // ✅ نوار پایین ثابت + FAB خانه
        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
        FloatingActionButton fabHome = findViewById(R.id.fabHome);

        // ✅ همین‌جا صفحه‌ی اصلی است: FAB باید نارنجی + لوگو با رنگ اصلی خودش باشد (حالت فعال)
        fabHome.setBackgroundTintList(ColorStateList.valueOf(0xFFFFFF31));
        fabHome.setImageTintList(null); // رنگ اصلی لوگو، بدون تینت اجباری

        // ✅ لینک‌های نوار پایین (در ویرایش قبلی پاک شده بود، برگردانده شد)
        bottomNav.setOnItemSelectedListener(item -> {
			bottomNav.setSelectedItemId(R.id.nav_empty);
            int id = item.getItemId();
            if (id == R.id.nav_donate) {
                startActivity(new Intent(MainActivity.this, DonationActivity.class));
                return true;
            } else if (id == R.id.nav_reminder) {
                startActivity(new Intent(MainActivity.this, MedicationScheduleActivity.class));
                return true;
            } else if (id == R.id.nav_pharmacy) {
                startActivity(new Intent(MainActivity.this, AddMedicineActivity.class));
                return true;
            } else if (id == R.id.nav_request) {
                startActivity(new Intent(MainActivity.this, RequestListActivity.class));
                return true;
            }
			return false;
        });

        // تنظیم NavigationView
        navigationView = findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        tvUserName = headerView.findViewById(R.id.tvUserName);
        ivHeaderAvatar = headerView.findViewById(R.id.ivHeaderAvatar);

        // تنظیم نام کاربر
        String userName = sessionManager.getUserName();
        tvUserName.setText(userName);

        // بارگذاری عکس پروفایل از کش
        loadAvatarFromCache();

        // رویداد کلیک روی نام کاربر
        headerView.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
            startActivity(intent);
            drawerLayout.closeDrawers();
        });

        // ===== مدیریت کلیک آیتم‌های منو =====
        navigationView.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_logout) {
                logoutUser();
            } else if (id == R.id.nav_profile) {
                Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                startActivity(intent);
            } else if (id == R.id.nav_messages) {
                Intent messagesIntent = new Intent(MainActivity.this, MessagesActivity.class);
                startActivity(messagesIntent);
            } else if (id == R.id.nav_my_requests) {
                Intent intent = new Intent(MainActivity.this, MyRequestActivity.class);
                startActivity(intent);
            } else if (id == R.id.nav_my_donations) {
                Intent intent = new Intent(MainActivity.this, MyDonationsActivity.class);
                startActivity(intent);
            }
            drawerLayout.closeDrawers();
            return true;
        });

        // ایجاد کانال‌های نوتیفیکیشن
        createNotificationChannel();
        createReminderNotificationChannel();

        // درخواست مجوز نوتیفیکیشن برای اندروید 13+
        requestNotificationPermission();

        // بارگذاری داده‌ها
        loadDashboardData();
        loadDonationStats();
        loadNextReminder();
        loadNearbyRequestsAlert();
        if (shouldShowAlertsToday()) {
            checkAlerts();
        }
    }

    private void loadAvatarFromCache() {
        String avatarPath = avatarPref.getString("avatar_path", "");
        if (!avatarPath.isEmpty()) {
            File imgFile = new File(avatarPath);
            if (imgFile.exists()) {
                Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                ivHeaderAvatar.setImageBitmap(bitmap);
            }
        }
    }

    private void requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        NOTIFICATION_PERMISSION_CODE);
            }
        }
    }

    // ✅ باکس‌های ۱ و ۲: سالم / منقضی (بر اساس جدول داروها)
    private void loadDashboardData() {
        List<Medicine> medicineList = dbHelper.getAllMedicines();
        int total = medicineList.size();
        int expired = 0;
        String mostUsedName = "-";
        int maxQuantity = -1;
        String leastUsedName = "-";
        int minQuantity = Integer.MAX_VALUE;

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd", Locale.ENGLISH);
        Date today = new Date();

        for (Medicine m : medicineList) {
            if (m.getExpiryDate() != null && !m.getExpiryDate().equals("نامشخص")) {
                try {
                    Date expiry = sdf.parse(m.getExpiryDate());
                    if (expiry != null && expiry.before(today)) expired++;
                } catch (Exception e) { /* ignore */ }
            }

            if (m.getQuantity() > maxQuantity) {
                maxQuantity = m.getQuantity();
                mostUsedName = m.getName();
            }
            if (m.getQuantity() < minQuantity) {
                minQuantity = m.getQuantity();
                leastUsedName = m.getName();
            }
        }

        int healthy = total - expired;

        tvMedicineTicker.setText("📦 بیشترین موجودی: " + mostUsedName + " (" + maxQuantity + " عدد)      —      📉 کمترین موجودی: " + leastUsedName + " (" + minQuantity + " عدد)");

        // باکس ۱: داروی سالم (سبز)
        tvBox1Count.setText(String.valueOf(healthy));
        tvBox1Total.setText("از " + total + " مورد");
        float ratio1 = total > 0 ? (float) healthy / total : 0.01f;
        setBarRatio(barBox1Fill, barBox1Spacer, ratio1, 0xFF43A047);

        // باکس ۲: داروی منقضی (قرمز)
        tvBox2Count.setText(String.valueOf(expired));
        tvBox2Total.setText("از " + total + " مورد");
        float ratio2 = total > 0 ? (float) expired / total : 0.01f;
        setBarRatio(barBox2Fill, barBox2Spacer, ratio2, 0xFFE53935);
    }

    // ✅ باکس‌های ۳ و ۴: اهدای تحویل‌شده / درخواست دریافت‌شده (بر اساس جدول donation_requests)
    private void loadDonationStats() {
        int donationTotal = dbHelper.getRequestCountByType("DONATION");
        int donationCompleted = dbHelper.getRequestCountByTypeAndStatus("DONATION", "COMPLETED");

        int requestTotal = dbHelper.getRequestCountByType("REQUEST");
        int requestAccepted = dbHelper.getRequestCountByTypeAndStatus("REQUEST", "ACCEPTED")
                + dbHelper.getRequestCountByTypeAndStatus("REQUEST", "COMPLETED");

        // باکس ۳: اهدای تحویل‌شده (آبی)
        tvBox3Count.setText(String.valueOf(donationCompleted));
        tvBox3Total.setText("از " + donationTotal + " مورد");
        float ratio3 = donationTotal > 0 ? (float) donationCompleted / donationTotal : 0.01f;
        setBarRatio(barBox3Fill, barBox3Spacer, ratio3, 0xFF1E88E5);

        // باکس ۴: درخواست دریافت‌شده (بنفش)
        tvBox4Count.setText(String.valueOf(requestAccepted));
        tvBox4Total.setText("از " + requestTotal + " مورد");
        float ratio4 = requestTotal > 0 ? (float) requestAccepted / requestTotal : 0.01f;
        setBarRatio(barBox4Fill, barBox4Spacer, ratio4, 0xFF8E24AA);
    }

    private void setBarRatio(View fill, View spacer, float ratio, int color) {
        ratio = Math.max(0.01f, Math.min(1f, ratio));

        LinearLayout.LayoutParams fillParams = (LinearLayout.LayoutParams) fill.getLayoutParams();
        fillParams.weight = ratio;
        fill.setLayoutParams(fillParams);

        LinearLayout.LayoutParams spacerParams = (LinearLayout.LayoutParams) spacer.getLayoutParams();
        spacerParams.weight = 1f - ratio;
        spacer.setLayoutParams(spacerParams);

        fill.setBackgroundTintList(ColorStateList.valueOf(color));
    }

    // ✅ باکس یادآوری داروی بعدی: دقیقاً همان منطق scheduleAlarm() در MedicationScheduleActivity
    private void loadNextReminder() {
        List<MedicationSchedule> schedules = dbHelper.getActiveSchedules();

        MedicationSchedule soonest = null;
        long soonestMillis = Long.MAX_VALUE;

        for (MedicationSchedule s : schedules) {
            if (s.getStartTime() == null || !s.getStartTime().contains(":")) continue;

            try {
                String[] timeParts = s.getStartTime().split(":");
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(timeParts[0]));
                calendar.set(Calendar.MINUTE, Integer.parseInt(timeParts[1]));
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);

                if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
                    calendar.add(Calendar.DAY_OF_YEAR, 1);
                }

                long triggerMillis = calendar.getTimeInMillis();
                if (triggerMillis < soonestMillis) {
                    soonestMillis = triggerMillis;
                    soonest = s;
                }
            } catch (Exception e) { /* فرمت ساعت نامعتبر - نادیده گرفته می‌شود */ }
        }

        if (soonest == null) {
            tvNextReminderText.setText("یادآوری فعالی وجود ندارد");
            return;
        }

        // آیا زمان بعدی امروز است یا فردا؟
        Calendar now = Calendar.getInstance();
        Calendar triggerCal = Calendar.getInstance();
        triggerCal.setTimeInMillis(soonestMillis);
        boolean isToday = now.get(Calendar.DAY_OF_YEAR) == triggerCal.get(Calendar.DAY_OF_YEAR)
                && now.get(Calendar.YEAR) == triggerCal.get(Calendar.YEAR);

        String dayLabel = isToday ? "امروز" : "فردا";
        tvNextReminderText.setText(soonest.getMedicineName() + " (" + soonest.getDosage() + ") — "
                + dayLabel + " ساعت " + soonest.getStartTime());
    }

    // ✅ هشدار «X درخواست جدید در نزدیکی شما»: از ابر (BIN_REQUESTS) می‌خواند،
    private void loadNearbyRequestsAlert() {
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        String userCity = prefs.getString("user_city", "");

        if (userCity == null || userCity.trim().isEmpty() || userCity.equals("نامشخص")) {
            cardNearbyRequests.setVisibility(View.GONE);
            return;
        }

        CloudStorageHelper.readFile(CloudStorageHelper.BIN_REQUESTS, new CloudStorageHelper.CloudCallback() {
            @Override
            public void onSuccess(String response) {
                int nearbyCount = 0;
                try {
                    JSONObject obj = new JSONObject(response);
                    JSONObject record = obj.getJSONObject("record");
                    JSONArray requests = record.getJSONArray("requests");

                    for (int i = 0; i < requests.length(); i++) {
                        JSONObject r = requests.getJSONObject(i);
                        String city = r.optString("city", "");
                        String status = r.optString("status", "PENDING");
                        String requestDate = r.optString("requestDate", "");

                        if (!city.equals(userCity)) continue;
                        if (!status.equals("PENDING")) continue; // فقط درخواست‌های فعال

                        // requestDate به شکل "1403/06/25 14:30" ذخیره شده؛ فقط بخش تاریخ لازم است
                        String datePart = requestDate.contains(" ")
                                ? requestDate.split(" ")[0]
                                : requestDate;

                        if (PersianDateHelper.isWithinLastDays(datePart, 10)) {
                            nearbyCount++;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    nearbyCount = -1; // خطای پردازش - کارت نمایش داده نشود
                }

                final int finalCount = nearbyCount;
                runOnUiThread(() -> {
                    if (finalCount > 0) {
                        tvNearbyRequestsText.setText(finalCount + " درخواست جدید در نزدیکی شما ثبت شده");
                        cardNearbyRequests.setVisibility(View.VISIBLE);
                    } else {
                        cardNearbyRequests.setVisibility(View.GONE);
                    }
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> cardNearbyRequests.setVisibility(View.GONE));
            }
        });
    }

    private void checkAlerts() {
        List<Medicine> allMedicines = dbHelper.getAllMedicines();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd", Locale.ENGLISH);
        Date today = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(today);
        calendar.add(Calendar.DAY_OF_YEAR, 30);
        Date thirtyDaysLater = calendar.getTime();

        StringBuilder alertMessage = new StringBuilder();

        for (Medicine m : allMedicines) {
            if (m.getExpiryDate() != null && !m.getExpiryDate().equals("نامشخص")) {
                try {
                    Date expiry = sdf.parse(m.getExpiryDate());
                    if (expiry != null) {
                        if (expiry.before(today)) {
                            alertMessage.append("⚠️ داروی ").append(m.getName()).append(" منقضی شده!\n");
                        } else if (expiry.before(thirtyDaysLater)) {
                            alertMessage.append("⏰ داروی ").append(m.getName()).append(" کمتر از ۳۰ روز تا انقضا دارد!\n");
                        }
                    }
                } catch (Exception e) { /* ignore */ }
            }

            if (m.getQuantity() < 5) {
                alertMessage.append("📉 موجودی داروی ").append(m.getName()).append(" کمتر از ۵ است!\n");
            }
        }

        if (alertMessage.length() > 0) {
            showNotification("هشدارهای داروخانه", alertMessage.toString());
        }
    }

    private void showNotification(String title, String message) {
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        if (notificationManager != null) {
            notificationManager.notify(1, builder.build());
        }
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "هشدارهای داروخانه",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("هشدارهای مربوط به داروهای منقضی، کم‌موجود و تاریخ نزدیک");
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private void createReminderNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    REMINDER_CHANNEL_ID,
                    "یادآوری مصرف دارو",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("یادآوری زمان مصرف دارو");
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            loadDashboardData();
            loadDonationStats();
            loadNextReminder();
            loadNearbyRequestsAlert();
            checkAlerts();
            loadAvatarFromCache();
        }
    }
	@Override
protected void onResume() {
    super.onResume();
    BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
    bottomNav.setSelectedItemId(R.id.nav_empty); // ✅ به‌جای حلقه‌ی قبلی
    loadDashboardData();
    loadDonationStats();
    loadNextReminder();
    loadNearbyRequestsAlert();
    loadAvatarFromCache();
}

    private boolean shouldShowAlertsToday() {

        SharedPreferences prefs =
                getSharedPreferences(ALERT_PREF, MODE_PRIVATE);

        String today =
                new SimpleDateFormat("yyyyMMdd", Locale.ENGLISH)
                        .format(new Date());

        String lastDate =
                prefs.getString(LAST_ALERT_DATE, "");

        if (today.equals(lastDate)) {
            return false;
        }

        prefs.edit()
                .putString(LAST_ALERT_DATE, today)
                .apply();

        return true;
    }

    private void logoutUser() {
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("is_logged_in", false);
        editor.remove("user_phone");
        editor.remove("user_name");
        editor.apply();

        SharedPreferences avatarPref = getSharedPreferences("AvatarPref", MODE_PRIVATE);
        avatarPref.edit().remove("avatar_path").apply();

        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void setupBanner() {
        BannerAdapter adapter = new BannerAdapter(bannerImages);
        bannerViewPager.setAdapter(adapter);
        setupBannerDots(0);

        bannerViewPager.registerOnPageChangeCallback(new androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                setupBannerDots(position);
            }
        });

        startBannerAutoScroll();
    }

    private void setupBannerDots(int activePosition) {
        bannerDotsLayout.removeAllViews();
        dots = new ImageView[bannerImages.length];

        for (int i = 0; i < bannerImages.length; i++) {
            dots[i] = new ImageView(this);
            int sizeInDp = 8;
            float scale = getResources().getDisplayMetrics().density;
            int sizeInPx = (int) (sizeInDp * scale);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(sizeInPx, sizeInPx);
            params.setMargins(6, 0, 6, 0);
            dots[i].setLayoutParams(params);
            dots[i].setImageResource(i == activePosition ? R.drawable.dot_active : R.drawable.dot_inactive);
            bannerDotsLayout.addView(dots[i]);
        }
    }

    private void startBannerAutoScroll() {
        bannerRunnable = () -> {
            int current = bannerViewPager.getCurrentItem();
            int next = (current + 1) % bannerImages.length;
            bannerViewPager.setCurrentItem(next, true);
            bannerHandler.postDelayed(bannerRunnable, 5000);
        };
        bannerHandler.postDelayed(bannerRunnable, 5000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bannerHandler != null && bannerRunnable != null) {
            bannerHandler.removeCallbacks(bannerRunnable);
        }
    }
}