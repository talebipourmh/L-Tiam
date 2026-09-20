package com.abania.dragmanager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DonationActivity extends AppCompatActivity {
    private ListView listViewDonations;
    private AutoCompleteTextView autoCompleteMedicine;
    private Spinner spinnerProvince, spinnerCity;
    private TextView tvError;
    private final List<DonationItem> donationList = new ArrayList<>();
    private final List<DonationItem> filteredList = new ArrayList<>();
    private DonationListAdapter adapter;
    private String selectedProvince = "همه استان‌ها";
    private String selectedCity = "همه شهرها";
    private final String[] provinces = {"همه استان‌ها", "تهران", "اصفهان", "فارس", "خراسان رضوی", "خوزستان", "مازندران", "گیلان"};
    private final String[] cities = {"همه شهرها", "تهران", "ری", "اصفهان", "شیراز", "مشهد"};

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donation_marketplace);
        listViewDonations = findViewById(R.id.listViewMyDonations);
        autoCompleteMedicine = findViewById(R.id.autoCompleteMedicine);
        spinnerProvince = findViewById(R.id.spinnerProvince);
        spinnerCity = findViewById(R.id.spinnerCity);
        tvError = findViewById(R.id.tvError);
        ImageView back = findViewById(R.id.ivBackButton);
        if (back != null) back.setOnClickListener(v -> goHome());
        setupNavigation();
        setupFilters();
        adapter = new DonationListAdapter(this, filteredList);
        adapter.setOnDonationRequestListener(this::showRequestDialog);
        listViewDonations.setAdapter(adapter);
        loadDonationList();
    }

    private void setupNavigation() {
        BottomNavigationView nav = findViewById(R.id.bottomNavigationView);
        if (nav != null) {
            nav.setSelectedItemId(R.id.nav_donate);
            nav.setOnItemSelectedListener(item -> {
                int id = item.getItemId();
                if (id == R.id.nav_donate) return true;
                if (id == R.id.nav_pharmacy) startActivity(new Intent(this, AddMedicineActivity.class));
                else if (id == R.id.nav_reminder) startActivity(new Intent(this, MedicationScheduleActivity.class));
                else if (id == R.id.nav_request) startActivity(new Intent(this, RequestListActivity.class));
                return true;
            });
        }
        FloatingActionButton home = findViewById(R.id.fabHome);
        if (home != null) home.setOnClickListener(v -> goHome());
    }

    private void setupFilters() {
        setSpinner(spinnerProvince, provinces);
        setSpinner(spinnerCity, cities);
        spinnerProvince.setOnItemSelectedListener(new SimpleSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> p, View v, int position, long id) {
                selectedProvince = provinces[position]; filterList();
            }
        });
        spinnerCity.setOnItemSelectedListener(new SimpleSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> p, View v, int position, long id) {
                selectedCity = cities[position]; filterList();
            }
        });
        autoCompleteMedicine.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int st, int c, int a) { }
            public void onTextChanged(CharSequence s, int st, int before, int count) { filterList(); }
            public void afterTextChanged(Editable e) { }
        });
    }

    private void setSpinner(Spinner spinner, String[] values) {
        ArrayAdapter<String> a = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, values);
        a.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(a);
    }

    private abstract static class SimpleSelectedListener implements AdapterView.OnItemSelectedListener {
        public void onNothingSelected(AdapterView<?> parent) { }
    }

    private void loadDonationList() {
        CloudStorageHelper.readFile(CloudStorageHelper.BIN_DONATIONS, new CloudStorageHelper.CloudCallback() {
            @Override public void onSuccess(String response) {
                try {
                    JSONArray donations = findDonationArray(new JSONObject(response));
                    List<DonationItem> loaded = new ArrayList<>();
                    for (int i = 0; i < donations.length(); i++) {
                        JSONObject d = donations.optJSONObject(i);
                        if (d == null) continue;
                        DonationItem item = new DonationItem();
                        item.setId(d.optInt("id", i + 1));
                        item.setMedicineName(first(d, "medicineName", "medicine_name", "name"));
                        item.setMedicineDosage(first(d, "dosage", "medicineDosage"));
                        item.setMedicineType(first(d, "type", "medicineType"));
                        item.setQuantity(d.optInt("quantity", 0));
                        item.setDonorName(first(d, "donorName", "donor_name"));
                        item.setDonorPhone(first(d, "phone", "donorPhone", "donor_phone"));
                        item.setDonorProvince(first(d, "province", "donorProvince"));
                        item.setDonorCity(first(d, "city", "donorCity"));
                        item.setDonationDate(first(d, "expiryDate", "expiry_date", "donationDate", "donation_date"));
                        item.setStatus(first(d, "status"));
                        if (item.getStatus() == null || item.getStatus().trim().isEmpty()) item.setStatus("AVAILABLE");
                        // Older records have no status. They are available unless explicitly completed/rejected.
                        boolean hidden = "COMPLETED".equalsIgnoreCase(item.getStatus()) || "REJECTED".equalsIgnoreCase(item.getStatus());
                        if (!hidden && item.getQuantity() > 0 && !item.getMedicineName().trim().isEmpty()) loaded.add(item);
                    }
                    runOnUiThread(() -> {
                        donationList.clear();
                        donationList.addAll(loaded);
                        setupMedicineSuggestions();
                        filterList();
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    showError("❌ ساختار داده‌های اهدایی نامعتبر است");
                }
            }
            @Override public void onError(String error) { showError("❌ خطا در دریافت داروهای اهدایی: " + error); }
        });
    }

    private JSONArray findDonationArray(Object value) throws Exception {
        if (value instanceof JSONArray) return (JSONArray) value;
        if (!(value instanceof JSONObject)) return new JSONArray();
        JSONObject object = (JSONObject) value;
        JSONArray array = object.optJSONArray("donations");
        if (array != null) return array;
        array = object.optJSONArray("record");
        if (array != null) return array;
        Object record = object.opt("record");
        if (record != null && record != JSONObject.NULL) return findDonationArray(record);
        Object data = object.opt("data");
        if (data != null && data != JSONObject.NULL) return findDonationArray(data);
        return new JSONArray();
    }

    private String first(JSONObject object, String... keys) {
        for (String key : keys) {
            String value = object.optString(key, "");
            if (!value.trim().isEmpty()) return value;
        }
        return "";
    }

    private void setupMedicineSuggestions() {
        List<String> names = new ArrayList<>();
        for (DonationItem i : donationList) if (!names.contains(i.getMedicineName())) names.add(i.getMedicineName());
        autoCompleteMedicine.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, names));
    }

    private void filterList() {
        if (adapter == null) return;
        String keyword = autoCompleteMedicine.getText().toString().trim().toLowerCase(Locale.ROOT);
        filteredList.clear();
        for (DonationItem item : donationList) {
            String medicine = item.getMedicineName() == null ? "" : item.getMedicineName();
            boolean name = keyword.isEmpty() || medicine.toLowerCase(Locale.ROOT).contains(keyword);
            boolean province = selectedProvince.startsWith("همه") || selectedProvince.equals(item.getDonorProvince());
            boolean city = selectedCity.startsWith("همه") || selectedCity.equals(item.getDonorCity());
            if (name && province && city) filteredList.add(item);
        }
        adapter.notifyDataSetChanged();
        if (filteredList.isEmpty()) showError(donationList.isEmpty() ? "📭 هیچ داروی اهدایی موجود نیست" : "❌ موردی با این فیلتر یافت نشد");
        else tvError.setVisibility(View.GONE);
    }

    private void showError(String message) { runOnUiThread(() -> { tvError.setText(message); tvError.setVisibility(View.VISIBLE); }); }

    private void showRequestDialog(DonationItem item) {
        View view = getLayoutInflater().inflate(R.layout.dialog_request_quick, null);
        TextView name = view.findViewById(R.id.tvRequestMedicineName);
        TextView details = view.findViewById(R.id.tvRequestMedicineDetails);
        android.widget.EditText quantity = view.findViewById(R.id.etRequestQuantity);
        name.setText("💊 " + item.getMedicineName());
        details.setText("موجودی: " + item.getQuantity() + " | " + item.getMedicineDosage());
        quantity.setText("1");
        new AlertDialog.Builder(this).setTitle("📩 درخواست دارو").setView(view)
                .setPositiveButton("ارسال درخواست", (d, w) -> {
                    try {
                        int count = Integer.parseInt(quantity.getText().toString().trim());
                        if (count < 1 || count > item.getQuantity()) throw new NumberFormatException();
                        submitRequest(item, count);
                    } catch (NumberFormatException e) { Toast.makeText(this, "❌ تعداد نامعتبر است", Toast.LENGTH_SHORT).show(); }
                }).setNegativeButton("لغو", null).show();
    }

    private void submitRequest(DonationItem item, int quantity) {
        SharedPreferences p = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        String recipientPhone = p.getString("user_phone", "");
        String recipientName = p.getString("user_name", "کاربر");
        DonationRequest request = new DonationRequest();
        request.setMedicineName(item.getMedicineName()); request.setQuantity(quantity);
        request.setDonorName(item.getDonorName()); request.setDonorPhone(item.getDonorPhone());
        request.setStatus("PENDING"); request.setTimestamp(System.currentTimeMillis());
        request.setMessage("درخواست از طرف " + recipientName + " برای کاربر " + item.getDonorName());
        request.setRequestType("REQUEST");
        if (!new DatabaseHelper(this).saveDonationRequest(request)) { Toast.makeText(this, "❌ درخواست ذخیره نشد", Toast.LENGTH_SHORT).show(); return; }
        appendRequestToCloud(item, quantity, recipientName, recipientPhone);
        Toast.makeText(this, "✅ درخواست برای اهداکننده ارسال شد", Toast.LENGTH_LONG).show();
    }

    private void appendRequestToCloud(DonationItem item, int quantity, String recipientName, String recipientPhone) {
        CloudStorageHelper.readFile(CloudStorageHelper.BIN_REQUESTS, new CloudStorageHelper.CloudCallback() {
            @Override public void onSuccess(String response) {
                try {
                    JSONObject root = new JSONObject(response);
                    JSONObject record = root.optJSONObject("record");
                    if (record == null) record = new JSONObject();
                    JSONArray requests = record.optJSONArray("requests");
                    if (requests == null) requests = new JSONArray();
                    JSONObject r = new JSONObject();
                    r.put("id", System.currentTimeMillis()); r.put("donationId", item.getId());
                    r.put("medicineName", item.getMedicineName()); r.put("dosage", item.getMedicineDosage());
                    r.put("quantity", quantity); r.put("donorName", item.getDonorName());
                    r.put("donorPhone", item.getDonorPhone()); r.put("requesterName", recipientName);
                    r.put("requesterPhone", recipientPhone); r.put("status", "PENDING");
                    r.put("createdAt", System.currentTimeMillis()); requests.put(r); record.put("requests", requests);
                    JSONObject updated = new JSONObject(); updated.put("record", record);
                    CloudStorageHelper.writeFile(CloudStorageHelper.BIN_REQUESTS, updated.toString(), new CloudStorageHelper.CloudCallback() {
                        @Override public void onSuccess(String response) { }
                        @Override public void onError(String error) { Toast.makeText(DonationActivity.this, "⚠️ در ابر ذخیره نشد", Toast.LENGTH_SHORT).show(); }
                    });
                } catch (Exception e) { Toast.makeText(DonationActivity.this, "⚠️ داده ابر نامعتبر است", Toast.LENGTH_SHORT).show(); }
            }
            @Override public void onError(String error) { Toast.makeText(DonationActivity.this, "⚠️ ارتباط با ابر برقرار نشد", Toast.LENGTH_SHORT).show(); }
        });
    }

    private void goHome() { startActivity(new Intent(this, MainActivity.class)); finish(); }
    @Override public void onBackPressed() { goHome(); }
    @Override protected void onResume() { super.onResume(); if (adapter != null) loadDonationList(); }
}
