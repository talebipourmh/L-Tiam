package com.abania.dragmanager;

import android.content.Context;
import android.content.SharedPreferences;
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

public class RequestAdapter extends ArrayAdapter<DonationRequest> {

    private Context context;
    private List<DonationRequest> requests;
    private String currentUserPhone;
    private OnRequestCancelListener cancelListener;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault());

    public interface OnRequestCancelListener {
        void onCancelClick(DonationRequest request, int position);
    }

    public RequestAdapter(Context context, List<DonationRequest> requests) {
        super(context, 0, requests);
        this.context = context;
        this.requests = requests;
        
        SharedPreferences prefs = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        currentUserPhone = prefs.getString("user_phone", "");
    }

    public void setOnRequestCancelListener(OnRequestCancelListener listener) {
        this.cancelListener = listener;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_my_request, parent, false);
        }

        DonationRequest request = requests.get(position);

        TextView tvMedicineName = convertView.findViewById(R.id.tvMedicineName);
        TextView tvDosage = convertView.findViewById(R.id.tvDosage);
        TextView tvCity = convertView.findViewById(R.id.tvCity);
        TextView tvDate = convertView.findViewById(R.id.tvDate);
        TextView tvStatus = convertView.findViewById(R.id.tvStatus);
        TextView tvRequester = convertView.findViewById(R.id.tvRequester);
        Button btnCancel = convertView.findViewById(R.id.btnCancelRequest);

        tvMedicineName.setText("💊 " + request.getMedicineName());
        
        String dosage = request.getDosage();
        if (dosage == null || dosage.isEmpty()) {
            dosage = "نامشخص";
        }
        tvDosage.setText("دوز: " + dosage + " | تعداد: " + request.getQuantity());
        
        String city = request.getCity();
        if (city == null || city.isEmpty()) {
            city = "نامشخص";
        }
        tvCity.setText("📍 " + city);
        
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

        String requester = request.getRequesterPhone();
        if (requester == null || requester.isEmpty()) {
            requester = request.getDonorPhone();
        }
        if (requester == null || requester.isEmpty()) {
            requester = "نامشخص";
        }
        tvRequester.setText("👤 " + requester);

        // دکمه لغو
        btnCancel.setOnClickListener(v -> {
            if (cancelListener != null) {
                cancelListener.onCancelClick(request, position);
            }
        });

        return convertView;
    }
}