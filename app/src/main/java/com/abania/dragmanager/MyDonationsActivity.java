package com.abania.dragmanager;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import java.util.ArrayList;
import java.util.List;

public class MyDonationsActivity extends AppCompatActivity {

    private ListView listViewDonations;
    private DatabaseHelper dbHelper;
    private DonationListAdapter adapter;
    private List<DonationItem> donationList;
    private String currentUserPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_donation);

        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        currentUserPhone = prefs.getString("user_phone", "");

        dbHelper = new DatabaseHelper(this);
        listViewDonations = findViewById(R.id.listViewMyDonations);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("🎁 داروهای اهدایی من");

        loadDonations();
    }

    private void loadDonations() {
        List<DonationItem> allDonations = dbHelper.getDonationList();
        donationList = new ArrayList<>();

        for (DonationItem item : allDonations) {
            if (item.getDonorPhone() != null && 
                item.getDonorPhone().equals(currentUserPhone)) {
                donationList.add(item);
            }
        }

        adapter = new DonationListAdapter(this, donationList);
        listViewDonations.setAdapter(adapter);

        if (donationList.isEmpty()) {
            Toast.makeText(this, "📭 هیچ دارویی اهدا نکرده‌اید", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDonations();
    }
}