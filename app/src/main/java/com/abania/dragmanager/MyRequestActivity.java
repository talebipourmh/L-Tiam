package com.abania.dragmanager;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import android.content.SharedPreferences;

public class MyRequestActivity extends AppCompatActivity {

    private ListView listViewRequests;
    private Button btnSubmitRequest;
    private EditText etMedicineName, etDosage, etCity, etQuantity;
    private DatabaseHelper dbHelper;
    private MyRequestAdapter adapter;
    private List<DonationRequest> requestList;
    private String currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_request);

        dbHelper = new DatabaseHelper(this);
        
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        currentUserId = prefs.getString("user_phone", "کاربر ناشناس");

        listViewRequests = findViewById(R.id.listViewMyRequests);
        btnSubmitRequest = findViewById(R.id.btnSubmitRequest);
        etMedicineName = findViewById(R.id.etMedicineName);
        etDosage = findViewById(R.id.etDosage);
        etCity = findViewById(R.id.etCity);
        etQuantity = findViewById(R.id.etQuantity);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("📋 درخواست‌های من");

        loadRequests();
        btnSubmitRequest.setOnClickListener(v -> submitRequest());
    }

    private void loadRequests() {
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        String currentUserPhone = prefs.getString("user_phone", "");
        
        List<DonationRequest> allRequests = dbHelper.getDonationRequests();
        requestList = new ArrayList<>();
        
        for (DonationRequest request : allRequests) {
            if (request.getDonorPhone() != null && 
                request.getDonorPhone().equals(currentUserPhone)) {
                requestList.add(request);
            }
        }
        
        adapter = new MyRequestAdapter(this, requestList);
        adapter.setOnRequestCancelListener(new MyRequestAdapter.OnRequestCancelListener() {
            @Override
            public void onCancelClick(DonationRequest request, int position) {
                cancelRequest(request, position);
            }
        });
        listViewRequests.setAdapter(adapter);

        if (requestList.isEmpty()) {
            Toast.makeText(this, "📭 هیچ درخواستی ثبت نکرده‌اید", Toast.LENGTH_SHORT).show();
        }
    }

    private void submitRequest() {
        String name = etMedicineName.getText().toString().trim();
        String dosage = etDosage.getText().toString().trim();
        String city = etCity.getText().toString().trim();
        String quantityStr = etQuantity.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(this, "❌ لطفاً نام دارو را وارد کنید", Toast.LENGTH_SHORT).show();
            return;
        }

        if (city.isEmpty()) {
            Toast.makeText(this, "❌ لطفاً شهر خود را وارد کنید", Toast.LENGTH_SHORT).show();
            return;
        }

        int quantity = 1;
        if (!quantityStr.isEmpty()) {
            try {
                quantity = Integer.parseInt(quantityStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "❌ تعداد نامعتبر است", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        String userName = prefs.getString("user_name", "کاربر ناشناس");
        String userCity = prefs.getString("user_city", "نامشخص");
        String userProvince = prefs.getString("user_province", "نامشخص");

        DonationRequest newRequest = new DonationRequest();
        newRequest.setMedicineName(name);
        newRequest.setDosage(dosage);
        newRequest.setCity(city);
        newRequest.setQuantity(quantity);
        newRequest.setRequesterPhone(currentUserId);
        newRequest.setDonorPhone(currentUserId);
        newRequest.setDonorName(userName);
        newRequest.setStatus("PENDING");
        newRequest.setTimestamp(System.currentTimeMillis());
        newRequest.setRequestDate(new java.text.SimpleDateFormat("yyyy/MM/dd", java.util.Locale.getDefault())
                .format(new java.util.Date()));

        boolean saved = dbHelper.saveDonationRequest(newRequest);
        saveRequestToCloud(name, dosage, quantity, userName, currentUserId, userCity, userProvince);
        
        if (saved) {
            Toast.makeText(this, "✅ درخواست شما ثبت شد", Toast.LENGTH_SHORT).show();
            etMedicineName.setText("");
            etDosage.setText("");
            etCity.setText("");
            etQuantity.setText("");
            loadRequests();
        } else {
            Toast.makeText(this, "❌ خطا در ثبت درخواست", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveRequestToCloud(String medicineName, String dosage, int quantity, 
                                    String userName, String userPhone, 
                                    String userCity, String userProvince) {
        CloudStorageHelper.readFile(CloudStorageHelper.BIN_REQUESTS, new CloudStorageHelper.CloudCallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    JSONObject obj = new JSONObject(response);
                    JSONObject record = obj.getJSONObject("record");
                    JSONArray requests = record.getJSONArray("requests");
                    
                    JSONObject newRequest = new JSONObject();
                    int newId = requests.length() + 1;
                    newRequest.put("id", newId);
                    newRequest.put("medicineName", medicineName);
                    newRequest.put("dosage", dosage);
                    newRequest.put("quantity", quantity);
                    newRequest.put("requesterName", userName);
                    newRequest.put("requesterPhone", userPhone);
                    newRequest.put("city", userCity);
                    newRequest.put("province", userProvince);
                    newRequest.put("requestDate", PersianDateHelper.getCurrentDate() + " " + 
                                new java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
                                .format(new java.util.Date()));
                    newRequest.put("status", "PENDING");
                    requests.put(newRequest);
                    
                    JSONObject updateObj = new JSONObject();
                    updateObj.put("record", requests);
                    CloudStorageHelper.writeFile(CloudStorageHelper.BIN_REQUESTS, updateObj.toString(), 
                        new CloudStorageHelper.CloudCallback() {
                            @Override
                            public void onSuccess(String response) {
                                // ذخیره موفق
                            }
                            @Override
                            public void onError(String error) {
                                runOnUiThread(() -> 
                                    Toast.makeText(MyRequestActivity.this, "⚠️ درخواست در ابر ذخیره نشد", Toast.LENGTH_SHORT).show()
                                );
                            }
                        });
                    
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onError(String error) {
                runOnUiThread(() -> 
                    Toast.makeText(MyRequestActivity.this, "⚠️ خطا در ارتباط با ابر", Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    private void cancelRequest(DonationRequest request, int position) {
        boolean updated = dbHelper.updateDonationRequestStatus(request.getId(), "REJECTED");
        
        if (updated) {
            request.setStatus("REJECTED");
            requestList.set(position, request);
            adapter.notifyDataSetChanged();
            Toast.makeText(this, "✅ درخواست لغو شد", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "❌ خطا در لغو درخواست", Toast.LENGTH_SHORT).show();
        }
    }

    private String convertRequestsToJson() {
        try {
            JSONArray jsonArray = new JSONArray();
            for (DonationRequest r : requestList) {
                JSONObject obj = new JSONObject();
                obj.put("medicineName", r.getMedicineName());
                obj.put("dosage", r.getDosage() != null ? r.getDosage() : "");
                obj.put("quantity", r.getQuantity());
                obj.put("city", r.getCity() != null ? r.getCity() : "");
                obj.put("requesterPhone", r.getRequesterPhone() != null ? r.getRequesterPhone() : "");
                obj.put("requestDate", r.getRequestDate() != null ? r.getRequestDate() : "");
                obj.put("status", r.getStatus());
                jsonArray.put(obj);
            }
            return jsonArray.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "[]";
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadRequests();
    }
}