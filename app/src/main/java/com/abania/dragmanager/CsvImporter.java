package com.abania.dragmanager;

import android.content.Context;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class CsvImporter {

    public static List<Medicine> readCsvFromAssets(Context context, String fileName) {
        List<Medicine> medicines = new ArrayList<>();
        try {
            InputStream inputStream = context.getAssets().open(fileName);
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
            String line;
            boolean isFirstLine = true;

            while ((line = br.readLine()) != null) {
                if (isFirstLine) {
                    isFirstLine = false;
                    continue;
                }

                line = line.trim();
                if (line.isEmpty() || line.replace(",", "").trim().isEmpty()) {
                    continue;
                }

                String[] values = line.split(",");

                if (values.length < 5) continue;

                try {
                    String englishName = values[0].trim();
                    String persianName = values[1].trim();
                    String type = values[2].trim();
                    String dosage = values[3].trim();

                    StringBuilder dispositionBuilder = new StringBuilder();
                    for (int i = 4; i < values.length; i++) {
                        if (i > 4) dispositionBuilder.append(",");
                        dispositionBuilder.append(values[i].trim());
                    }
                    String disposition = dispositionBuilder.toString();
                    if (disposition.isEmpty()) disposition = "نامشخص";

                    if (persianName.isEmpty()) persianName = englishName;
                    if (persianName.isEmpty() && englishName.isEmpty()) continue;

                    if (type.isEmpty()) type = "نامشخص";
                    if (dosage.isEmpty()) dosage = "نامشخص";

                    String expiryDate = "نامشخص";
                    int quantity = 0;
                    String disease = "نامشخص";

                    Medicine medicine = new Medicine(persianName, dosage, expiryDate, type, quantity, disease, disposition, disposition);
                    if (!englishName.isEmpty()) {
                        medicine.setEnglishName(englishName);
                    }
                    medicines.add(medicine);

                } catch (Exception e) {
                    continue;
                }
            }
            br.close();
            inputStream.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return medicines;
    }
}