package com.abania.dragmanager;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import android.content.res.ColorStateList;

public class DonationActivity extends AppCompatActivity {

    private ListView listViewDonations;
    private DatabaseHelper dbHelper;
    private DonationListAdapter adapter;
    private List<DonationItem> donationList;
    private List<DonationItem> filteredList;

    // ویجت‌های جستجو و فیلتر
    private AutoCompleteTextView autoCompleteMedicine;
    private Spinner spinnerProvince, spinnerCity;
    private TextView tvError;

    // ✅ هدر مشترک + نوار پایین ثابت + FAB خانه
    private ImageView ivBackButton;
    private BottomNavigationView bottomNavigationView;
    private FloatingActionButton fabHome;

    // داده‌های Spinner
    private String[] provinces = {"همه استان‌ها", "تهران", "اصفهان", "فارس", "خراسان رضوی", "خوزستان", "مازندران", "گیلان", "آذربایجان شرقی", "آذربایجان غربی", "کرمان", "کرمانشاه", "همدان", "یزد"};
    private String[] citiesTehran = {"همه شهرها", "تهران", "ری", "شهریار", "اسلامشهر", "ورامین"};
    private String[] citiesIsfahan = {"همه شهرها", "اصفهان", "نجف‌آباد", "خمینی‌شهر", "شاهین‌شهر", "کاشان"};
    private String[] citiesDefault = {"همه شهرها"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_donation);

        // پیدا کردن ویجت‌ها
        listViewDonations = findViewById(R.id.listViewMyDonations);
        dbHelper = new DatabaseHelper(this);

        // ویجت‌های جستجو و فیلتر
        autoCompleteMedicine = findViewById(R.id.autoCompleteMedicine);
        spinnerProvince = findViewById(R.id.spinnerProvince);
        spinnerCity = findViewById(R.id.spinnerCity);
        tvError = findViewById(R.id.tvError);

        // ✅ هدر: دکمه‌ی برگشت
        ivBackButton = findViewById(R.id.ivBackButton);
        ivBackButton.setOnClickListener(v -> goHome());

        // ✅ نوار پایین ثابت: این صفحه خودش «اهدا» است
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.nav_donate);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_donate) {
                return true; // همین صفحه‌ایم، کاری لازم نیست
            } else if (id == R.id.nav_pharmacy) {
                startActivity(new Intent(DonationActivity.this, AddMedicineActivity.class));
                return true;
            } else if (id == R.id.nav_reminder) {
                startActivity(new Intent(DonationActivity.this, MedicationScheduleActivity.class));
                return true;
            } else if (id == R.id.nav_request) {
                startActivity(new Intent(DonationActivity.this, RequestListActivity.class));
                return true;
            }
            return false;
        });

        // ✅ دکمه‌ی شناور خانه
		fabHome = findViewById(R.id.fabHome);
if (fabHome != null) {
    fabHome.setOnClickListener(v -> goHome());
    fabHome.setBackgroundTintList(ColorStateList.valueOf(0xFFF7F3B0)); // سفید
    fabHome.setImageTintList(ColorStateList.valueOf(0xFF9AA0A6));      // خاکستری
}

        // تنظیم Spinner استان
        ArrayAdapter<String> provinceAdapter = new ArrayAdapter<>(this, 
                android.R.layout.simple_spinner_item, provinces);
        provinceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerProvince.setAdapter(provinceAdapter);

        // تنظیم Spinner شهرستان (پیش‌فرض)
        ArrayAdapter<String> cityAdapter = new ArrayAdapter<>(this, 
                android.R.layout.simple_spinner_item, citiesDefault);
        cityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCity.setAdapter(cityAdapter);

        // رویداد انتخاب استان
        spinnerProvince.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                updateCities(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // تنظیم AutoCompleteTextView برای جستجوی دارو
        setupAutoComplete();

        // بارگذاری لیست اهدایی از ابر
        loadDonationList();

        // رویداد کلیک روی آیتم لیست
        listViewDonations.setOnItemClickListener((parent, view, position, id) -> {
            if (filteredList != null && position < filteredList.size()) {
                DonationItem item = filteredList.get(position);
                if (item != null) {
                    showDonationDetailDialog(item);
                }
            }
        });
    }
	
	private void goHome() {
    Intent homeIntent = new Intent(DonationActivity.this, MainActivity.class);
    homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
    startActivity(homeIntent);
    finish();
}

