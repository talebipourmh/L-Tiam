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
import java.util.List;

public class DonationListAdapter extends ArrayAdapter<DonationItem> {
    public interface OnDonationRequestListener {
        void onRequest(DonationItem item);
    }

    private final Context context;
    private final List<DonationItem> donationList;
    private OnDonationRequestListener requestListener;

    public DonationListAdapter(Context context, List<DonationItem> donationList) {
        super(context, 0, donationList);
        this.context = context;
        this.donationList = donationList;
    }

    public void setOnDonationRequestListener(OnDonationRequestListener listener) {
        this.requestListener = listener;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_donation_marketplace, parent, false);
        }
        DonationItem item = donationList.get(position);
        TextView name = convertView.findViewById(R.id.tvDonationMedicineName);
        TextView dosage = convertView.findViewById(R.id.tvDonationMedicineDosage);
        TextView location = convertView.findViewById(R.id.tvDonationLocation);
        TextView quantity = convertView.findViewById(R.id.tvDonationQuantity);
        TextView date = convertView.findViewById(R.id.tvDonationDate);
        TextView status = convertView.findViewById(R.id.tvDonationStatus);
        Button request = convertView.findViewById(R.id.btnRequestDonation);

        name.setText(value(item.getMedicineName(), "داروی نامشخص"));
        dosage.setText("💊 دوز: " + value(item.getMedicineDosage(), "نامشخص"));
        String province = value(item.getDonorProvince(), "");
        String city = value(item.getDonorCity(), "نامشخص");
        location.setText("📍 " + (province.isEmpty() ? city : province + "، " + city));
        quantity.setText("📦 تعداد موجود: " + item.getQuantity());
        date.setText("📅 انقضا: " + value(item.getDonationDate(), "نامشخص"));

        String itemStatus = value(item.getStatus(), "AVAILABLE");
        status.setText("AVAILABLE".equalsIgnoreCase(itemStatus) ? "موجود" : item.getStatusPersian());
        boolean available = "AVAILABLE".equalsIgnoreCase(itemStatus) || "PENDING".equalsIgnoreCase(itemStatus);
        request.setEnabled(available && item.getQuantity() > 0);
        request.setText(request.isEnabled() ? "درخواست این دارو" : "در دسترس نیست");
        request.setOnClickListener(v -> { if (requestListener != null && request.isEnabled()) requestListener.onRequest(item); });
        convertView.setOnClickListener(v -> { if (requestListener != null && request.isEnabled()) requestListener.onRequest(item); });
        return convertView;
    }

    private String value(String value, String fallback) { return value == null || value.trim().isEmpty() ? fallback : value; }

    public void updateList(List<DonationItem> newList) {
        donationList.clear();
        donationList.addAll(newList);
        notifyDataSetChanged();
    }
}
