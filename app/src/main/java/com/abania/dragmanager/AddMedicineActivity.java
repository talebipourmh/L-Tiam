package com.abania.dragmanager;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import android.widget.ImageView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import android.content.res.ColorStateList;

public class AddMedicineActivity extends AppCompatActivity {

    private ListView medicineListView;
    private EditText searchBox;
    private Button btnSearch;
    private FloatingActionButton btnAddMedicine, btnSave;
    private View addMedicinePanelScroll;
    private ImageView btnCloseForm;
    private DatabaseHelper dbHelper;
    private MedicineListAdapter adapter;
    private List<Medicine> medicineList;
    private AutoCompleteTextView autoCompleteMedicine;
    private Spinner spinnerDosage, spinnerType;
    private Button btnExpiryDate;
    private EditText etQuantity, etDisease, etRoutine, etInstructions;
    private String expiryDate = "";
    private List<Medicine> allMedicines;

    // ✅ هدر مشترک + نوار پایین ثابت + FAB خانه
    private ImageView ivBackButton;
    private BottomNavigationView bottomNavigationView;
    private FloatingActionButton fabHome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_medicine);

        dbHelper = new DatabaseHelper(this);
        allMedicines = CsvImporter.readCsvFromAssets(this, "medicines.csv");

        medicineListView = findViewById(R.id.medicineListView);
        searchBox = findViewById(R.id.searchBox);
        btnSearch = findViewById(R.id.btnSearch);
        btnAddMedicine = findViewById(R.id.btnAddMedicine);
        btnSave = findViewById(R.id.btnSave);
        addMedicinePanelScroll = findViewById(R.id.addMedicinePanelScroll);
        btnCloseForm = findViewById(R.id.btnCloseForm);
        autoCompleteMedicine = findViewById(R.id.autoCompleteMedicine);
        spinnerDosage = findViewById(R.id.spinnerDosage);
        spinnerType = findViewById(R.id.spinnerType);
        btnExpiryDate = findViewById(R.id.btnExpiryDate);
        etQuantity = findViewById(R.id.etQuantity);
        etDisease = findViewById(R.id.etDisease);
        etRoutine = findViewById(R.id.etRoutine);
        etInstructions = findViewById(R.id.etInstructions);

        // ✅ هدر: دکمه‌ی برگشت
        ivBackButton = findViewById(R.id.ivBackButton);
        ivBackButton.setOnClickListener(v -> goHome());

        // ✅ نوار پایین ثابت: این صفحه خودش «داروخانه» است
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.nav_pharmacy);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_pharmacy) {
                return true; // همین صفحه‌ایم، کاری لازم نیست
            } else if (id == R.id.nav_reminder) {
                startActivity(new Intent(AddMedicineActivity.this, MedicationScheduleActivity.class));
                return true;
            } else if (id == R.id.nav_donate) {
                startActivity(new Intent(AddMedicineActivity.this, DonationActivity.class));
                return true;
            } else if (id == R.id.nav_request) {
                startActivity(new Intent(AddMedicineActivity.this, RequestListActivity.class));
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

        addMedicinePanelScroll.setVisibility(View.GONE);
        loadMedicines();

        btnSearch.setOnClickListener(v -> {
            String keyword = searchBox.getText().toString().trim();
            if (keyword.isEmpty()) {
                loadMedicines();
            } else {
                List<Medicine> results = dbHelper.searchMedicines(keyword);
                MedicineListAdapter searchAdapter = new MedicineListAdapter(this, results, 
                    new MedicineListAdapter.OnMedicineActionListener() {
                        @Override
                        public void onEditClick(Medicine medicine) {
                            showEditDialog(medicine);
                        }
                        @Override
                        public void onDonateClick(Medicine medicine) {
                            showDonateDialog(medicine);
                        }
                        @Override
                        public void onRequestClick(Medicine medicine) {
                            showRequestDialog(medicine);
                        }
                    });
                medicineListView.setAdapter(searchAdapter);
            }
        });

        btnAddMedicine.setOnClickListener(v -> {
            if (addMedicinePanelScroll.getVisibility() == View.GONE) {
                addMedicinePanelScroll.setVisibility(View.VISIBLE);
            } else {
                addMedicinePanelScroll.setVisibility(View.GONE);
                clearFields();
            }
        });

        btnCloseForm.setOnClickListener(v -> {
            addMedicinePanelScroll.setVisibility(View.GONE);
            clearFields();
        });

        String[] types = {"قرص", "شربت", "آمپول", "پماد", "قطره", "کپسول", "شیاف", "اسپری"};
        ArrayAdapter<String> typeAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, types);
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(typeAdapter);

        setupAutoComplete();

        autoCompleteMedicine.setOnItemClickListener((parent, view, position, id) -> {
            String selectedName = parent.getItemAtPosition(position).toString();
            updateFields(selectedName);
        });

        btnExpiryDate.setOnClickListener(v -> showDatePickerDialog());
        btnSave.setOnClickListener(v -> saveMedicine());
    }
	
	private void goHome() {
    Intent homeIntent = new Intent(AddMedicineActivity.this, MainActivity.class);
    homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
    startActivity(homeIntent);
    finish();
}

