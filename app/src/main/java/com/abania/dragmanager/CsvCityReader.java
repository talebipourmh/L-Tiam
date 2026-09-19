package com.abania.dragmanager;

import android.content.Context;
import android.util.Log;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CsvCityReader {

    private static final String TAG = "CsvCityReader";

    public static Map<String, List<String>> readProvincesAndCities(Context context) {
        Map<String, List<String>> provinceCityMap = new HashMap<>();
        Map<String, String> provinceCodeNameMap = new HashMap<>();

        try {
            // ===== ۱. خواندن فایل استان‌ها (province.csv) =====
            try {
                InputStream provStream = context.getAssets().open("province.csv");
                BufferedReader provReader = new BufferedReader(new InputStreamReader(provStream, "UTF-8"));
                String line;
                boolean isFirstLine = true;

                while ((line = provReader.readLine()) != null) {
                    if (isFirstLine) {
                        isFirstLine = false;
                        continue;
                    }
                    line = line.trim();
                    if (line.isEmpty()) continue;

                    String[] parts = line.split(",");
                    if (parts.length >= 2) {
                        String code = parts[0].trim();
                        String name = parts[1].trim();
                        name = name.replaceAll("\\d+$", "").trim();
                        if (!code.isEmpty() && !name.isEmpty()) {
                            provinceCodeNameMap.put(code, name);
                            Log.d(TAG, "استان: " + code + " -> " + name);
                        }
                    }
                }
                provReader.close();
                Log.d(TAG, "تعداد استان‌های خوانده شده: " + provinceCodeNameMap.size());

            } catch (Exception e) {
                Log.e(TAG, "فایل province.csv پیدا نشد: " + e.getMessage());
            }

            // ===== ۲. خواندن فایل شهرها (provinces_cities.csv) =====
            try {
                InputStream cityStream = context.getAssets().open("provinces_cities.csv");
                BufferedReader cityReader = new BufferedReader(new InputStreamReader(cityStream, "UTF-8"));
                String line;
                boolean isFirstLine = true;

                while ((line = cityReader.readLine()) != null) {
                    if (isFirstLine) {
                        isFirstLine = false;
                        continue;
                    }
                    line = line.trim();
                    if (line.isEmpty()) continue;

                    String[] parts = line.split(",");
                    if (parts.length >= 4) {
                        String provinceCode = parts[0].trim();
                        String provinceName = parts[1].trim();
                        String cityName = parts[3].trim();

                        provinceName = provinceName.replaceAll("^\\d+\\.\\s*", "").trim();
                        cityName = cityName.replaceAll("^\\d+\\.\\s*", "").trim();

                        if (provinceName.isEmpty() && !provinceCode.isEmpty()) {
                            provinceName = provinceCodeNameMap.get(provinceCode);
                        }

                        if (provinceName != null && !provinceName.isEmpty() && !cityName.isEmpty()) {
                            if (!provinceCityMap.containsKey(provinceName)) {
                                provinceCityMap.put(provinceName, new ArrayList<>());
                            }
                            if (!provinceCityMap.get(provinceName).contains(cityName)) {
                                provinceCityMap.get(provinceName).add(cityName);
                                Log.d(TAG, "شهر: " + cityName + " -> استان: " + provinceName);
                            }
                        }
                    }
                }
                cityReader.close();
                Log.d(TAG, "تعداد استان‌های نهایی: " + provinceCityMap.size());

            } catch (Exception e) {
                Log.e(TAG, "فایل provinces_cities.csv پیدا نشد: " + e.getMessage());
                for (String code : provinceCodeNameMap.keySet()) {
                    String name = provinceCodeNameMap.get(code);
                    if (!provinceCityMap.containsKey(name)) {
                        provinceCityMap.put(name, new ArrayList<>());
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "خطا کلی: " + e.getMessage());
        }
        return provinceCityMap;
    }
}