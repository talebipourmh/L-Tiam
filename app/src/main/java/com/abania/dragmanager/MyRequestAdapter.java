package com.abania.dragmanager;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MyRequestAdapter extends ArrayAdapter<DonationRequest> {

    private Context context;
    private List<DonationRequest> requestList;
    private OnRequestCancelListener listener; // ✅ تغییر نام به OnRequestCancelListener
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault());

    // ✅ اینترفیس با نام صحیح
    public interface OnRequestCancelListener {
        void onCancelClick(DonationRequest request, int position);
    }

    // Constructor بدون Listener
    public MyRequestAdapter(Context context, List<DonationRequest> requestList) {
        super(context, 0, requestList);
        this.context = context;
        this.requestList = requestList;
        this.listener = null;
    }

    // Constructor با Listener
    public MyRequestAdapter(Context context, List<DonationRequest> requestList, OnRequestCancelListener listener) {
        super(context, 0, requestList);
        this.context = context;
        this.requestList = requestList;
        this.listener = listener;
    }

    // متد تنظیم Listener
    public void setOnRequestCancelListener(OnRequestCancelListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_my_request, parent, false);
        }

        DonationRequest request = requestList.get(position);

        TextView tvMedicineName = convertView.findViewById(R.id.tvMedicineName);
        TextView tvDosage = convertView.findViewById(R.id.tvDosage);
        TextView tvCity = convertView.findViewById(R.id.tvCity);
        TextView tvDate = convertView.findViewById(R.id.tvDate);
        TextView tvStatus = convertView.findViewById(R.id.tvStatus);
        TextView tvRequester = convertView.findViewById(R.id.tvRequester);
        Button btnCancel = convertView.findViewById(R.id.btnCancelRequest);

        // تنظیم مقادیر
        tvMedicineName.setText("💊 " + request.getMedicineName());
        
        String dosage = request.getDosage();
        if (dosage == null || dosage.isEmpty()) {
            dosage = "نامشخص";
        }
        tvDosage.setText("💊 دوز: " + dosage);
        
        String city = request.getCity();
        if (city == null || city.isEmpty()) {
            city = "نامشخص";
        }
        tvCity.setText("📍 شهر: " + city);
        
        tvRequester.setText("👤 درخواست‌کننده: " + request.getDonorName());
        
        String date = dateFormat.format(new Date(request.getTimestamp()));
        tvDate.setText("📅 " + date);

        // وضعیت‌های هماهنگ (4 وضعیت)
        String status = request.getStatus();
        if (status == null) status = "PENDING";
        
        switch (status) {
            case "PENDING":
                tvStatus.setText("⏳ در انتظار اهداکننده");
                tvStatus.setTextColor(context.getColor(android.R.color.holo_orange_dark));
                btnCancel.setVisibility(View.VISIBLE);
                btnCancel.setEnabled(true);
                break;
            case "ACCEPTED":
                tvStatus.setText("✅ قبول شده");
                tvStatus.setTextColor(context.getColor(android.R.color.holo_green_dark));
                btnCancel.setVisibility(View.GONE);
                break;
            case "COMPLETED":
                tvStatus.setText("✔️ تکمیل شده");
                tvStatus.setTextColor(context.getColor(android.R.color.holo_blue_dark));
                btnCancel.setVisibility(View.GONE);
                break;
            case "REJECTED":
                tvStatus.setText("❌ رد شده");
                tvStatus.setTextColor(context.getColor(android.R.color.holo_red_dark));
                btnCancel.setVisibility(View.GONE);
                break;
            default:
                tvStatus.setText(status);
                btnCancel.setVisibility(View.GONE);
        }

        // دکمه لغو
        btnCancel.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCancelClick(request, position);
            }
        });

        return convertView;
    }
}