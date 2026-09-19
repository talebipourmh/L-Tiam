package com.abania.dragmanager;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.util.List;

public class MyDonationAdapter extends ArrayAdapter<DonationItem> {

    private Context context;
    private List<DonationItem> donationList;

    public MyDonationAdapter(Context context, List<DonationItem> donationList) {
        super(context, 0, donationList);
        this.context = context;
        this.donationList = donationList;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_my_donation, parent, false);
        }

        DonationItem item = donationList.get(position);

        TextView tvMedicineName = convertView.findViewById(R.id.tvDonationMedicineName);
        TextView tvDosage = convertView.findViewById(R.id.tvDonationDosage);
        TextView tvQuantity = convertView.findViewById(R.id.tvDonationQuantity);
        TextView tvDate = convertView.findViewById(R.id.tvDonationDate);
        TextView tvStatus = convertView.findViewById(R.id.tvDonationStatus);

        tvMedicineName.setText("💊 " + item.getMedicineName());
        
        String dosage = item.getMedicineDosage() != null ? item.getMedicineDosage() : "نامشخص";
        tvDosage.setText("دوز: " + dosage);
        
        tvQuantity.setText("📦 تعداد: " + item.getQuantity());
        tvDate.setText("📅 " + item.getDonationDate());

        // ✅ وضعیت‌های هماهنگ (4 وضعیت)
        String status = item.getStatus();
        if (status == null) status = "PENDING";
        
        switch (status) {
            case "PENDING":
                tvStatus.setText("⏳ در انتظار درخواست‌کننده");
                tvStatus.setTextColor(context.getColor(android.R.color.holo_orange_dark));
                break;
            case "ACCEPTED":
                tvStatus.setText("✅ قبول شده");
                tvStatus.setTextColor(context.getColor(android.R.color.holo_green_dark));
                break;
            case "COMPLETED":
                tvStatus.setText("✔️ تکمیل شده");
                tvStatus.setTextColor(context.getColor(android.R.color.holo_blue_dark));
                break;
            case "REJECTED":
                tvStatus.setText("❌ رد شده");
                tvStatus.setTextColor(context.getColor(android.R.color.holo_red_dark));
                break;
            default:
                tvStatus.setText(status);
        }

        return convertView;
    }
}