package com.abania.dragmanager;

import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;

public class MessagesActivity extends AppCompatActivity {

    private ListView listViewMessages;
    private DatabaseHelper dbHelper;
    private DonationRequestAdapter adapter;
    private List<DonationRequest> requestList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);

        dbHelper = new DatabaseHelper(this);
        listViewMessages = findViewById(R.id.listViewMessages);
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        loadMessages();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadMessages(); // بارگذاری مجدد هنگام بازگشت
    }

    private void loadMessages() {
        requestList = dbHelper.getDonationRequests();
        
        if (requestList == null || requestList.isEmpty()) {
            Toast.makeText(this, "📭 هیچ پیامی وجود ندارد", Toast.LENGTH_SHORT).show();
            // می‌توانید یک TextView خالی هم نمایش دهید
        }

        adapter = new DonationRequestAdapter(this, requestList);
        listViewMessages.setAdapter(adapter);
    }
}