@Override
public void onBackPressed() {
    goHome();
}

    private void loadMedicines() {
        medicineList = dbHelper.getAllMedicines();
        
        adapter = new MedicineListAdapter(this, medicineList, new MedicineListAdapter.OnMedicineActionListener() {
            @Override
            public void onEditClick(Medicine medicine) {
                showEditDialog(medicine);
            }
            @Override
            public void onDonateClick(Medicine medicine) {
                showDonateDialog(medicine);
            }
            @Override
            public void onRequestClick(Medicine medicine) {
                showRequestDialog(medicine);
            }
        });
        
        medicineListView.setAdapter(adapter);
    }

    private void setupAutoComplete() {
        Set<String> uniqueNames = new HashSet<>();
        for (Medicine m : allMedicines) {
            uniqueNames.add(m.getName());
            if (m.getEnglishName() != null && !m.getEnglishName().isEmpty()) {
                uniqueNames.add(m.getEnglishName());
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line,
                new ArrayList<>(uniqueNames));
        autoCompleteMedicine.setAdapter(adapter);
        autoCompleteMedicine.setThreshold(1);
    }

    private void updateFields(String selectedName) {
        List<String> dosages = new ArrayList<>();
        String type = "";
        String routine = "";
        String instructions = "";

        for (Medicine m : allMedicines) {
            if (m.getName().equals(selectedName) || (m.getEnglishName() != null && m.getEnglishName().equals(selectedName))) {
                dosages.add(m.getDosage());
                type = m.getType();
                routine = m.getRoutine();
                instructions = m.getInstructions();
            }
        }

        if (!dosages.isEmpty()) {
            spinnerDosage.setVisibility(View.VISIBLE);
            ArrayAdapter<String> dosageAdapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item, dosages);
            dosageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerDosage.setAdapter(dosageAdapter);
        } else {
            spinnerDosage.setVisibility(View.GONE);
        }

        if (!type.isEmpty()) {
            String[] types = {"قرص", "شربت", "آمپول", "پماد", "قطره", "کپسول", "شیاف", "اسپری"};
            for (int i = 0; i < types.length; i++) {
                if (types[i].equals(type)) {
                    spinnerType.setSelection(i);
                    break;
                }
            }
        }

        etRoutine.setText(routine);
        etInstructions.setText(instructions);
        etDisease.setText("");
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    String date = selectedYear + "/" + (selectedMonth + 1) + "/" + selectedDay;
                    btnExpiryDate.setText("📅 " + date);
                    expiryDate = date;
                }, year, month, day);
        datePickerDialog.show();
    }

    private void saveMedicine() {
        String name = autoCompleteMedicine.getText().toString().trim();
        if (name.isEmpty()) {
            Toast.makeText(this, "❌ لطفاً نام دارو را انتخاب کنید", Toast.LENGTH_SHORT).show();
            return;
        }

        String dosage = "نامشخص";
        if (spinnerDosage.getVisibility() == View.VISIBLE && spinnerDosage.getSelectedItem() != null) {
            dosage = spinnerDosage.getSelectedItem().toString();
        }

        String type = spinnerType.getSelectedItem().toString();
        String quantityStr = etQuantity.getText().toString().trim();
        int quantity = quantityStr.isEmpty() ? 0 : Integer.parseInt(quantityStr);
        String disease = etDisease.getText().toString().trim();
        String routine = etRoutine.getText().toString().trim();
        String instructions = etInstructions.getText().toString().trim();

        if (expiryDate.isEmpty()) expiryDate = "نامشخص";

        Medicine medicine = new Medicine(name, dosage, expiryDate, type, quantity, disease, routine, instructions);
        boolean result = dbHelper.addMedicine(medicine);
		
        if (result) {
            Toast.makeText(this, "✅ دارو با موفقیت به داروخانه اضافه شد", Toast.LENGTH_SHORT).show();
            clearFields();
            addMedicinePanelScroll.setVisibility(View.GONE);
            loadMedicines();
        } else {
            Toast.makeText(this, "❌ خطا در ذخیره دارو", Toast.LENGTH_SHORT).show();
        }
    }

    private void clearFields() {
        autoCompleteMedicine.setText("");
        etQuantity.setText("");
        etDisease.setText("");
        etRoutine.setText("");
        etInstructions.setText("");
        btnExpiryDate.setText("📅 انتخاب تاریخ انقضا");
        expiryDate = "";
        spinnerDosage.setVisibility(View.GONE);
    }

    private void showDonateDialog(Medicine medicine) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_donate_quick, null);
        
        TextView tvMedicineName = dialogView.findViewById(R.id.tvDonateMedicineName);
        TextView tvMedicineDetails = dialogView.findViewById(R.id.tvDonateMedicineDetails);
        EditText etQuantity = dialogView.findViewById(R.id.etDonateQuantity);
        
        tvMedicineName.setText("💊 " + medicine.getName());
        tvMedicineDetails.setText("موجودی: " + medicine.getQuantity() + " | " + medicine.getDosage());
        etQuantity.setText("1");

        builder.setView(dialogView)
                .setTitle("🎁 اهدای دارو")
                .setPositiveButton("اهداء", (dialog, which) -> {
                    String quantityStr = etQuantity.getText().toString().trim();
                    int quantity = 1;
                    try {
                        quantity = Integer.parseInt(quantityStr);
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "❌ تعداد نامعتبر است", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    if (quantity > medicine.getQuantity()) {
                        Toast.makeText(this, "❌ تعداد بیشتر از موجودی است!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    submitDonationRequest(medicine, quantity);
                })
                .setNegativeButton("لغو", null)
                .show();
    }

    // =============== متد submitDonationRequest ===============
    private void submitDonationRequest(Medicine medicine, int quantity) {
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        String userPhone = prefs.getString("user_phone", "نامشخص");
        String userName = prefs.getString("user_name", "کاربر ناشناس");
        String userCity = prefs.getString("user_city", "نامشخص");
        String userProvince = prefs.getString("user_province", "نامشخص");
        
        // 1. ذخیره در دیتابیس محلی
        DonationRequest request = new DonationRequest();
        request.setMedicineName(medicine.getName());
        request.setMedicineId(medicine.getId());
        request.setQuantity(quantity);
        request.setDonorName(userName);
        request.setDonorPhone(userPhone);
        request.setStatus("PENDING");
        request.setTimestamp(System.currentTimeMillis());
        request.setMessage("درخواست اهدا از طرف " + userName);
        request.setRequestType("DONATION"); // ✅ برای شمارش صحیح در باکس ۳ داشبورد
        
        boolean saved = dbHelper.saveDonationRequest(request);
        boolean addedToDonationList = dbHelper.addToDonationList(medicine, quantity, userName, userPhone);
        
        if (saved && addedToDonationList) {
            // 2. ✅ ذخیره در JSON Bin (ابر)
            saveDonationToCloud(medicine, quantity, userName, userPhone, userCity, userProvince);
            
            Toast.makeText(this, 
                "✅ درخواست اهدا برای " + medicine.getName() + 
                " به تعداد " + quantity + " عدد ارسال شد", 
                Toast.LENGTH_LONG).show();
            
            medicine.setQuantity(medicine.getQuantity() - quantity);
            dbHelper.updateMedicine(medicine);
            loadMedicines();
            
            NotificationHelper.showDonationNotification(
                this, 
                "📦 درخواست اهدا", 
                "درخواست جدید برای داروی " + medicine.getName(),
                (int) System.currentTimeMillis()
            );
        } else {
            Toast.makeText(this, "❌ خطا در ثبت درخواست اهدا", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * ✅ ذخیره داروی اهدایی در JSON Bin
     */
    private void saveDonationToCloud(Medicine medicine, int quantity, String donorName, 
                                     String donorPhone, String donorCity, String donorProvince) {
        CloudStorageHelper.readFile(CloudStorageHelper.BIN_DONATIONS, new CloudStorageHelper.CloudCallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    JSONObject obj = new JSONObject(response);
                    JSONArray donations = obj.getJSONArray("record");
                    
                    JSONObject newDonation = new JSONObject();
                    int newId = donations.length() + 1;
                    newDonation.put("id", newId);
                    newDonation.put("medicineName", medicine.getName());
                    newDonation.put("dosage", medicine.getDosage());
                    newDonation.put("quantity", quantity);
                    newDonation.put("expiryDate", medicine.getExpiryDate());
                    newDonation.put("donorName", donorName);
                    newDonation.put("city", donorCity);
                    newDonation.put("province", donorProvince);
                    newDonation.put("phone", donorPhone);
                    donations.put(newDonation);
                    
                    JSONObject updateObj = new JSONObject();
                    updateObj.put("record", donations);
                    CloudStorageHelper.writeFile(CloudStorageHelper.BIN_DONATIONS, updateObj.toString(), 
                        new CloudStorageHelper.CloudCallback() {
                            @Override
                            public void onSuccess(String response) {
                                updateUserDonations(donorPhone, newId);
                            }
                            @Override
                            public void onError(String error) {
                                runOnUiThread(() -> 
                                    Toast.makeText(AddMedicineActivity.this, "⚠️ داده در ابر ذخیره نشد", Toast.LENGTH_SHORT).show()
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
                    Toast.makeText(AddMedicineActivity.this, "⚠️ خطا در ارتباط با ابر", Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    /**
     * ✅ به‌روزرسانی ارتباط کاربر-دارو در ابر
     */
    private void updateUserDonations(String userId, int donationId) {
        CloudStorageHelper.readFile(CloudStorageHelper.BIN_USER_DONATIONS, new CloudStorageHelper.CloudCallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    JSONObject obj = new JSONObject(response);
                    JSONObject records = obj.getJSONObject("record");
                    JSONArray userList = records.optJSONArray(userId);
                    if (userList == null) {
                        userList = new JSONArray();
                    }
                    userList.put(donationId);
                    records.put(userId, userList);
                    
                    JSONObject updateObj = new JSONObject();
                    updateObj.put("record", records);
                    CloudStorageHelper.writeFile(CloudStorageHelper.BIN_USER_DONATIONS, updateObj.toString(), null);
                    
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onError(String error) {
                // خطا
            }
        });
    }

    private void showRequestDialog(Medicine medicine) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_request_quick, null);
        
        TextView tvMedicineName = dialogView.findViewById(R.id.tvRequestMedicineName);
        TextView tvMedicineDetails = dialogView.findViewById(R.id.tvRequestMedicineDetails);
        EditText etQuantity = dialogView.findViewById(R.id.etRequestQuantity);
        
        tvMedicineName.setText("💊 " + medicine.getName());
        tvMedicineDetails.setText("موجودی: " + medicine.getQuantity() + " | " + medicine.getDosage());
        etQuantity.setText("1");

        builder.setView(dialogView)
                .setTitle("📩 درخواست دارو")
                .setPositiveButton("ثبت درخواست", (dialog, which) -> {
                    String quantityStr = etQuantity.getText().toString().trim();
                    int quantity = 1;
                    try {
                        quantity = Integer.parseInt(quantityStr);
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "❌ تعداد نامعتبر است", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    submitRequest(medicine, quantity);
                })
                .setNegativeButton("لغو", null)
                .show();
    }

    // =============== ✅ متد submitRequest (اصلاح شده با ذخیره در JSON Bin) ===============
    private void submitRequest(Medicine medicine, int quantity) {
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        String userPhone = prefs.getString("user_phone", "نامشخص");
        String userName = prefs.getString("user_name", "کاربر ناشناس");
        String userCity = prefs.getString("user_city", "نامشخص");
        String userProvince = prefs.getString("user_province", "نامشخص");
        
        // 1. ذخیره در دیتابیس محلی (کش موبایل)
        DonationRequest request = new DonationRequest();
        request.setMedicineName(medicine.getName());
        request.setMedicineId(medicine.getId());
        request.setQuantity(quantity);
        request.setDonorName(userName);
        request.setDonorPhone(userPhone);
        request.setStatus("PENDING");
        request.setTimestamp(System.currentTimeMillis());
        request.setMessage("درخواست دارو از طرف " + userName);
        request.setRequestType("REQUEST"); // ✅ برای شمارش صحیح در باکس ۴ داشبورد
        
        boolean saved = dbHelper.saveDonationRequest(request);
        
        // 2. ✅ ذخیره در JSON Bin (برای همه کاربران)
        saveRequestToCloud(medicine, quantity, userName, userPhone, userCity, userProvince);
        
        if (saved) {
            Toast.makeText(this, 
                "✅ درخواست برای " + medicine.getName() + 
                " به تعداد " + quantity + " عدد ثبت شد", 
                Toast.LENGTH_LONG).show();
            
            NotificationHelper.showDonationNotification(
                this, 
                "📩 درخواست دارو", 
                "درخواست جدید برای داروی " + medicine.getName(),
                (int) System.currentTimeMillis()
            );
        } else {
            Toast.makeText(this, "❌ خطا در ثبت درخواست", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * ✅ ذخیره درخواست در JSON Bin
     */
    private void saveRequestToCloud(Medicine medicine, int quantity, String userName, 
                                    String userPhone, String userCity, String userProvince) {
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
                    newRequest.put("medicineName", medicine.getName());
                    newRequest.put("dosage", medicine.getDosage());
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
                                    Toast.makeText(AddMedicineActivity.this, "⚠️ درخواست در ابر ذخیره نشد", Toast.LENGTH_SHORT).show()
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
                    Toast.makeText(AddMedicineActivity.this, "⚠️ خطا در ارتباط با ابر", Toast.LENGTH_SHORT).show()
                );
            }
        });
    }

    private void showEditDialog(Medicine medicine) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_medicine, null);
        
        EditText etName = dialogView.findViewById(R.id.etEditName);
        EditText etDosage = dialogView.findViewById(R.id.etEditDosage);
        EditText etQuantity = dialogView.findViewById(R.id.etEditQuantity);
        EditText etExpiry = dialogView.findViewById(R.id.etEditExpiry);
        EditText etDisease = dialogView.findViewById(R.id.etEditDisease);
        EditText etRoutine = dialogView.findViewById(R.id.etEditRoutine);
        EditText etInstructions = dialogView.findViewById(R.id.etEditInstructions);

        etName.setText(medicine.getName());
        etDosage.setText(medicine.getDosage());
        etQuantity.setText(String.valueOf(medicine.getQuantity()));
        etExpiry.setText(medicine.getExpiryDate());
        etDisease.setText(medicine.getDisease());
        etRoutine.setText(medicine.getRoutine());
        etInstructions.setText(medicine.getInstructions());

        builder.setView(dialogView)
                .setTitle("✏️ ویرایش دارو")
                .setPositiveButton("ذخیره", (dialog, which) -> {
                    medicine.setName(etName.getText().toString().trim());
                    medicine.setDosage(etDosage.getText().toString().trim());
                    medicine.setQuantity(Integer.parseInt(etQuantity.getText().toString().trim()));
                    medicine.setExpiryDate(etExpiry.getText().toString().trim());
                    medicine.setDisease(etDisease.getText().toString().trim());
                    medicine.setRoutine(etRoutine.getText().toString().trim());
                    medicine.setInstructions(etInstructions.getText().toString().trim());

                    if (dbHelper.updateMedicine(medicine)) {
                        Toast.makeText(AddMedicineActivity.this, "✅ دارو با موفقیت ویرایش شد", Toast.LENGTH_SHORT).show();
                        loadMedicines();
                    } else {
                        Toast.makeText(AddMedicineActivity.this, "❌ خطا در ویرایش دارو", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("انصراف", null)
                .show();
    }
}