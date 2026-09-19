package com.abania.dragmanager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
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
import java.util.List;import android.content.res.ColorStateList;

public class RequestListActivity extends AppCompatActivity {

    private ListView listViewRequests;
    private RequestListAdapter adapter;
    private List<DonationRequest> requestList;
    private List<DonationRequest> filteredList;
    private TextView tvError;

    private AutoCompleteTextView autoCompleteMedicine;
    private Spinner spinnerProvince, spinnerCity;

    // ✅ هدر مشترک + نوار پایین ثابت + FAB خانه
    private ImageView ivBackButton;
    private BottomNavigationView bottomNavigationView;
    private FloatingActionButton fabHome;

    private String[] provinces = {"همه استان‌ها", "تهران", "اصفهان", "فارس", "خراسان رضوی", "خوزستان", "مازندران", "گیلان", "آذربایجان شرقی", "آذربایجان غربی", "کرمان", "کرمانشاه", "همدان", "یزد"};
    private String[] citiesDefault = {"همه شهرها"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_list);

        listViewRequests = findViewById(R.id.listViewRequests);
        autoCompleteMedicine = findViewById(R.id.autoCompleteMedicine);
        spinnerProvince = findViewById(R.id.spinnerProvince);
        spinnerCity = findViewById(R.id.spinnerCity);
        tvError = findViewById(R.id.tvError);

        // ✅ هدر: دکمه‌ی برگشت
        ivBackButton = findViewById(R.id.ivBackButton);
        ivBackButton.setOnClickListener(v -> goHome());

        // ✅ نوار پایین ثابت: این صفحه خودش «درخواست» است
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.nav_request);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_request) {
                return true; // همین صفحه‌ایم، کاری لازم نیست
            } else if (id == R.id.nav_donate) {
                startActivity(new Intent(RequestListActivity.this, DonationActivity.class));
                return true;
            } else if (id == R.id.nav_reminder) {
                startActivity(new Intent(RequestListActivity.this, MedicationScheduleActivity.class));
                return true;
            } else if (id == R.id.nav_pharmacy) {
                startActivity(new Intent(RequestListActivity.this, AddMedicineActivity.class));
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

        ArrayAdapter<String> provinceAdapter = new ArrayAdapter<>(this, 
                android.R.layout.simple_spinner_item, provinces);
        provinceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerProvince.setAdapter(provinceAdapter);

        ArrayAdapter<String> cityAdapter = new ArrayAdapter<>(this, 
                android.R.layout.simple_spinner_item, citiesDefault);
        cityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCity.setAdapter(cityAdapter);

        loadRequests();

        autoCompleteMedicine.setOnItemClickListener((parent, view, position, id) -> {
            String selected = parent.getItemAtPosition(position).toString();
            filterList(selected);
        });
    }
	
	private void goHome() {
    Intent homeIntent = new Intent(RequestListActivity.this, MainActivity.class);
    homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
    startActivity(homeIntent);
    finish();
}

@Override
public void onBackPressed() {
    goHome();
}

    private void loadRequests() {
        CloudStorageHelper.readFile(CloudStorageHelper.BIN_REQUESTS, new CloudStorageHelper.CloudCallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    JSONObject obj = new JSONObject(response);
                    JSONObject record = obj.getJSONObject("record");
                    JSONArray requests = record.getJSONArray("requests");
                    
                    requestList = new ArrayList<>();
                    for (int i = 0; i < requests.length(); i++) {
                        JSONObject r = requests.getJSONObject(i);
                        DonationRequest item = new DonationRequest();
                        item.setId(r.getInt("id"));
                        item.setMedicineName(r.getString("medicineName"));
                        item.setDosage(r.optString("dosage", "نامشخص"));
                        item.setQuantity(r.getInt("quantity"));
                        item.setDonorName(r.getString("requesterName"));
                        item.setDonorPhone(r.getString("requesterPhone"));
                        item.setCity(r.optString("city", "نامشخص"));
                        item.setStatus(r.optString("status", "PENDING"));
                        item.setRequestDate(r.optString("requestDate", ""));
                        requestList.add(item);
                    }
                    
                    filteredList = new ArrayList<>(requestList);
                    runOnUiThread(() -> {
                        if (requestList.isEmpty()) {
                            tvError.setVisibility(View.VISIBLE);
                            tvError.setText("📭 هیچ درخواستی وجود ندارد");
                        } else {
                            tvError.setVisibility(View.GONE);
                        }
                        adapter = new RequestListAdapter(RequestListActivity.this, filteredList);
                        listViewRequests.setAdapter(adapter);
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
                    Toast.makeText(RequestListActivity.this, "❌ خطا در دریافت داده از سرور", Toast.LENGTH_SHORT).show();
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
            if (requestList != null) {
                filteredList.addAll(requestList);
            }
        } else {
            if (requestList != null) {
                for (DonationRequest item : requestList) {
                    if (item != null && item.getMedicineName() != null &&
                        item.getMedicineName().toLowerCase().contains(keyword.toLowerCase())) {
                        filteredList.add(item);
                    }
                }
            }
        }
        
        if (filteredList.isEmpty()) {
            tvError.setVisibility(View.VISIBLE);
            tvError.setText("❌ درخواستی با این نام یافت نشد");
        } else {
            tvError.setVisibility(View.GONE);
        }
        
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }
}