package com.abania.dragmanager;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DonationListAdapter extends ArrayAdapter<DonationItem> {

    private Context context;
    private List<DonationItem> donationList;
    private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

    public DonationListAdapter(Context context, List<DonationItem> donationList) {
        super(context, 0, donationList);
        this.context = context;
        this.donationList = donationList;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_donation_list, parent, false);
        }

        DonationItem item = donationList.get(position);

        // پیدا کردن ویجت‌ها
        TextView tvMedicineName = convertView.findViewById(R.id.tvDonationMedicineName);
        TextView tvDosage = convertView.findViewById(R.id.tvDonationMedicineDosage);
        TextView tvQuantity = convertView.findViewById(R.id.tvDonationQuantity);
        TextView tvDate = convertView.findViewById(R.id.tvDonationDate);

        // تنظیم مقادیر
        tvMedicineName.setText(item.getMedicineName() != null ? item.getMedicineName() : "نامشخص");
        
        // دوز دارو
        String dosage = item.getMedicineDosage() != null && !item.getMedicineDosage().isEmpty() 
                ? item.getMedicineDosage() : "نامشخص";
        tvDosage.setText("💊 دوز: " + dosage);
        
        // تعداد
        tvQuantity.setText("📦 تعداد: " + item.getQuantity());
        
        // تاریخ شمسی با ساعت
        String persianDate = convertToPersianDate(item.getDonationDate());
        tvDate.setText("📅 " + persianDate);

        return convertView;
    }

    /**
     * تبدیل تاریخ به شمسی با ساعت
     */
    private String convertToPersianDate(String dateTime) {
        if (dateTime == null || dateTime.isEmpty()) {
            return "نامشخص";
        }

        try {
            long timestamp;
            
            // اگر تاریخ به صورت timestamp است
            if (dateTime.matches("\\d+")) {
                timestamp = Long.parseLong(dateTime);
            } else {
                // اگر تاریخ به صورت رشته است، آن را به timestamp تبدیل کنید
                // یا از تاریخ فعلی استفاده کنید
                timestamp = System.currentTimeMillis();
            }
            
            // دریافت تاریخ شمسی
            String persianDate = PersianDateHelper.getCurrentDate();
            
            // دریافت ساعت از timestamp
            String time = timeFormat.format(new Date(timestamp));
            
            return persianDate + " " + time;
            
        } catch (Exception e) {
            e.printStackTrace();
            return "نامشخص";
        }
    }

    /**
     * به‌روزرسانی لیست
     */
    public void updateList(List<DonationItem> newList) {
        this.donationList = newList;
        notifyDataSetChanged();
    }
}