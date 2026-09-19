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

public class DonationRequestAdapter extends ArrayAdapter<DonationRequest> {

    private Context context;
    private List<DonationRequest> requests;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.getDefault());

    public DonationRequestAdapter(Context context, List<DonationRequest> requests) {
        super(context, 0, requests);
        this.context = context;
        this.requests = requests;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_donation_request, parent, false);
        }

        DonationRequest request = requests.get(position);

        TextView tvMedicineName = convertView.findViewById(R.id.tvMedicineName);
        TextView tvQuantity = convertView.findViewById(R.id.tvQuantity);
        TextView tvDonorName = convertView.findViewById(R.id.tvDonorName);
        TextView tvStatus = convertView.findViewById(R.id.tvStatus);
        TextView tvTimestamp = convertView.findViewById(R.id.tvTimestamp);

        tvMedicineName.setText("💊 " + request.getMedicineName());
        tvQuantity.setText("تعداد: " + request.getQuantity());
        tvDonorName.setText("👤 " + request.getDonorName());

        // نمایش وضعیت با رنگ
        String status = request.getStatus();
        switch (status) {
            case "PENDING":
                tvStatus.setText("⏳ در انتظار");
                tvStatus.setTextColor(context.getColor(android.R.color.holo_orange_dark));
                break;
            case "ACCEPTED":
                tvStatus.setText("✅ پذیرفته شده");
                tvStatus.setTextColor(context.getColor(android.R.color.holo_green_dark));
                break;
            case "REJECTED":
                tvStatus.setText("❌ رد شده");
                tvStatus.setTextColor(context.getColor(android.R.color.holo_red_dark));
                break;
            case "COMPLETED":
                tvStatus.setText("✔️ تکمیل شده");
                tvStatus.setTextColor(context.getColor(android.R.color.holo_blue_dark));
                break;
            default:
                tvStatus.setText(status);
        }

        // تاریخ
        String date = dateFormat.format(new Date(request.getTimestamp()));
        tvTimestamp.setText("🕒 " + date);

        return convertView;
    }
}