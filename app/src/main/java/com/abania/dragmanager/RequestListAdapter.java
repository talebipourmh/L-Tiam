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

public class RequestListAdapter extends ArrayAdapter<DonationRequest> {

    private Context context;
    private List<DonationRequest> requestList;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault());

    public RequestListAdapter(Context context, List<DonationRequest> requestList) {
        super(context, 0, requestList);
        this.context = context;
        this.requestList = requestList;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_request_list, parent, false);
        }

        DonationRequest request = requestList.get(position);

        TextView tvMedicineName = convertView.findViewById(R.id.tvMedicineName);
        TextView tvDosage = convertView.findViewById(R.id.tvDosage);
        TextView tvRequester = convertView.findViewById(R.id.tvRequester);
        TextView tvCity = convertView.findViewById(R.id.tvCity);
        TextView tvDate = convertView.findViewById(R.id.tvDate);
        TextView tvStatus = convertView.findViewById(R.id.tvStatus);

        // تنظیم مقادیر
        tvMedicineName.setText("💊 " + request.getMedicineName());
        
        String dosage = request.getDosage();
        if (dosage == null || dosage.isEmpty()) {
            dosage = "نامشخص";
        }
        tvDosage.setText("دوز: " + dosage + " | تعداد: " + request.getQuantity());
        
        String requester = request.getDonorName();
        if (requester == null || requester.isEmpty()) {
            requester = "نامشخص";
        }
        tvRequester.setText("👤 درخواست‌کننده: " + requester);
        
        String city = request.getCity();
        if (city == null || city.isEmpty()) {
            city = "نامشخص";
        }
        tvCity.setText("📍 شهر: " + city);
        
        String date = request.getRequestDate();
        if (date == null || date.isEmpty()) {
            date = dateFormat.format(new Date(request.getTimestamp()));
        }
        tvDate.setText("📅 " + date);

        // وضعیت
        String status = request.getStatus();
        if (status == null) status = "PENDING";
        
        switch (status) {
            case "PENDING":
                tvStatus.setText("⏳ در انتظار");
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