package com.abania.dragmanager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.signature.ObjectKey;
import com.github.dhaval2404.imagepicker.ImagePicker;
import com.github.dhaval2404.imagepicker.constant.ImageProvider;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.widget.Button;

public class ProfileActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1;
    private ImageView ivAvatar;
    private EditText etFullName, etPhone;
    private Spinner spinnerProvince, spinnerCity;
    private Button btnSaveProfile;
    private SessionManager sessionManager;
    private SharedPreferences avatarPref;
    private String selectedProvince = "";
    private Map<String, List<String>> provinceCityMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        sessionManager = new SessionManager(this);
        avatarPref = getSharedPreferences("AvatarPref", MODE_PRIVATE);

        ivAvatar = findViewById(R.id.ivAvatar);
        etFullName = findViewById(R.id.etFullName);
        etPhone = findViewById(R.id.etPhone);
        spinnerProvince = findViewById(R.id.spinnerProvince);
        spinnerCity = findViewById(R.id.spinnerCity);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);

        loadCitiesFromCsv();
        setupProvinceSpinner();
        updateCitySpinner("");
        loadUserProfile();

        spinnerProvince.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                String selected = parent.getItemAtPosition(position).toString();
                if (selected.equals("استان را انتخاب کنید...")) {
                    selectedProvince = "";
                    updateCitySpinner("");
                } else {
                    selectedProvince = selected;
                    updateCitySpinner(selectedProvince);
                }
            }

            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        ImageView btnChangeAvatar = findViewById(R.id.btnChangeAvatar);
        btnChangeAvatar.setOnClickListener(v -> openImagePicker());

        btnSaveProfile.setOnClickListener(v -> saveUserProfile());
    }

    private void loadCitiesFromCsv() {
        provinceCityMap = CsvCityReader.readProvincesAndCities(this);
        if (provinceCityMap.isEmpty()) {
            Toast.makeText(this, "⚠️ خطا در خواندن فایل CSV", Toast.LENGTH_LONG).show();
        }
    }

    private void setupProvinceSpinner() {
        List<String> provinces = new ArrayList<>(provinceCityMap.keySet());
        provinces.add(0, "استان را انتخاب کنید...");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, provinces);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerProvince.setAdapter(adapter);

        String savedProvince = getSharedPreferences("UserLocation", MODE_PRIVATE).getString("province", "");
        if (!savedProvince.isEmpty()) {
            int position = adapter.getPosition(savedProvince);
            if (position >= 0) spinnerProvince.setSelection(position);
        }
    }

    private void updateCitySpinner(String province) {
        List<String> cities = new ArrayList<>();
        cities.add("شهرستان را انتخاب کنید...");

        if (province != null && !province.isEmpty() && !province.equals("استان را انتخاب کنید...")
                && provinceCityMap.containsKey(province)) {
            cities.addAll(provinceCityMap.get(province));
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, cities);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCity.setAdapter(adapter);

        String savedCity = getSharedPreferences("UserLocation", MODE_PRIVATE).getString("city", "");
        if (!savedCity.isEmpty() && cities.contains(savedCity)) {
            int position = adapter.getPosition(savedCity);
            if (position >= 0) spinnerCity.setSelection(position);
        }
    }

    private void loadUserProfile() {
        etFullName.setText(sessionManager.getUserName());
        etPhone.setText(sessionManager.getUserPhone());

        String avatarPath = avatarPref.getString("avatar_path", "");
        if (!avatarPath.isEmpty()) {
            File avatarFile = new File(avatarPath);
            if (avatarFile.exists()) {
                Glide.with(this)
                        .load(avatarFile)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .signature(new ObjectKey(System.currentTimeMillis()))
                        .placeholder(R.drawable.ic_default_avatar)
                        .circleCrop()
                        .into(ivAvatar);
            }
        }

        CloudStorageHelper.readFile(CloudStorageHelper.BIN_USERS, new CloudStorageHelper.CloudCallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONArray jsonArray = jsonObject.getJSONArray("record");
                    String currentPhone = sessionManager.getUserPhone();

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject obj = jsonArray.getJSONObject(i);
                        if (obj.getString("phone").equals(currentPhone)) {
                            runOnUiThread(() -> {
                                try {
                                    etFullName.setText(obj.optString("fullName", ""));
                                    etPhone.setText(obj.optString("phone", ""));
                                    String province = obj.optString("province", "");
                                    String city = obj.optString("city", "");
                                    if (!province.isEmpty()) {
                                        ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinnerProvince.getAdapter();
                                        int pos = adapter.getPosition(province);
                                        if (pos >= 0) {
                                            spinnerProvince.setSelection(pos);
                                            updateCitySpinner(province);
                                        }
                                    }
                                    if (!city.isEmpty()) {
                                        ArrayAdapter<String> cityAdapter = (ArrayAdapter<String>) spinnerCity.getAdapter();
                                        int cityPos = cityAdapter.getPosition(city);
                                        if (cityPos >= 0) spinnerCity.setSelection(cityPos);
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            });
                            break;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(() -> Toast.makeText(ProfileActivity.this, "❌ خطا در بارگذاری اطلاعات", Toast.LENGTH_SHORT).show());
                }
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> Toast.makeText(ProfileActivity.this, "❌ خطا در اتصال به سرور", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void saveUserProfile() {
        String fullName = etFullName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String province = spinnerProvince.getSelectedItem().toString();
        String city = spinnerCity.getSelectedItem().toString();

        if (fullName.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "❌ نام و تلفن اجباری هستند", Toast.LENGTH_SHORT).show();
            return;
        }

        sessionManager.createLoginSession(phone, fullName);

        SharedPreferences.Editor editor = getSharedPreferences("UserLocation", MODE_PRIVATE).edit();
        editor.putString("province", province);
        editor.putString("city", city);
        editor.apply();

        CloudStorageHelper.readFile(CloudStorageHelper.BIN_USERS, new CloudStorageHelper.CloudCallback() {
            @Override
            public void onSuccess(String response) {
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    JSONArray jsonArray = jsonObject.getJSONArray("record");
                    boolean found = false;

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject obj = jsonArray.getJSONObject(i);
                        if (obj.getString("phone").equals(phone)) {
                            obj.put("fullName", fullName);
                            obj.put("province", province);
                            obj.put("city", city);
                            found = true;
                            break;
                        }
                    }

                    if (!found) {
                        JSONObject newUser = new JSONObject();
                        newUser.put("id", "user_" + System.currentTimeMillis());
                        newUser.put("fullName", fullName);
                        newUser.put("phone", phone);
                        newUser.put("province", province);
                        newUser.put("city", city);
                        newUser.put("address", "");
                        newUser.put("avatarPath", "");
                        jsonArray.put(newUser);
                    }

                    JSONObject updateObject = new JSONObject();
                    updateObject.put("record", jsonArray);

                    CloudStorageHelper.writeFile(CloudStorageHelper.BIN_USERS, updateObject.toString(),
                        new CloudStorageHelper.CloudCallback() {
                            @Override
                            public void onSuccess(String response) {
                                runOnUiThread(() -> {
                                    Toast.makeText(ProfileActivity.this, "✅ اطلاعات ذخیره شد", Toast.LENGTH_SHORT).show();
                                    finish();
                                });
                            }

                            @Override
                            public void onError(String error) {
                                runOnUiThread(() -> Toast.makeText(ProfileActivity.this, "❌ خطا در ذخیره: " + error, Toast.LENGTH_SHORT).show());
                            }
                        });

                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(() -> Toast.makeText(ProfileActivity.this, "❌ خطا در پردازش داده", Toast.LENGTH_SHORT).show());
                }
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> Toast.makeText(ProfileActivity.this, "❌ خطا در خواندن اطلاعات: " + error, Toast.LENGTH_SHORT).show());
            }
        });
    }

    // ✅ استفاده از ImagePicker برای انتخاب و برش عکس
    private void openImagePicker() {
        ImagePicker.with(this)
                .crop()                    // فعال کردن برش
                .cropSquare()              // برش مربعی
                .compress(512)             // کیفیت
                .maxResultSize(512, 512)   // اندازه نهایی
                .provider(ImageProvider.BOTH)
                .start(PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                saveImageToCache(bitmap);
                Glide.with(this)
                        .load(uri)
                        .circleCrop()
                        .into(ivAvatar);
                Toast.makeText(this, "✅ عکس با موفقیت انتخاب شد", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "❌ خطا در ذخیره عکس", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveImageToCache(Bitmap bitmap) {
        try {
            File cacheDir = getCacheDir();
            File avatarFile = new File(cacheDir, "avatar_" + System.currentTimeMillis() + ".jpg");
            FileOutputStream fos = new FileOutputStream(avatarFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, fos);
            fos.close();

            SharedPreferences.Editor editor = avatarPref.edit();
            editor.putString("avatar_path", avatarFile.getAbsolutePath());
            editor.apply();

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "❌ خطا در ذخیره عکس", Toast.LENGTH_SHORT).show();
        }
    }
}