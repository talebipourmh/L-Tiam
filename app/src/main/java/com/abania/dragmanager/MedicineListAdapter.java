package com.abania.dragmanager;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MedicineListAdapter extends ArrayAdapter<Medicine> {

    private Context context;
    private List<Medicine> medicineList;
    private OnMedicineActionListener listener;

    public interface OnMedicineActionListener {
        void onEditClick(Medicine medicine);
        void onDonateClick(Medicine medicine);
        void onRequestClick(Medicine medicine);
    }

    public MedicineListAdapter(Context context, List<Medicine> medicineList, OnMedicineActionListener listener) {
        super(context, 0, medicineList);
        this.context = context;
        this.medicineList = medicineList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_medicine_main, parent, false);
        }

        Medicine medicine = medicineList.get(position);

        TextView tvName = convertView.findViewById(R.id.tvMedicineName);
        TextView tvDosage = convertView.findViewById(R.id.tvMedicineDosage);
        TextView tvExpiry = convertView.findViewById(R.id.tvMedicineExpiry);
        TextView tvQuantity = convertView.findViewById(R.id.tvMedicineQuantity);
        ImageView ivExpiryIcon = convertView.findViewById(R.id.ivExpiryIcon);
        ImageView ivEdit = convertView.findViewById(R.id.ivEdit);
        ImageView ivRequest = convertView.findViewById(R.id.ivRequest);
        ImageView ivDonate = convertView.findViewById(R.id.ivDonate);

        tvName.setText(medicine.getName());
        tvDosage.setText(medicine.getType() + " · " + medicine.getDosage());

        // بج موجودی، رنگ بر اساس مقدار
        int quantity = medicine.getQuantity();
        int quantityColor = quantity < 5 ? 0xFFE53935 : (quantity < 15 ? 0xFFFB8C00 : 0xFF43A047);
        tvQuantity.setText(quantity + " عدد");
        tvQuantity.setTextColor(quantityColor);
        tvQuantity.getBackground().mutate();
        tvQuantity.setBackgroundTintList(ColorStateList.valueOf(withAlpha(quantityColor, 0x22)));

        // وضعیت انقضا، رنگ بر اساس تاریخ
        String expiryDateStr = medicine.getExpiryDate();
        int expiryColor = 0xFF888888;
        String expiryLabel;

        if (expiryDateStr != null && !expiryDateStr.isEmpty() && !expiryDateStr.equals("نامشخص")) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy/M/d", Locale.ENGLISH);
                Date expiry = sdf.parse(expiryDateStr);
                Date today = new Date();
                Calendar cal = Calendar.getInstance();
                cal.setTime(today);
                cal.add(Calendar.DAY_OF_YEAR, 30);
                Date in30Days = cal.getTime();

                if (expiry != null && expiry.before(today)) {
                    expiryColor = 0xFFE53935;
                    expiryLabel = "منقضی شده: " + expiryDateStr;
                } else if (expiry != null && expiry.before(in30Days)) {
                    expiryColor = 0xFFFB8C00;
                    expiryLabel = "نزدیک انقضا: " + expiryDateStr;
                } else {
                    expiryColor = 0xFF43A047;
                    expiryLabel = "معتبر تا: " + expiryDateStr;
                }
            } catch (Exception e) {
                expiryLabel = "تاریخ انقضا: " + expiryDateStr;
            }
        } else {
            expiryLabel = "تاریخ انقضا: نامشخص";
        }

        tvExpiry.setText(expiryLabel);
        tvExpiry.setTextColor(expiryColor);

        ivEdit.setOnClickListener(v -> {
            if (listener != null) listener.onEditClick(medicine);
        });

        ivRequest.setOnClickListener(v -> {
            if (listener != null) listener.onRequestClick(medicine);
        });

        ivDonate.setOnClickListener(v -> {
            if (listener != null) listener.onDonateClick(medicine);
        });

        return convertView;
    }

    private int withAlpha(int color, int alpha) {
        return (color & 0x00FFFFFF) | (alpha << 24);
    }
}