@Override
public void onBackPressed() {
    goHome();
}

    private void setupAutoComplete() {
        // دریافت لیست اسامی داروها از دیتابیس
        List<DonationItem> items = dbHelper.getDonationList();
        List<String> medicineNames = new ArrayList<>();
        if (items != null) {
            for (DonationItem item : items) {
                if (item != null && item.getMedicineName() != null && 
                    !medicineNames.contains(item.getMedicineName())) {
                    medicineNames.add(item.getMedicineName());
                }
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, medicineNames);
        autoCompleteMedicine.setAdapter(adapter);
        autoCompleteMedicine.setThreshold(1);

        // رویداد جستجو
        autoCompleteMedicine.setOnItemClickListener((parent, view, position, id) -> {
            String selected = parent.getItemAtPosition(position).toString();
            filterList(selected);
        });
    }

    private void updateCities(int provincePosition) {
        String[] cities;
        switch (provincePosition) {
            case 1: // تهران
                cities = citiesTehran;
                break;
            case 2: // اصفهان
                cities = citiesIsfahan;
                break;
            default:
                cities = citiesDefault;
                break;
        }

        ArrayAdapter<String> cityAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, cities);
        cityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCity.setAdapter(cityAdapter);
    }

    // =============== ✅ متد loadDonationList (اصلاح شده با شهر و استان) ===============
    private void loadDonationList() {
        CloudStorageHelper.readFile(CloudStorageHelper.BIN_DONATIONS, new CloudStorageHelper.CloudCallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    JSONObject obj = new JSONObject(response);
                    JSONArray donations = obj.getJSONArray("record");
                    
                    donationList = new ArrayList<>();
                    for (int i = 0; i < donations.length(); i++) {
                        JSONObject d = donations.getJSONObject(i);
                        DonationItem item = new DonationItem();
                        item.setId(d.getInt("id"));
                        item.setMedicineName(d.getString("medicineName"));
                        item.setMedicineDosage(d.optString("dosage", "نامشخص"));
                        item.setQuantity(d.getInt("quantity"));
                        item.setDonorName(d.getString("donorName"));
                        item.setDonorPhone(d.getString("phone"));
                        item.setDonorCity(d.optString("city", "نامشخص"));
                        item.setDonorProvince(d.optString("province", "نامشخص"));
                        item.setDonationDate(d.optString("expiryDate", ""));
                        donationList.add(item);
                    }
                    
                    filteredList = new ArrayList<>(donationList);
                    runOnUiThread(() -> {
                        if (donationList.isEmpty()) {
                            tvError.setVisibility(View.VISIBLE);
                            tvError.setText("📭 هیچ داروی اهدایی وجود ندارد");
                        } else {
                            tvError.setVisibility(View.GONE);
                        }
                        adapter = new DonationListAdapter(DonationActivity.this, filteredList);
                        listViewDonations.setAdapter(adapter);
                    });
                    
                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(() -> {
                        tvError.setVisibility(View.VISIBLE);
                        tvError.setText("❌ خطا در پردازش داده");
                    });
                }
            }
            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    tvError.setVisibility(View.VISIBLE);
                    tvError.setText("❌ خطا در دریافت داده از سرور");
                    Toast.makeText(DonationActivity.this, "❌ خطا در دریافت داده از سرور", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void filterList(String keyword) {
        if (filteredList == null) {
            filteredList = new ArrayList<>();
        }
        filteredList.clear();
        
        if (keyword == null || keyword.isEmpty()) {
            if (donationList != null) {
                filteredList.addAll(donationList);
            }
        } else {
            if (donationList != null) {
                for (DonationItem item : donationList) {
                    if (item != null && item.getMedicineName() != null &&
                        item.getMedicineName().toLowerCase().contains(keyword.toLowerCase())) {
                        filteredList.add(item);
                    }
                }
            }
        }
        
        if (filteredList.isEmpty()) {
            tvError.setVisibility(View.VISIBLE);
            tvError.setText("❌ دارویی با این نام یافت نشد");
        } else {
            tvError.setVisibility(View.GONE);
        }
        
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    /**
     * نمایش جزئیات اهدا در پنجره جدید
     */
    private void showDonationDetailDialog(DonationItem item) {
        try {
            Dialog dialog = new Dialog(this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.dialog_donation_detail);
            dialog.setCancelable(true);
            
            // پیدا کردن ویجت‌ها
            TextView detailMedicineName = dialog.findViewById(R.id.detailMedicineName);
            TextView detailDosage = dialog.findViewById(R.id.detailDosage);
            TextView detailQuantity = dialog.findViewById(R.id.detailQuantity);
            TextView detailDate = dialog.findViewById(R.id.detailDate);
            TextView detailDonorName = dialog.findViewById(R.id.detailDonorName);
            TextView detailProvince = dialog.findViewById(R.id.detailProvince);
            TextView detailCity = dialog.findViewById(R.id.detailCity);
            Button btnSendMessage = dialog.findViewById(R.id.btnSendMessage);
            
            // تنظیم مقادیر با بررسی null
            if (detailMedicineName != null) {
                detailMedicineName.setText(item.getMedicineName() != null ? item.getMedicineName() : "نامشخص");
            }
            
            if (detailDosage != null) {
                String dosage = item.getMedicineDosage() != null && !item.getMedicineDosage().isEmpty() 
                        ? item.getMedicineDosage() : "نامشخص";
                detailDosage.setText(dosage);
            }
            
            if (detailQuantity != null) {
                detailQuantity.setText(String.valueOf(item.getQuantity()));
            }
            
            if (detailDate != null) {
                String persianDate = convertToPersianDate(item.getDonationDate());
                detailDate.setText(persianDate);
            }
            
            if (detailDonorName != null) {
                detailDonorName.setText(item.getDonorName() != null ? item.getDonorName() : "نامشخص");
            }
            
            // ✅ نمایش استان و شهر از اطلاعات اهداکننده
            if (detailProvince != null) {
                String province = item.getDonorProvince() != null ? item.getDonorProvince() : "نامشخص";
                detailProvince.setText(province);
            }
            
            if (detailCity != null) {
                String city = item.getDonorCity() != null ? item.getDonorCity() : "نامشخص";
                detailCity.setText(city);
            }
            
            // رویداد دکمه ارسال پیام
            if (btnSendMessage != null) {
                btnSendMessage.setOnClickListener(v -> {
                    String donorName = item.getDonorName() != null ? item.getDonorName() : "اهداکننده";
                    Toast.makeText(this, "📩 پیام به " + donorName + " ارسال شد", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                });
            }
            
            dialog.show();
            
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "❌ خطا در نمایش جزئیات", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * تبدیل تاریخ میلادی به شمسی با ساعت
     */
    private String convertToPersianDate(String dateTime) {
        if (dateTime == null || dateTime.isEmpty()) {
            return "نامشخص";
        }

        try {
            if (dateTime.matches("\\d+")) {
                long timestamp = Long.parseLong(dateTime);
                java.util.Date date = new java.util.Date(timestamp);
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm", 
                        java.util.Locale.getDefault());
                String time = sdf.format(date);
                String persianDate = PersianDateHelper.getCurrentDate();
                return persianDate + " " + time;
            }
            
            return PersianDateHelper.getCurrentDate();
            
        } catch (Exception e) {
            e.printStackTrace();
            return "نامشخص";
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDonationList();
        setupAutoComplete();
    }
}